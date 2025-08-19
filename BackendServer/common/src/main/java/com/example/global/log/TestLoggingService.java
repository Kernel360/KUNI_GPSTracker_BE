package com.example.BackendServer.global.log;

import com.example.global.log.PerfLogging;
import org.springframework.stereotype.Service;

@Service
public class TestLoggingService {

  public void doInfo() {
    System.out.println("INFO 메서드 실행");
  }

  public void doWarn() {
    throw new RuntimeException("WARN/ERROR 테스트용 예외");
  }

  @PerfLogging
  public void annotatedMethod() {
    System.out.println("애노테이션 기반 로그 테스트 메서드 실행");
  }
}
