package com.jpagenerator.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class TableInfo {
    // Getters and Setters
    private String schema;
    private String name;
    private List<ColumnInfo> columns;
    private PrimaryKeyInfo primaryKey;
    private List<ForeignKeyInfo> foreignKeys;
    private List<SequenceInfo> sequences;

    // Constructors
    public TableInfo() {
    }

}