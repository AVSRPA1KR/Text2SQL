package com.bits.rag.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Handle HuggingFace API errors
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleHuggingFaceError(WebClientResponseException ex) {
        log.error("HuggingFace API error: {}", ex.getResponseBodyAsString());

        return ResponseEntity.status(ex.getStatusCode())
                .body(new ErrorResponse(
                        "EMBEDDING_SERVICE_ERROR",
                        "Failed to generate text embeddings: " + ex.getMessage()
                ));
    }

    // Handle invalid requests
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        "INVALID_REQUEST",
                        ex.getMessage()
                ));
    }

    // Catch-all for other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        return ResponseEntity.internalServerError()
                .body(new ErrorResponse(
                        "INTERNAL_ERROR",
                        "An unexpected error occurred"
                ));
    }

    // Error response DTO
    public record ErrorResponse(String code, String message) {}
}