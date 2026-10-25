package mx.com.fredkor.apps.citamedica.usuario.web.dto.request;

import jakarta.validation.constraints.*;

public record UsuarioRolRequest(@NotNull @Positive Integer idRol) {}
