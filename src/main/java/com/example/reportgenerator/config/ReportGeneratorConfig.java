package com.example.reportgenerator.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class ReportGeneratorConfig {

    @Value("${userActivity.host}")
    private String userActivityHost;

    @Value("${timedOutRequests.timeoutInMs}")
    private int timedOutRequestsTimeoutInMs;

    @Value("${retriedRequests.maxAttempts}")
    private int retriedRequestsMaxAttempts;

    @Value("${retriedRequests.retryTimeIntervalInMs}")
    private int retriedRequestsRetryTimeIntervalInMs;

}
