package com.example.reportgenerator.controller;

import com.example.reportgenerator.exception.ServiceException;
import com.example.reportgenerator.model.rest.ErrorMessage;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String UNEXPECTED_ERROR = "Unexpected error";

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleException(Exception e) {
        log.error("Exception thrown", e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorMessage
                        .builder()
                        .message(UNEXPECTED_ERROR)
                        .build());
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorMessage> handleServiceException(ServiceException e) {
        if (e.getStatus().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            log.error("ServiceException thrown", e);
        } else {
            log.info("ServiceException thrown", e);
        }

        return ResponseEntity.status(e.getStatus())
                .body(ErrorMessage
                        .builder()
                        .message(e.getMessage())
                        .build());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  @Nonnull HttpHeaders headers,
                                                                  @Nonnull HttpStatusCode status,
                                                                  @Nonnull WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(d -> String.format("%s: %s", d.getField(), d.getDefaultMessage()))
                .collect(java.util.stream.Collectors.joining(", "));
        logger.error("Method argument is not valid", ex);

        return handleExceptionInternal(ex, ErrorMessage
                        .builder()
                        .message(message)
                        .build(),
                headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(@Nonnull HttpMessageNotReadableException ex,
                                                                  @Nonnull HttpHeaders headers,
                                                                  @Nonnull HttpStatusCode status,
                                                                  @Nonnull WebRequest request) {
        log.error("Http Message is not readable", ex);

        return handleExceptionInternal(ex, ErrorMessage
                        .builder()
                        .message(ex.getMessage())
                        .build(),
                headers, HttpStatus.BAD_REQUEST, request);
    }

}