package mx.com.fredkor.apps.citamedica.catalogos.web.dto.request;

import jakarta.validation.constraints.*;

public record EspecialidadRequest(
    @NotBlank(message = "nombre es obligatorio") @Size(max = 100) String nombre
) {}
