package mx.com.fredkor.apps.citamedica.core.web.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MedicoEspecialidadRequest(
        @Positive @NotNull(message = "El médico es obligatorio") Integer medicoId,
        @Positive @NotNull(message = "La especialidad es obligatoria") Integer especialidadId
) {
}
