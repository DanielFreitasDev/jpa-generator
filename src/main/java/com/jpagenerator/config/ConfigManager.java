package com.jpagenerator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static final String CONFIG_DIR = "config";
    private static final String CONFIG_FILE = "database.json";
    private static final String CONFIG_PATH = CONFIG_DIR + File.separator + CONFIG_FILE;

    private final ObjectMapper objectMapper;

    public ConfigManager() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        File configDir = new File(CONFIG_DIR);
        if (!configDir.exists()) {
            if (configDir.mkdirs()) {
                logger.info("Diretório de configuração criado com sucesso: {}", CONFIG_DIR);
            } else {
                logger.error("Falha ao criar o diretório de configuração: {}", CONFIG_DIR);
                throw new RuntimeException("Não foi possível criar o diretório de configuração: " + CONFIG_DIR);
            }
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
                if (!parentDir.mkdirs()) {
                    logger.error("Falha ao criar diretórios para o caminho: {}", configPath);
                    throw new IOException("Não foi possível criar os diretórios pai: " + parentDir.getAbsolutePath());
                }
            }

            objectMapper.writeValue(configFile, config);
            logger.info("Configuração salva com sucesso em: {}", configPath);

        } catch (IOException e) {
            logger.error("Erro ao salvar configuração em {}: {}", configPath, e.getMessage(), e);
        }
    }
}