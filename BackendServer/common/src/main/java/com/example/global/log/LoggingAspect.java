package com.example.global.log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAspect.class);

  // com.example 하위의 모든 controller 메서드 호출 로그
  @Around("execution(* com.example..controller..*(..))")
  public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.currentTimeMillis();
    LOGGER.info(">>> API START: {}.{}()",
        joinPoint.getSignature().getDeclaringTypeName(),
        joinPoint.getSignature().getName()
    );
    Object result = joinPoint.proceed();
    long duration = System.currentTimeMillis() - start;
    LOGGER.info("<<< API END: {}.{}(), Duration: {}ms",
        joinPoint.getSignature().getDeclaringTypeName(),
        joinPoint.getSignature().getName(),
        duration
    );
    return result;
  }
}
