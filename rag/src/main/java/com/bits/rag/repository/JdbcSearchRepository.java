package com.bits.rag.repository;

import com.bits.rag.dto.SearchRequest;
import com.pgvector.PGvector;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Repository
public class JdbcSearchRepository {

    private final DataSource dataSource;

    public JdbcSearchRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> search(String tableName,
                              String searchQuery,
                              Object embedding,
                              String[] searchFields,
                              SearchRequest.SearchType searchType,
                              int topK,
                              Function<ResultSet, T> mapper) {
        final String SQL = buildSearchQuery(tableName, searchFields, searchType);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            int paramIndex = 1;
            if (searchType == SearchRequest.SearchType.SEMANTIC || searchType == SearchRequest.SearchType.HYBRID) {
                stmt.setObject(paramIndex++, new PGvector((float[]) embedding));
            }
            if (searchType == SearchRequest.SearchType.KEYWORD || searchType == SearchRequest.SearchType.HYBRID) {
                stmt.setString(paramIndex++, searchQuery);
            }
            stmt.setInt(paramIndex, topK);

            ResultSet rs = stmt.executeQuery();
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapper.apply(rs));
            }
            return results;

        } catch (SQLException e) {
            throw new RuntimeException("Search failed for " + tableName, e);
        }
    }

    private String buildSearchQuery(String tableName,
                                    String[] searchFields,
                                    SearchRequest.SearchType searchType) {
        return switch (searchType) {
            case KEYWORD -> """
            SELECT * FROM %s
            ORDER BY ts_rank_cd(
                to_tsvector('english', %s), 
                websearch_to_tsquery('english', ?)
            ) DESC
            LIMIT ?
            """.formatted(tableName, String.join(" || ' ' || ", searchFields));

            case SEMANTIC -> """
            SELECT * FROM %s
            ORDER BY (embedding <=> ?) ASC
            LIMIT ?
            """.formatted(tableName);

            case HYBRID -> """
            SELECT * FROM %s
            ORDER BY (0.5 * (1 - (embedding <=> ?)) + 
                 0.5 * ts_rank_cd(to_tsvector('english', %s), 
                                websearch_to_tsquery('english', ?))) DESC
            LIMIT ?
            """.formatted(tableName, String.join(" || ' ' || ", searchFields));
        };
    }

}
