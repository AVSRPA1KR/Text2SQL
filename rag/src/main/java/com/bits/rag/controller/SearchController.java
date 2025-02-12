package com.bits.rag.controller;


import com.bits.rag.dto.ColumnResult;
import com.bits.rag.dto.SearchRequest;
import com.bits.rag.dto.SearchResult;
import com.bits.rag.dto.TableResult;
import com.bits.rag.service.EmbeddingService;
import com.bits.rag.service.JdbcSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schema")
@RequiredArgsConstructor
public class SearchController {

    private final JdbcSearchService jdbcSearchService;
    private final EmbeddingService embeddingService;

    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestBody SearchRequest request) {
        List<TableResult> tables;
        List<ColumnResult> columns;

        float[] embedding = embeddingService.generateEmbedding(request.query());

        tables = jdbcSearchService.searchTables(request.query(), embedding, request.searchType(), request.topK());
        columns = jdbcSearchService.searchColumns(request.query(), embedding, request.searchType(), 2);

        return ResponseEntity.ok(new SearchResult(tables, columns).toResponseMap());
    }


}