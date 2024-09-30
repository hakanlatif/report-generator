package com.example.reportgenerator.client;

import com.example.reportgenerator.config.ReportGeneratorConfig;
import com.example.reportgenerator.config.RestClientConfig;
import com.example.reportgenerator.exception.ServiceException;
import com.example.reportgenerator.model.enums.ReportStatus;
import com.example.reportgenerator.model.jpa.Report;
import com.example.reportgenerator.model.rest.useractivitiy.UserActivityResponse;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = RestClientConfig.class)
class UserActivityClientImplTest {

    private static final String JOB_ID = "rl5m2bwpth6g";
    private static final String REPORT_DATA = "some-data";

    private static final ZonedDateTime DATE_FROM = ZonedDateTime
            .of(2023, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
    private static final ZonedDateTime DATE_TO = ZonedDateTime
            .of(2023, 1, 1, 23, 59, 0, 0, ZoneId.of("UTC"));

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private RetryTemplate retryTemplateFor5xxResponse;

    @Mock
    private ReportGeneratorConfig reportGeneratorConfig;

    @InjectMocks
    private UserActivityClientImpl userActivityClient;

    @BeforeEach
    public void setup() {
        when(reportGeneratorConfig.getUserActivityHost()).thenReturn("localhost:8080");
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldFetchReportsWithRetry() {
        Report report = new Report();
        report.setReportStatus(ReportStatus.IN_PROGRESS);
        report.setJobId(JOB_ID);

        UserActivityResponse userActivityResponse = UserActivityResponse.builder()
                .data("some-data")
                .build();

        when(restClient.get())
                .thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString()))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve())
                .thenReturn(responseSpec);
        when(responseSpec.body(UserActivityResponse.class))
                .thenReturn(userActivityResponse);

        when(retryTemplateFor5xxResponse.execute(any(RetryCallback.class))).thenAnswer(invocation -> {
            RetryCallback retry = invocation.getArgument(0);
            return retry.doWithRetry(null);
        });

        UserActivityResponse expectedUserActivityResponse = UserActivityResponse.builder()
                .data(REPORT_DATA)
                .build();

        UserActivityResponse actualUserActivityResponse = userActivityClient.fetchReportsWithRetry(DATE_FROM, DATE_TO);
        assertEquals(expectedUserActivityResponse, actualUserActivityResponse);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldFailFetchingReportsWithRetry() {
        Report report = new Report();
        report.setReportStatus(ReportStatus.IN_PROGRESS);
        report.setJobId(JOB_ID);

        when(restClient.get())
                .thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString()))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve())
                .thenReturn(responseSpec);
        when(responseSpec.body(UserActivityResponse.class))
                .thenThrow(new RestClientException("some spring error message"));

        when(retryTemplateFor5xxResponse.execute(any(RetryCallback.class))).thenAnswer(invocation -> {
            RetryCallback retry = invocation.getArgument(0);
            return retry.doWithRetry(null);
        });

        ServiceException exception = assertThrows(ServiceException.class, () ->
                userActivityClient.fetchReportsWithRetry(DATE_FROM, DATE_TO));

        assertEquals("Request of http://localhost:8080/user-activity?date_from=1672531200000&" +
                "date_to=1672617540000 failed some spring error message", exception.getMessage());
    }

}
