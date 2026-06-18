--  USERS TABLE
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- TABLE FOR ALL THE INCOMING ATHLETIC DATA
CREATE TABLE raw_telemetry (
    telemetry_id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(user_id) ON DELETE CASCADE,
    device_source VARCHAR(30) NOT NULL, -- 'AppleWatch', 'Strava_API', 'Mock_Garmin'
    timestamp TIMESTAMP NOT NULL,
    heart_rate INT,
    activity_type_claimed VARCHAR(50),  -- What the device *thinks* you are doing
    raw_payload JSONB                    -- Stores any extra nested sensor data dynamically
);


-- CLEAN DEDUPLICATED DATA THAT WILL GET PASSED TO THE UI
CREATE TABLE unified_activities (
    activity_id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(user_id) ON DELETE CASCADE,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    calculated_avg_hr INT,
    calculated_peak_hr INT,
    final_activity_label VARCHAR(50),   -- The definitive label your algorithm assigns
    strain_score NUMERIC(4, 2)          -- A computed float (e.g., 14.5) mimicking Whoop's metrics
);

