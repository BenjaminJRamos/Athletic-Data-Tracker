package com.fitsync.pipeline.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "raw_telemetry")
@Data
public class RawTelemetry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "telemetry_id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "device_source")
    private String deviceSource;

    private LocalDateTime timestamp;

    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "activity_type_claimed")
    private String activityTypeClaimed;

    // Mapping PG JSONB natively requires a String format for simple pipeline storage
    @Column(name = "raw_payload", columnDefinition = "jsonb")
    private String rawPayload;
}