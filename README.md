# Report Generator

## About The Project
This project involves generating reports asynchronously, allowing users to create report generation jobs and check the 
status of ongoing reports. Spring's restClient and retryTemplates library used for providing resilient service 
to handle stability issues from user-activity-service. This setup addresses stability issues by seamlessly retrying 
failed REST requests, abstracting the retry logic from the core application code through a configuration-driven approach.

## Runbook

### Running the application stand-alone

```bash
mvn spring-boot:run
```

### Dockerizing the application
user-activity-service and the report generator services needs to be deployed to run in docker 
```bash
mvn clean package
docker build . -t report-generator -f Dockerfile
docker pull antipintk/user-activity-service

docker compose up
```
## Configuration
Following configurations can be changed to optimize the resiliency of the application

```bash
timedOutRequests.timeoutInMs=10000
retriedRequests.maxAttempts=2
retriedRequests.retryTimeIntervalInMs=2000
```

## End-points

Application runs on http://localhost:8080/ as a default configuration

### /report-generator POST
Request Body: 
```json
{
    "date_from": "2020-01-01T12:00:00Z",
    "date_to": "2024-01-01T12:00:00Z"
}
```

Response :
```json
{
    "jobId": "rl5m2bwpth6g"
}
```

### /report-generator/{jobId} GET

Response of in progress job :
```json
{
"reportId": "1",
"reportStatus": "IN_PROGRESS"
}
```

Response of successful job :
```json
{
  "reportId": "1",
  "reportStatus": "SUCCEEDED"
}
```

Response of failed job with Internal Server Error:
```json
{
  "reportId": "1",
  "reportStatus": "FAILED",
  "error": "Request of http://localhost:8000/user-activity?date_from=1262347200000&date_to=1262347200000 failed An internal server error occurred with 500"
}
```

Response of failed job with Request Time Out:
```json
{
"reportId": "1",
"reportStatus": "FAILED",
"error": "Request of http://localhost:8000/user-activity?date_from=1262347200000&date_to=1262347200000 failed I/O error on GET request for \"http://localhost:8000/user-activity\": Read timed out"
}
```