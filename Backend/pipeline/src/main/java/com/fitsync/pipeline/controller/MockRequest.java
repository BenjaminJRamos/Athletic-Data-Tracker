package com.fitsync.pipeline.controller;

//package com.fitsync.telemetry.dto;

public class MockRequest {
    private String type; // HIIT, SLEEP, NAP, CARDIO
    private int durationMinutes;
    private boolean generateHeartRate;

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public boolean isGenerateHeartRate() { return generateHeartRate; }
    public void setGenerateHeartRate(boolean generateHeartRate) { this.generateHeartRate = generateHeartRate; }
}
