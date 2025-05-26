package at.ac.tgm.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Aspekt zur Protokollierung der Ausführungszeiten von Methoden im Service-Layer.
 * Dieser Aspekt fängt Aufrufe aller Methoden im Package at.ac.tgm.service
 * ab und protokolliert die benötigte Zeit zur Ausführung. Dies dient der Überwachung und Optimierung
 * der Performance der Anwendung.
 */
@Aspect
@Component
public class PerformanceLoggingAspect {
    /**
     * Logger für Performance-Protokollierung.
     */
    private static final Logger PERF_LOGGER = LoggerFactory.getLogger("at.ac.tgm.performance");

    /**
     * Around Advice zur Messung und Protokollierung der Ausführungszeit einer Methode.
     *
     * @param joinPoint der JoinPoint, der die ausgeführte Methode repräsentiert
     * @return das Ergebnis der Methode
     * @throws Throwable falls die aufgerufene Methode eine Exception wirft
     */
    @Around("execution(public * at.ac.tgm..*Service.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;

        PERF_LOGGER.info("Methode {}.{}() dauerte {} ms",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                duration);

        return proceed;
    }
}
