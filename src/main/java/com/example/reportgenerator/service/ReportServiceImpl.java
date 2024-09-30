package com.example.reportgenerator.service;

import com.example.reportgenerator.client.UserActivityClient;
import com.example.reportgenerator.exception.ServiceException;
import com.example.reportgenerator.model.enums.ReportStatus;
import com.example.reportgenerator.model.jpa.Report;
import com.example.reportgenerator.model.rest.ReportGenerationRequest;
import com.example.reportgenerator.model.rest.ReportQueryResponse;
import com.example.reportgenerator.model.rest.useractivitiy.UserActivityResponse;
import com.example.reportgenerator.repository.ReportRepository;
import jakarta.annotation.Nonnull;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    private final UserActivityClient userActivityClient;
    private final ReportRepository reportRepository;

    @Autowired
    public ReportServiceImpl(UserActivityClient userActivityClient, ReportRepository reportRepository) {
        this.userActivityClient = userActivityClient;
        this.reportRepository = reportRepository;
    }

    @Transactional
    @Override
    public Report initiateReport(ReportGenerationRequest reportGenerationRequest) {
        Report report = new Report();
        report.setReportStatus(ReportStatus.IN_PROGRESS);

        report = reportRepository.save(report);

        log.info("Job {} was initialized for report {}", report.getJobId(), report.getId());
        return report;
    }

    @Async("asyncExecutor")
    @Transactional
    @Override
    public void generateReport(ReportGenerationRequest reportGenerationRequest, Report report) {
        try {
            UserActivityResponse userActivityResponse =
                    userActivityClient.fetchReportsWithRetry(reportGenerationRequest.dateFrom(), reportGenerationRequest.dateTo());
            report.setData(userActivityResponse.data());
            report.setReportStatus(ReportStatus.SUCCEEDED);
        } catch (ServiceException exception) {
            report.setError(exception.getMessage());
            report.setReportStatus(ReportStatus.FAILED);
        }

        report = reportRepository.save(report);
        log.info("Job {} was generated report {}, reportStatus : {}", report.getJobId(), report.getId(),
                report.getReportStatus().name());
    }

    @Override
    public ReportQueryResponse queryReport(@Nonnull String jobId) {
        Report report = reportRepository.findByJobId(jobId)
                .orElseThrow(() -> new ServiceException(String.format("Job %s not found", jobId), HttpStatus.NOT_FOUND));
        return ReportQueryResponse.builder()
                .reportId(report.getId())
                .reportStatus(report.getReportStatus())
                .error(report.getError())
                .build();
    }

}