package mx.com.fredkor.apps.citamedica.catalogos.web.controller;

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
import mx.com.fredkor.apps.citamedica.catalogos.web.dto.request.EspecialidadRequest;
import mx.com.fredkor.apps.citamedica.catalogos.web.dto.response.EspecialidadResponse;
import mx.com.fredkor.apps.citamedica.catalogos.application.service.EspecialidadService;
import mx.com.fredkor.apps.citamedica.shared.pagination.PaginatedResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/especialidades")
@Tag(name = "Especialidades", description = "API REST para mantener Especialidades")
public class EspecialidadController {

    private final EspecialidadService especialidadService;

    @Operation(
            summary = "Obtener especialidades",
            description = """
                Permite consultar las especialidades registradas de forma paginada.
                Se puede filtrar, ordenar y definir el tamaño de la página.
                """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Página de especialidades obtenida correctamente.",
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
    @PreAuthorize("hasAuthority('ESPECIALIDAD_VER_TODO')")
    @GetMapping
    public ResponseEntity<PaginatedResponseDto<EspecialidadResponse>> getAll(
            @Parameter(
                    description = "Texto utilizado para filtrar las especialidades.",
                    example = "Dentista"
            )
            @RequestParam(defaultValue = "")
            String filtro,
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
                    example = "nombre"
            )
            @Pattern(regexp = "id|nombre|activo", message = "Campo de ordenamiento no permitido") @RequestParam(defaultValue = "nombre")
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
        var response = especialidadService.getEspecialidades(
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
            summary = "Obtener especialidad por ID",
            description = "Permite consultar una especialidad específica mediante su identificador.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Clase encontrada correctamente.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = EspecialidadResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No existe una especialidad con el identificador proporcionado.",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno del servidor.",
                            content = @Content
                    )
            }
    )
    @PreAuthorize("hasAuthority('ESPECIALIDAD_VER')")
    @GetMapping("/{id}")
    public ResponseEntity<EspecialidadResponse> getById(
            @Parameter(
                    description = "Identificador único de la especialidad.",
                    example = "1",
                    required = true
            )
            @PathVariable @Positive Integer id
    ) {
        var especialidad = especialidadService.getById(id);

        if (especialidad == null) {
            throw new ResourceNotFoundException("Recurso no encontrado: " + id);
        }

        return ResponseEntity.ok(especialidad);
    }

    @Operation(
            summary = "Crear especialidad",
            description = "Permite registrar una nueva especialidad.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Clase creada correctamente.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = EspecialidadResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Los datos enviados son inválidos.",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Ya existe una especialidad con los datos proporcionados.",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno del servidor.",
                            content = @Content
                    )
            }
    )
    @PreAuthorize("hasAuthority('ESPECIALIDAD_CREAR')")
    @PostMapping
    public ResponseEntity<EspecialidadResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Información de la especialidad a registrar.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = EspecialidadRequest.class)
                    )
            )
            @Valid @RequestBody EspecialidadRequest request
    ) {
        var especialidad = especialidadService.create(request);
        return new ResponseEntity<>(especialidad, CREATED);
    }

    @Operation(
            summary = "Actualizar especialidad",
            description = "Permite actualizar la información de una especialidad existente mediante su identificador.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Clase actualizada correctamente.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = EspecialidadResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Los datos enviados son inválidos.",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No existe una especialidad con el identificador proporcionado.",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno del servidor.",
                            content = @Content
                    )
            }
    )
    @PreAuthorize("hasAuthority('ESPECIALIDAD_MODIFICAR')")
    @PutMapping("/{id}")
    public ResponseEntity<EspecialidadResponse> update(
            @Parameter(
                    description = "Identificador único de la especialidad.",
                    required = true,
                    example = "1"
            )
            @PathVariable @Positive Integer id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Información actualizada de la especialidad.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = EspecialidadRequest.class)
                    )
            )
            @Valid @RequestBody EspecialidadRequest request
    ) {
        var especialidad = especialidadService.update(id, request);
        return ResponseEntity.ok(especialidad);
    }

    @PreAuthorize("hasAuthority('ESPECIALIDAD_ELIMINAR')")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Integer id) {
        especialidadService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
