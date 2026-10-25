package mx.com.fredkor.apps.citamedica.platform.observability.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggableAspect {

    @Before("@annotation(loggable)")
    public void auditoriaAction(JoinPoint joinpoint, Loggable loggable) {
        var methodName = joinpoint.getSignature().getName();
        var action = loggable.action();

        log.info("### Auditoria: Method: " + methodName + " - " + action);
    }
}
