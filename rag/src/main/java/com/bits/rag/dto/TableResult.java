package com.bits.rag.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TableResult {

    private UUID tableId;
    private String tableName;
    private String description;
}
