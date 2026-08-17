package com.fitsync.pipeline.controller;

import com.fitsync.pipeline.model.RawTelemetry;
import com.fitsync.pipeline.repository.RawTelemetryRepository;
import com.fitsync.pipeline.service.DeduplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/telemetry")
public class TelemetryController {

    @Autowired
    private RawTelemetryRepository telemetryRepository;

    @Autowired
    private DeduplicationService deduplicationService;

    @PostMapping("/ingest")
    public ResponseEntity<String> ingestRawTelemetry(@RequestBody List<RawTelemetry> payload) {
        telemetryRepository.saveAll(payload);
        return ResponseEntity.ok("Successfully ingested " + payload.size() + " raw packets into holding database.");
    }

    @GetMapping("/process/{userId}")
    public ResponseEntity<List<RawTelemetry>> getCleanTimeline(@PathVariable Long userId) {
        List<RawTelemetry> rawUserData = telemetryRepository.findByUserIdOrderByTimestampAsc(userId);
        List<RawTelemetry> goldStandardTimeline = deduplicationService.processDeduplication(rawUserData);
        return ResponseEntity.ok(goldStandardTimeline);
    }
}