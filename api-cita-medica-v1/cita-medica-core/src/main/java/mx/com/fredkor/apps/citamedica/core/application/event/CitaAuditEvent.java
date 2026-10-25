package mx.com.fredkor.apps.citamedica.core.application.event;

import java.time.Instant;

/** Datos inmutables capturados dentro de la transacción, antes de perder el contexto HTTP. */
public record CitaAuditEvent(Action action, Integer citaId, String actor, Instant timestamp, String traceId) {
    public enum Action { CITA_CREATE, CITA_UPDATE, CITA_DELETE, CITA_CONFIRM, CITA_CANCEL, CITA_ATTEND }
}
