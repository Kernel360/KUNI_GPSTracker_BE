package com.example.BackendServer.global.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 이 애노테이션을 사용하면 메서드의 처음과 끝에 어떤 메서드인지 출력해줍니다.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface PerfLogging {

}
