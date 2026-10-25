package mx.com.fredkor.apps.citamedica.core.web.controller;

import mx.com.fredkor.apps.citamedica.shared.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import mx.com.fredkor.apps.citamedica.core.web.dto.request.CitaFilterRequest;
import mx.com.fredkor.apps.citamedica.core.web.dto.request.CitaRequest;
import mx.com.fredkor.apps.citamedica.core.web.dto.response.CitaResponse;
import mx.com.fredkor.apps.citamedica.core.application.service.CitaService;
import mx.com.fredkor.apps.citamedica.shared.pagination.PaginatedResponseDto;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/citas")
@Tag(name = "Citas", description = "API REST para mantener Citas")
public class CitaController {

    private final CitaService citaService;

    @Operation(
            summary = "Obtener citas",
            description = """
                Permite consultar las citas registradas de forma paginada.
                Se puede filtrar, ordenar y definir el tamaño de la página.
                """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Página de citas obtenida correctamente.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = PaginatedResponseDto.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Ocurrió un error interno en el servidor.",
                            content = @Content
                    )
            }
    )
    @PreAuthorize("hasAnyAuthority('CITA_VER_TODO', 'CITA_VER')")
    @GetMapping
    public ResponseEntity<PaginatedResponseDto<CitaResponse>> getAll(
            @Valid
            @ParameterObject
            @ModelAttribute
            CitaFilterRequest filtro,
            @Parameter(
                    description = "Número de página. Comienza en cero.",
                    example = "0"
            )
            @Min(0) @RequestParam(defaultValue = "0")
            int page,

            @Parameter(
                    description = "Cantidad de registros por página.",
                    example = "10"
            )
            @Min(1) @Max(100) @RequestParam(defaultValue = "10")
            int size,

            @Parameter(
                    description = "Campo utilizado para ordenar los resultados.",
                    example = "id"
            )
            @Pattern(regexp = "id|fechaHoraInicio|fechaHoraTermino|fechaCreacion|precio|iva|total", message = "Campo de ordenamiento no permitido") @RequestParam(defaultValue = "id")
            String orderBy,

            @Parameter(
                    description = "Dirección del ordenamiento.",
                    example = "asc",
                    schema = @Schema(
                            allowableValues = {"asc", "desc"}
                    )
            )
            @Pattern(regexp = "(?i)asc|desc", message = "Debe ser asc o desc") @RequestParam(defaultValue = "asc")
            String orderDir
    ) {
        var response = citaService.getCitas(
                filtro,
                page,
                size,
                orderBy,
                orderDir
        );

        if (response.content().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obtener cita por ID",
            description = "Permite consultar una cita específica mediante su identificador.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Cita encontrada correctamente.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CitaResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No existe una cita con el identificador proporcionado.",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno del servidor.",
                            content = @Content
                    )
            }
    )
    @PreAuthorize("hasAuthority('CITA_VER')")
    @GetMapping("/{id}")
    public ResponseEntity<CitaResponse> getById(
            @Parameter(
                    description = "Identificador único de la cita.",
                    example = "1",
                    required = true
            )
            @PathVariable @Positive Integer id
    ) {
        var cita = citaService.getById(id);

        if (cita == null) {
            throw new ResourceNotFoundException("Recurso no encontrado: " + id);
        }

        return ResponseEntity.ok(cita);
    }

    @Operation(
            summary = "Crear cita",
            description = "Permite registrar una nueva cita.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Cita creada correctamente.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CitaResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Los datos enviados son inválidos.",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Ya existe una cita con los datos proporcionados.",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno del servidor.",
                            content = @Content
                    )
            }
    )
    @PreAuthorize("hasAuthority('CITA_CREAR')")
    @PostMapping
    public ResponseEntity<CitaResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Información de la cita a registrar.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CitaRequest.class)
                    )
            )
            @Valid @RequestBody CitaRequest request
    ) {
        var cita = citaService.create(request);
        return new ResponseEntity<>(cita, CREATED);
    }

    @Operation(
            summary = "Actualizar cita",
            description = "Permite actualizar la información de una cita existente mediante su identificador.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Cita actualizada correctamente.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CitaResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Los datos enviados son inválidos.",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No existe una cita con el identificador proporcionado.",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno del servidor.",
                            content = @Content
                    )
            }
    )
    @PreAuthorize("hasAuthority('CITA_MODIFICAR')")
    @PutMapping("/{id}")
    public ResponseEntity<CitaResponse> update(
            @Parameter(
                    description = "Identificador único de la cita.",
                    required = true,
                    example = "1"
            )
            @PathVariable @Positive Integer id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Información actualizada de la cita.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CitaRequest.class)
                    )
            )
            @Valid @RequestBody CitaRequest request
    ) {
        var cita = citaService.update(id, request);
        return ResponseEntity.ok(cita);
    }

    @PreAuthorize("hasAuthority('CITA_ELIMINAR')")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Integer id) {
        citaService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "Confirmar una cita", description = "RESERVADA o REPROGRAMADA a CONFIRMADA. Solo el paciente, el médico asignado o un administrador.")
    @PreAuthorize("hasAuthority('CITA_CONFIRMAR')")
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<CitaResponse> confirmar(@PathVariable @Positive Integer id) {
        return ResponseEntity.ok(citaService.confirmar(id));
    }

    @Operation(summary = "Cancelar una cita", description = "Cancela una cita RESERVADA, REPROGRAMADA o CONFIRMADA accesible al usuario.")
    @PreAuthorize("hasAuthority('CITA_CANCELAR')")
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<CitaResponse> cancelar(@PathVariable @Positive Integer id) {
        return ResponseEntity.ok(citaService.cancelar(id));
    }

    @Operation(summary = "Registrar atención médica", description = "CONFIRMADA a ATENDIDA. Solo el médico asignado o un administrador con CITA_ATENDER.")
    @PreAuthorize("hasAuthority('CITA_ATENDER')")
    @PatchMapping("/{id}/atender")
    public ResponseEntity<CitaResponse> atender(@PathVariable @Positive Integer id) {
        return ResponseEntity.ok(citaService.atender(id));
    }
}
