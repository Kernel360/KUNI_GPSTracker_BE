package com.example.global.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class PerfLoggingAspect {

  @Around("@annotation(com.example.global.log.PerfLogging)")
  public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.currentTimeMillis();
    String methodName = joinPoint.getSignature().toShortString();
    log.info("▶▶ [@PerfLogging] {} 시작", methodName);

    try {
      Object result = joinPoint.proceed();
      long duration = System.currentTimeMillis() - start;
      log.info("✔✔ [@PerfLogging] {} 종료 ({}ms)", methodName, duration);
      return result;
    } catch (Throwable ex) {
      log.error("⚠⚠ [@PerfLogging] {} 중 예외 발생: {}", methodName, ex.getMessage(), ex);
      throw ex;
    }
  }
}
