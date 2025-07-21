package com.example.BackendServer.record.controller;

import com.example.BackendServer.record.db.RecordEntity;
import com.example.BackendServer.record.model.RecordRequest;
import com.example.BackendServer.record.service.RecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/record")
@RequiredArgsConstructor
public class RecordApiController {

  private final RecordService recordService;

  @PostMapping
  public ResponseEntity<RecordEntity> create(@Valid @RequestBody RecordRequest recordRequest) {
    return ResponseEntity.ok().body(recordService.create(recordRequest));
  }
}
