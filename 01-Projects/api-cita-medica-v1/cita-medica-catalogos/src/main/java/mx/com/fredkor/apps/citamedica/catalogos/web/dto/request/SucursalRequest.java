package mx.com.fredkor.apps.citamedica.catalogos.web.dto.request;

import jakarta.validation.constraints.*;

public record SucursalRequest(
    @NotBlank(message = "nombre es obligatorio") @Size(max = 100) String nombre,
    @NotBlank(message = "direccion es obligatorio") @Size(max = 500) String direccion
) {}
