# Project 3 — Serverless Log Analytics System on AWS

## What This Project Does
A fully serverless log analytics pipeline built on AWS that:
- Ingests logs from a Spring Boot microservices simulator (5 services)
- Automatically processes each upload via AWS Lambda
- Enables SQL queries on raw log data using AWS Athena
- Sends real-time alerts via AWS SNS when error rates spike

## Architecture
Spring Boot (Log Generator)
↓
Local Log File (application.log)
↓
Python Upload Script (every 5 minutes)
↓
S3 Raw Bucket
↓ (S3 Event Trigger)
AWS Lambda (Parse + Classify logs)
↓
S3 Processed Bucket (structured JSON)
↓
AWS Glue Crawler (auto schema detection)
↓
AWS Athena (SQL queries on S3)
↓
AWS SNS (alerts when error rate > 5%)

## Tech Stack
| Layer | Technology |
|---|---|
| Log Generator | Java 21, Spring Boot 4.0.6 |
| Cloud Storage | AWS S3 (ap-south-1 Mumbai) |
| Serverless Processing | AWS Lambda (Python 3.12) |
| Schema Catalog | AWS Glue Crawler |
| Query Engine | AWS Athena |
| Alerting | AWS SNS |
| Uploader | Python 3, boto3 |

## Project Structure
├── springboot-log-generator/   # Spring Boot app simulating 5 microservices
├── uploader/                   # Python script uploading logs to S3 every 5 min
├── lambda/                     # Lambda function parsing raw logs
├── athena/                     # SQL queries for log analysis
├── glue/                       # Glue crawler configuration
├── sample_logs/                # Sample log output
└── screenshots/                # AWS console screenshots

## Services Simulated
- **PaymentService** — payment transactions, DB timeouts
- **AuthService** — login attempts, suspicious activity
- **OrderService** — order creation, inventory issues  
- **NotificationService** — SMS/Email/Push delivery
- **InventoryService** — stock level monitoring

## Incident Simulation
The log generator automatically triggers incidents every ~5 minutes —
error rates spike from 3% to 40% simulating real production outages.
This is what the SNS alert system detects and reports on.

## How to Run Locally
1. Run Spring Boot app in STS → logs generate to `logs/application.log`
2. Run `python uploader/upload_logs.py` → logs upload to S3 every 5 minutes
3. Lambda auto-triggers on S3 upload → processes to structured JSON
4. Query in Athena → `SELECT * FROM swiftpay_logs WHERE level='ERROR'`

## Status
- [x] Day 1 — Spring Boot log generator
- [x] Day 2 — AWS S3 setup + Python uploader  
- [ ] Day 3 — AWS Lambda parser
- [ ] Day 4 — Glue Crawler + Athena queries
- [ ] Day 5 — SNS alerts + end to end testing

## Background
Built as Project 3 of my Data Engineering roadmap.  