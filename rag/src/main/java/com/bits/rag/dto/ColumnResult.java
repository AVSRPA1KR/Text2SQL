package com.bits.rag.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ColumnResult {

    private UUID columnId;
    private UUID tableId;
    private String columnName;
    private String description;
}
