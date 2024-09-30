package com.example.reportgenerator.model.rest;

import com.example.reportgenerator.model.enums.ReportStatus;
import lombok.Builder;

@Builder
public record ReportQueryResponse(Long reportId, ReportStatus reportStatus, String error) {

}
