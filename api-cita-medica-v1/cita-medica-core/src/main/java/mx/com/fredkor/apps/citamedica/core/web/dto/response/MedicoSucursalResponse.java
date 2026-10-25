package mx.com.fredkor.apps.citamedica.core.web.dto.response;

import mx.com.fredkor.apps.citamedica.catalogos.web.dto.response.SucursalResponse;

public record MedicoSucursalResponse(
    Integer id,
    SucursalResponse sucursal,
    MedicoEspecialidadResponse medicoEspecialidad
) {}
