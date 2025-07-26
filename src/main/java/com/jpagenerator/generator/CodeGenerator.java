package com.jpagenerator.generator;

import com.jpagenerator.config.DatabaseConfig;
import com.jpagenerator.model.ColumnInfo;
import com.jpagenerator.model.ForeignKeyInfo;
import com.jpagenerator.model.SequenceInfo;
import com.jpagenerator.model.TableInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class CodeGenerator {
    private final DatabaseConfig config;

    public CodeGenerator(DatabaseConfig config) {
        this.config = config;
    }

    public String generateEntity(TableInfo tableInfo, String className, Map<String, String> foreignKeyHandling, Map<String, String> allClassNames) throws IOException {
        StringBuilder code = new StringBuilder();

        // Package declaration
        code.append("package ").append(config.getBasePackage()).append(";\n\n");

        // Imports
        generateImports(code, tableInfo, foreignKeyHandling);

        // Class declaration
        generateClassDeclaration(code, className, tableInfo);

        // Fields
        generateFields(code, tableInfo, foreignKeyHandling, allClassNames);

        // Close class
        code.append("}\n");

        // Save to file
        return saveToFile(className, code.toString());
    }

    private void generateImports(StringBuilder code, TableInfo tableInfo, Map<String, String> foreignKeyHandling) {
        Set<String> imports = new TreeSet<>();

        // Base JPA imports
        String persistencePackage = config.isJakartaMode() ? "jakarta.persistence" : "javax.persistence";
        imports.add(persistencePackage + ".Column");
        imports.add(persistencePackage + ".Entity");
        imports.add(persistencePackage + ".GeneratedValue");
        imports.add(persistencePackage + ".GenerationType");
        imports.add(persistencePackage + ".Id");
        imports.add(persistencePackage + ".Table");

        // Check if we need sequence generator
        boolean hasSequence = tableInfo.getColumns().stream()
                .anyMatch(col -> col.isSerial() && col.isPrimaryKey(tableInfo.getPrimaryKey()));

        if (hasSequence) {
            imports.add(persistencePackage + ".SequenceGenerator");
        }

        // Check for foreign keys
        boolean hasForeignKeys = foreignKeyHandling.values().stream()
                .anyMatch("relationship"::equals);

        if (hasForeignKeys) {
            imports.add(persistencePackage + ".FetchType");
            imports.add(persistencePackage + ".JoinColumn");
            imports.add(persistencePackage + ".ManyToOne");
        }

        // Validation imports
        boolean hasValidation = tableInfo.getColumns().stream()
                .anyMatch(col -> !col.isNullable() || col.getMaxLength() != null);

        if (hasValidation) {
            String validationPackage = config.isJakartaMode() ? "jakarta.validation.constraints" : "javax.validation.constraints";
            imports.add(validationPackage + ".NotNull");
            imports.add(validationPackage + ".Size");
        }

        // Lombok imports
        if (config.isUseLombok()) {
            imports.add("lombok.Getter");
            imports.add("lombok.Setter");
        }

        // Default value imports
        boolean hasDefaultValues = tableInfo.getColumns().stream()
                .anyMatch(col -> col.getDefaultValue() != null && !col.isSerial());

        if (hasDefaultValues) {
            imports.add("org.hibernate.annotations.ColumnDefault");
        }

        // Java type imports
        boolean hasInstant = tableInfo.getColumns().stream()
                .anyMatch(col -> "timestamp".equals(col.getDataType()) || "timestamptz".equals(col.getDataType()));

        if (hasInstant) {
            imports.add("java.time.Instant");
        }

        boolean hasBigDecimal = tableInfo.getColumns().stream()
                .anyMatch(col -> "numeric".equals(col.getDataType()) || "decimal".equals(col.getDataType()));

        if (hasBigDecimal) {
            imports.add("java.math.BigDecimal");
        }

        // Write imports
        for (String importStr : imports) {
            code.append("import ").append(importStr).append(";\n");
        }
        code.append("\n");
    }

    private void generateClassDeclaration(StringBuilder code, String className, TableInfo tableInfo) {
        // Lombok annotations
        if (config.isUseLombok()) {
            code.append("@Getter\n");
            code.append("@Setter\n");
        }

        // JPA annotations
        code.append("@Entity\n");
        code.append("@Table(name = \"").append(tableInfo.getName()).append("\"");
        if (tableInfo.getSchema() != null) {
            code.append(", schema = \"").append(tableInfo.getSchema()).append("\"");
        }
        code.append(")\n");

        // Class declaration
        code.append("public class ").append(className).append(" {\n");
    }

    private void generateFields(StringBuilder code, TableInfo tableInfo, Map<String, String> foreignKeyHandling, Map<String, String> allClassNames) {
        for (ColumnInfo column : tableInfo.getColumns()) {
            generateField(code, column, tableInfo, foreignKeyHandling, allClassNames);
        }
    }

    private void generateField(StringBuilder code, ColumnInfo column, TableInfo tableInfo, Map<String, String> foreignKeyHandling, Map<String, String> allClassNames) {
        // Check if this column is a foreign key
        ForeignKeyInfo fk = null;
        if (tableInfo.getForeignKeys() != null) {
            fk = tableInfo.getForeignKeys().stream()
                    .filter(f -> f.getColumnName().equals(column.getName()))
                    .findFirst()
                    .orElse(null);
        }

        boolean isForeignKey = fk != null;
        boolean createRelationship = isForeignKey && "relationship".equals(foreignKeyHandling.get(column.getName()));

        // Generate field annotations and declaration
        if (createRelationship) {
            generateRelationshipField(code, column, fk, allClassNames);
        } else {
            generateRegularField(code, column, tableInfo);
        }

        code.append("\n");
    }

    private void generateRegularField(StringBuilder code, ColumnInfo column, TableInfo tableInfo) {
        // Primary key annotations
        if (column.isPrimaryKey(tableInfo.getPrimaryKey())) {
            code.append("    @Id\n");

            if (column.isSerial()) {
                // Find sequence info
                SequenceInfo sequence = tableInfo.getSequences().stream()
                        .filter(seq -> seq.getColumnName().equals(column.getName()))
                        .findFirst()
                        .orElse(null);

                String sequenceName = sequence != null ? sequence.getSequenceSchema() + "." + sequence.getSequenceName() :
                        tableInfo.getSchema() + "." + tableInfo.getName() + "_" + column.getName() + "_seq";

                String generatorName = tableInfo.getName() + "_" + column.getName() + "_gen";

                code.append("    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = \"")
                        .append(generatorName).append("\")\n");
                code.append("    @SequenceGenerator(name = \"").append(generatorName)
                        .append("\", sequenceName = \"").append(sequenceName)
                        .append("\", allocationSize = 1)\n");
            }
        }

        // Validation annotations
        if (column.getMaxLength() != null && "character varying".equals(column.getDataType())) {
            code.append("    @Size(max = ").append(column.getMaxLength()).append(")\n");
        }

        if (!column.isNullable()) {
            code.append("    @NotNull\n");
        }

        // Default value annotation
        if (column.getDefaultValue() != null && !column.isSerial()) {
            String defaultValue = formatDefaultValue(column.getDefaultValue());
            code.append("    @ColumnDefault(\"").append(defaultValue).append("\")\n");
        }

        // Column annotation
        code.append("    @Column(name = \"").append(column.getName()).append("\"");

        if (!column.isNullable()) {
            code.append(", nullable = false");
        }

        if (column.getMaxLength() != null && "character varying".equals(column.getDataType())) {
            code.append(", length = ").append(column.getMaxLength());
        }

        code.append(")\n");

        // Field declaration
        String javaType = mapSqlTypeToJava(column);
        String fieldName = toCamelCase(column.getName());

        code.append("    private ").append(javaType).append(" ").append(fieldName).append(";");
    }

    private void generateRelationshipField(StringBuilder code, ColumnInfo column, ForeignKeyInfo fk, Map<String, String> allClassNames) {
        boolean isNotNull = !column.isNullable();

        // ManyToOne annotation
        code.append("    @ManyToOne(fetch = FetchType.LAZY");
        if (isNotNull) {
            code.append(", optional = false");
        }
        code.append(")\n");

        // JoinColumn annotation
        code.append("    @JoinColumn(name = \"").append(column.getName()).append("\"");
        if (isNotNull) {
            code.append(", nullable = false");
        }
        code.append(")\n");

        // Field declaration
        String referencedTableName = fk.getReferencedTable();
        String referencedClassName = allClassNames.getOrDefault(referencedTableName, toPascalCase(referencedTableName));
        String fieldName = toCamelCase(fk.getColumnName().replaceAll("_id$", "")); // Remove _id suffix for a cleaner name

        code.append("    private ").append(referencedClassName).append(" ").append(fieldName).append(";");
    }

    private String mapSqlTypeToJava(ColumnInfo column) {
        String dataType = column.getDataType().toLowerCase();

        switch (dataType) {
            case "smallint":
            case "smallserial":
                return "Integer";
            case "integer":
            case "serial":
                return "Integer";
            case "bigint":
            case "bigserial":
                return "Long";
            case "character varying":
            case "varchar":
            case "text":
            case "char":
            case "character":
                return "String";
            case "boolean":
                return "Boolean";
            case "timestamp":
            case "timestamptz":
            case "timestamp with time zone":
            case "timestamp without time zone":
                return "Instant";
            case "date":
                return "LocalDate";
            case "time":
                return "LocalTime";
            case "numeric":
            case "decimal":
                return "BigDecimal";
            case "real":
                return "Float";
            case "double precision":
                return "Double";
            case "uuid":
                return "UUID";
            default:
                return "String"; // Default fallback
        }
    }

    private String formatDefaultValue(String defaultValue) {
        if (defaultValue == null) return null;

        // Remove PostgreSQL casting
        defaultValue = defaultValue.replaceAll("::[a-zA-Z_]+.*", "");

        // Handle string literals
        if (defaultValue.startsWith("'") && defaultValue.endsWith("'")) {
            return defaultValue; // Keep quotes for string literals
        }

        // Handle other literals
        return "'" + defaultValue + "'";
    }

    private String toCamelCase(String input) {
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

    private String toPascalCase(String input) {
        if (input == null || input.isEmpty()) return input;

        String[] parts = input.split("_");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    result.append(part.substring(1).toLowerCase());
                }
            }
        }

        return result.toString();
    }

    private String saveToFile(String className, String code) throws IOException {
        // Create package directory structure
        String packagePath = config.getBasePackage().replace(".", File.separator);
        File packageDir = new File(config.getOutputDirectory(), packagePath);

        if (!packageDir.exists()) {
            packageDir.mkdirs();
        }

        // Create file
        File javaFile = new File(packageDir, className + ".java");

        try (FileWriter writer = new FileWriter(javaFile)) {
            writer.write(code);
        }

        return javaFile.getAbsolutePath();
    }
}