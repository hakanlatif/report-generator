package com.example.reportgenerator.model.rest.useractivitiy;

import lombok.Builder;

@Builder
public record UserActivityErrorMessage(String statusCode, String error, String message) {

}
