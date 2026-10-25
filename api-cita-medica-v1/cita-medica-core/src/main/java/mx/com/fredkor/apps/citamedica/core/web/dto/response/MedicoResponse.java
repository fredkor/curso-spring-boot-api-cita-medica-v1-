package mx.com.fredkor.apps.citamedica.core.web.dto.response;

import mx.com.fredkor.apps.citamedica.catalogos.web.dto.response.EspecialidadResponse;

import java.util.List;

public record MedicoResponse(
    Integer id,
    String nombres,
    String apellidoPaterno,
    String apellidoMaterno,
    List<EspecialidadResponse> especialidades
) {}
