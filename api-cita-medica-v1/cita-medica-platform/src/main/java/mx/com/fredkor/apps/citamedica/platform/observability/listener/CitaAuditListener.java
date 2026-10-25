package mx.com.fredkor.apps.citamedica.platform.observability.listener;

import lombok.extern.slf4j.Slf4j;
import mx.com.fredkor.apps.citamedica.core.application.event.CitaAuditEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Auditoría de operaciones confirmadas. Reemplaza el advice @AfterReturning anterior. */
@Slf4j
@Component
public class CitaAuditListener {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void auditCita(CitaAuditEvent event) {
        log.info("[AUDIT] action={} citaId={} actor={} timestamp={} traceId={}",
                event.action(), event.citaId(), event.actor(), event.timestamp(), event.traceId());
    }
}
