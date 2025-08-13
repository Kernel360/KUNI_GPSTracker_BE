package com.example.BackendServer.global.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.annotations.Comment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
public class LogAspect {

    //@Around("execution(* com.example.BackendServer.dashboard..*Service.*(..))")
    @Around("execution(* com.example.BackendServer..*Service.*(..))")
    public Object logPerf(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        log.info("▶ Start: {}", methodName);

        try {
            Object result = joinPoint.proceed();
            log.info("◀ End: {}", methodName);
            return result;
        } catch (Throwable ex) {
            log.error("✖ Exception in {}: {}", methodName, ex.getMessage(), ex);
            throw ex;
        }
    }

    //애노테이션 기반
    @Around("@annotation(PerfLogging)")
    public Object annotationPerf(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        log.info("▶ Start: {}", methodName);

        try {
            Object result = joinPoint.proceed();
            log.info("◀ End: {}", methodName);
            return result;
        } catch (Throwable ex) {
            log.error("✖ Exception in {}: {}", methodName, ex.getMessage(), ex);
            throw ex;
        }
    }
}
