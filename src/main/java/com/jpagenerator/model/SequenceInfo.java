package com.jpagenerator.model;

public class SequenceInfo {
    private String columnName;
    private String sequenceName;
    private String sequenceSchema;

    public SequenceInfo() {
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    public String getSequenceSchema() {
        return sequenceSchema;
    }

    public void setSequenceSchema(String sequenceSchema) {
        this.sequenceSchema = sequenceSchema;
    }
}