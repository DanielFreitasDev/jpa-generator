package com.jpagenerator.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PrimaryKeyInfo {
    private List<String> columnNames;

    public PrimaryKeyInfo() {
    }

    public boolean isComposite() {
        return columnNames != null && columnNames.size() > 1;
    }
}