package com.example.global.log;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestLoggingController {

  private final com.example.global.log.TestLoggingService testLoggingService;

  public TestLoggingController(com.example.global.log.TestLoggingService testLoggingService) {
    this.testLoggingService = testLoggingService;
  }

  @GetMapping("/test/info")
  public String testInfo() {
    testLoggingService.doInfo();
    return "INFO 호출 완료";
  }

  @GetMapping("/test/warn")
  public String testWarn() {
    try {
      testLoggingService.doWarn();
    } catch (Exception e) {
      // 예외를 잡아도 AOP 로그에서 WARN/ERROR 파일에 기록됨
    }
    return "WARN/ERROR 호출 완료";
  }

  @GetMapping("/test/annotated")
  public String testAnnotated() {
    testLoggingService.annotatedMethod();
    return "@PerfLogging 호출 완료";
  }
}
