package com.example.global.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	// ✅ 기존 코드는 그대로 유지, 팀원 요청대로 CustomException 로그만 변경
	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> globalException(CustomException e) {
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();

		// 상세 로그 추가 (팀원 요청 반영)
		log.error("""
                [CustomException 발생]
                - Path     : {}
                - Method   : {}
                - ErrorCode: {} ({})
                - Message  : {}
                """,
				request.getRequestURI(),
				request.getMethod(),
				e.getErrorCode().getCode(),
				e.getErrorCode().getStatus(),
				e.getMessage(),
				e // 스택트레이스 출력
		);

		ErrorResponse response = ErrorResponse.builder()
				.timeStamp(LocalDateTime.now())
				.status(e.getErrorCode().getStatus())
				.error(e.getErrorCode().getCode())
				.message(e.getMessage())
				.path(request.getRequestURI())
				.build();

		return ResponseEntity.status(e.getErrorCode().getStatus()).body(response);
	}

	// @Valid 에서 발생한 예외 처리
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> methodArgumentNotValidException(MethodArgumentNotValidException e) {
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		ErrorResponse response = ErrorResponse.builder()
				.timeStamp(LocalDateTime.now())
				.status(400)
				.error("BAD_REQUEST")
				.message(e.getBindingResult().getFieldError().getDefaultMessage())
				.path(request.getRequestURI())
				.build();
		return ResponseEntity.badRequest().body(response);
	}

	// DTO enum 값 오류 처리
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleEnumMismatchError(HttpMessageNotReadableException ex) {
		String errorMessage = "잘못된 요청 형식입니다. 입력 값을 확인해주세요.";
		Throwable cause = ex.getCause();
		if (cause instanceof InvalidFormatException invalidFormatException) {
			if (invalidFormatException.getTargetType() != null && invalidFormatException.getTargetType().isEnum()) {
				String fieldName = invalidFormatException.getPath().get(0).getFieldName();
				String rejectedValue = invalidFormatException.getValue().toString();
				String allowedValues = Arrays.stream(invalidFormatException.getTargetType().getEnumConstants())
						.map(Object::toString)
						.collect(Collectors.joining(", "));
				errorMessage = String.format("필드 '%s'에 잘못된 값이 입력되었습니다. 입력된 값: '%s', 허용되는 값: [%s]",
						fieldName, rejectedValue, allowedValues);
			}
		}

		HttpServletRequest request = ((ServletRequestAttributes)
				RequestContextHolder.currentRequestAttributes()).getRequest();
		ErrorResponse response = ErrorResponse.builder()
				.timeStamp(LocalDateTime.now())
				.status(HttpStatus.BAD_REQUEST.value())
				.error("BAD_REQUEST")
				.message(errorMessage)
				.path(request.getRequestURI())
				.build();

		return ResponseEntity.badRequest().body(response);
	}
}
