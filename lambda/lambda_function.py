import boto3
import json
import re
import os
from datetime import datetime
from collections import defaultdict

PROCESSED_BUCKET = "swiftpay-processed-logs-venkat"
SNS_TOPIC_ARN = ""
ERROR_THRESHOLD = 5.0

s3_client = boto3.client("s3")
sns_client = boto3.client("sns", region_name="ap-south-1")

LOG_PATTERN = re.compile(
    r"(?P<timestamp>\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}:)\s+"
    r"(?P<level>INFO|WARN|ERROR)\s+"
    r"\[.*?\]\s+"
    r"(?P<service>\S+)\s+" 
    r"=,?\s*"
    r"(?P<message>.+)"
)

def parse_log_line(line):
    """
    Takes one raw log line, returns structured dict or none
    """
    line = line.strip()
    if not line:
     return None

    match = LOG_PATTERN.match(line)
    if not match:
      return None

    return {
      "timestamp": match.group("timestamp"),
      "level" : match.group("level").strip(),
      "service_name": match.group("service").strip(),
      "message" : match.group("message").strip

   }

def calculate_stats(parsed_lines):
    """
    Given a list of parsed log events, calculate:
    - total lines
    - error count
    - warn count  
    - error rate
    - errors per service

    """
    total = len(parsed_lines)
    errors = [l for l in parsed_lines if l["level"] == "ERROR"]
    warns = [l for l in parsed_lines if l["level"] == "WARN"]

    errors_by_service = defaultdict(int)
    for e in errors:
       errors_by_service[e["service_name"]] +=1

    error_rate = (len(errors) / total) * 100 if total > 0 else 0

    return {
       "total_lines" : total,
       "error_count" : len(errors),
       "warn_count" : len(warns),
       "info_count" : total - len(errors) - len(warns),
       "error_rate_percent" : error_rate,
       "errors_by_service" : dict(errors_by_service)
    }

def write_to_processed_bucket(parsed_lines, stats, source_key):
   """
   Writes two files to processed bucket:
    1. structured log events as JSON lines
    2. batch statistics summary
   """

   output_key = source_key.replace("raw-logs/", "processed-logs/") \
                          .replace(".log", ".json")
   
   state_key = source_key.replace("raw-logs/", "processed-stats/") \
                          .replace(".log", ".stats.json")
   
   log_lines_json = "\n".join(json.dumps(line) for line in parsed_lines)

   s3_client.put_object(
      Bucket = PROCESSED_BUCKET,
      Key = output_key,
      Body = log_lines_json.encode("utf-8"),
      CountentType = "application/json"
   )
   
   s3_client.put_object(
      Bucket = PROCESSED_BUCKET,
      Key = state_key,
      Body = json.dumps(stats, indent=2).encode("utf-8"),
      ContentType = "application/json"
   )

   return output_key, state_key

def send_alert_if_needed(stats, source_key):
    
    if not SNS_TOPIC_ARN:
        return
    
    if stats["error_rate_percent"] > ERROR_THRESHOLD: 
       message = f"""
🚨 SWIFTPAY ALERT — High Error Rate Detected

File:         {source_key}
Error Rate:   {stats["error_rate_percent"]}%  (threshold: {ERROR_THRESHOLD}%)
Total Logs:   {stats["total_lines"]}
Errors:       {stats["error_count"]}
Warnings:     {stats["warn_count"]}

Errors by Service:
{json.dumps(stats["errors_by_service"], indent=2)}

Time: {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}
        """

       sns_client.publish(
            TopicArn = SNS_TOPIC_ARN,
            Subject = f"🚨 SwiftPay Error Rate {stats['error_rate_percent']}% — Immediate Attention Required",
            Message = message
      )
       print(f"SNS alert sent — error rate {stats['error_rate_percent']}%")
    


def lambda_handler(event, context):
   """
   Entry point. AWS calls this automatically when S3 triggers Lambda.
   """
   print(f"Lambda triggered — processing {len(event['Records'])} record(s)")
   
   for record in event["Records"]:
       bucket_name = record["s3"]["bucket"]["name"]
       object_key  = record["s3"]["object"]["key"]
   
       print(f"Processing: s3://{bucket_name}/{object_key}")

       # Step 1 — Download raw log file from S3
       response    = s3_client.get_object(Bucket=bucket_name, Key=object_key)
       raw_content = response["Body"].read().decode("utf-8")
       raw_lines   = raw_content.split("\n")

       print(f"Total lines in file: {len(raw_lines)}")

       # Step 2 — Parse each line
       parsed_lines = []
       skipped      = 0

       for line in raw_lines:
           parsed = parse_log_line(line)
           if parsed:
               parsed_lines.append(parsed)
           else:
               skipped += 1

       print(f"Parsed lines: {len(parsed_lines)} lines | Skipped: {skipped} lines")

       if not parsed_lines:
          print("No valid log lines found, skipping further processing.")
          continue

      # Step 3 — Calculate statistics      
       stats = calculate_stats(parsed_lines)
       print(f"Stats: {json.dumps(stats)}")

      # Step 4 — Write structured logs and stats to processed bucket
       output_key, state_key = write_to_processed_bucket(
             parsed_lines, stats, object_key
      )
       print(f"Written to: s3://{PROCESSED_BUCKET}/{output_key}")
       print(f"Stats at: s3://{PROCESSED_BUCKET}/{state_key}")
       
      # Step 5 — Send alert if error rate exceeds threshold
       send_alert_if_needed(stats, object_key)
   
   return {
      "statusCode": 200,
      "body": f"Processed {len(event['Records'])} files(s) successfully."
   }