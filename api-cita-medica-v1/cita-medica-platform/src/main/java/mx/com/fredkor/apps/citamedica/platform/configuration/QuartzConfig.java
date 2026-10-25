package mx.com.fredkor.apps.citamedica.platform.configuration;

import mx.com.fredkor.apps.citamedica.platform.schedule.CitasNoConfirmadasJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail auditJobDetail() {
        return JobBuilder.newJob(CitasNoConfirmadasJob.class)
                .withIdentity("citasNoConfirmadasJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger auditJobTrigger(JobDetail auditJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(auditJobDetail)
                .withIdentity("citasNoConfirmadasJobTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 01 23 * * ?")) // This cron expression means every day at 11:20 AM
                .build();
    }
}
