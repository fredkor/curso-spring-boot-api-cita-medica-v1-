package mx.com.fredkor.apps.citamedica.usuario.web.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;

public record UsuarioRequest(
    @NotBlank @Size(max = 50) String username,
    @NotBlank @Size(max = 150) String nombres,
    @NotBlank @Size(max = 150) String apellidoPaterno,
    @Size(max = 150) String apellidoMaterno,
    @NotBlank @Email @Size(max = 150) String correo,
    @NotEmpty List<@NotNull @Positive Integer> roles,
    @NotBlank(groups = Creacion.class)
    @Size(min = 6, max = 72)
    @io.swagger.v3.oas.annotations.media.Schema(accessMode = io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY, description = "Obligatoria al crear; omitir al editar para conservar la contraseña")
    String password
) {
    public interface Creacion {}

    @Override
    public String toString() {
        return "UsuarioRequest[username=" + username + ", password=[REDACTED]]";
    }
}
