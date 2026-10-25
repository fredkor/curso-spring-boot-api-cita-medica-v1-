package mx.com.fredkor.apps.citamedica.core.web.dto.response;

import mx.com.fredkor.apps.citamedica.catalogos.web.dto.response.EspecialidadResponse;

public record MedicoEspecialidadResponse(
    Integer id,
    MedicoResponse medico,
    EspecialidadResponse especialidad
) {}
