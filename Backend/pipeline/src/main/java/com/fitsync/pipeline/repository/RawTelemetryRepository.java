package com.fitsync.pipeline.repository;


import com.fitsync.pipeline.model.RawTelemetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RawTelemetryRepository extends JpaRepository<RawTelemetry, Long> {
    // Custom enterprise query to fetch raw data by a user window for deduplication processing
    List<RawTelemetry> findByUserIdOrderByTimestampAsc(Long userId);
}
