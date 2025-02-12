package com.bits.rag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SearchRequest(
        @NotBlank String query,
        @NotNull SearchType searchType,
        int topK
) {
    public enum SearchType { SEMANTIC, KEYWORD, HYBRID }
}