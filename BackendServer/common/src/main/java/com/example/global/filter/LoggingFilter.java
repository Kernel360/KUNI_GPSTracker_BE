package com.example.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
public class LoggingFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class);

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {

    // 요청/응답 Wrapping
    var requestWrapper = new ContentCachingRequestWrapper(request);
    var responseWrapper = new ContentCachingResponseWrapper(response);

    long startTime = System.currentTimeMillis();
    try {
      filterChain.doFilter(requestWrapper, responseWrapper);
    } finally {
      long duration = System.currentTimeMillis() - startTime;

      // 인코딩 처리 (응답은 강제로 UTF-8 fallback 적용)
      String requestEncoding = request.getCharacterEncoding() != null ? request.getCharacterEncoding() : "UTF-8";
      String responseEncoding = response.getCharacterEncoding() != null ? response.getCharacterEncoding() : "UTF-8";

      String requestBody = new String(requestWrapper.getContentAsByteArray(), requestEncoding);
      String responseBody = new String(responseWrapper.getContentAsByteArray(), responseEncoding);

      LOGGER.info("""
                    [HTTP REQUEST]
                    Method: {}
                    Path  : {}
                    Body  : {}
                    """,
          request.getMethod(),
          request.getRequestURI(),
          requestBody
      );

      LOGGER.info("""
                    [HTTP RESPONSE]
                    Status: {}
                    Duration: {}ms
                    Body: {}
                    """,
          response.getStatus(),
          duration,
          responseBody
      );

      responseWrapper.copyBodyToResponse();
    }
  }

  /**
   * Swagger 관련 요청은 로깅 제외
   */
  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    String uri = request.getRequestURI();
    return uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs");
  }
}
