package com.ef.stateeventslogger.commons;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Centralized logging using Spring AOP.
 */
@Aspect
@Component
public class EfLogger {
    /**
     * The Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(EfLogger.class);
    /**
     * The Object Mapper.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * App pointcut.
     */
    @Pointcut(value = "execution(* com.ef.stateeventslogger.controllers.*.*(..) )")
    public void appPointcut() {
        // This method is kept empty on purpose
    }

    /**
     * Common Logger.
     *
     * @param proceedingJoinPoint join point
     * @return Object object
     * @throws Throwable exception
     */
    @Around("appPointcut()")
    public Object routingEngineLogger(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        String className = proceedingJoinPoint.getTarget().getClass().toString();
        String methodName = proceedingJoinPoint.getSignature().getName();
        logger.info("{} : {}() Invoked", className, methodName);
        Object response = proceedingJoinPoint.proceed();
        String responseString = objectMapper.writeValueAsString(response);
        logger.info("{} : {}() Response : {}", className, methodName, responseString);
        return response;

    }
}