package com.example.reportgenerator.client;

import com.example.reportgenerator.config.ReportGeneratorConfig;
import com.example.reportgenerator.exception.ServiceException;
import com.example.reportgenerator.model.rest.useractivitiy.UserActivityErrorMessage;
import com.example.reportgenerator.model.rest.useractivitiy.UserActivityResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class UserActivityClientImpl implements UserActivityClient {

    private static final String USER_ACTIVITY_URI_TEMPLATE = "http://%s/user-activity?date_from=%s&date_to=%s";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final RestClient restClientWithRequestTimeout;
    private final RetryTemplate retryTemplateFor5xxResponse;
    private final ReportGeneratorConfig reportGeneratorConfig;

    @Autowired
    public UserActivityClientImpl(RestClient restClientWithRequestTimeout, RetryTemplate retryTemplateFor5xxResponse,
                                  ReportGeneratorConfig reportGeneratorConfig) {
        this.restClientWithRequestTimeout = restClientWithRequestTimeout;
        this.retryTemplateFor5xxResponse = retryTemplateFor5xxResponse;
        this.reportGeneratorConfig = reportGeneratorConfig;
    }

    @Override
    public UserActivityResponse fetchReportsWithRetry(@Nonnull ZonedDateTime dateFrom, @Nonnull ZonedDateTime dateTo) throws ServiceException {
        return retryTemplateFor5xxResponse.execute(context -> fetchReports(dateFrom, dateTo));
    }

    private UserActivityResponse fetchReports(@Nonnull ZonedDateTime dateFrom, @Nonnull ZonedDateTime dateTo) {
        String url = String.format(USER_ACTIVITY_URI_TEMPLATE,
                reportGeneratorConfig.getUserActivityHost(),
                dateFrom.toInstant().toEpochMilli(),
                dateTo.toInstant().toEpochMilli());

        try {
            log.debug("Sending request to : {}", url);
            UserActivityResponse response = restClientWithRequestTimeout.get()
                    .uri(url)
                    .retrieve()
                    .body(UserActivityResponse.class);

            log.debug("Request successfully made to : {}", url);
            return response;
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("Unable to make request to {}", url, e);

            if (!(e instanceof HttpStatusCodeException httpStatusCodeException)) {
                throw new ServiceException(
                        String.format("Request of %s failed %s", url, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR, e);
            }

            UserActivityErrorMessage errorMessage = getErrorMessage(httpStatusCodeException);

            throw new ServiceException(
                    String.format("Request of %s failed %s with %s",
                            url, errorMessage.message(), errorMessage.statusCode()),
                    (HttpStatus) httpStatusCodeException.getStatusCode(), e);
        }
    }

    // TODO: https://github.com/spring-projects/spring-framework/issues/15589
    private static UserActivityErrorMessage getErrorMessage(HttpStatusCodeException httpStatusCodeException) {
        String responseBody = httpStatusCodeException.getResponseBodyAsString();

        try {
            return OBJECT_MAPPER.readValue(responseBody, UserActivityErrorMessage.class);
        } catch (IOException e) {
            log.error("Unable to parse user activity error message : {}", responseBody, e);
            throw new ServiceException("Error request parsing failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
