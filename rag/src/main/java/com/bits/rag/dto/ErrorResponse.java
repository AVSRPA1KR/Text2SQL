package com.bits.rag.dto;

public record ErrorResponse(
        String code,      // Machine-readable error code
        String message    // Human-readable description
) {}