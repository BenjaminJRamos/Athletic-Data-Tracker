package com.fitsync.pipeline.controller;

import com.fitsync.pipeline.model.RawTelemetry;
import com.fitsync.pipeline.repository.RawTelemetryRepository;
import com.fitsync.pipeline.service.DeduplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;



@CrossOrigin(origins = "http://localhost:5173") // This is for the Vite frontend portion (to connect both the front end and backend)
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

    // For new functional buttons (sleep, workout, etc.)
    @PostMapping("/simulate/{userId}/custom")
public ResponseEntity<String> triggerSimulation(
        @PathVariable Long userId, 
        @RequestParam String type,
        @RequestParam(defaultValue = "45") int durationMinutes,
        @RequestParam(defaultValue = "true") boolean generateHeartRate) {
    
    // Dispatches generator or directly creates domain payload
    List<RawTelemetry> simulatedPackets = deduplicationService.generateSimulatedEvent(userId, type, durationMinutes, generateHeartRate);
    telemetryRepository.saveAll(simulatedPackets);
    
   // return ResponseEntity.ok("Simulated " + type + " event generated for User " + userId);
   return ResponseEntity.ok("Generated " + simulatedPackets.size() + " telemetry logs for " + type);
}

// Fast truncate / reset endpoint for clearing database during local development
    @DeleteMapping("/reset")
    public ResponseEntity<Void> resetAllTelemetry() {
        telemetryRepository.deleteAllInBatch();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/reset/{userId}")
    public ResponseEntity<Void> resetUserTelemetry(@PathVariable Long userId) {
        List<RawTelemetry> userLogs = telemetryRepository.findByUserIdOrderByTimestampAsc(userId);
        telemetryRepository.deleteAllInBatch(userLogs);
        return ResponseEntity.noContent().build();
    }

    // @GetMapping("/process/{userId}") was causing issues connecting backend and front end b/c urls did not match
    @GetMapping("/deduplicate/{userId}")
    public ResponseEntity<List<RawTelemetry>> getCleanTimeline(@PathVariable Long userId) {
        List<RawTelemetry> rawUserData = telemetryRepository.findByUserIdOrderByTimestampAsc(userId);
        List<RawTelemetry> goldStandardTimeline = deduplicationService.processDeduplication(rawUserData);
        return ResponseEntity.ok(goldStandardTimeline);
    }
}