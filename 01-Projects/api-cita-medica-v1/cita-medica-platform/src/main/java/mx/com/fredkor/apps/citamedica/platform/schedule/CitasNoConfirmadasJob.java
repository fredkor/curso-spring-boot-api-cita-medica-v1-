package mx.com.fredkor.apps.citamedica.platform.schedule;

import lombok.extern.slf4j.Slf4j;
import mx.com.fredkor.apps.citamedica.core.application.service.CitaService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@DisallowConcurrentExecution
public class CitasNoConfirmadasJob extends QuartzJobBean {

    private CitaService citaService;

    @Autowired
    public void setCitaService(CitaService citaService) {
        this.citaService = citaService;
    }

    @Override
    protected void executeInternal(JobExecutionContext context)
            throws JobExecutionException {

        log.info(
                "Iniciando cancelación de citas no confirmadas: fireTime={}, nextFireTime={}",
                context.getFireTime(),
                context.getNextFireTime()
        );

        try {
            int citasCanceladas = citaService.cancelarCitasNoConfirmadas();

            log.info(
                    "Cancelación de citas no confirmadas finalizada: canceladas={}",
                    citasCanceladas
            );
        } catch (Exception exception) {
            log.error(
                    "Ocurrió un error al cancelar las citas no confirmadas",
                    exception
            );

            JobExecutionException jobException = new JobExecutionException(
                    "Error durante la cancelación de citas no confirmadas",
                    exception
            );

            throw jobException;
        }
    }
}