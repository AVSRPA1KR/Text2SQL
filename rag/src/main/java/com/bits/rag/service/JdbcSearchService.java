package com.bits.rag.service;

import com.bits.rag.dto.ColumnResult;
import com.bits.rag.dto.SearchRequest;
import com.bits.rag.dto.TableResult;
import com.bits.rag.repository.JdbcSearchRepository;
import com.pgvector.PGvector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JdbcSearchService {

    private final DataSource dataSource;
    private final JdbcSearchRepository repository;
    private final EmbeddingService embeddingService;

    // Table searches
    public List<TableResult> searchTables(String query, float[] embedding, SearchRequest.SearchType type, int topK) {
        return repository.search(
                "retrieval.tables_metadata",
                query,
                embedding,
                new String[]{"table_name", "description"},
                type,
                topK,
                this::mapTableResult
        );

    }

    // Column searches
    public List<ColumnResult> searchColumns(String query, float[] embedding, SearchRequest.SearchType type, int topK) {
        return repository.search(
                "retrieval.columns_metadata",
                query,
                embedding,
                new String[]{"column_name", "description"},
                type,
                topK,
                this::mapColumnResult
        );
    }

    // Existing mapping methods
    private TableResult mapTableResult(ResultSet rs){
        TableResult result = new TableResult();
        try {
            result.setTableId(rs.getObject("table_id", UUID.class));
            result.setTableName(rs.getString("table_name"));
            result.setDescription(rs.getString("description"));
            return result;
        }
        catch (SQLException e){
            throw new RuntimeException("Table search failed", e);
        }
    }

    private ColumnResult mapColumnResult(ResultSet rs) {
        ColumnResult result = new ColumnResult();
        try {
            result.setColumnId(rs.getObject("column_id", UUID.class));
            result.setTableId(rs.getObject("table_id", UUID.class));
            result.setColumnName(rs.getString("column_name"));
            result.setDescription(rs.getString("description"));
            return result;
        }
        catch (SQLException e){
            throw new RuntimeException("Column search failed", e);
        }
    }


}
