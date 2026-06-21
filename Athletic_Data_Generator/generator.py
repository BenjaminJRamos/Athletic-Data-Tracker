# CHECK TO SEE IF YOU HAVE THE STANDAR LIBRABIES INSTALLED

import random
import json
import time
from datetime import datetime, timedelta, timezone

class WearableSimulator:
    def __init__(self, user_id=1):
        # CONSTRUCTOR  
        self.user_id = user_id
        # Start heart rate at a resting baseline, choosing to hard encode 70 BPM as a common resting heart rate for adults. ( may choose to change to adapt to age later)
        self.current_hr = 70.0 
        
    def generate_workout_stream(self, duration_minutes=30):
        """
        THIS IS THE ALGORITHM THAT DRIVES THE SIMULATION. IT IS A MEAN-REVERTING RANDOM WALK, WHICH IS A COMMON MODEL FOR SIMULATING TIME SERIES DATA LIKE HEART RATE.
        THE HEART RATE DRIFTS TOWARD A TARGET VALUE (WHICH CHANGES BASED ON THE PHASE OF THE WORKOUT) WITH SOME RANDOM JITTER ADDED IN TO MAKE IT FEEL MORE REALISTIC AND ORGANIC.
        THE TARGET HEART RATE IS HIGHER DURING THE MIDDLE OF THE WORKOUT AND LOWER DURING THE WARM-UP AND COOL-DOWN PHASES.
        """
        start_time = datetime.now(timezone.utc) # datetime.utcnow() <-- (depricated)
        telemetry_packets = []
        
        print(f"Generating {duration_minutes}-minute mock workout session...")
        
        # Simulate data entry every 5 seconds. ALso, inside the range function the duration_minutes is being converted into seconds.
        for second in range(0, duration_minutes * 60, 5):
            current_timestamp = start_time + timedelta(seconds=second)
            iso_timestamp = current_timestamp.strftime("%Y-%m-%dT%H:%M:%SZ")
            
            # --- MEAN REVERTING RANDOM WALK ALGORITHM ---
            # Mid-workout target HR is high (140), resting target is low (70)
            """  
            The script assumes that for the first and last 5 minutes (300 seconds) of the workout,
            you are resting or warming down (targeting 75 BPM). In the middle of the workout,
            it shifts the target to a high-intensity 140 BPM. That's what this algorithm is doing. 
            """

            target_hr = 140.0 if second > 300 and second < (duration_minutes * 60 - 300) else 75.0
            
            # Drift slightly toward the target, add some organic noise/jitter
            drift = (target_hr - self.current_hr) * 0.05
            jitter = random.uniform(-4.0, 4.0) # This jitter variable accounts for slight varriations in how different people's heart rates respond to exercise, and how different devices might measure it.
            self.current_hr += drift + jitter
            
            # Clamp bounds to humanly realistic values
            self.current_hr = max(60.0, min(190.0, self.current_hr))  # if you are 30 years old your max estimated geart rate is 190 BPM. I believe the formula is 220 - age, but for this simulation I am going to just use 190 as a hard cap for realism. I might change this later. Also I will be using 60 BPM as the lowest as below this number can be an indication of bradycardia.
            final_hr = int(self.current_hr)

            # ─── DEVICE 1: APPLE WATCH PACKET ───
            # High-frequency telemetry with a raw payload structural detail
            apple_packet = {
                "user_id": self.user_id,
                "device_source": "AppleWatch",
                "timestamp": iso_timestamp,
                "heart_rate": final_hr,
                "activity_type_claimed": "Functional Strength Training",
                "raw_payload": {
                    "accelerometer_z_axis": round(random.uniform(0.1, 1.8), 2),
                    "battery_level_pct": 84 # this is just for realism. Though maybe the lower in batter percentage the less acurate the number (or battery health) (though this is probablty unlikely).
                }
            }
            telemetry_packets.append(apple_packet)

            # ─── DEVICE 2: STRAVA PACKET (THE DUPLICATE OVERLAP) ───
            # Strava also records the exact same workout hour, but logs a different name
            # This sets up the exact "data cleaning" challenge!
            if random.random() > 0.1: # 10% chance of packet loss to simulate network reality. Packet drop is when a data packet fails to reach its destination. This adds an extra layer of realism to the generated dataset and allows us to test how well our data cleaning and processing algorithms can handle incomplete or missing data.
                strava_packet = {
                    "user_id": self.user_id,
                    "device_source": "Strava_API",
                    "timestamp": iso_timestamp,
                    "heart_rate": final_hr + random.randint(-1, 1), # slight variance
                    "activity_type_claimed": "Weight Training",
                    "raw_payload": {
                        "api_app_id": 48291,
                        "elevation_gain_m": 0
                    }
                }
                telemetry_packets.append(strava_packet)
                
        return telemetry_packets
    

# This is the actual creation of the JSON file. 
if __name__ == "__main__": # This is for safety: it's to ensure that the code only runs when this script is executed directly, and not when it's imported as a module in another script.
    simulator = WearableSimulator(user_id=1) # right now to create a new user I have to manually change the user_id. Look to change this later on!
    mock_data = simulator.generate_workout_stream(duration_minutes=15)  # RIGHT NOW THE SCRIPT IS ONLY RECORDING 15 MIN WORKOUTS, WE CAN AND SHOULD CHANGE THIS IN THE FUTURE TO ALSO BE RANDOM WITH A MIN AND MAX TIME. 
    
    # Save the output locally as a mock data payload JSON file
    output_filename = "mock_telemetry_stream.json"
    with open(output_filename, "w") as f:
        json.dump(mock_data, f, indent=4)
        
    print(f"Success! Generated {len(mock_data)} data packets and saved to {output_filename}") # If this message prints, then the file was successfully created and you should see the JSON file.