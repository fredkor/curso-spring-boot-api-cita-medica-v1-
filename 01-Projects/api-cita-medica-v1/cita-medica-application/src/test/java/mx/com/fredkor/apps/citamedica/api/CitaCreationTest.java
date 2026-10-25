package mx.com.fredkor.apps.citamedica.api;

import mx.com.fredkor.apps.citamedica.core.application.service.impl.CitaServiceImp;
import mx.com.fredkor.apps.citamedica.core.application.mapper.CitaMapper;
import mx.com.fredkor.apps.citamedica.core.application.event.CitaCreatedEvent;
import mx.com.fredkor.apps.citamedica.core.application.event.CitaAuditEvent;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.junit.jupiter.api.AfterEach;
import mx.com.fredkor.apps.citamedica.core.persistence.entity.*;
import mx.com.fredkor.apps.citamedica.core.persistence.repository.*;
import mx.com.fredkor.apps.citamedica.catalogos.persistence.entity.EstatusCitaEntity;
import mx.com.fredkor.apps.citamedica.catalogos.persistence.repository.EstatusCitaRepository;
import mx.com.fredkor.apps.citamedica.core.web.dto.request.CitaRequest;
import mx.com.fredkor.apps.citamedica.shared.exception.ResourceNotFoundException;
import mx.com.fredkor.security.persistence.entity.UsuarioEntity;
import mx.com.fredkor.security.persistence.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.context.ApplicationEventPublisher;
import org.mockito.MockMakers;
import java.math.BigDecimal;
import java.time.*;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class CitaCreationTest {
    @AfterEach
    void clearSecurity() { SecurityContextHolder.clearContext(); }

    @Test
    void creationResolvesReferencesPreservesTimeAndPublishesEvent() {
        var repository = proxy(CitaRepository.class);
        var estados = proxy(EstatusCitaRepository.class);
        var medicos = proxy(MedicoSucursalRepository.class);
        var usuarios = proxy(UsuarioRepository.class);
        var events = proxy(ApplicationEventPublisher.class);
        var medico = new MedicoSucursalEntity(); medico.setId(1);
        var usuario = new UsuarioEntity(); usuario.setId(6);
        var estado = new EstatusCitaEntity(); estado.setId(1); estado.setClave("RESERVADA");
        when(medicos.findById(1)).thenReturn(Optional.of(medico));
        when(usuarios.findById(6)).thenReturn(Optional.of(usuario));
        when(usuarios.findByUsernameIgnoreCase("administrador")).thenReturn(Optional.of(usuario));
        when(estados.findById(1)).thenReturn(Optional.of(estado));
        when(repository.save(any())).thenAnswer(invocation -> {
            CitaEntity entity = invocation.getArgument(0);
            assertSame(medico, entity.getMedicoSucursal());
            assertSame(usuario, entity.getPaciente());
            assertSame(estado, entity.getEstatusCita());
            assertEquals(LocalDateTime.of(2027, 1, 1, 0, 0), entity.getFechaCreacion());
            entity.setId(12);
            return entity;
        });
        var service = new CitaServiceImp(estados, repository, Mappers.getMapper(CitaMapper.class), events,
            Clock.fixed(Instant.parse("2027-01-01T00:00:00Z"), ZoneOffset.UTC), medicos, usuarios);
        var inicio = LocalDateTime.of(2027, 1, 20, 9, 0);
        var request = new CitaRequest(1, 6, 1, inicio, inicio.plusMinutes(30),
            new BigDecimal("500.00"), new BigDecimal("80.00"), new BigDecimal("580.00"));
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("administrador", null, java.util.List.of(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("CITA_CREAR"),
                new org.springframework.security.core.authority.SimpleGrantedAuthority("CITA_VER_TODO"))));
        var response = service.create(request);
        assertEquals(12, response.id());
        assertEquals(inicio, response.fechaHoraInicio());
        assertEquals(request.total(), response.total());
        verify(events).publishEvent(new CitaCreatedEvent(12));
        verify(events).publishEvent(new CitaAuditEvent(CitaAuditEvent.Action.CITA_CREATE, 12,
            "administrador", Instant.parse("2027-01-01T00:00:00Z"), null));
    }

    @Test
    void missingMedicalAssignmentDoesNotSaveOrPublish() {
        var repository = proxy(CitaRepository.class);
        var events = proxy(ApplicationEventPublisher.class);
        var medicos = proxy(MedicoSucursalRepository.class);
        when(medicos.findById(999)).thenReturn(Optional.empty());
        var usuarios = proxy(UsuarioRepository.class);
        var actor = new UsuarioEntity(); actor.setId(1);
        when(usuarios.findByUsernameIgnoreCase("administrador")).thenReturn(Optional.of(actor));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("administrador", null,
            java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("CITA_CREAR"))));
        var service = new CitaServiceImp(proxy(EstatusCitaRepository.class), repository,
            Mappers.getMapper(CitaMapper.class), events, Clock.systemUTC(), medicos, usuarios);
        var inicio = LocalDateTime.of(2027, 1, 20, 9, 0);
        assertThrows(ResourceNotFoundException.class, () -> service.create(new CitaRequest(999, 6, 1,
            inicio, inicio.plusMinutes(30), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)));
        verifyNoInteractions(repository, events);
    }

    private <T> T proxy(Class<T> type) { return mock(type, withSettings().mockMaker(MockMakers.PROXY)); }
}
