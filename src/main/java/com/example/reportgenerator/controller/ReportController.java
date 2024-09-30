package com.example.reportgenerator.controller;

import com.example.reportgenerator.helper.IdGenerator;
import com.example.reportgenerator.model.jpa.Report;
import com.example.reportgenerator.model.rest.ReportGenerationRequest;
import com.example.reportgenerator.model.rest.ReportGenerationResponse;
import com.example.reportgenerator.model.rest.ReportQueryResponse;
import com.example.reportgenerator.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/report-generator")
    public ResponseEntity<ReportGenerationResponse> generateReport(@Valid @RequestBody ReportGenerationRequest reportGenerationRequest) {
        Report report = reportService.initiateReport(reportGenerationRequest);
        report.setJobId(IdGenerator.generateId());
        reportService.generateReport(reportGenerationRequest, report);

        return new ResponseEntity<>(
                ReportGenerationResponse.builder()
                        .jobId(report.getJobId()).build(), HttpStatus.OK);
    }

    @GetMapping("/report-generator/{jobId}")
    public ResponseEntity<ReportQueryResponse> queryReport(@Valid @PathVariable String jobId) {
        return new ResponseEntity<>(reportService.queryReport(jobId), HttpStatus.OK);
    }

}
