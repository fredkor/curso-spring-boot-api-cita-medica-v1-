package mx.com.fredkor.apps.citamedica.platform.notification.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.com.fredkor.apps.citamedica.core.application.event.CitaCreatedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CitaEventListener {

    //private final EmailService emailService;

    @Async("notificationTaskExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleCitaCreatedEvent(
            CitaCreatedEvent event
    ) {
        log.info(
                "Procesando correo de cita creada: citaId={}",
                event.idCita()
        );

        try {
            //emailService.enviarConfirmacionCita(event.idCita());

            log.info(
                    "Correo de cita creada enviado: citaId={}",
                    event.idCita()
            );
        } catch (Exception exception) {
            log.error(
                    "No fue posible enviar el correo de la cita: citaId={}",
                    event.idCita(),
                    exception
            );
        }
    }
}