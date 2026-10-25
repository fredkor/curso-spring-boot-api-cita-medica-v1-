package mx.com.fredkor.apps.citamedica.core.web.controller;

import mx.com.fredkor.apps.citamedica.shared.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import mx.com.fredkor.apps.citamedica.core.application.service.MedicoEspecialidadService;
import mx.com.fredkor.apps.citamedica.core.web.dto.request.MedicoEspecialidadRequest;
import mx.com.fredkor.apps.citamedica.core.web.dto.response.MedicoEspecialidadResponse;
import mx.com.fredkor.apps.citamedica.shared.pagination.PaginatedResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/medico-especialidades")
@Tag(name = "Médicos especialidades", description = "API REST para mantener especialidades por médico")
public class MedicoEspecialidadController {
    private final MedicoEspecialidadService service;

    @Operation(summary = "Obtener especialidades por médico")
    @PreAuthorize("hasAuthority('MEDICO_ESPECIALIDAD_VER_TODO')")
    @GetMapping
    public ResponseEntity<PaginatedResponseDto<MedicoEspecialidadResponse>> getAll(
            @RequestParam(defaultValue = "") String filtro,
            @Min(0) @RequestParam(defaultValue = "0") int page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size,
            @Pattern(regexp = "id|fechaCreacion", message = "Campo de ordenamiento no permitido") @RequestParam(defaultValue = "id") String orderBy,
            @Pattern(regexp = "(?i)asc|desc", message = "Debe ser asc o desc") @RequestParam(defaultValue = "asc") String orderDir) {
        var response = service.getMedicoEspecialidades(filtro, page, size, orderBy, orderDir);
        return response.content().isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener una especialidad de médico por ID")
    @PreAuthorize("hasAuthority('MEDICO_ESPECIALIDAD_VER')")
    @GetMapping("/{id}")
    public ResponseEntity<MedicoEspecialidadResponse> getById(@PathVariable @Positive Integer id) {
        var response = service.getById(id);
        if (response == null) throw new ResourceNotFoundException("Recurso no encontrado: " + id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Asignar una especialidad a un médico")
    @PreAuthorize("hasAuthority('MEDICO_ESPECIALIDAD_CREAR')")
    @PostMapping
    public ResponseEntity<MedicoEspecialidadResponse> create(@Valid @RequestBody MedicoEspecialidadRequest request) {
        return new ResponseEntity<>(service.create(request), CREATED);
    }

    @Operation(summary = "Actualizar una especialidad de médico")
    @PreAuthorize("hasAuthority('MEDICO_ESPECIALIDAD_MODIFICAR')")
    @PutMapping("/{id}")
    public ResponseEntity<MedicoEspecialidadResponse> update(
            @PathVariable @Positive Integer id, @Valid @RequestBody MedicoEspecialidadRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Operation(summary = "Eliminar una especialidad de médico")
    @PreAuthorize("hasAuthority('MEDICO_ESPECIALIDAD_ELIMINAR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
