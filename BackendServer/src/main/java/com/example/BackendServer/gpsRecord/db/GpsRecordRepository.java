package com.example.BackendServer.gpsRecord.db;

import com.example.BackendServer.device.db.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface GpsRecordRepository extends JpaRepository<GpsRecordEntity,Long> {
    @Query("""
  select g
  from GpsRecordEntity g
  where g.record.id = :recordId
  order by g.oTime desc
  limit 1
""")
    Optional<GpsRecordEntity> findLatest(Long recordId);

}
