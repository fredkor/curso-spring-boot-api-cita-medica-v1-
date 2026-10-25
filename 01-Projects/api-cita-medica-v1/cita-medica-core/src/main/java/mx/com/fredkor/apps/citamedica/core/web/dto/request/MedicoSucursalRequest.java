package mx.com.fredkor.apps.citamedica.core.web.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MedicoSucursalRequest(
        @Positive @NotNull(message = "La especialidad del médico es obligatoria") Integer medicoEspecialidadId,
        @Positive @NotNull(message = "La sucursal es obligatoria") Integer sucursalId
) {
}
