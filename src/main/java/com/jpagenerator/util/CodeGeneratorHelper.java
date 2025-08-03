package com.jpagenerator.util;

import com.jpagenerator.model.ColumnInfo;
import com.jpagenerator.model.TableInfo;

import java.util.List;
import java.util.stream.Collectors;

public class CodeGeneratorHelper {

    public String toCamelCase(String input) {
        if (input == null || input.isEmpty()) return input;
        String[] parts = input.split("_");
        StringBuilder result = new StringBuilder(parts[0].toLowerCase());
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                result.append(Character.toUpperCase(parts[i].charAt(0)));
                if (parts[i].length() > 1) {
                    result.append(parts[i].substring(1).toLowerCase());
                }
            }
        }
        return result.toString();
    }

    public String mapSqlTypeToJava(ColumnInfo column) {
        String dataType = column.getDataType().toLowerCase();
        return switch (dataType) {
            case "smallint", "smallserial", "integer", "serial" -> "Integer";
            case "bigint", "bigserial" -> "Long";
            case "character varying", "varchar", "text", "char", "character" -> "String";
            case "boolean" -> "Boolean";
            case "timestamp", "timestamptz", "timestamp with time zone", "timestamp without time zone" -> "Instant";
            case "date" -> "java.time.LocalDate";
            case "time" -> "java.time.LocalTime";
            case "numeric", "decimal" -> "java.math.BigDecimal";
            case "real" -> "Float";
            case "double precision" -> "Double";
            case "uuid" -> "java.util.UUID";
            default -> "String";
        };
    }

    public String getPrimaryKeyType(TableInfo tableInfo) {
        if (tableInfo.getPrimaryKey() != null && !tableInfo.getPrimaryKey().getColumnNames().isEmpty()) {
            String pkColumnName = tableInfo.getPrimaryKey().getColumnNames().getFirst();
            return tableInfo.getColumns().stream()
                    .filter(c -> c.getName().equals(pkColumnName))
                    .findFirst()
                    .map(this::mapSqlTypeToJava)
                    .orElse("Long");
        }
        return "Long"; // Usa Long como padrão se nenhuma PK for encontrada
    }

    public List<ColumnInfo> getUpdatableColumns(TableInfo tableInfo) {
        return tableInfo.getColumns().stream()
                .filter(c -> !c.isPrimaryKey(tableInfo.getPrimaryKey()) && !isAuditField(c.getName()))
                .collect(Collectors.toList());
    }

    public boolean hasField(TableInfo tableInfo, String fieldName) {
        return tableInfo.getColumns().stream().anyMatch(c -> c.getName().equalsIgnoreCase(fieldName));
    }

    boolean isAuditField(String columnName) {
        return "created_at".equalsIgnoreCase(columnName) || "updated_at".equalsIgnoreCase(columnName);
    }

    public boolean isResponseField(String columnName) {
        return true; // Todos os campos são incluídos por padrão
    }

    public boolean needsInstant(TableInfo tableInfo) {
        return tableInfo.getColumns().stream()
                .anyMatch(col -> "timestamp".equals(col.getDataType()) || "timestamptz".equals(col.getDataType()));
    }

    public boolean needsBigDecimal(TableInfo tableInfo) {
        return tableInfo.getColumns().stream()
                .anyMatch(col -> "numeric".equals(col.getDataType()) || "decimal".equals(col.getDataType()));
    }
}