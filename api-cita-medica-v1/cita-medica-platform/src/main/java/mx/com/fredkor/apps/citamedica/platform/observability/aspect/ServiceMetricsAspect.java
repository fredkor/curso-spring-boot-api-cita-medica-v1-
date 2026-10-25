package mx.com.fredkor.apps.citamedica.platform.observability.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
public class ServiceMetricsAspect {

    @Around(
            "execution(public * " +
                    "mx.com.fredkor.apps.citamedica.core.application.service.impl..*(..))"
    )
    public Object measureExecution(
            ProceedingJoinPoint point
    ) throws Throwable {

        MethodSignature signature =
                (MethodSignature) point.getSignature();

        String className =
                signature.getDeclaringType().getSimpleName();

        String methodName = signature.getName();
        String traceId = MDC.get("traceId");

        long startTime = System.nanoTime();

        log.info(
                "Iniciando servicio: class={}, method={}, traceId={}",
                className,
                methodName,
                traceId
        );

        try {
            Object result = point.proceed();

            log.info(
                    "Servicio finalizado: class={}, method={}, durationMs={}, result=SUCCESS, traceId={}",
                    className,
                    methodName,
                    elapsedMilliseconds(startTime),
                    traceId
            );

            return result;
        } catch (Throwable exception) {
            log.error(
                    "Error ejecutando servicio: class={}, method={}, durationMs={}, result=ERROR, exception={}, traceId={}",
                    className,
                    methodName,
                    elapsedMilliseconds(startTime),
                    exception.getClass().getSimpleName(),
                    traceId,
                    exception
            );

            throw exception;
        }
    }

    private long elapsedMilliseconds(long startTime) {
        return TimeUnit.NANOSECONDS.toMillis(
                System.nanoTime() - startTime
        );
    }
}