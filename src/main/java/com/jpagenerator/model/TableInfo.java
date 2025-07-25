package com.jpagenerator.model;

import java.util.List;

public class TableInfo {
    private String schema;
    private String name;
    private List<ColumnInfo> columns;
    private PrimaryKeyInfo primaryKey;
    private List<ForeignKeyInfo> foreignKeys;
    private List<SequenceInfo> sequences;

    // Constructors
    public TableInfo() {
    }

    // Getters and Setters
    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ColumnInfo> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnInfo> columns) {
        this.columns = columns;
    }

    public PrimaryKeyInfo getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(PrimaryKeyInfo primaryKey) {
        this.primaryKey = primaryKey;
    }

    public List<ForeignKeyInfo> getForeignKeys() {
        return foreignKeys;
    }

    public void setForeignKeys(List<ForeignKeyInfo> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }

    public List<SequenceInfo> getSequences() {
        return sequences;
    }

    public void setSequences(List<SequenceInfo> sequences) {
        this.sequences = sequences;
    }
}