package com.example.global.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String errorCode, String message, HttpServletRequest request) {
		ErrorResponse response = ErrorResponse.builder()
				.timeStamp(LocalDateTime.now())
				.status(status.value())
				.error(errorCode)
				.message(message)
				.path(request.getRequestURI())
				.build();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

		return new ResponseEntity<>(response, headers, status);
	}

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> globalException(CustomException e) {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

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
				e.getMessage()
		);

		return buildResponse(HttpStatus.valueOf(e.getErrorCode().getStatus()), e.getErrorCode().getCode(), e.getMessage(), request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> methodArgumentNotValidException(MethodArgumentNotValidException e) {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		String errorMessage = e.getBindingResult().getFieldError().getDefaultMessage();

		log.error("[MethodArgumentNotValidException] Path: {}, Message: {}", request.getRequestURI(), errorMessage);

		return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", errorMessage, request);
	}

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

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

		log.error("[HttpMessageNotReadableException] Path: {}, Message: {}", request.getRequestURI(), errorMessage);

		return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", errorMessage, request);
	}
}
