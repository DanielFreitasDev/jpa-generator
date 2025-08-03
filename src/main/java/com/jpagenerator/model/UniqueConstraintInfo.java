package com.jpagenerator.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class UniqueConstraintInfo {
    private String constraintName;
    private List<String> columnNames;
}