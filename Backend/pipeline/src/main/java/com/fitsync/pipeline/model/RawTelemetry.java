package com.fitsync.pipeline.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

// New imports to connect python generator script to backend
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "raw_telemetry")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    
     // New to fix data generator to backend
    @JdbcTypeCode(SqlTypes.JSON)

    // Mapping PG JSONB natively requires a String format for simple pipeline storage
    @Column(name = "raw_payload", columnDefinition = "jsonb")
    private String rawPayload;
    
    // Custom constructor excluding auto-generated 'id'
    public RawTelemetry(Long userId, String deviceSource, LocalDateTime timestamp, Integer heartRate, String activityTypeClaimed, String rawPayload) {
        this.userId = userId;
        this.deviceSource = deviceSource;
        this.timestamp = timestamp;
        this.heartRate = heartRate;
        this.activityTypeClaimed = activityTypeClaimed;
        this.rawPayload = rawPayload;
    }
}