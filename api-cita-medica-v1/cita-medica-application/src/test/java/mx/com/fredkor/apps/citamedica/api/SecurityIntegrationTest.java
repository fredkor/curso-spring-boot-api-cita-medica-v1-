package mx.com.fredkor.apps.citamedica.api;

import mx.com.fredkor.apps.citamedica.catalogos.persistence.entity.*;
import mx.com.fredkor.apps.citamedica.catalogos.persistence.repository.*;
import mx.com.fredkor.apps.citamedica.core.persistence.entity.*;
import mx.com.fredkor.apps.citamedica.core.persistence.repository.*;
import mx.com.fredkor.security.persistence.entity.*;
import mx.com.fredkor.security.persistence.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:security-integration;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.properties.hibernate.default_schema=PUBLIC", "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false", "spring.quartz.auto-startup=false",
    "fredkor.security.jwt.signing-key=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
@ActiveProfiles("test")
class SecurityIntegrationTest {
    @Autowired WebApplicationContext context;
    @Autowired ObjectMapper json;
    @Autowired PasswordEncoder encoder;
    @Autowired UsuarioRepository users;
    @Autowired RolRepository roles;
    @Autowired CitaRepository citas;
    @Autowired MedicoSucursalRepository assignments;
    @Autowired MedicoEspecialidadRepository medicalSpecialties;
    @Autowired EspecialidadRepository specialties;
    @Autowired SucursalRepository branches;
    @Autowired EstatusCitaRepository states;
    @Autowired PlatformTransactionManager transactionManager;
    private MockMvc mvc;
    private UsuarioEntity patient, otherPatient;
    private MedicoSucursalEntity assignment;
    private CitaEntity reserved;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            citas.deleteAll(); assignments.deleteAll(); medicalSpecialties.deleteAll();
            users.deleteAll(); specialties.deleteAll(); branches.deleteAll(); states.deleteAll();
            users.flush();
            for (String clave : List.of("RESERVADA", "CONFIRMADA", "ATENDIDA", "PAGADA", "REPROGRAMADA", "CANCELADA")) {
                var state = new EstatusCitaEntity(); state.setClave(clave); state.setDescripcion(clave); states.save(state);
            }
            patient = user("paciente", "PACIENTE"); otherPatient = user("otro", "PACIENTE");
            var doctor = user("medico", "MEDICO"); user("medico-ajeno", "MEDICO"); user("admin", "ADMINISTRADOR");
            var specialty = new EspecialidadEntity(); specialty.setNombre("General"); specialties.save(specialty);
            var branch = new SucursalEntity(); branch.setNombre("Centro"); branch.setDireccion("Reforma 1"); branches.save(branch);
            var medicalSpecialty = new MedicoEspecialidadEntity(); medicalSpecialty.setMedico(doctor);
            medicalSpecialty.setEspecialidad(specialty); medicalSpecialties.save(medicalSpecialty);
            assignment = new MedicoSucursalEntity(); assignment.setMedicoEspecialidad(medicalSpecialty);
            assignment.setSucursal(branch); assignments.save(assignment);
            reserved = cita(patient, "RESERVADA"); cita(otherPatient, "RESERVADA");
        });
    }

    @Test
    void authenticationAndUserAdministrationEnforcePermissions() throws Exception {
        mvc.perform(get("/api/v1/usuarios")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer token-invalido"))
            .andExpect(status().isUnauthorized());
        String patientToken = login("paciente");
        for (var request : List.of(get("/api/v1/usuarios"), get("/api/v1/usuarios/" + patient.getId()),
            post("/api/v1/usuarios"), put("/api/v1/usuarios/" + patient.getId()), delete("/api/v1/usuarios/" + patient.getId()))) {
            // Los métodos con body se validan antes de @PreAuthorize; enviar un DTO válido.
            mvc.perform(request.header("Authorization", patientToken).contentType("application/json").content("""
                {"username":"intruso","nombres":"Nombre","apellidoPaterno":"Apellido",
                 "correo":"intruso@example.com","roles":[1]}
                """)).andExpect(status().isForbidden());
        }
        mvc.perform(get("/api/v1/usuarios").header("Authorization", login("admin")))
            .andExpect(status().isOk());
    }

    @Test
    void patientAndDoctorOnlyReadTheirAppointments() throws Exception {
        String token = login("paciente");
        mvc.perform(get("/api/v1/citas").header("Authorization", token))
            .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/api/v1/citas/" + reserved.getId()).header("Authorization", token)).andExpect(status().isOk());
        String other = login("otro");
        mvc.perform(get("/api/v1/citas/" + reserved.getId()).header("Authorization", other)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/citas/" + reserved.getId()).header("Authorization", login("medico-ajeno")))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/citas").header("Authorization", login("medico")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));
        mvc.perform(get("/api/v1/citas").header("Authorization", login("admin")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void creationCannotImpersonateAnotherPatientOrSetArbitraryState() throws Exception {
        String token = login("paciente");
        mvc.perform(post("/api/v1/citas").header("Authorization", token).contentType("application/json")
            .content(body(otherPatient.getId(), "RESERVADA"))).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/citas").header("Authorization", token).contentType("application/json")
            .content(body(patient.getId(), "ATENDIDA"))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/citas").header("Authorization", token).contentType("application/json")
            .content(body(patient.getId(), "RESERVADA"))).andExpect(status().isCreated())
            .andExpect(jsonPath("$.estatusCita.clave").value("RESERVADA"));
    }

    @Test
    void stateTransitionsRequireOwnershipPermissionAndValidSourceState() throws Exception {
        String base = "/api/v1/citas/" + reserved.getId();
        mvc.perform(patch(base + "/confirmar").header("Authorization", login("otro"))).andExpect(status().isForbidden());
        mvc.perform(patch(base + "/atender").header("Authorization", login("medico"))).andExpect(status().isBadRequest());
        mvc.perform(patch(base + "/confirmar").header("Authorization", login("paciente"))).andExpect(status().isOk())
            .andExpect(jsonPath("$.estatusCita.clave").value("CONFIRMADA"));
        mvc.perform(patch(base + "/atender").header("Authorization", login("paciente"))).andExpect(status().isForbidden());
        mvc.perform(patch(base + "/atender").header("Authorization", login("medico-ajeno"))).andExpect(status().isForbidden());
        mvc.perform(patch(base + "/atender").header("Authorization", login("medico"))).andExpect(status().isOk())
            .andExpect(jsonPath("$.estatusCita.clave").value("ATENDIDA"));
        mvc.perform(patch(base + "/cancelar").header("Authorization", login("paciente"))).andExpect(status().isBadRequest());
    }

    @Test
    void generalUpdateCannotBypassStateOrAssignmentRules() throws Exception {
        String base = "/api/v1/citas/" + reserved.getId();
        mvc.perform(put(base).header("Authorization", login("medico")).contentType("application/json")
            .content(body(patient.getId(), "ATENDIDA"))).andExpect(status().isBadRequest());
        mvc.perform(put(base).header("Authorization", login("medico")).contentType("application/json")
            .content(body(otherPatient.getId(), "RESERVADA"))).andExpect(status().isForbidden());
        mvc.perform(put(base).header("Authorization", login("paciente")).contentType("application/json")
            .content(body(patient.getId(), "RESERVADA"))).andExpect(status().isForbidden());
        mvc.perform(delete(base).header("Authorization", login("medico"))).andExpect(status().isForbidden());
        mvc.perform(patch(base + "/cancelar").header("Authorization", login("paciente"))).andExpect(status().isOk())
            .andExpect(jsonPath("$.estatusCita.clave").value("CANCELADA"));
        mvc.perform(delete(base).header("Authorization", login("admin"))).andExpect(status().isNoContent());
    }

    private String login(String username) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/login").contentType("application/json")
            .content(json.writeValueAsString(Map.of("username", username, "password", "clave-prueba"))))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(body).get("token").asText();
    }
    private UsuarioEntity user(String username, String role) {
        var user = new UsuarioEntity(); user.setUsername(username); user.setPassword(encoder.encode("clave-prueba"));
        user.setNombres(username); user.setApellidoPaterno("Prueba"); user.setCorreo(username + "@example.com");
        user.setRoles(new HashSet<>(Set.of(roles.findByClaveIgnoreCase(role).orElseThrow()))); return users.save(user);
    }
    private CitaEntity cita(UsuarioEntity patient, String state) {
        var cita = new CitaEntity(); cita.setPaciente(patient); cita.setMedicoSucursal(assignment);
        cita.setEstatusCita(states.findByClave(state).orElseThrow());
        cita.setFechaHoraInicio(LocalDateTime.now().plusDays(3)); cita.setFechaHoraTermino(cita.getFechaHoraInicio().plusMinutes(30));
        cita.setFechaCreacion(LocalDateTime.now()); cita.setPrecio(new BigDecimal("500"));
        cita.setIva(new BigDecimal("80")); cita.setTotal(new BigDecimal("580")); return citas.save(cita);
    }
    private String body(Integer patientId, String state) throws Exception {
        return json.writeValueAsString(Map.of("medicoSucursalId", assignment.getId(), "pacienteId", patientId,
            "estatusCitaId", states.findByClave(state).orElseThrow().getId(),
            "fechaHoraInicio", LocalDateTime.now().plusDays(4).toString(),
            "fechaHoraTermino", LocalDateTime.now().plusDays(4).plusMinutes(30).toString(),
            "precio", 500, "iva", 80, "total", 580));
    }
}
