package mx.com.fredkor.apps.citamedica.platform.configuration;

import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Override
    @Bean(name = {"omsThreadPool", "notificationTaskExecutor"})
    public @Nullable Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);   // Hilos mínimos activos
        executor.setMaxPoolSize(10);   // Hilos máximos bajo carga
        executor.setQueueCapacity(25); // Cola de espera antes de rechazar
        executor.setThreadNamePrefix("OMS-Async-");
        executor.initialize();
        return executor;
    }
}
