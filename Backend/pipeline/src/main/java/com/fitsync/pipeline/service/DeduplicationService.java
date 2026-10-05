package com.fitsync.pipeline.service;

import com.fitsync.pipeline.model.RawTelemetry;
import com.fitsync.pipeline.repository.RawTelemetryRepository;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*; 

import java.time.temporal.ChronoUnit;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;


@Service
public class DeduplicationService {

    private final Random random = new Random();
    private final RawTelemetryRepository repository; // Add repository reference

    public DeduplicationService(RawTelemetryRepository repository) {
        this.repository = repository;
    }

    // code for new buttons:

public List<RawTelemetry> generateSimulatedEvent(Long userId, String type, int durationMinutes, boolean generateHeartRate) {
        List<RawTelemetry> packets = new ArrayList<>();
        
        // 1. Find the latest timestamp in DB so new sessions append chronologically
        LocalDateTime latestTime = repository.findMaxTimestampByUserId(userId);
        
        Instant startTime;
        if (latestTime != null) {
            // Start 1 second after the last recorded activity ended
            startTime = latestTime.toInstant(ZoneOffset.UTC).plusSeconds(1);
        } else {
            // If DB is empty, start from current system time
            startTime = Instant.now();
        }

        // 2. Sampling interval per activity
        int stepSeconds = switch (type.toLowerCase()) {
            case "sleep" -> 300;        // 1 packet every 5 minutes
            case "nap" -> 120;          // 1 packet every 2 minutes
            case "hiit", "cardio" -> 30; // 1 packet every 30 seconds
            default -> 60;
        };

        String deviceSource = switch (type.toLowerCase()) {
            case "sleep", "nap" -> "OURA_RING_GEN3";
            case "hiit" -> "WHOOP_STRAP_4";
            case "cardio" -> "GARMIN_FORERUNNER";
            default -> "GENERIC_FITNESS_TRACKER";
        };

        String activityClaimed = switch (type.toLowerCase()) {
            case "sleep", "nap" -> "SLEEP_SESSION";
            case "hiit" -> "HIIT_WEIGHTLIFTING";
            case "cardio" -> "RUNNING";
            default -> "GENERAL_ACTIVITY";
        };

        long totalSeconds = durationMinutes * 60L;
        for (long sec = 0; sec < totalSeconds; sec += stepSeconds) {
            RawTelemetry packet = new RawTelemetry();
            packet.setUserId(userId);
            
            // Step forward in time sequentially from last end point
            packet.setTimestamp(LocalDateTime.ofInstant(startTime.plusSeconds(sec), ZoneOffset.UTC));
            packet.setDeviceSource(deviceSource);
            packet.setActivityTypeClaimed(activityClaimed);

            if (generateHeartRate) {
                packet.setHeartRate(calculateHeartRate(type));
            } else {
                packet.setHeartRate(0);
            }

            packet.setRawPayload(buildPayload(type, durationMinutes));
            packets.add(packet);
        }

        return packets;
    }

    // Keep calculateHeartRate(), buildPayload(), processDeduplication(), and getDevicePriority() as they are
//}










        // OLD CODE FOR THE BUTTONS CREATED TOO MUCH DATA AND WAS UNSYNCHRONIZED TIMEWISE
/* 
    public List<RawTelemetry> generateSimulatedEvent(Long userId, String type, int durationMinutes, boolean generateHeartRate) {
        List<RawTelemetry> packets = new ArrayList<>();
        Instant endTime = Instant.now();
        Instant startTime = endTime.minus(durationMinutes, ChronoUnit.MINUTES);

        // Define sampling frequency based on event type
        int stepSeconds = switch (type.toLowerCase()) {
            case "sleep", "nap" -> 120; // 1 packet every 2 minutes
            case "hiit", "cardio" -> 10; // 1 packet every 10 seconds
            default -> 30;
        };

        String deviceSource = switch (type.toLowerCase()) {
            case "sleep", "nap" -> "OURA_RING_GEN3";
            case "hiit" -> "WHOOP_STRAP_4";
            case "cardio" -> "GARMIN_FORERUNNER";
            default -> "GENERIC_FITNESS_TRACKER";
        };

        String activityClaimed = switch (type.toLowerCase()) {
            case "sleep", "nap" -> "SLEEP_SESSION";
            case "hiit" -> "HIIT_WEIGHTLIFTING";
            case "cardio" -> "RUNNING";
            default -> "GENERAL_ACTIVITY";
        };

        long totalSeconds = durationMinutes * 60L;
        for (long sec = 0; sec < totalSeconds; sec += stepSeconds) {
            RawTelemetry packet = new RawTelemetry();
            packet.setUserId(userId);
           // packet.setTimestamp(startTime.plusSeconds(sec).toString());
            packet.setTimestamp(LocalDateTime.ofInstant(startTime.plusSeconds(sec), java.time.ZoneOffset.UTC));
            packet.setDeviceSource(deviceSource);
            packet.setActivityTypeClaimed(activityClaimed);

            if (generateHeartRate) {
                packet.setHeartRate(calculateHeartRate(type));
            } else {
                packet.setHeartRate(0);
            }

            // Mock raw payload matching your Python script structure
            packet.setRawPayload(buildPayload(type, durationMinutes));
            packets.add(packet);
        }

        return packets;
    }
    */

    private int calculateHeartRate(String type) {
        return switch (type.toLowerCase()) {
            case "hiit" -> random.nextBoolean() ? 110 + random.nextInt(20) : 170 + random.nextInt(18); // Spike profile
            case "cardio" -> 135 + random.nextInt(30);  // Sustained Zone 3-4
            case "sleep" -> 45 + random.nextInt(10);    // Low resting HR
            case "nap" -> 52 + random.nextInt(12);
            default -> 70 + random.nextInt(20);
        };
    }

    private String buildPayload(String type, int durationMinutes) {
        if ("sleep".equalsIgnoreCase(type) || "nap".equalsIgnoreCase(type)) {
            return String.format("{\"duration_minutes\": %d, \"hrv_rmssd\": %d}", durationMinutes, 55 + random.nextInt(30));
        } else if ("hiit".equalsIgnoreCase(type)) {
            return "{\"perceived_exertion\": 9, \"interval_count\": 8}";
        } else {
            return "{\"cadence\": 172, \"gps_accuracy\": \"HIGH\"}";
        }
    }















    /**
     * Algorithmic Deduplication: Groups raw entries by timestamp, resolves device hierarchy,
     * prioritizing AppleWatch telemetry over Strava_API metrics for higher structural fidelity.
     */
    public List<RawTelemetry> processDeduplication(List<RawTelemetry> rawData) {
        if (rawData == null || rawData.isEmpty()) return Collections.emptyList();

        // Step 1: Bucket data elements by unique timestamps using a LinkedHashMap to preserve timeline
        Map<String, List<RawTelemetry>> timelineMap = new LinkedHashMap<>();
        for (RawTelemetry record : rawData) {
            String timeKey = record.getTimestamp().toString();
            timelineMap.computeIfAbsent(timeKey, k -> new ArrayList<>()).add(record);
        }

        List<RawTelemetry> deduplicatedGoldStandard = new ArrayList<>();

        // Step 2: Apply enterprise conflict resolution algorithm across timestamp duplicates
        for (Map.Entry<String, List<RawTelemetry>> entry : timelineMap.entrySet()) {
            List<RawTelemetry> conflictingRecords = entry.getValue();

            if (conflictingRecords.size() == 1) {
                deduplicatedGoldStandard.add(conflictingRecords.get(0));
            } else {
                // Conflict found! Evaluate hierarchy priorities
                // THIS IS THE CORE LOGIC
                RawTelemetry resolvedRecord = conflictingRecords.stream()
                    .min(Comparator.comparingInt(r -> getDevicePriority(r.getDeviceSource())))
                    .orElse(conflictingRecords.get(0));
                
                deduplicatedGoldStandard.add(resolvedRecord);
            }
        }
        return deduplicatedGoldStandard;
    }

    private int getDevicePriority(String source) {
       // This is a helper method used to set a priority to the data in case there are multiple 
       // data points in the same timestamp. The priority determines which data point to use.
       // This function determines apple watch data, hardware sensor data, to be of higher accuracy/fidelity 
       // than Strava API data, which is derived from the phone's accelerometer and GPS.
                      
        if ("AppleWatch".equalsIgnoreCase(source)) return 1; // Top priority: rich sensor metrics
        if ("Strava_API".equalsIgnoreCase(source)) return 2;  // Secondary priority
        return 3; // Fallback
    }
}
