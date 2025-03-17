package com.bits.rag.dto;

import java.util.*;
import java.util.stream.Collectors;

public class SearchResult {
    private List<TableResult> tables;
    private List<ColumnResult> columns;

    private String sqlGenerated;

    public SearchResult(List<TableResult> tables, List<ColumnResult> columns, String sqlGenerated){
        this.tables = tables;
        this.columns = columns;
        this.sqlGenerated = sqlGenerated;
    }

    public Map<String, Object> toResponseMap() {
        Map<String, Object> response = new LinkedHashMap<>();

        // Group columns by table ID for efficient lookup
        Map<UUID, List<ColumnResult>> columnsByTableId = columns.stream()
                .collect(Collectors.groupingBy(ColumnResult::getTableId));

        // Build structured response
        List<Map<String, Object>> structuredTables = tables.stream()
                .map(table -> {
                    Map<String, Object> tableMap = new LinkedHashMap<>();
                    tableMap.put("tableId", table.getTableId());
                    tableMap.put("tableName", table.getTableName());
                    tableMap.put("description", table.getDescription());

                    // Add related columns
                    List<Map<String, Object>> tableColumns = columnsByTableId.getOrDefault(
                                    table.getTableId(),
                                    Collections.emptyList()
                            ).stream()
                            .map(col -> {
                                Map<String, Object> colMap = new LinkedHashMap<>();
                                colMap.put("columnId", col.getColumnId());
                                colMap.put("columnName", col.getColumnName());
                                colMap.put("description", col.getDescription());
                                return colMap;
                            })
                            .collect(Collectors.toList());

                    tableMap.put("columns", tableColumns);
                    return tableMap;
                })
                .collect(Collectors.toList());

        response.put("tables", structuredTables);
        response.put("response", sqlGenerated);
        return response;
    }


}