package com.bits.rag.model;

import com.pgvector.PGvector;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "columns_metadata", schema = "retrieval")
@Setter
@Getter
public class ColumnMetadata {

    @Id
    @GeneratedValue
    @Column(name = "column_id")
    private UUID columnId;

    private UUID tableId;
    private String columnName;
    private String description;
    private String dataType;

    @Column(columnDefinition = "vector(384)")
    private float[] embedding;

}