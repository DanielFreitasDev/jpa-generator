package com.jpagenerator;

import com.jpagenerator.config.ConfigManager;
import com.jpagenerator.config.DatabaseConfig;
import com.jpagenerator.generator.CodeGenerator;
import com.jpagenerator.inspector.DatabaseInspector;
import com.jpagenerator.model.TableInfo;
import com.jpagenerator.util.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final Scanner scanner = new Scanner(System.in);
    private static final ConfigManager configManager = new ConfigManager();
    private static DatabaseInspector inspector;
    private static CodeGenerator generator;

    public static void main(String[] args) {
        try {
            System.out.println("=== Gerador Automático de Classes JPA ===\n");

            // Parse command line arguments
            CommandLineArgs cmdArgs = parseArgs(args);

            // Load or create configuration
            DatabaseConfig config = loadConfiguration();

            // Initialize components
            inspector = new DatabaseInspector(config);
            generator = new CodeGenerator(config);

            // Execute based on arguments
            if (cmdArgs.interactive || (cmdArgs.schema == null && cmdArgs.table == null)) {
                runInteractiveMode(config);
            } else {
                runBatchMode(cmdArgs, config);
            }

        } catch (Exception e) {
            System.err.println("Erro durante a execução: " + e.getMessage());
            logger.error("Erro durante execução", e);
        } finally {
            scanner.close();
        }
    }

    private static CommandLineArgs parseArgs(String[] args) {
        CommandLineArgs cmdArgs = new CommandLineArgs();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--schema":
                    if (i + 1 < args.length) {
                        cmdArgs.schema = args[++i];
                    }
                    break;
                case "--table":
                    if (i + 1 < args.length) {
                        cmdArgs.table = args[++i];
                    }
                    break;
                case "--config":
                    if (i + 1 < args.length) {
                        cmdArgs.configFile = args[++i];
                    }
                    break;
                case "--interactive":
                    cmdArgs.interactive = true;
                    break;
                case "--output":
                    if (i + 1 < args.length) {
                        cmdArgs.outputDir = args[++i];
                    }
                    break;
            }
        }

        return cmdArgs;
    }

    private static DatabaseConfig loadConfiguration() {
        DatabaseConfig config = configManager.loadConfig();

        if (config == null) {
            System.out.println("Configuração não encontrada. Vamos criar uma nova configuração.");
            config = createNewConfiguration();
        } else {
            System.out.println("Configuração carregada: " + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase());

            System.out.print("Deseja usar esta configuração? (s/n): ");
            String choice = scanner.nextLine().trim().toLowerCase();

            if (!choice.equals("s") && !choice.equals("sim") && !choice.isEmpty()) {
                config = createNewConfiguration();
            }
        }

        return config;
    }

    private static DatabaseConfig createNewConfiguration() {
        DatabaseConfig config = new DatabaseConfig();

        System.out.print("Host do banco (localhost): ");
        String host = scanner.nextLine().trim();
        config.setHost(host.isEmpty() ? "localhost" : host);

        System.out.print("Porta (5432): ");
        String port = scanner.nextLine().trim();
        config.setPort(port.isEmpty() ? 5432 : Integer.parseInt(port));

        System.out.print("Nome do banco: ");
        config.setDatabase(scanner.nextLine().trim());

        System.out.print("Usuário: ");
        config.setUsername(scanner.nextLine().trim());

        System.out.print("Senha: ");
        config.setPassword(scanner.nextLine().trim());

        System.out.print("Versão do Java (8/17) [17]: ");
        String javaVersion = scanner.nextLine().trim();
        config.setJavaVersion(javaVersion.isEmpty() ? 17 : Integer.parseInt(javaVersion));

        System.out.print("Diretório de saída [src/main/java]: ");
        String outputDir = scanner.nextLine().trim();
        config.setOutputDirectory(outputDir.isEmpty() ? "src/main/java" : outputDir);

        System.out.print("Package base [com.example.entity]: ");
        String basePackage = scanner.nextLine().trim();
        config.setBasePackage(basePackage.isEmpty() ? "com.example.entity" : basePackage);

        // Nova pergunta para singularização automática
        System.out.print("Usar nomeação automática de classes (singularização)? (s/n) [s]: ");
        String autoSingularize = scanner.nextLine().trim().toLowerCase();
        config.setUseAutomaticSingularization(!autoSingularize.equals("n") && !autoSingularize.equals("nao"));

        // Nova pergunta para tratamento de Foreign Keys
        System.out.println("\nComo tratar as Foreign Keys (chaves estrangeiras)?");
        System.out.println("1. Perguntar para cada uma (interativo)");
        System.out.println("2. Tratar todas como Relacionamento JPA (@ManyToOne)");
        System.out.println("3. Tratar todas como Coluna Simples (campo ID)");
        System.out.print("Escolha uma opção [1]: ");
        String fkChoice = scanner.nextLine().trim();
        switch (fkChoice) {
            case "2":
                config.setForeignKeyStrategy("relationship");
                break;
            case "3":
                config.setForeignKeyStrategy("column");
                break;
            default:
                config.setForeignKeyStrategy("interactive");
                break;
        }


        System.out.print("\nDeseja salvar esta configuração? (s/n): ");
        String save = scanner.nextLine().trim().toLowerCase();

        if (save.equals("s") || save.equals("sim") || save.isEmpty()) {
            configManager.saveConfig(config);
            System.out.println("Configuração salva com sucesso!");
        }

        return config;
    }

    private static void runInteractiveMode(DatabaseConfig config) throws Exception {
        System.out.println("\n=== Modo Interativo ===");

        // Connect to database
        inspector.connect();

        // List available schemas
        List<String> schemas = inspector.getSchemas();
        System.out.println("\nSchemas disponíveis:");
        for (int i = 0; i < schemas.size(); i++) {
            System.out.println((i + 1) + ". " + schemas.get(i));
        }

        System.out.print("\nEscolha o schema (número ou nome): ");
        String schemaChoice = scanner.nextLine().trim();

        String selectedSchema;
        try {
            int index = Integer.parseInt(schemaChoice) - 1;
            selectedSchema = schemas.get(index);
        } catch (NumberFormatException e) {
            selectedSchema = schemaChoice;
        }

        // List tables in selected schema
        List<String> tables = inspector.getTables(selectedSchema);
        System.out.println("\nTabelas disponíveis no schema '" + selectedSchema + "':");
        for (int i = 0; i < tables.size(); i++) {
            System.out.println((i + 1) + ". " + tables.get(i));
        }

        System.out.println("\nOpções:");
        System.out.println("1. Todas as tabelas");
        System.out.println("2. Tabelas específicas (números separados por vírgula)");
        System.out.print("Escolha (1 ou 2): ");

        String optionChoice = scanner.nextLine().trim();
        List<String> selectedTables;

        if (optionChoice.equals("1")) {
            selectedTables = tables;
            System.out.println("✓ Selecionadas todas as " + tables.size() + " tabelas do schema.");
        } else if (optionChoice.equals("2")) {
            selectedTables = new ArrayList<>();
            System.out.print("Digite os números das tabelas separados por vírgula (ex: 1,3,5): ");
            String tableIndices = scanner.nextLine().trim();

            if (tableIndices.isEmpty()) {
                System.out.println("⚠ Nenhuma tabela selecionada. Encerrando.");
                return;
            }

            String[] indices = tableIndices.split(",");
            for (String index : indices) {
                try {
                    int i = Integer.parseInt(index.trim()) - 1;
                    if (i >= 0 && i < tables.size()) {
                        selectedTables.add(tables.get(i));
                        System.out.println("✓ Adicionada: " + tables.get(i));
                    } else {
                        System.out.println("⚠ Índice inválido ignorado: " + (i + 1));
                    }
                } catch (NumberFormatException e) {
                    System.out.println("⚠ Valor inválido ignorado: " + index.trim());
                }
            }

            if (selectedTables.isEmpty()) {
                System.out.println("⚠ Nenhuma tabela válida selecionada. Encerrando.");
                return;
            }

            System.out.println("✓ Total de " + selectedTables.size() + " tabela(s) selecionada(s).");
        } else {
            System.out.println("⚠ Opção inválida. Encerrando.");
            return;
        }

        // Process selected tables
        processSelectedTables(selectedSchema, selectedTables, config);
    }

    private static void runBatchMode(CommandLineArgs args, DatabaseConfig config) throws Exception {
        inspector.connect();

        if (args.table != null) {
            // Process single table
            String[] parts = args.table.split("\\.");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Formato de tabela deve ser: schema.tabela");
            }

            List<String> tables = Collections.singletonList(parts[1]);
            processSelectedTables(parts[0], tables, config);

        } else if (args.schema != null) {
            // Process all tables in schema
            List<String> tables = inspector.getTables(args.schema);
            processSelectedTables(args.schema, tables, config);
        }
    }

    private static void processSelectedTables(String schema, List<String> initialTableNames, DatabaseConfig config) throws Exception {
        System.out.println("\n=== Processando Tabelas ===");

        Map<String, String> classNames = new HashMap<>();
        Map<String, Map<String, String>> foreignKeyHandling = new HashMap<>();
        List<String> allTableNames = new ArrayList<>(initialTableNames);
        List<String> configuredTables = new ArrayList<>();

        boolean autoNameClasses = config.isUseAutomaticSingularization();
        // Apenas pergunta se estiver em modo interativo de verdade
        if (System.console() != null && config.isUseAutomaticSingularization()) {
            System.out.print("\nNomear classes automaticamente (singularizando o nome das tabelas)? (s/n) [s]: ");
            String choice = scanner.nextLine().trim().toLowerCase();
            if (!choice.isEmpty()) {
                autoNameClasses = !choice.equals("n") && !choice.equals("nao");
            }
        }

        for (int i = 0; i < allTableNames.size(); i++) {
            String tableName = allTableNames.get(i);
            if (configuredTables.contains(tableName)) {
                continue;
            }

            TableInfo tableInfo = inspector.getTableInfo(schema, tableName);

            String pascalCaseName = Inflector.toPascalCase(tableName);
            String singularName = Inflector.singularize(pascalCaseName);

            if (autoNameClasses) {
                classNames.put(tableName, singularName);
                System.out.println("\nTabela: " + tableName + " -> Classe gerada: " + singularName);
            } else {
                System.out.print("\nTabela: " + tableName + " -> Nome da Classe [" + singularName + "]: ");
                String className = scanner.nextLine().trim();
                classNames.put(tableName, className.isEmpty() ? singularName : className);
            }

            if (tableInfo.getForeignKeys() != null && !tableInfo.getForeignKeys().isEmpty()) {
                System.out.println("Foreign Keys encontradas para a tabela: " + tableName);
                Map<String, String> fkHandling = foreignKeyHandling.getOrDefault(tableName, new HashMap<>());

                for (var fk : tableInfo.getForeignKeys()) {
                    String handling;
                    switch (config.getForeignKeyStrategy()) {
                        case "relationship":
                            System.out.println("  " + fk.getColumnName() + " -> " + fk.getReferencedTable() + " (Tratando como Relacionamento JPA por configuração)");
                            handling = "relationship";
                            break;
                        case "column":
                            System.out.println("  " + fk.getColumnName() + " -> " + fk.getReferencedTable() + " (Tratando como Coluna Simples por configuração)");
                            handling = "column";
                            break;
                        default: // "interactive"
                            System.out.println("  " + fk.getColumnName() + " -> " + fk.getReferencedSchema() + "." + fk.getReferencedTable());
                            System.out.print("  Tratamento (1=coluna simples, 2=relacionamento JPA) [2]: ");
                            String choice = scanner.nextLine().trim();
                            handling = choice.equals("1") ? "column" : "relationship";
                            break;
                    }

                    fkHandling.put(fk.getColumnName(), handling);

                    if (handling.equals("relationship")) {
                        String referencedTable = fk.getReferencedTable();
                        if (!allTableNames.contains(referencedTable)) {
                            allTableNames.add(referencedTable);
                            System.out.println("-> Tabela relacionada '" + referencedTable + "' adicionada para geração.");
                        }
                    }
                }
                foreignKeyHandling.put(tableName, fkHandling);
            }
            configuredTables.add(tableName);
        }

        System.out.println("\n=== Gerando Classes ===");
        List<String> generatedFiles = new ArrayList<>();

        for (String tableName : allTableNames) {
            TableInfo tableInfo = inspector.getTableInfo(schema, tableName);
            String className = classNames.get(tableName);

            if (className == null) {
                String pascalCaseName = Inflector.toPascalCase(tableName);
                className = Inflector.singularize(pascalCaseName);
                classNames.put(tableName, className);
            }
            Map<String, String> fkHandling = foreignKeyHandling.getOrDefault(tableName, new HashMap<>());

            String filePath = generator.generateEntity(tableInfo, className, fkHandling, classNames);
            generatedFiles.add(filePath);

            System.out.println("✓ " + className + ".java gerado em: " + filePath);
        }

        System.out.println("\n=== Resumo ===");
        System.out.println("Total de classes geradas: " + generatedFiles.size());
        System.out.println("Arquivos:");
        generatedFiles.forEach(file -> System.out.println("  " + file));

        inspector.disconnect();
    }

    private static class CommandLineArgs {
        String schema;
        String table;
        String configFile;
        String outputDir;
        boolean interactive = false;
    }
}