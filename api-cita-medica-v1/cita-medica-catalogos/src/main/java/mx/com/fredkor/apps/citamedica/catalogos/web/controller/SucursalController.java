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
import mx.com.fredkor.apps.citamedica.catalogos.web.dto.request.SucursalFilterRequest;
import mx.com.fredkor.apps.citamedica.catalogos.web.dto.request.SucursalRequest;
import mx.com.fredkor.apps.citamedica.catalogos.web.dto.response.SucursalResponse;
import mx.com.fredkor.apps.citamedica.catalogos.application.service.SucursalService;
import mx.com.fredkor.apps.citamedica.shared.pagination.PaginatedResponseDto;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/sucursales")
@Tag(name = "Sucursales", description = "API REST para mantener Sucursales")
public class SucursalController {

    private final SucursalService sucursalService;

    @Operation(
            summary = "Obtener sucursales",
            description = """
                Permite consultar las sucursales registradas de forma paginada.
                Se puede filtrar, ordenar y definir el tamaño de la página.
                """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Página de sucursales obtenida correctamente.",
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
    @PreAuthorize("hasAuthority('SUCURSAL_VER_TODO')")
    @GetMapping
    public ResponseEntity<PaginatedResponseDto<SucursalResponse>> getAll(
            @Valid
            @ParameterObject
            @ModelAttribute
            SucursalFilterRequest filtro,
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
            @Pattern(regexp = "id|nombre|direccion|fechaCreacion|activo", message = "Campo de ordenamiento no permitido") @RequestParam(defaultValue = "nombre")
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
        var response = sucursalService.getSucursales(
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
            summary = "Obtener sucursal por ID",
            description = "Permite consultar una sucursal específica mediante su identificador.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Clase encontrada correctamente.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SucursalResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No existe una sucursal con el identificador proporcionado.",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno del servidor.",
                            content = @Content
                    )
            }
    )
    @PreAuthorize("hasAuthority('SUCURSAL_VER')")
    @GetMapping("/{id}")
    public ResponseEntity<SucursalResponse> getById(
            @Parameter(
                    description = "Identificador único de la sucursal.",
                    example = "1",
                    required = true
            )
            @PathVariable @Positive Integer id
    ) {
        var sucursal = sucursalService.getById(id);

        if (sucursal == null) {
            throw new ResourceNotFoundException("Recurso no encontrado: " + id);
        }

        return ResponseEntity.ok(sucursal);
    }

    @Operation(
            summary = "Crear sucursal",
            description = "Permite registrar una nueva sucursal.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Clase creada correctamente.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SucursalResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Los datos enviados son inválidos.",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Ya existe una sucursal con los datos proporcionados.",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno del servidor.",
                            content = @Content
                    )
            }
    )
    @PreAuthorize("hasAuthority('SUCURSAL_CREAR')")
    @PostMapping
    public ResponseEntity<SucursalResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Información de la sucursal a registrar.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SucursalRequest.class)
                    )
            )
            @Valid @RequestBody SucursalRequest request
    ) {
        var sucursal = sucursalService.create(request);
        return new ResponseEntity<>(sucursal, CREATED);
    }

    @Operation(
            summary = "Actualizar sucursal",
            description = "Permite actualizar la información de una sucursal existente mediante su identificador.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Clase actualizada correctamente.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SucursalResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Los datos enviados son inválidos.",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No existe una sucursal con el identificador proporcionado.",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno del servidor.",
                            content = @Content
                    )
            }
    )
    @PreAuthorize("hasAuthority('SUCURSAL_MODIFICAR')")
    @PutMapping("/{id}")
    public ResponseEntity<SucursalResponse> update(
            @Parameter(
                    description = "Identificador único de la sucursal.",
                    required = true,
                    example = "1"
            )
            @PathVariable @Positive Integer id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Información actualizada de la sucursal.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SucursalRequest.class)
                    )
            )
            @Valid @RequestBody SucursalRequest request
    ) {
        var sucursal = sucursalService.update(id, request);
        return ResponseEntity.ok(sucursal);
    }

    @PreAuthorize("hasAuthority('SUCURSAL_ELIMINAR')")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Integer id) {
        sucursalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
