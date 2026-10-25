package mx.com.fredkor.apps.citamedica.core.web.dto.response;

public record PacienteResponse(
    Integer id,
    String nombres,
    String apellidoPaterno,
    String apellidoMaterno
) {}
