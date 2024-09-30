package com.example.reportgenerator.config;

import com.example.reportgenerator.exception.ServiceException;
import org.springframework.classify.Classifier;
import org.springframework.http.HttpStatus;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

public class RetryPolicyForServerErrorAndTimeouts extends ExceptionClassifierRetryPolicy {

    public RetryPolicyForServerErrorAndTimeouts(int maxAttempts) {
        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
        simpleRetryPolicy.setMaxAttempts(maxAttempts);

        this.setExceptionClassifier((Classifier<Throwable, RetryPolicy>) classifiable -> {
            if (!(classifiable instanceof ServiceException serviceException)) {
                return new NeverRetryPolicy();
            }

            // Http status 500 and 504
            if (serviceException.getException() instanceof HttpServerErrorException httpServerErrorException &&
                    (httpServerErrorException.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR
                            || httpServerErrorException.getStatusCode() == HttpStatus.GATEWAY_TIMEOUT)) {
                return simpleRetryPolicy;
            }

            // Request timeouts
            if (serviceException.getException() instanceof ResourceAccessException) {
                return simpleRetryPolicy;
            }

            return new NeverRetryPolicy();
        });

    }

}
