package com.jpagenerator.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class PrimaryKeyInfo {
    private List<String> columnNames;

    public PrimaryKeyInfo() {
        // Inicializa a lista para evitar NullPointerException
        this.columnNames = new ArrayList<>();
    }

    public boolean isComposite() {
        return columnNames != null && columnNames.size() > 1;
    }
}