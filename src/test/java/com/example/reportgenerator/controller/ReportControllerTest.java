package com.example.reportgenerator.controller;

import com.example.reportgenerator.helper.IdGenerator;
import com.example.reportgenerator.model.enums.ReportStatus;
import com.example.reportgenerator.model.jpa.Report;
import com.example.reportgenerator.model.rest.ReportGenerationRequest;
import com.example.reportgenerator.service.ReportServiceImpl;
import com.example.reportgenerator.test.MockitoStaticExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerTest {

    private static final String REPORT_GENERATE_URI = "/report-generator";
    private static final String JOB_ID = "rl5m2bwpth6g";

    private static final ZonedDateTime DATE_FROM = ZonedDateTime
            .of(2023, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
    private static final ZonedDateTime DATE_TO = ZonedDateTime
            .of(2023, 1, 1, 23, 59, 0, 0, ZoneId.of("UTC"));

    @RegisterExtension
    public MockitoStaticExtension<IdGenerator> mockedIdGenerator = new MockitoStaticExtension<>(IdGenerator.class);

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ReportServiceImpl reportService;

    private JacksonTester<ReportGenerationRequest> reportGenerationRequestTester;

    @BeforeEach
    public void setup() {
        JacksonTester.initFields(this, new ObjectMapper().registerModule(new JavaTimeModule()));

        when(IdGenerator.generateId()).thenReturn(JOB_ID);
    }

    @Test
    void shouldGenerateReport() throws Exception {
        Report report = new Report();
        report.setJobId(JOB_ID);
        report.setReportStatus(ReportStatus.IN_PROGRESS);

        ReportGenerationRequest reportGenerationRequest = ReportGenerationRequest.builder()
                .dateFrom(DATE_FROM)
                .dateTo(DATE_TO)
                .build();

        when(reportService.initiateReport(any())).thenReturn(report);
        doNothing().when(reportService).generateReport(reportGenerationRequest, report);

        mvc.perform(post(REPORT_GENERATE_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reportGenerationRequestTester.write(reportGenerationRequest).getJson()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("{\"jobId\":\"rl5m2bwpth6g\"}"));
    }

}
