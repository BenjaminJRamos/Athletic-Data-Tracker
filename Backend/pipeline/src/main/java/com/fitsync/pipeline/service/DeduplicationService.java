package com.fitsync.pipeline.service;

import com.fitsync.pipeline.model.RawTelemetry;
import org.springframework.stereotype.Service;
import java.util.*; 

@Service
public class DeduplicationService {

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
