package com.example.reportgenerator.service;

import com.example.reportgenerator.client.UserActivityClientImpl;
import com.example.reportgenerator.config.RestClientConfig;
import com.example.reportgenerator.exception.ServiceException;
import com.example.reportgenerator.model.enums.ReportStatus;
import com.example.reportgenerator.model.jpa.Report;
import com.example.reportgenerator.model.rest.ReportGenerationRequest;
import com.example.reportgenerator.model.rest.ReportQueryResponse;
import com.example.reportgenerator.model.rest.useractivitiy.UserActivityResponse;
import com.example.reportgenerator.repository.ReportRepository;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = RestClientConfig.class)
class ReportServiceImplTest {

    private static final String ID = "rl5m2bwpth6g";
    private static final String REPORT_DATA = "some-data";

    private static final ZonedDateTime DATE_FROM = ZonedDateTime
            .of(2023, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
    private static final ZonedDateTime DATE_TO = ZonedDateTime
            .of(2023, 1, 1, 23, 59, 0, 0, ZoneId.of("UTC"));

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserActivityClientImpl userActivityClient;

    @Captor
    private ArgumentCaptor<Report> reportCaptor;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    void shouldInitiateReport() {
        ReportGenerationRequest reportGenerationRequest = ReportGenerationRequest.builder()
                .dateFrom(DATE_FROM)
                .dateTo(DATE_TO)
                .build();

        Report expectedReport = new Report();
        expectedReport.setReportStatus(ReportStatus.IN_PROGRESS);
        expectedReport.setJobId(ID);

        when(reportRepository.save(any())).thenReturn(expectedReport);

        Report actualReport = reportService.initiateReport(reportGenerationRequest);
        assertEquals(expectedReport, actualReport);
    }

    @Test
    void shouldGenerateReport() {
        ReportGenerationRequest reportGenerationRequest = ReportGenerationRequest.builder()
                .dateFrom(DATE_FROM)
                .dateTo(DATE_TO)
                .build();

        Report report = new Report();
        report.setReportStatus(ReportStatus.IN_PROGRESS);
        report.setJobId(ID);

        UserActivityResponse userActivityResponse = UserActivityResponse.builder()
                .data(REPORT_DATA)
                .build();

        when(userActivityClient.fetchReportsWithRetry(any(), any()))
                .thenReturn(userActivityResponse);

        Report expectedReport = new Report();
        expectedReport.setReportStatus(ReportStatus.SUCCEEDED);
        expectedReport.setData(REPORT_DATA);
        expectedReport.setJobId(ID);

        when(reportRepository.save(any())).thenReturn(expectedReport);

        reportService.generateReport(reportGenerationRequest, report);
        verify(reportRepository, times(1))
                .save(reportCaptor.capture());


        assertEquals(expectedReport, reportCaptor.getValue());
    }

    @Test
    void shouldFailAtGeneratingReport() {
        ReportGenerationRequest reportGenerationRequest = ReportGenerationRequest.builder()
                .dateFrom(DATE_FROM)
                .dateTo(DATE_TO)
                .build();

        Report report = new Report();
        report.setReportStatus(ReportStatus.IN_PROGRESS);
        report.setJobId(ID);

        when(userActivityClient.fetchReportsWithRetry(any(), any()))
                .thenThrow(new ServiceException("some error message",
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        new ServiceException("some spring exception", HttpStatus.INTERNAL_SERVER_ERROR)));

        Report expectedReport = new Report();
        expectedReport.setReportStatus(ReportStatus.FAILED);
        expectedReport.setJobId(ID);
        expectedReport.setError("some error message");

        when(reportRepository.save(any())).thenReturn(expectedReport);

        reportService.generateReport(reportGenerationRequest, report);
        verify(reportRepository, times(1))
                .save(reportCaptor.capture());

        assertEquals(expectedReport, reportCaptor.getValue());
    }

    @Test
    void shouldQueryReport() {
        Report report = new Report();
        report.setReportStatus(ReportStatus.IN_PROGRESS);
        report.setJobId(ID);

        when(reportRepository.findByJobId(any()))
                .thenReturn(Optional.of(report));

        ReportQueryResponse expectedReportQueryResponse = ReportQueryResponse
                .builder()
                .reportStatus(ReportStatus.IN_PROGRESS)
                .build();
        ReportQueryResponse actualReportQueryResponse = reportService.queryReport(ID);
        assertEquals(expectedReportQueryResponse, actualReportQueryResponse);
    }

}
