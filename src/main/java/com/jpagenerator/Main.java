package com.jpagenerator;

import com.jpagenerator.config.ConfigManager;
import com.jpagenerator.config.DatabaseConfig;
import com.jpagenerator.generator.CodeGenerator;
import com.jpagenerator.inspector.DatabaseInspector;
import com.jpagenerator.model.TableInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static ConfigManager configManager = new ConfigManager();
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
                runInteractiveMode();
            } else {
                runBatchMode(cmdArgs);
            }

        } catch (Exception e) {
            System.err.println("Erro durante a execução: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (scanner != null) scanner.close();
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

        System.out.print("Deseja salvar esta configuração? (s/n): ");
        String save = scanner.nextLine().trim().toLowerCase();

        if (save.equals("s") || save.equals("sim") || save.isEmpty()) {
            configManager.saveConfig(config);
            System.out.println("Configuração salva com sucesso!");
        }

        return config;
    }

    private static void runInteractiveMode() throws Exception {
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
        processSelectedTables(selectedSchema, selectedTables);
    }

    private static void runBatchMode(CommandLineArgs args) throws Exception {
        inspector.connect();

        if (args.table != null) {
            // Process single table
            String[] parts = args.table.split("\\.");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Formato de tabela deve ser: schema.tabela");
            }

            List<String> tables = Arrays.asList(parts[1]);
            processSelectedTables(parts[0], tables);

        } else if (args.schema != null) {
            // Process all tables in schema
            List<String> tables = inspector.getTables(args.schema);
            processSelectedTables(args.schema, tables);
        }
    }

    private static void processSelectedTables(String schema, List<String> initialTableNames) throws Exception {
        System.out.println("\n=== Processando Tabelas ===");

        Map<String, String> classNames = new HashMap<>();
        Map<String, Map<String, String>> foreignKeyHandling = new HashMap<>();
        List<String> allTableNames = new ArrayList<>(initialTableNames); // Start with initial tables
        List<String> configuredTables = new ArrayList<>(); // Keep track of tables we've asked config for

        for (int i = 0; i < allTableNames.size(); i++) {
            String tableName = allTableNames.get(i);
            if (configuredTables.contains(tableName)) {
                continue;
            }

            TableInfo tableInfo = inspector.getTableInfo(schema, tableName);

            // Configure class name
            String defaultClassName = toPascalCase(tableName);
            System.out.print("\nTabela: " + tableName + " -> Classe [" + defaultClassName + "]: ");
            String className = scanner.nextLine().trim();
            classNames.put(tableName, className.isEmpty() ? defaultClassName : className);

            // Handle foreign keys
            if (tableInfo.getForeignKeys() != null && !tableInfo.getForeignKeys().isEmpty()) {
                System.out.println("Foreign Keys encontradas para a tabela: " + tableName);
                Map<String, String> fkHandling = foreignKeyHandling.getOrDefault(tableName, new HashMap<>());

                for (var fk : tableInfo.getForeignKeys()) {
                    System.out.println("  " + fk.getColumnName() + " -> " + fk.getReferencedSchema() + "." + fk.getReferencedTable());
                    System.out.print("  Tratamento (1=coluna simples, 2=relacionamento JPA) [2]: ");
                    String choice = scanner.nextLine().trim();

                    String handling = choice.equals("1") ? "column" : "relationship";
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


        // Generate classes
        System.out.println("\n=== Gerando Classes ===");
        List<String> generatedFiles = new ArrayList<>();

        for (String tableName : allTableNames) {
            TableInfo tableInfo = inspector.getTableInfo(schema, tableName);
            String className = classNames.get(tableName);
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

    private static String toPascalCase(String input) {
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

    private static class CommandLineArgs {
        String schema;
        String table;
        String configFile;
        String outputDir;
        boolean interactive = false;
    }
}