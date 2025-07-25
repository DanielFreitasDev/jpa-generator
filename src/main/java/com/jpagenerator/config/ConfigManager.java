package com.jpagenerator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private static final String CONFIG_DIR = "config";
    private static final String CONFIG_FILE = "database.json";
    private static final String CONFIG_PATH = CONFIG_DIR + File.separator + CONFIG_FILE;

    private final ObjectMapper objectMapper;

    public ConfigManager() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Create config directory if it doesn't exist
        File configDir = new File(CONFIG_DIR);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
    }

    public DatabaseConfig loadConfig() {
        return loadConfig(CONFIG_PATH);
    }

    public DatabaseConfig loadConfig(String configPath) {
        File configFile = new File(configPath);

        if (!configFile.exists()) {
            return null;
        }

        try {
            return objectMapper.readValue(configFile, DatabaseConfig.class);
        } catch (IOException e) {
            System.err.println("Erro ao carregar configuração: " + e.getMessage());
            return null;
        }
    }

    public void saveConfig(DatabaseConfig config) {
        saveConfig(config, CONFIG_PATH);
    }

    public void saveConfig(DatabaseConfig config, String configPath) {
        try {
            File configFile = new File(configPath);

            // Create parent directories if they don't exist
            File parentDir = configFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            objectMapper.writeValue(configFile, config);
        } catch (IOException e) {
            System.err.println("Erro ao salvar configuração: " + e.getMessage());
        }
    }

    public boolean configExists() {
        return configExists(CONFIG_PATH);
    }

    public boolean configExists(String configPath) {
        return new File(configPath).exists();
    }

    public void deleteConfig() {
        deleteConfig(CONFIG_PATH);
    }

    public void deleteConfig(String configPath) {
        File configFile = new File(configPath);
        if (configFile.exists()) {
            configFile.delete();
        }
    }

    public DatabaseConfig createDefaultConfig() {
        DatabaseConfig config = new DatabaseConfig();
        config.setHost("localhost");
        config.setPort(5432);
        config.setDatabase("postgres");
        config.setUsername("postgres");
        config.setPassword("");
        config.setJavaVersion(17);
        config.setOutputDirectory("src/main/java");
        config.setBasePackage("com.example.entity");

        return config;
    }
}