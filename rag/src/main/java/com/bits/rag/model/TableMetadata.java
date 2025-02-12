package com.bits.rag.model;

import com.pgvector.PGvector;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "tables_metadata", schema = "retrieval")
@Getter
@Setter
public class TableMetadata {

    @Id
    @GeneratedValue
    @Column(name = "table_id")
    private UUID tableId;
    private String tableName;
    private String description;
    @Column(columnDefinition = "vector(384)")
    private float[] embedding;

}