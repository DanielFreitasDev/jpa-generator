package com.jpagenerator.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TableInfo {
    // Getters and Setters
    private String schema;
    private String name;
    private List<ColumnInfo> columns;
    private PrimaryKeyInfo primaryKey;
    private List<ForeignKeyInfo> foreignKeys;
    private List<SequenceInfo> sequences;
    private List<UniqueConstraintInfo> uniqueConstraints;
}