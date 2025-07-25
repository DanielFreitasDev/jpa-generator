package com.jpagenerator.model;

import java.util.List;

public class PrimaryKeyInfo {
    private List<String> columnNames;

    public PrimaryKeyInfo() {
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public boolean isComposite() {
        return columnNames != null && columnNames.size() > 1;
    }
}