package mx.com.fredkor.apps.citamedica.platform.observability.filter;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class TraceabilityAspect {

    @Around("within(mx.com.fredkor.apps.citamedica.core.service..*)")
    public Object logServiceExecution(
            ProceedingJoinPoint joinPoint
    ) throws Throwable {

        String className =
                joinPoint.getSignature().getDeclaringType().getSimpleName();

        String methodName =
                joinPoint.getSignature().getName();

        log.info(
                "Iniciando servicio {}.{}",
                className,
                methodName
        );

        try {
            Object result = joinPoint.proceed();

            log.info(
                    "Servicio {}.{} finalizado correctamente",
                    className,
                    methodName
            );

            return result;
        } catch (Exception exception) {
            log.error(
                    "Error en servicio {}.{}: {}",
                    className,
                    methodName,
                    exception.getMessage(),
                    exception
            );

            throw exception;
        }
    }
}
