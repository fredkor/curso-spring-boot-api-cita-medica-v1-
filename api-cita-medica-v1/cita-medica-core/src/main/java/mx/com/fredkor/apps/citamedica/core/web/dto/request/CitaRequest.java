package mx.com.fredkor.apps.citamedica.core.web.dto.request;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CitaRequest(
    @NotNull @Positive @Schema(example = "1") Integer medicoSucursalId,
    @NotNull @Positive @Schema(example = "6") Integer pacienteId,
    @NotNull @Positive @Schema(description = "En creación debe identificar RESERVADA; en edición debe conservar el estado actual", example = "1") Integer estatusCitaId,
    @NotNull @Schema(example = "2027-01-20T09:00:00") LocalDateTime fechaHoraInicio,
    @NotNull @Schema(example = "2027-01-20T09:30:00") LocalDateTime fechaHoraTermino,
    @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2)
    @Schema(example = "500.00") BigDecimal precio,
    @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2)
    @Schema(description = "Importe del IVA, no porcentaje", example = "80.00") BigDecimal iva,
    @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2)
    @Schema(example = "580.00") BigDecimal total
) {
    @AssertTrue(message = "El término debe ser posterior al inicio")
    @Schema(hidden = true)
    public boolean isHorarioValido() {
        return fechaHoraInicio == null || fechaHoraTermino == null
            || fechaHoraTermino.isAfter(fechaHoraInicio);
    }

    @AssertTrue(message = "El total debe ser igual a precio + IVA")
    @Schema(hidden = true)
    public boolean isTotalValido() {
        return precio == null || iva == null || total == null
            || precio.add(iva).compareTo(total) == 0;
    }
}
