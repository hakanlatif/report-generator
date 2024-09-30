package com.example.reportgenerator.model.rest;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import lombok.Builder;

@Builder
public record ReportGenerationRequest(

        @Valid
        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("date_from") ZonedDateTime dateFrom,

        @Valid
        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonProperty("date_to") ZonedDateTime dateTo) {

}
