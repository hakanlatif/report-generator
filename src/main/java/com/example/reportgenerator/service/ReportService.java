package com.example.reportgenerator.service;

import com.example.reportgenerator.model.jpa.Report;
import com.example.reportgenerator.model.rest.ReportGenerationRequest;
import com.example.reportgenerator.model.rest.ReportQueryResponse;

public interface ReportService {

    Report initiateReport(ReportGenerationRequest reportGenerationRequest);

    void generateReport(ReportGenerationRequest reportGenerationRequest, Report report);

    ReportQueryResponse queryReport(String jobId);

}
