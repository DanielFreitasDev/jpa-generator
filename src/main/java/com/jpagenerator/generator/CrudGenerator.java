package com.jpagenerator.generator;

import com.jpagenerator.config.DatabaseConfig;
import com.jpagenerator.util.CodeGeneratorHelper;
import com.jpagenerator.model.ColumnInfo;
import com.jpagenerator.model.TableInfo;
import com.jpagenerator.model.UniqueConstraintInfo;
import com.jpagenerator.util.Inflector;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * Gera arquivos de CRUD do Spring Boot (Controller, Service, Repository, DTOs) para uma determinada entidade.
 */

@AllArgsConstructor
public class CrudGenerator {

    private final DatabaseConfig config;
    private final CodeGeneratorHelper helper;
    private final Random random = new Random();

    /**
     * Metodo principal para gerar todos os arquivos de CRUD para uma dada entidade.
     */
    public List<String> generateCrud(TableInfo tableInfo, String className, Map<String, String> allClassNames) throws IOException {
        List<String> generatedFiles = new ArrayList<>();

        generatedFiles.add(generateRequestDto(tableInfo, className));
        generatedFiles.add(generateResponseDto(tableInfo, className));
        generatedFiles.add(generateRepository(tableInfo, className));
        generatedFiles.add(generateService(tableInfo, className, allClassNames));
        generatedFiles.add(generateController(className));

        return generatedFiles;
    }

    private String generateController(String className) throws IOException {
        String controllerName = className + "Controller";
        String serviceName = className + "Service";
        String responseDtoName = className + "Response";
        String requestDtoName = className + "Request";
        String variableName = helper.toCamelCase(className);
        String path = Inflector.pluralize(variableName).toLowerCase();

        StringBuilder code = new StringBuilder();
        String basePackage = config.getBasePackage();

        code.append("package ").append(basePackage).append(".controller.v1;\n\n");

        // Imports
        code.append("import ").append(basePackage).append(".dto.request.").append(requestDtoName).append(";\n");
        code.append("import ").append(basePackage).append(".dto.response.").append(responseDtoName).append(";\n");
        code.append("import ").append(basePackage).append(".service.").append(serviceName).append(";\n");
        code.append("import io.swagger.v3.oas.annotations.security.SecurityRequirement;\n");
        code.append("import io.swagger.v3.oas.annotations.tags.Tag;\n");
        code.append("import jakarta.validation.Valid;\n");
        code.append("import lombok.RequiredArgsConstructor;\n");
        code.append("import org.springframework.data.domain.Page;\n");
        code.append("import org.springframework.data.domain.Pageable;\n");
        code.append("import org.springframework.data.web.PageableDefault;\n");
        code.append("import org.springframework.http.HttpStatus;\n");
        code.append("import org.springframework.http.MediaType;\n");
        code.append("import org.springframework.http.ResponseEntity;\n");
        code.append("import org.springframework.validation.annotation.Validated;\n");
        code.append("import org.springframework.web.bind.annotation.*;\n\n");

        // Definição da classe
        code.append("@RestController\n");
        code.append("@Validated\n");
        code.append("@RequiredArgsConstructor\n");
        code.append("@RequestMapping(value = \"/api/v1/").append(path).append("\", produces = MediaType.APPLICATION_JSON_VALUE)\n");
        code.append("@Tag(name = \"").append(controllerName).append("\", description = \"Endpoint para gerenciar os(as) ").append(path).append("\")\n");
        code.append("@SecurityRequirement(name = \"chaveAcesso\")\n");
        code.append("public class ").append(controllerName).append(" {\n\n");

        code.append("    private final ").append(serviceName).append(" ").append(variableName).append("Service;\n\n");

        // FindAll
        code.append("    @GetMapping\n");
        code.append("    public ResponseEntity<Page<").append(responseDtoName).append(">> findAll(\n");
        code.append("            @PageableDefault(size = 20, sort = \"id\") Pageable pageable) {\n");
        code.append("        Page<").append(responseDtoName).append("> items = ").append(variableName).append("Service.findAll(pageable);\n");
        code.append("        return ResponseEntity.ok(items);\n");
        code.append("    }\n\n");

        // FindById
        code.append("    @GetMapping(\"/{id}\")\n");
        code.append("    public ResponseEntity<").append(responseDtoName).append("> findById(@PathVariable Long id) {\n");
        code.append("        ").append(responseDtoName).append(" item = ").append(variableName).append("Service.findById(id);\n");
        code.append("        return ResponseEntity.ok(item);\n");
        code.append("    }\n\n");

        // Create
        code.append("    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)\n");
        code.append("    public ResponseEntity<").append(responseDtoName).append("> create(@Valid @RequestBody ").append(requestDtoName).append(" request) {\n");
        code.append("        ").append(responseDtoName).append(" createdItem = ").append(variableName).append("Service.create(request);\n");
        code.append("        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);\n");
        code.append("    }\n\n");

        // Update
        code.append("    @PutMapping(value = \"/{id}\", consumes = MediaType.APPLICATION_JSON_VALUE)\n");
        code.append("    public ResponseEntity<").append(responseDtoName).append("> update(\n");
        code.append("            @PathVariable Long id,\n");
        code.append("            @Valid @RequestBody ").append(requestDtoName).append(" request) {\n");
        code.append("        ").append(responseDtoName).append(" updatedItem = ").append(variableName).append("Service.update(id, request);\n");
        code.append("        return ResponseEntity.ok(updatedItem);\n");
        code.append("    }\n\n");

        // Delete
        code.append("    @DeleteMapping(\"/{id}\")\n");
        code.append("    public ResponseEntity<Void> delete(@PathVariable Long id) {\n");
        code.append("        ").append(variableName).append("Service.delete(id);\n");
        code.append("        return ResponseEntity.noContent().build();\n");
        code.append("    }\n");

        code.append("}\n");

        return saveToFile(controllerName, code.toString(), "controller/v1");
    }

    private String generateService(TableInfo tableInfo, String className, Map<String, String> allClassNames) throws IOException {
        String serviceName = className + "Service";
        String repositoryName = className + "Repository";
        String requestDtoName = className + "Request";
        String responseDtoName = className + "Response";
        String variableName = helper.toCamelCase(className);

        StringBuilder code = new StringBuilder();
        String basePackage = config.getBasePackage();

        code.append("package ").append(basePackage).append(".service;\n\n");

        // Imports
        code.append("import ").append(basePackage).append(".dto.request.").append(requestDtoName).append(";\n");
        code.append("import ").append(basePackage).append(".dto.response.").append(responseDtoName).append(";\n");
        code.append("import ").append(basePackage).append(".model.").append(className).append(";\n");
        code.append("import ").append(basePackage).append(".repository.").append(repositoryName).append(";\n");
        code.append("import lombok.RequiredArgsConstructor;\n");
        code.append("import org.springframework.data.domain.Page;\n");
        code.append("import org.springframework.data.domain.Pageable;\n");
        code.append("import org.springframework.stereotype.Service;\n");
        code.append("import org.springframework.transaction.annotation.Transactional;\n");
        code.append("import org.springframework.validation.annotation.Validated;\n");
        code.append("import java.time.Instant;\n\n");

        // Definição da classe
        code.append("@Service\n");
        code.append("@RequiredArgsConstructor\n");
        code.append("@Validated\n");
        code.append("public class ").append(serviceName).append(" {\n\n");

        code.append("    private final ").append(repositoryName).append(" ").append(variableName).append("Repository;\n\n");

        // findAll
        code.append("    @Transactional(readOnly = true)\n");
        code.append("    public Page<").append(responseDtoName).append("> findAll(Pageable pageable) {\n");
        code.append("        return ").append(variableName).append("Repository.findAll(pageable)\n");
        code.append("                .map(this::mapToResponse);\n");
        code.append("    }\n\n");

        // findById
        code.append("    @Transactional(readOnly = true)\n");
        code.append("    public ").append(responseDtoName).append(" findById(Long id) {\n");
        code.append("        ").append(className).append(" ").append(variableName).append(" = ").append(variableName).append("Repository.findById(id)\n");
        code.append("                .orElseThrow(() -> new RuntimeException(\"").append(className).append(" não encontrado(a) com ID: \" + id));\n");
        code.append("        return mapToResponse(").append(variableName).append(");\n");
        code.append("    }\n\n");

        // create
        code.append("    @Transactional(rollbackFor = {Exception.class})\n");
        code.append("    public ").append(responseDtoName).append(" create(").append(requestDtoName).append(" request) {\n");
        code.append(generateUniqueChecks(tableInfo, variableName, "request", false));
        code.append("        ").append(className).append(" ").append(variableName).append(" = mapToEntity(request);\n");
        if (helper.hasField(tableInfo, "createdAt")) {
            code.append("        Instant now = Instant.now();\n");
            code.append("        ").append(variableName).append(".setCreatedAt(now);\n");
            code.append("        ").append(variableName).append(".setUpdatedAt(now);\n");
        }
        code.append("        ").append(className).append(" saved").append(className).append(" = ").append(variableName).append("Repository.save(").append(variableName).append(");\n");
        code.append("        return mapToResponse(saved").append(className).append(");\n");
        code.append("    }\n\n");

        // update
        code.append("    @Transactional(rollbackFor = {Exception.class})\n");
        code.append("    public ").append(responseDtoName).append(" update(Long id, ").append(requestDtoName).append(" request) {\n");
        code.append("        ").append(className).append(" ").append(variableName).append(" = ").append(variableName).append("Repository.findById(id)\n");
        code.append("                .orElseThrow(() -> new RuntimeException(\"").append(className).append(" não encontrado(a) com ID: \" + id));\n\n");
        code.append(generateUniqueChecks(tableInfo, variableName, "request", true));
        for (ColumnInfo col : helper.getUpdatableColumns(tableInfo)) {
            String fieldName = helper.toCamelCase(col.getName());
            String setter = "set" + Inflector.toPascalCase(fieldName);
            String getter = "get" + Inflector.toPascalCase(fieldName);
            code.append("        ").append(variableName).append(".").append(setter).append("(request.").append(getter).append("());\n");
        }
        if (helper.hasField(tableInfo, "updatedAt")) {
            code.append("        ").append(variableName).append(".setUpdatedAt(Instant.now());\n\n");
        }
        code.append("        ").append(className).append(" updated").append(className).append(" = ").append(variableName).append("Repository.save(").append(variableName).append(");\n");
        code.append("        return mapToResponse(updated").append(className).append(");\n");
        code.append("    }\n\n");

        // delete
        code.append("    @Transactional(rollbackFor = {Exception.class})\n");
        code.append("    public void delete(Long id) {\n");
        code.append("        if (!").append(variableName).append("Repository.existsById(id)) {\n");
        code.append("            throw new RuntimeException(\"").append(className).append(" não encontrado(a) com ID: \" + id);\n");
        code.append("        }\n");
        code.append("        ").append(variableName).append("Repository.deleteById(id);\n");
        code.append("    }\n\n");

        // mapToEntity
        code.append("    private ").append(className).append(" mapToEntity(").append(requestDtoName).append(" request) {\n");
        code.append("        ").append(className).append(" ").append(variableName).append(" = new ").append(className).append("();\n");
        for (ColumnInfo col : helper.getUpdatableColumns(tableInfo)) {
            String fieldName = helper.toCamelCase(col.getName());
            String setter = "set" + Inflector.toPascalCase(fieldName);
            String getter = "get" + Inflector.toPascalCase(fieldName);
            code.append("        ").append(variableName).append(".").append(setter).append("(request.").append(getter).append("());\n");
        }
        code.append("        return ").append(variableName).append(";\n");
        code.append("    }\n\n");

        // mapToResponse
        code.append("    private ").append(responseDtoName).append(" mapToResponse(").append(className).append(" ").append(variableName).append(") {\n");
        code.append("        ").append(responseDtoName).append(" response = new ").append(responseDtoName).append("();\n");
        for (ColumnInfo col : tableInfo.getColumns()) {
            if (helper.isResponseField(col.getName())) {
                String fieldName = helper.toCamelCase(col.getName());
                String setter = "set" + Inflector.toPascalCase(fieldName);
                String getter = "get" + Inflector.toPascalCase(fieldName);
                code.append("        response.").append(setter).append("(").append(variableName).append(".").append(getter).append("());\n");
            }
        }
        code.append("        return response;\n");
        code.append("    }\n");
        code.append("}\n");

        return saveToFile(serviceName, code.toString(), "service");
    }

    private String generateRepository(TableInfo tableInfo, String className) throws IOException {
        String repositoryName = className + "Repository";
        String idType = helper.getPrimaryKeyType(tableInfo);
        StringBuilder code = new StringBuilder();
        String basePackage = config.getBasePackage();

        code.append("package ").append(basePackage).append(".repository;\n\n");
        code.append("import ").append(basePackage).append(".model.").append(className).append(";\n");
        code.append("import org.springframework.data.jpa.repository.JpaRepository;\n\n");
        code.append("public interface ").append(repositoryName).append(" extends JpaRepository<").append(className).append(", ").append(idType).append("> {\n\n");

        if (tableInfo.getUniqueConstraints() != null) {
            for (UniqueConstraintInfo constraint : tableInfo.getUniqueConstraints()) {
                if (constraint.getColumnNames().size() == 1) {
                    String columnName = constraint.getColumnNames().getFirst();
                    ColumnInfo columnInfo = tableInfo.getColumns().stream().filter(c -> c.getName().equals(columnName)).findFirst().orElse(null);
                    if (columnInfo != null) {
                        String methodName = "existsBy" + Inflector.toPascalCase(helper.toCamelCase(columnName));
                        String paramType = helper.mapSqlTypeToJava(columnInfo);
                        String paramName = helper.toCamelCase(columnName);
                        code.append("    boolean ").append(methodName).append("(").append(paramType).append(" ").append(paramName).append(");\n\n");
                    }
                }
            }
        }
        code.append("}\n");
        return saveToFile(repositoryName, code.toString(), "repository");
    }

    private String generateRequestDto(TableInfo tableInfo, String className) throws IOException {
        String dtoName = className + "Request";
        List<ColumnInfo> columns = helper.getUpdatableColumns(tableInfo);
        StringBuilder code = new StringBuilder();

        code.append("package ").append(config.getBasePackage()).append(".dto.request;\n\n");
        code.append("import com.fasterxml.jackson.annotation.JsonIgnoreProperties;\n");
        code.append("import jakarta.validation.constraints.NotBlank;\n");
        code.append("import jakarta.validation.constraints.NotNull;\n");
        code.append("import jakarta.validation.constraints.Size;\n");
        code.append("import lombok.Data;\n");
        code.append("import java.io.Serial;\n");
        code.append("import java.io.Serializable;\n\n");

        code.append("@Data\n");
        code.append("@JsonIgnoreProperties(ignoreUnknown = true)\n");
        code.append("public class ").append(dtoName).append(" implements Serializable {\n\n");
        code.append("    @Serial\n");
        code.append("    private static final long serialVersionUID = ").append(random.nextLong()).append("L;\n\n");

        for (ColumnInfo col : columns) {
            String fieldName = helper.toCamelCase(col.getName());
            String pascalName = Inflector.toPascalCase(fieldName);
            String javaType = helper.mapSqlTypeToJava(col);

            if (col.getMaxLength() != null && "String".equals(javaType)) {
                code.append("    @Size(message = \"").append(pascalName).append(" deve ter no máximo ").append(col.getMaxLength()).append(" caracteres\", max = ").append(col.getMaxLength()).append(")\n");
            }
            if (!col.isNullable()) {
                if ("String".equals(javaType)) {
                    code.append("    @NotBlank(message = \"").append(pascalName).append(" é obrigatório(a)\")\n");
                } else {
                    code.append("    @NotNull(message = \"").append(pascalName).append(" é obrigatório(a)\")\n");
                }
            }
            code.append("    private ").append(javaType).append(" ").append(fieldName).append(";\n\n");
        }
        code.append("}\n");
        return saveToFile(dtoName, code.toString(), "dto/request");
    }

    private String generateResponseDto(TableInfo tableInfo, String className) throws IOException {
        String dtoName = className + "Response";
        StringBuilder code = new StringBuilder();

        code.append("package ").append(config.getBasePackage()).append(".dto.response;\n\n");
        code.append("import lombok.Data;\n");
        code.append("import java.io.Serial;\n");
        code.append("import java.io.Serializable;\n");
        if (helper.needsInstant(tableInfo)) {
            code.append("import java.time.Instant;\n");
        }
        if (helper.needsBigDecimal(tableInfo)) {
            code.append("import java.math.BigDecimal;\n");
        }
        code.append("\n");

        code.append("@Data\n");
        code.append("public class ").append(dtoName).append(" implements Serializable {\n\n");
        code.append("    @Serial\n");
        code.append("    private static final long serialVersionUID = ").append(random.nextLong()).append("L;\n\n");

        for (ColumnInfo col : tableInfo.getColumns()) {
            if (helper.isResponseField(col.getName())) {
                String fieldName = helper.toCamelCase(col.getName());
                String javaType = helper.mapSqlTypeToJava(col);
                code.append("    private ").append(javaType).append(" ").append(fieldName).append(";\n");
            }
        }
        code.append("}\n");
        return saveToFile(dtoName, code.toString(), "dto/response");
    }

    private String saveToFile(String className, String code, String subPackage) throws IOException {
        String packagePath = config.getBasePackage().replace(".", File.separator);
        File packageDir = new File(config.getOutputDirectory(), packagePath + File.separator + subPackage.replace("/", File.separator));

        if (!packageDir.exists() && !packageDir.mkdirs()) {
            throw new IOException("Erro ao criar o diretório: " + packageDir.getAbsolutePath());
        }

        File javaFile = new File(packageDir, className + ".java");
        try (FileWriter writer = new FileWriter(javaFile)) {
            writer.write(code);
        }
        return javaFile.getAbsolutePath();
    }

    private String generateUniqueChecks(TableInfo tableInfo, String entityVar, String dtoVar, boolean isUpdate) {
        StringBuilder checks = new StringBuilder();
        if (tableInfo.getUniqueConstraints() == null) {
            return "";
        }

        String repoVar = entityVar + "Repository";

        for (UniqueConstraintInfo constraint : tableInfo.getUniqueConstraints()) {
            if (constraint.getColumnNames().size() == 1) { // Lida apenas com constraints de coluna única por agora
                String columnName = constraint.getColumnNames().getFirst();
                String camelCaseName = helper.toCamelCase(columnName);
                String pascalCaseName = Inflector.toPascalCase(camelCaseName);
                String getter = "get" + pascalCaseName;
                String repoMethod = "existsBy" + pascalCaseName;

                if (isUpdate) {
                    checks.append("        if (!").append(entityVar).append(".").append(getter).append("().equals(").append(dtoVar).append(".").append(getter).append("()) &&\n");
                    checks.append("                ").append(repoVar).append(".").append(repoMethod).append("(").append(dtoVar).append(".").append(getter).append("())) {\n");
                } else {
                    checks.append("        if (").append(repoVar).append(".").append(repoMethod).append("(").append(dtoVar).append(".").append(getter).append("())) {\n");
                }
                checks.append("            throw new RuntimeException(\"").append(pascalCaseName).append(" já cadastrado(a): \" + ").append(dtoVar).append(".").append(getter).append("());\n");
                checks.append("        }\n\n");
            }
        }
        return checks.toString();
    }
}