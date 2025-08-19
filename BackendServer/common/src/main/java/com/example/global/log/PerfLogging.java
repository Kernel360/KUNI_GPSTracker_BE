package com.example.global.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 이 애노테이션을 사용하면 메서드의 처음과 끝에 로그를 남깁니다.
 */
@Retention(RetentionPolicy.RUNTIME) // ⚠ 여기 CLASS → RUNTIME으로 바꿔야 AOP에서 동작
@Target(ElementType.METHOD)
public @interface PerfLogging {
}
