package com.example.global.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

	@GetMapping("/health")
	@Operation(summary = "Infra health 체크", description = "서버가 실행중인지 체크용 api입니다.")
	public ResponseEntity<String> healthCheck(){
		return ResponseEntity.ok().build();
	}
}
