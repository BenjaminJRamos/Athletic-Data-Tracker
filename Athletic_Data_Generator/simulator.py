import argparse
import json
import random
import requests
from datetime import datetime, timezone, timedelta

API_URL = "http://localhost:8080/api/v1/telemetry/ingest"

def generate_telemetry(user_id, event_type):
    base_time = datetime.now(timezone.utc)
    packets = []
    
    if event_type == "cardio":
        # Generate 30 minutes of cardio data (1-minute intervals)
        for i in range(30):
            timestamp = (base_time + timedelta(minutes=i)).isoformat()
            packets.append({
                "userId": user_id,
                "timestamp": timestamp,
                "heartRate": random.randint(135, 165),
                "deviceSource": "GARMIN_FORERUNNER",
                "activityTypeClaimed": "RUNNING",
                "rawPayload": json.dumps({"cadence": 172, "elevation_gain_m": 45, "gps_accuracy": "HIGH"})
            })
            
    elif event_type == "hiit":
        # Generate 20 minutes of HIIT data (1-minute intervals)
        for i in range(20):
            timestamp = (base_time + timedelta(minutes=i)).isoformat()
            packets.append({
                "userId": user_id,
                "timestamp": timestamp,
                "heartRate": random.choice([110, 178, 185, 125, 172]),
                "deviceSource": "WHOOP_STRAP_4",
                "activityTypeClaimed": "HIIT_WEIGHTLIFTING",
                "rawPayload": json.dumps({"perceived_exertion": 9, "interval_count": 8})
            })
            
    elif event_type in ["sleep", "nap"]:
        is_nap = (event_type == "nap")
        duration_minutes = 45 if is_nap else 480 # 8 hours for sleep
        step_minutes = 5 if not is_nap else 1     # Downsample sleep: 1 record every 5 mins
        
        for i in range(0, duration_minutes, step_minutes):
            timestamp = (base_time + timedelta(minutes=i)).isoformat()
            packets.append({
                "userId": user_id,
                "timestamp": timestamp,
                "heartRate": random.randint(52, 64) if is_nap else random.randint(45, 55),
                "deviceSource": "OURA_RING_GEN3",
                "activityTypeClaimed": "SLEEP_SESSION",
                "rawPayload": json.dumps({
                    "duration_minutes": duration_minutes,
                    "hrv_rmssd": random.randint(55, 85),
                    "rem_percentage": 0.10 if is_nap else 0.22
                })
            })

    response = requests.post(API_URL, json=packets)
    print(f"[{event_type.upper()}] Status {response.status_code}: {response.text}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="FitSync Telemetry Simulator")
    parser.add_argument("--user", type=int, default=1, help="User ID")
    parser.add_argument("--type", choices=["cardio", "hiit", "sleep", "nap"], required=True)
    args = parser.parse_args()
    
    generate_telemetry(args.user, args.type)






















# OLD BACK UP CODE (too many data packets and unsycronized timewise mock data)
# import argparse
# import json
# import random
# import time
# import requests
# from datetime import datetime, timezone, timedelta

# API_URL = "http://localhost:8080/api/v1/telemetry/ingest"

# def generate_telemetry(user_id, event_type):
#     now = datetime.now(timezone.utc).isoformat()
    
#     if event_type == "cardio":
#         # Sustained Zone 3-4 HR
#         packets = [{
#             "userId": user_id,
#             "timestamp": now,
#             "heartRate": random.randint(135, 165),
#             "deviceSource": "GARMIN_FORERUNNER",
#             "activityTypeClaimed": "RUNNING",
#             "rawPayload": json.dumps({"cadence": 172, "elevation_gain_m": 45, "gps_accuracy": "HIGH"})
#         } for _ in range(5)]
        
#     elif event_type == "hiit":
#         # High HR spike / burst profile
#         packets = [{
#             "userId": user_id,
#             "timestamp": now,
#             "heartRate": random.choice([110, 178, 185, 125, 172]),
#             "deviceSource": "WHOOP_STRAP_4",
#             "activityTypeClaimed": "HIIT_WEIGHTLIFTING",
#             "rawPayload": json.dumps({"perceived_exertion": 9, "interval_count": 8})
#         } for _ in range(5)]
        
#     elif event_type in ["sleep", "nap"]:
#         is_nap = (event_type == "nap")
#         packets = [{
#             "userId": user_id,
#             "timestamp": now,
#             "heartRate": random.randint(52, 64) if is_nap else random.randint(45, 55),
#             "deviceSource": "OURA_RING_GEN3",
#             "activityTypeClaimed": "SLEEP_SESSION",
#             "rawPayload": json.dumps({
#                 "duration_minutes": 45 if is_nap else 460,
#                 "hrv_rmssd": random.randint(55, 85),
#                 "rem_percentage": 0.10 if is_nap else 0.22
#             })
#         } for _ in range(1)]

#     response = requests.post(API_URL, json=packets)
#     print(f"[{event_type.upper()}] Status {response.status_code}: {response.text}")

# if __name__ == "__main__":
#     parser = argparse.ArgumentParser(description="FitSync Telemetry Simulator")
#     parser.add_argument("--user", type=int, default=1, help="User ID")
#     parser.add_argument("--type", choices=["cardio", "hiit", "sleep", "nap"], required=True)
#     args = parser.parse_args()
    
#     generate_telemetry(args.user, args.type)