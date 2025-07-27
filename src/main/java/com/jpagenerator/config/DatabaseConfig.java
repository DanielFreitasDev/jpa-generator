package com.jpagenerator.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatabaseConfig {
    // Getters and Setters
    private String host = "localhost";
    private int port = 5432;
    private String database;
    private String username;
    private String password;
    private int javaVersion = 17;
    private String outputDirectory = "src/main/java";
    private String basePackage = "com.example.entity";
    private boolean generateAuditFields = true;
    private boolean useLombok = true;
    private boolean useAutomaticSingularization = true;

    // Constructors
    public DatabaseConfig() {
    }

    public DatabaseConfig(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public String getJdbcUrl() {
        return String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
    }

    public boolean isJakartaMode() {
        return javaVersion >= 17;
    }

    @Override
    public String toString() {
        return "DatabaseConfig{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", database='" + database + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", javaVersion=" + javaVersion +
                ", outputDirectory='" + outputDirectory + '\'' +
                ", basePackage='" + basePackage + '\'' +
                ", generateAuditFields=" + generateAuditFields +
                ", useLombok=" + useLombok +
                ", useAutomaticSingularization=" + useAutomaticSingularization +
                '}';
    }
}