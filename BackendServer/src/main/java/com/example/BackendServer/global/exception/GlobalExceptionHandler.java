package com.example.BackendServer.global.exception;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> globalException(CustomException e) {
		log.info("exception 발생 : {}", e.getMessage());
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		ErrorResponse response = ErrorResponse.builder()
			.timeStamp(LocalDateTime.now())
			.status(e.getErrorCode().getStatus())
			.error(e.getErrorCode().getCode())
			.message(e.getMessage())
			.path(request.getRequestURI())
			.build();
		return ResponseEntity.status(e.getErrorCode().getStatus()).body(response);
	}

	/**
	 * 클라이언트의 요청에서 오는
	 * @Valid 에서 발생한 예외 처리
	 *
	 * @param e : MethodArgumentNotValidException
	 * @return Error 메시지를 반환
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> methodArgumentNotValidException(MethodArgumentNotValidException e) {
		log.info("exception 발생");
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

	/**
	 * DTO의 enum값이 올바르지 않을 경우 발생하는 예외 처리
	 * HttpMessageNotReadableException 발생할 경우 호출
	 *
	 * @param ex : HttpMessageNotReadableException
	 * @return Error 메시지를 반환
	 */
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
