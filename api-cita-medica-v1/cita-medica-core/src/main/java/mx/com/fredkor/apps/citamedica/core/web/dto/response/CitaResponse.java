package mx.com.fredkor.apps.citamedica.core.web.dto.response;

import mx.com.fredkor.apps.citamedica.catalogos.web.dto.response.EstatusCitaResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CitaResponse(
    Integer id,
    MedicoSucursalResponse medicoSucursal,
    PacienteResponse paciente,
    EstatusCitaResponse estatusCita,
    LocalDateTime fechaHoraInicio,
    LocalDateTime fechaHoraTermino,
    LocalDateTime fechaCreacion,
    BigDecimal precio,
    BigDecimal iva,
    BigDecimal total
) {}
