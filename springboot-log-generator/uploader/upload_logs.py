
import boto3
import os
import time
import schedule
from datetime import datetime


LOG_FILE_PATH = r"C:\Users\venkat\Documents\workspace-spring-tools-for-eclipse-5.0.1.RELEASE\loggen\logs\application.log"
RAW_BUCKET = "swiftpay-raw-logs-venkat"
UPLOAD_INTERVAL = 5

s3_client = boto3.client("s3",region_name="ap-south-1")


def upload_logs():
    if not os.path.exists(LOG_FILE_PATH):
        print(f"[{now()}] Log file not found : {LOG_FILE_PATH}")
        return
    
    file_size = os.path.getsize(LOG_FILE_PATH)
    if file_size == 0:
        print(f"[{now()}] Log file is empty, skipping upload.")
        return
    
    timestamp = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
    date_path = datetime.now().strftime("%Y/%m/%d")
    s3_key = f"raw-logs/{date_path}/application_{timestamp}.log"

    print(f"[{now()}] Uploading {file_size} bytes -> s3://{RAW_BUCKET}/{s3_key}")

    try:
        s3_client.upload_file(LOG_FILE_PATH, RAW_BUCKET, s3_key)
        print(f"[{now()}] Upload successful.")

    except Exception as e:
        print(f"[{now()}] Upload failed: {e}")


def now():
    return datetime.now().strftime("%Y-%m-%d %H:%M:%S")

print(f"[{now()}] SwiftPay Log Uplaoder started.")
print(f"[{now()}] Uploading every {UPLOAD_INTERVAL} minutes to s3://{RAW_BUCKET}")
print(f"[{now()}] First upload in {UPLOAD_INTERVAL} minutes...")


upload_logs()
schedule.every(UPLOAD_INTERVAL).minutes.do(upload_logs)

while True:
    schedule.run_pending()
    time.sleep(30)
