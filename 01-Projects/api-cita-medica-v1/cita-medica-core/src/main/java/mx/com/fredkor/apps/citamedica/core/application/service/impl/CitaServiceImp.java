package mx.com.fredkor.apps.citamedica.core.application.service.impl;

import mx.com.fredkor.security.persistence.repository.UsuarioRepository;
import mx.com.fredkor.apps.citamedica.core.persistence.repository.MedicoSucursalRepository;
import mx.com.fredkor.apps.citamedica.core.persistence.entity.CitaEntity;
import mx.com.fredkor.apps.citamedica.shared.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.com.fredkor.apps.citamedica.catalogos.persistence.entity.EstatusCitaEntity;
import mx.com.fredkor.apps.citamedica.catalogos.persistence.repository.EstatusCitaRepository;
import mx.com.fredkor.apps.citamedica.core.application.event.CitaCreatedEvent;
import mx.com.fredkor.apps.citamedica.core.application.event.CitaAuditEvent;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import mx.com.fredkor.apps.citamedica.core.web.dto.request.CitaFilterRequest;
import mx.com.fredkor.apps.citamedica.core.web.dto.request.CitaRequest;
import mx.com.fredkor.apps.citamedica.core.web.dto.response.CitaResponse;
import mx.com.fredkor.apps.citamedica.core.application.mapper.CitaMapper;
import mx.com.fredkor.apps.citamedica.core.persistence.repository.CitaRepository;
import mx.com.fredkor.apps.citamedica.core.application.service.CitaService;
import mx.com.fredkor.apps.citamedica.core.persistence.specification.CitaSpecification;
import mx.com.fredkor.apps.citamedica.shared.pagination.PaginatedResponseDto;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import mx.com.fredkor.security.persistence.entity.UsuarioEntity;
import mx.com.fredkor.apps.citamedica.shared.exception.BusinessException;
import org.springframework.data.jpa.domain.Specification;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@AllArgsConstructor
@Service
@Transactional(readOnly = true)
@Slf4j
public class CitaServiceImp implements CitaService {

    private static final String ESTATUS_RESERVADA = "RESERVADA";
    private static final String ESTATUS_CANCELADA = "CANCELADA";

    private final EstatusCitaRepository estatusCitaRepository;

    private final CitaRepository citaRepository;

    private final CitaMapper citaMapper;

    private final ApplicationEventPublisher eventPublisher;

    private final Clock clock;
    private final MedicoSucursalRepository medicoSucursalRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public PaginatedResponseDto<CitaResponse> getCitas(CitaFilterRequest filtro, int page, int size, String orderBy, String orderDir) {
        var actor = actorWithPermission("CITA_VER", "CITA_VER_TODO");
        var sort = orderDir.equalsIgnoreCase("desc")
                ? Sort.by(orderBy).descending()
                : Sort.by(orderBy).ascending();

        var pageable = PageRequest.of(page, size, sort);

        Specification<CitaEntity> specification = CitaSpecification.buscar(filtro);
        if (!hasPermission("CITA_VER_TODO")) {
            specification = specification.and((root, query, cb) -> cb.or(
                cb.equal(root.get("paciente").get("id"), actor.getId()),
                cb.equal(root.get("medicoSucursal").get("medicoEspecialidad").get("medico").get("id"), actor.getId())));
        }
        var pageResult = citaRepository.findAll(specification, pageable);

        var content = pageResult.getContent()
                .stream()
                .map(citaMapper::toResponse)
                .toList();

        return new PaginatedResponseDto<>(
                content,
                pageResult.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public CitaResponse getById(Integer id) {
        var actor = actorWithPermission("CITA_VER");
        var entity = citaRepository.findById(id).orElse(null);
        if (entity == null) return null;
        requireAccess(entity, actor);
        return citaMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public CitaResponse create(CitaRequest request) {
        var actor = actorWithPermission("CITA_CREAR");
        var entity = citaMapper.toEntity(request);
        setRelations(entity, request);
        requireAccess(entity, actor);
        if (!ESTATUS_RESERVADA.equals(entity.getEstatusCita().getClave())) {
            throw new BusinessException("Una cita nueva debe tener estado RESERVADA.");
        }
        if (!entity.getFechaHoraInicio().isAfter(LocalDateTime.now(clock))) {
            throw new BusinessException("La cita debe iniciar en una fecha futura.");
        }
        entity.setFechaCreacion(LocalDateTime.now(clock));
        var citaSaved = citaRepository.save(entity);

        // Se notifica de manera Asincrona
        eventPublisher.publishEvent(new CitaCreatedEvent(citaSaved.getId()));
        publishAudit(CitaAuditEvent.Action.CITA_CREATE, citaSaved.getId());
        return citaMapper.toResponse(citaSaved);
    }

    @Override
    @Transactional
    public CitaResponse update(Integer id, CitaRequest request) {
        var actor = actorWithPermission("CITA_MODIFICAR");
        var entity = lockedCita(id);
        requireAccess(entity, actor);
        String estado = entity.getEstatusCita().getClave();
        if (!Set.of("RESERVADA", "REPROGRAMADA").contains(estado)) {
            throw new BusinessException("Solo se pueden editar citas RESERVADAS o REPROGRAMADAS.");
        }
        if (!request.estatusCitaId().equals(entity.getEstatusCita().getId())) {
            throw new BusinessException("Utilice los endpoints de confirmar, cancelar o atender para cambiar el estado.");
        }
        if (!hasPermission("CITA_VER_TODO") &&
                (!request.pacienteId().equals(entity.getPaciente().getId())
                || !request.medicoSucursalId().equals(entity.getMedicoSucursal().getId())
                || request.precio().compareTo(entity.getPrecio()) != 0
                || request.iva().compareTo(entity.getIva()) != 0
                || request.total().compareTo(entity.getTotal()) != 0)) {
            throw new AccessDeniedException("Solo administración puede cambiar paciente, asignación médica o importes.");
        }
        if (!request.fechaHoraInicio().isAfter(LocalDateTime.now(clock))) {
            throw new BusinessException("La cita debe iniciar en una fecha futura.");
        }
        citaMapper.entityToUpdate(request, entity);
        setRelations(entity, request);
        requireAccess(entity, actor);
        entity.setFechaModificacion(LocalDateTime.now(clock));
        var saved = citaRepository.save(entity);
        publishAudit(CitaAuditEvent.Action.CITA_UPDATE, saved.getId());
        return citaMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        actorWithPermission("CITA_ELIMINAR");
        if (!hasPermission("CITA_VER_TODO")) throw new AccessDeniedException("Solo administración puede eliminar citas.");
        var entity = lockedCita(id);
        citaRepository.delete(entity);
        publishAudit(CitaAuditEvent.Action.CITA_DELETE, id);
    }

    @Override
    @Transactional
    public int cancelarCitasNoConfirmadas() {
        LocalDateTime ahora = LocalDateTime.now(clock);
        LocalDateTime limiteConfirmacion = ahora.plusHours(24);

        EstatusCitaEntity estatusReservada =
                obtenerEstatus(ESTATUS_RESERVADA);

        EstatusCitaEntity estatusCancelada =
                obtenerEstatus(ESTATUS_CANCELADA);

        int citasCanceladas =
                citaRepository.cancelarCitasNoConfirmadas(
                        estatusReservada,
                        estatusCancelada,
                        limiteConfirmacion,
                        ahora
                );

        log.info(
                "Citas no confirmadas procesadas: limite={}, canceladas={}",
                limiteConfirmacion,
                citasCanceladas
        );

        return citasCanceladas;
    }

    @Override
    @Transactional
    public CitaResponse confirmar(Integer id) {
        return transition(id, "CITA_CONFIRMAR", Set.of("RESERVADA", "REPROGRAMADA"), "CONFIRMADA", CitaAuditEvent.Action.CITA_CONFIRM);
    }

    @Override
    @Transactional
    public CitaResponse cancelar(Integer id) {
        return transition(id, "CITA_CANCELAR", Set.of("RESERVADA", "REPROGRAMADA", "CONFIRMADA"), "CANCELADA", CitaAuditEvent.Action.CITA_CANCEL);
    }

    @Override
    @Transactional
    public CitaResponse atender(Integer id) {
        return transition(id, "CITA_ATENDER", Set.of("CONFIRMADA"), "ATENDIDA", CitaAuditEvent.Action.CITA_ATTEND);
    }

    private CitaResponse transition(Integer id, String permission, Set<String> allowed, String target, CitaAuditEvent.Action action) {
        var actor = actorWithPermission(permission);
        var entity = lockedCita(id);
        requireAccess(entity, actor);
        if ("ATENDIDA".equals(target) && !hasPermission("CITA_VER_TODO") && !isAssignedDoctor(entity, actor)) {
            throw new AccessDeniedException("Solo el médico asignado puede registrar la atención.");
        }
        if (!allowed.contains(entity.getEstatusCita().getClave())) {
            throw new BusinessException("No se permite pasar de " + entity.getEstatusCita().getClave() + " a " + target + ".");
        }
        entity.setEstatusCita(obtenerEstatus(target));
        entity.setFechaModificacion(LocalDateTime.now(clock));
        var saved = citaRepository.save(entity);
        publishAudit(action, saved.getId());
        return citaMapper.toResponse(saved);
    }

    private CitaEntity lockedCita(Integer id) {
        return citaRepository.findByIdForUpdate(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada: " + id));
    }

    private UsuarioEntity actorWithPermission(String... permissions) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new BadCredentialsException("Debe autenticarse.");
        }
        if (java.util.Arrays.stream(permissions).noneMatch(this::hasPermission)) {
            throw new AccessDeniedException("Permisos insuficientes.");
        }
        return usuarioRepository.findByUsernameIgnoreCase(authentication.getName())
            .filter(user -> Boolean.TRUE.equals(user.getActivo()))
            .orElseThrow(() -> new BadCredentialsException("Usuario no disponible."));
    }

    private boolean hasPermission(String permission) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
            .anyMatch(authority -> permission.equals(authority.getAuthority()));
    }

    private void requireAccess(CitaEntity entity, UsuarioEntity actor) {
        if (!hasPermission("CITA_VER_TODO") && !actor.getId().equals(entity.getPaciente().getId()) && !isAssignedDoctor(entity, actor)) {
            throw new AccessDeniedException("La cita no pertenece al usuario ni está asignada al médico autenticado.");
        }
    }

    private boolean isAssignedDoctor(CitaEntity entity, UsuarioEntity actor) {
        return entity.getMedicoSucursal().getMedicoEspecialidad() != null
            && actor.getId().equals(entity.getMedicoSucursal().getMedicoEspecialidad().getMedico().getId());
    }

    private void publishAudit(CitaAuditEvent.Action action, Integer citaId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String actor = authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                ? authentication.getName() : "SISTEMA";
        eventPublisher.publishEvent(new CitaAuditEvent(action, citaId, actor, clock.instant(), MDC.get("traceId")));
    }

    private void setRelations(CitaEntity entity, CitaRequest request) {
        entity.setMedicoSucursal(medicoSucursalRepository.findById(request.medicoSucursalId())
            .orElseThrow(() -> new ResourceNotFoundException("Médico/sucursal no encontrado")));
        entity.setPaciente(usuarioRepository.findById(request.pacienteId())
            .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado")));
        entity.setEstatusCita(estatusCitaRepository.findById(request.estatusCitaId())
            .orElseThrow(() -> new ResourceNotFoundException("Estado de cita no encontrado")));
    }

    private EstatusCitaEntity obtenerEstatus(String clave) {
        return estatusCitaRepository.findByClave(clave)
                .orElseThrow(() -> new IllegalStateException(
                        "No se encontró el estatus de cita: " + clave
                ));
    }

}
