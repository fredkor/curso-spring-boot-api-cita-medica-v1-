package mx.com.fredkor.apps.citamedica.usuario.web.dto.response;

import java.util.List;

public record UsuarioResponse(
    Integer id,
    String username,
    String nombres,
    String apellidoPaterno,
    String apellidoMaterno,
    String correo,
    List<UsuarioRolResponse> roles
) {}
