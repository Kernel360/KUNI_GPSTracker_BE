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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Component
public class LoggingFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class);

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {

    var requestWrapper = new ContentCachingRequestWrapper(request);
    var responseWrapper = new ContentCachingResponseWrapper(response);

    long startTime = System.currentTimeMillis();
    try {
      filterChain.doFilter(requestWrapper, responseWrapper);
    } finally {
      long duration = System.currentTimeMillis() - startTime;

      // 요청 인코딩 (fallback: UTF-8)
      Charset requestCharset = request.getCharacterEncoding() != null
          ? Charset.forName(request.getCharacterEncoding())
          : StandardCharsets.UTF_8;
      String requestBody = new String(requestWrapper.getContentAsByteArray(), requestCharset);

      // 응답 인코딩 → 무조건 UTF-8 강제
      String responseBody = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);

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

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    String uri = request.getRequestURI();
    return uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs");
  }
}
