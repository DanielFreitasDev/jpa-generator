package com.jpagenerator.model;

public class ForeignKeyInfo {
    private String columnName;
    private String referencedSchema;
    private String referencedTable;
    private String referencedColumn;
    private String constraintName;

    public ForeignKeyInfo() {
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getReferencedSchema() {
        return referencedSchema;
    }

    public void setReferencedSchema(String referencedSchema) {
        this.referencedSchema = referencedSchema;
    }

    public String getReferencedTable() {
        return referencedTable;
    }

    public void setReferencedTable(String referencedTable) {
        this.referencedTable = referencedTable;
    }

    public String getReferencedColumn() {
        return referencedColumn;
    }

    public void setReferencedColumn(String referencedColumn) {
        this.referencedColumn = referencedColumn;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }
}