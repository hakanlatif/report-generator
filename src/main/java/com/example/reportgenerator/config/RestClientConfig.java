package com.example.reportgenerator.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClientWithRequestTimeout(@Autowired ReportGeneratorConfig reportGeneratorConfig) {
        return RestClient
                .builder()
                .requestFactory(getClientHttpRequestFactory(reportGeneratorConfig.getTimedOutRequestsTimeoutInMs()))
                .build();
    }

    @Bean
    public RetryTemplate retryTemplateFor5xxResponse(@Autowired ReportGeneratorConfig reportGeneratorConfig) {
        RetryTemplate template = new RetryTemplate();

        template.setRetryPolicy(new RetryPolicyForServerErrorAndTimeouts(reportGeneratorConfig.getRetriedRequestsMaxAttempts()));

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(reportGeneratorConfig.getRetriedRequestsRetryTimeIntervalInMs());

        template.setBackOffPolicy(backOffPolicy);

        return template;
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory(Integer requestTimeout) {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(requestTimeout);
        clientHttpRequestFactory.setConnectionRequestTimeout(requestTimeout);
        return clientHttpRequestFactory;
    }

}
