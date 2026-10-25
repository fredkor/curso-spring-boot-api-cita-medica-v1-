package mx.com.fredkor.apps.citamedica.usuario.web.controller;

import mx.com.fredkor.apps.citamedica.shared.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;

import mx.com.fredkor.apps.citamedica.shared.pagination.PaginatedResponseDto;
import mx.com.fredkor.apps.citamedica.usuario.web.dto.request.UsuarioRequest;
import mx.com.fredkor.apps.citamedica.usuario.web.dto.response.UsuarioResponse;
import mx.com.fredkor.apps.citamedica.usuario.application.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "API REST para mantener Usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Operation(
            summary = "Obtener usuarios",
            description = """
                Permite consultar las usuarios registradas de forma paginada.
                Se puede filtrar, ordenar y definir el tamaño de la página.
                """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Página de usuarios obtenida correctamente.",
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
    @PreAuthorize("hasAuthority('USUARIO_VER')")
    @GetMapping
    public ResponseEntity<PaginatedResponseDto<UsuarioResponse>> getAll(
            @Parameter(
                    description = "Texto utilizado para filtrar las usuarios.",
                    example = ""
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
                    example = "nombres"
            )
            @Pattern(regexp = "id|username|nombres|apellidoPaterno|apellidoMaterno|correo|fechaCreacion|activo", message = "Campo de ordenamiento no permitido") @RequestParam(defaultValue = "nombres")
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
        var response = usuarioService.getUsuarios(
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
            summary = "Obtener usuario por ID",
            description = "Permite consultar una usuario específica mediante su identificador.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Clase encontrada correctamente.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UsuarioResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No existe un usuario con el identificador proporcionado.",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno del servidor.",
                            content = @Content
                    )
            }
    )
    @PreAuthorize("hasAuthority('USUARIO_VER')")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> getById(
            @Parameter(
                    description = "Identificador único de la usuario.",
                    example = "1",
                    required = true
            )
            @PathVariable @Positive Integer id
    ) {
        var usuario = usuarioService.getById(id);

        if (usuario == null) {
            throw new ResourceNotFoundException("Recurso no encontrado: " + id);
        }

        return ResponseEntity.ok(usuario);
    }

    @Operation(
            summary = "Crear usuario",
            description = "Permite registrar un nuevo usuario.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Clase creada correctamente.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UsuarioResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Los datos enviados son inválidos.",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Ya existe una usuario con los datos proporcionados.",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno del servidor.",
                            content = @Content
                    )
            }
    )
    @PreAuthorize("hasAuthority('USUARIO_CREAR')")
    @PostMapping
    public ResponseEntity<UsuarioResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Información de la usuario a registrar.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UsuarioRequest.class)
                    )
            )
            @org.springframework.validation.annotation.Validated({jakarta.validation.groups.Default.class, UsuarioRequest.Creacion.class}) @RequestBody UsuarioRequest request
    ) {
        var usuario = usuarioService.create(request);
        return new ResponseEntity<>(usuario, CREATED);
    }

    @Operation(
            summary = "Actualizar usuario",
            description = "Permite actualizar la información de un usuario existente mediante su identificador.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Clase actualizada correctamente.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UsuarioResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Los datos enviados son inválidos.",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No existe un usuario con el identificador proporcionado.",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno del servidor.",
                            content = @Content
                    )
            }
    )
    @PreAuthorize("hasAuthority('USUARIO_MODIFICAR')")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> update(
            @Parameter(
                    description = "Identificador único de la usuario.",
                    required = true,
                    example = "1"
            )
            @PathVariable @Positive Integer id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Información actualizada de la usuario.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UsuarioRequest.class)
                    )
            )
            @Valid @RequestBody UsuarioRequest request
    ) {
        var usuario = usuarioService.update(id, request);
        return ResponseEntity.ok(usuario);
    }


    @PreAuthorize("hasAuthority('USUARIO_ELIMINAR')")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Integer id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
