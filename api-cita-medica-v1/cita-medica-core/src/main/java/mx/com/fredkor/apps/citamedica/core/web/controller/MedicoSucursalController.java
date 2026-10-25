package mx.com.fredkor.apps.citamedica.core.web.controller;

import mx.com.fredkor.apps.citamedica.shared.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import mx.com.fredkor.apps.citamedica.core.application.service.MedicoSucursalService;
import mx.com.fredkor.apps.citamedica.core.web.dto.request.MedicoSucursalRequest;
import mx.com.fredkor.apps.citamedica.core.web.dto.response.MedicoSucursalResponse;
import mx.com.fredkor.apps.citamedica.shared.pagination.PaginatedResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/medico-sucursales")
@Tag(name = "Médicos sucursales", description = "API REST para mantener sucursales por médico")
public class MedicoSucursalController {
    private final MedicoSucursalService service;

    @Operation(summary = "Obtener sucursales por médico")
    @PreAuthorize("hasAuthority('MEDICO_SUCURSAL_VER_TODO')")
    @GetMapping
    public ResponseEntity<PaginatedResponseDto<MedicoSucursalResponse>> getAll(
            @RequestParam(defaultValue = "") String filtro,
            @Min(0) @RequestParam(defaultValue = "0") int page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size,
            @Pattern(regexp = "id|fechaCreacion", message = "Campo de ordenamiento no permitido") @RequestParam(defaultValue = "id") String orderBy,
            @Pattern(regexp = "(?i)asc|desc", message = "Debe ser asc o desc") @RequestParam(defaultValue = "asc") String orderDir) {
        var response = service.getMedicoSucursales(filtro, page, size, orderBy, orderDir);
        return response.content().isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener una sucursal de médico por ID")
    @PreAuthorize("hasAuthority('MEDICO_SUCURSAL_VER')")
    @GetMapping("/{id}")
    public ResponseEntity<MedicoSucursalResponse> getById(@PathVariable @Positive Integer id) {
        var response = service.getById(id);
        if (response == null) throw new ResourceNotFoundException("Recurso no encontrado: " + id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Asignar una sucursal a un médico")
    @PreAuthorize("hasAuthority('MEDICO_SUCURSAL_CREAR')")
    @PostMapping
    public ResponseEntity<MedicoSucursalResponse> create(@Valid @RequestBody MedicoSucursalRequest request) {
        return new ResponseEntity<>(service.create(request), CREATED);
    }

    @Operation(summary = "Actualizar una sucursal de médico")
    @PreAuthorize("hasAuthority('MEDICO_SUCURSAL_MODIFICAR')")
    @PutMapping("/{id}")
    public ResponseEntity<MedicoSucursalResponse> update(
            @PathVariable @Positive Integer id, @Valid @RequestBody MedicoSucursalRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Operation(summary = "Eliminar una sucursal de médico")
    @PreAuthorize("hasAuthority('MEDICO_SUCURSAL_ELIMINAR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
