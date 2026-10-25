package mx.com.fredkor.apps.citamedica.api;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.persistence.EntityManagerFactory;
import mx.com.fredkor.apps.citamedica.core.application.event.CitaAuditEvent;
import mx.com.fredkor.apps.citamedica.platform.observability.listener.CitaAuditListener;
import mx.com.fredkor.apps.citamedica.shared.exception.ResourceNotFoundException;
import mx.com.fredkor.apps.citamedica.usuario.application.mapper.UsuarioMapper;
import mx.com.fredkor.apps.citamedica.usuario.application.service.UsuarioServiceImp;
import mx.com.fredkor.apps.citamedica.usuario.persistence.UsuarioConsultaRepository;
import mx.com.fredkor.apps.citamedica.usuario.web.dto.request.UsuarioRequest;
import mx.com.fredkor.security.persistence.entity.*;
import mx.com.fredkor.security.persistence.repository.*;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.mapstruct.factory.Mappers;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import javax.sql.DataSource;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PersistenceBehaviorTest {
    private AnnotationConfigApplicationContext context;
    private UsuarioServiceImp service;
    private TransactionTemplate tx;
    private Integer firstUserId;

    @BeforeAll
    void setup() {
        context = new AnnotationConfigApplicationContext(Config.class);
        service = context.getBean(UsuarioServiceImp.class);
        tx = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
        tx.executeWithoutResult(status -> {
            var roles = context.getBean(RolRepository.class);
            var users = context.getBean(UsuarioRepository.class);
            var medico = role("MEDICO"); var paciente = role("PACIENTE");
            roles.saveAll(List.of(medico, paciente));
            firstUserId = users.save(user("a_usuario", Set.of())).getId();
            users.save(user("b_usuario", Set.of(medico, paciente)));
            users.save(user("c_usuario", Set.of(paciente)));
        });
    }

    @AfterAll
    void close() { context.close(); }

    @Test
    void paginationIncludesUsersWithoutRolesAndLoadsRolesInBatch() {
        var statistics = context.getBean(EntityManagerFactory.class).unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        var page = service.getUsuarios("", 0, 2, "username", "asc");
        assertEquals(3, page.totalElements());
        assertEquals(List.of("a_usuario", "b_usuario"), page.content().stream().map(u -> u.username()).toList());
        assertTrue(page.content().get(0).roles().isEmpty());
        assertEquals(2, page.content().get(1).roles().size());
        // Página + count + carga de roles; sin consultas individuales por usuario.
        assertEquals(3, statistics.getPrepareStatementCount());
        var next = service.getUsuarios("", 1, 2, "username", "asc");
        assertEquals(List.of("c_usuario"), next.content().stream().map(u -> u.username()).toList());
    }

    @Test
    void missingRoleRollsBackUserChanges() {
        var request = new UsuarioRequest("a_usuario", "Nombre modificado", "Apellido", null,
            "a_usuario@example.com", List.of(99999));
        assertThrows(ResourceNotFoundException.class, () -> service.update(firstUserId, request));
        assertEquals("Nombre original", service.getById(firstUserId).nombres());
    }

    @Test
    void auditOnlyLogsCommittedEventsWithCapturedActor() {
        var logger = (Logger) LoggerFactory.getLogger(CitaAuditListener.class);
        var appender = new ListAppender<ILoggingEvent>();
        appender.start(); logger.addAppender(appender);
        try {
            for (var action : CitaAuditEvent.Action.values()) {
                tx.executeWithoutResult(status -> {
                    int before = appender.list.size();
                    context.publishEvent(new CitaAuditEvent(action, 12, "administrador",
                        Instant.parse("2027-01-01T00:00:00Z"), "trace-prueba"));
                    assertEquals(before, appender.list.size());
                });
            }
            assertEquals(CitaAuditEvent.Action.values().length, appender.list.size());
            assertTrue(appender.list.stream().allMatch(e -> e.getFormattedMessage().contains("actor=administrador")
                && e.getFormattedMessage().contains("traceId=trace-prueba")));
            tx.executeWithoutResult(status -> {
                context.publishEvent(new CitaAuditEvent(CitaAuditEvent.Action.CITA_DELETE, 13,
                    "administrador", Instant.now(), "rollback"));
                status.setRollbackOnly();
            });
            assertEquals(CitaAuditEvent.Action.values().length, appender.list.size());
            context.publishEvent(new CitaAuditEvent(CitaAuditEvent.Action.CITA_CREATE, 14,
                "SISTEMA", Instant.now(), "sin-transaccion"));
            assertEquals(CitaAuditEvent.Action.values().length, appender.list.size());
        } finally {
            logger.detachAppender(appender); appender.stop();
        }
    }

    private RolEntity role(String clave) {
        var role = new RolEntity(); role.setClave(clave); role.setDescripcion(clave); return role;
    }

    private UsuarioEntity user(String username, Set<RolEntity> roles) {
        var user = new UsuarioEntity();
        user.setUsername(username); user.setPassword("hash-de-prueba");
        user.setNombres("Nombre original"); user.setApellidoPaterno("Apellido");
        user.setCorreo(username + "@example.com"); user.setRoles(new HashSet<>(roles));
        return user;
    }

    @Configuration(proxyBeanMethods = false)
    @EnableTransactionManagement(proxyTargetClass = true)
    @EnableJpaRepositories(basePackageClasses = {UsuarioConsultaRepository.class, UsuarioRepository.class})
    static class Config {
        @Bean DataSource dataSource() {
            return new DriverManagerDataSource("jdbc:h2:mem:persistence-behavior;DB_CLOSE_DELAY=-1", "sa", "");
        }
        @Bean LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource ds) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(ds);
            factory.setPackagesToScan("mx.com.fredkor.security.persistence.entity");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop",
                "hibernate.generate_statistics", "true",
                "hibernate.query.fail_on_pagination_over_collection_fetch", "true"));
            return factory;
        }
        @Bean PlatformTransactionManager transactionManager(EntityManagerFactory emf) { return new JpaTransactionManager(emf); }
        @Bean UsuarioServiceImp usuarioService(UsuarioConsultaRepository query, UsuarioRepository users, RolRepository roles) {
            return new UsuarioServiceImp(query, users, roles, Mappers.getMapper(UsuarioMapper.class));
        }
        @Bean CitaAuditListener auditListener() { return new CitaAuditListener(); }
    }
}
