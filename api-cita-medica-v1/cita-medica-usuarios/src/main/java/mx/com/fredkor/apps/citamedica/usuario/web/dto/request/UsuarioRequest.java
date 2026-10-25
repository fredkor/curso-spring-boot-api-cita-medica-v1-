package mx.com.fredkor.apps.citamedica.usuario.web.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;

public record UsuarioRequest(
    @NotBlank @Size(max = 50) String username,
    @NotBlank @Size(max = 150) String nombres,
    @NotBlank @Size(max = 150) String apellidoPaterno,
    @Size(max = 150) String apellidoMaterno,
    @NotBlank @Email @Size(max = 150) String correo,
    @NotEmpty List<@NotNull @Positive Integer> roles
) {}
