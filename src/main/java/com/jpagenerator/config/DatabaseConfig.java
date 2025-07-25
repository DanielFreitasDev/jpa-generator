package com.jpagenerator.config;

public class DatabaseConfig {
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

    // Getters and Setters
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(int javaVersion) {
        this.javaVersion = javaVersion;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public boolean isGenerateAuditFields() {
        return generateAuditFields;
    }

    public void setGenerateAuditFields(boolean generateAuditFields) {
        this.generateAuditFields = generateAuditFields;
    }

    public boolean isUseLombok() {
        return useLombok;
    }

    public void setUseLombok(boolean useLombok) {
        this.useLombok = useLombok;
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
                ", javaVersion=" + javaVersion +
                ", outputDirectory='" + outputDirectory + '\'' +
                ", basePackage='" + basePackage + '\'' +
                '}';
    }
}