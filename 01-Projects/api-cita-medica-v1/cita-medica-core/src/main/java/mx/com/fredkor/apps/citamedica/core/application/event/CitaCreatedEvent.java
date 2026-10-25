package mx.com.fredkor.apps.citamedica.core.application.event;

import lombok.Builder;

@Builder
public record CitaCreatedEvent(
        Integer idCita
) {
}
