package com.jpagenerator.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ForeignKeyInfo {
    private String columnName;
    private String referencedSchema;
    private String referencedTable;
    private String referencedColumn;
    private String constraintName;

    public ForeignKeyInfo() {
    }

}