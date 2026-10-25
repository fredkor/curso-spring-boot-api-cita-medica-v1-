package mx.com.fredkor.apps.citamedica.core.web.dto.request;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;


public record CitaFilterRequest(

        @Schema(
                description = "Nombre de la sucursal de la cita.",
                example = "Sucursal Centro"
        )
        @Size(max = 100) String nombre,

        @Schema(
                description = "Dirección de la sucursal de la cita.",
                example = "Avenida Reforma 100"
        )
        @Size(max = 500) String direccion
) {
}
