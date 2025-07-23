package com.example.BackendServer.global.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
	@GetMapping("/health")
	public ResponseEntity<String> healthCheck(){
		return ResponseEntity.ok().build();
	}
}
