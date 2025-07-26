package com.jpagenerator.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ColumnInfo {
    // Getters and Setters
    private String name;
    private String dataType;
    private Integer maxLength;
    private Integer precision;
    private Integer scale;
    private boolean nullable;
    private String defaultValue;
    private int ordinalPosition;

    // Constructors
    public ColumnInfo() {
    }

    public boolean isSerial() {
        return defaultValue != null && defaultValue.contains("nextval");
    }

    public boolean isPrimaryKey(PrimaryKeyInfo pk) {
        return pk != null && pk.getColumnNames().contains(name);
    }
}