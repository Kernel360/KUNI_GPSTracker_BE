package com.example.BackendServer.emulator.db;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OnOffRecordRepository extends JpaRepository<OnOffRecordEntity, Long> {
}
