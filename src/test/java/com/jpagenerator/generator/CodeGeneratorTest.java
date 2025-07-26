package com.jpagenerator.generator;

import com.jpagenerator.config.DatabaseConfig;
import com.jpagenerator.model.ColumnInfo;
import com.jpagenerator.model.ForeignKeyInfo;
import com.jpagenerator.model.PrimaryKeyInfo;
import com.jpagenerator.model.SequenceInfo;
import com.jpagenerator.model.TableInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeGeneratorTest {

    @TempDir
    Path tempDir;

    private CodeGenerator generator;
    private DatabaseConfig config;

    @BeforeEach
    void setUp() {
        config = new DatabaseConfig();
        config.setJavaVersion(17); // Test Jakarta mode
        config.setBasePackage("com.example.entity");
        config.setOutputDirectory(tempDir.toString());
        config.setUseLombok(true);

        generator = new CodeGenerator(config);
    }

    @Test
    void testGenerateSimpleEntity() throws IOException {
        // Given
        TableInfo tableInfo = createSimpleTableInfo();
        String className = "Perfil";
        Map<String, String> foreignKeyHandling = new HashMap<>();
        // Adicionado o mapa de nomes de classes
        Map<String, String> allClassNames = Map.of(tableInfo.getName(), className);

        // When
        // Corrigida a chamada do metodo para incluir o quarto argumento
        String filePath = generator.generateEntity(tableInfo, className, foreignKeyHandling, allClassNames);

        // Then
        assertNotNull(filePath);
        File generatedFile = new File(filePath);
        assertTrue(generatedFile.exists());

        String content = Files.readString(generatedFile.toPath());

        // Verify package
        assertTrue(content.contains("package com.example.entity;"));

        // Verify imports
        assertTrue(content.contains("import jakarta.persistence.Entity;"));
        assertTrue(content.contains("import jakarta.persistence.Table;"));
        assertTrue(content.contains("import lombok.Getter;"));
        assertTrue(content.contains("import lombok.Setter;"));

        // Verify class declaration
        assertTrue(content.contains("@Entity"));
        assertTrue(content.contains("@Table(name = \"perfis\", schema = \"desmonte\")"));
        assertTrue(content.contains("public class Perfil {"));

        // Verify fields
        assertTrue(content.contains("@Id"));
        assertTrue(content.contains("@GeneratedValue"));
        assertTrue(content.contains("private Integer id;"));
        assertTrue(content.contains("private String descricao;"));
        assertTrue(content.contains("private Instant createdAt;"));
    }

    @Test
    void testGenerateEntityWithForeignKey() throws IOException {
        // Given
        TableInfo tableInfo = createTableWithForeignKey();
        String className = "Empresa";
        Map<String, String> foreignKeyHandling = Map.of(
                "papel_empresa_id", "relationship"
        );
        // Adicionado o mapa de nomes de classes, incluindo a classe referenciada
        Map<String, String> allClassNames = new HashMap<>();
        allClassNames.put("empresas", "Empresa");
        allClassNames.put("papel_empresas", "PapelEmpresa"); // Classe da tabela referenciada

        // When
        // Corrigida a chamada do metodo para incluir o quarto argumento
        String filePath = generator.generateEntity(tableInfo, className, foreignKeyHandling, allClassNames);

        // Then
        File generatedFile = new File(filePath);
        String content = Files.readString(generatedFile.toPath());

        // Verify relationship annotations
        assertTrue(content.contains("@ManyToOne(fetch = FetchType.LAZY)"));
        assertTrue(content.contains("@JoinColumn(name = \"papel_empresa_id\")"));
        assertTrue(content.contains("private PapelEmpresa papelEmpresa;"));

        // Verify FK imports
        assertTrue(content.contains("import jakarta.persistence.ManyToOne;"));
        assertTrue(content.contains("import jakarta.persistence.JoinColumn;"));
        assertTrue(content.contains("import jakarta.persistence.FetchType;"));
    }

    @Test
    void testJavaVersionCompatibility() throws IOException {
        // Given - Java 8 mode
        config.setJavaVersion(8);
        generator = new CodeGenerator(config);

        TableInfo tableInfo = createSimpleTableInfo();
        String className = "Perfil";
        Map<String, String> foreignKeyHandling = new HashMap<>();
        Map<String, String> allClassNames = Map.of(tableInfo.getName(), className);

        // When
        // Corrigida a chamada do metodo para incluir o quarto argumento
        String filePath = generator.generateEntity(tableInfo, className, foreignKeyHandling, allClassNames);

        // Then
        File generatedFile = new File(filePath);
        String content = Files.readString(generatedFile.toPath());

        // Verify javax imports for Java 8
        assertTrue(content.contains("import javax.persistence.Entity;"));
        assertTrue(content.contains("import javax.validation.constraints.NotNull;"));
    }

    @Test
    void testDefaultValueHandling() throws IOException {
        // Given
        TableInfo tableInfo = createTableWithDefaultValues();
        String className = "TestEntity";
        Map<String, String> foreignKeyHandling = new HashMap<>();
        Map<String, String> allClassNames = Map.of(tableInfo.getName(), className);

        // When
        String filePath = generator.generateEntity(tableInfo, className, foreignKeyHandling, allClassNames);

        // Then
        File generatedFile = new File(filePath);
        String content = Files.readString(generatedFile.toPath());

        // Verify default value annotation
        assertTrue(content.contains("@ColumnDefault(\"'CE'\")"));
        assertTrue(content.contains("import org.hibernate.annotations.ColumnDefault;"));
    }

    private TableInfo createSimpleTableInfo() {
        TableInfo table = new TableInfo();
        table.setSchema("desmonte");
        table.setName("perfis");

        // Columns
        List<ColumnInfo> columns = new ArrayList<>();

        // ID column
        ColumnInfo idCol = new ColumnInfo();
        idCol.setName("id");
        idCol.setDataType("smallserial");
        idCol.setNullable(false);
        idCol.setDefaultValue("nextval('desmonte.perfis_id_seq'::regclass)");
        idCol.setOrdinalPosition(1);
        columns.add(idCol);

        // Description column
        ColumnInfo descCol = new ColumnInfo();
        descCol.setName("descricao");
        descCol.setDataType("character varying");
        descCol.setMaxLength(100);
        descCol.setNullable(false);
        descCol.setOrdinalPosition(2);
        columns.add(descCol);

        // Created at column
        ColumnInfo createdCol = new ColumnInfo();
        createdCol.setName("created_at");
        createdCol.setDataType("timestamp");
        createdCol.setNullable(false);
        createdCol.setOrdinalPosition(3);
        columns.add(createdCol);

        table.setColumns(columns);

        // Primary key
        PrimaryKeyInfo pk = new PrimaryKeyInfo();
        pk.setColumnNames(List.of("id"));
        table.setPrimaryKey(pk);

        // Sequences
        SequenceInfo seq = new SequenceInfo();
        seq.setColumnName("id");
        seq.setSequenceName("perfis_id_seq");
        seq.setSequenceSchema("desmonte");
        table.setSequences(List.of(seq));

        table.setForeignKeys(new ArrayList<>());

        return table;
    }

    private TableInfo createTableWithForeignKey() {
        TableInfo table = new TableInfo();
        table.setSchema("desmonte");
        table.setName("empresas");

        // Columns
        List<ColumnInfo> columns = new ArrayList<>();

        // ID column
        ColumnInfo idCol = new ColumnInfo();
        idCol.setName("id");
        idCol.setDataType("bigserial");
        idCol.setNullable(false);
        idCol.setOrdinalPosition(1);
        columns.add(idCol);

        // FK column
        ColumnInfo fkCol = new ColumnInfo();
        fkCol.setName("papel_empresa_id");
        fkCol.setDataType("smallint");
        fkCol.setNullable(true);
        fkCol.setOrdinalPosition(2);
        columns.add(fkCol);

        table.setColumns(columns);

        // Primary key
        PrimaryKeyInfo pk = new PrimaryKeyInfo();
        pk.setColumnNames(List.of("id"));
        table.setPrimaryKey(pk);

        // Foreign key
        ForeignKeyInfo fk = new ForeignKeyInfo();
        fk.setColumnName("papel_empresa_id");
        fk.setReferencedSchema("desmonte");
        fk.setReferencedTable("papel_empresas");
        fk.setReferencedColumn("id");
        fk.setConstraintName("empresa_papel_empresa_fk");
        table.setForeignKeys(List.of(fk));

        table.setSequences(new ArrayList<>());

        return table;
    }

    private TableInfo createTableWithDefaultValues() {
        TableInfo table = new TableInfo();
        table.setSchema("test");
        table.setName("test_table");

        // Column with default value
        ColumnInfo ufCol = new ColumnInfo();
        ufCol.setName("uf");
        ufCol.setDataType("character varying");
        ufCol.setMaxLength(2);
        ufCol.setNullable(true);
        ufCol.setDefaultValue("'CE'::character varying");
        ufCol.setOrdinalPosition(1);

        table.setColumns(List.of(ufCol));
        table.setForeignKeys(new ArrayList<>());
        table.setSequences(new ArrayList<>());
        table.setPrimaryKey(new PrimaryKeyInfo()); // Adicionado para evitar NullPointerException

        return table;
    }
}