package com.example.BackendServer.global.exception;

import static lombok.AccessLevel.*;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class ErrorResponse {
	private LocalDateTime timeStamp;
	private int status;
	private String error;
	private String message;
	private String path;
}
