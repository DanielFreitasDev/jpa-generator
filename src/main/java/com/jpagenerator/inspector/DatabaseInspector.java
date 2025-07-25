package com.jpagenerator.inspector;

import com.jpagenerator.config.DatabaseConfig;
import com.jpagenerator.model.ColumnInfo;
import com.jpagenerator.model.ForeignKeyInfo;
import com.jpagenerator.model.PrimaryKeyInfo;
import com.jpagenerator.model.SequenceInfo;
import com.jpagenerator.model.TableInfo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseInspector {
    private final DatabaseConfig config;
    private Connection connection;

    public DatabaseInspector(DatabaseConfig config) {
        this.config = config;
    }

    public void connect() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC driver not found", e);
        }

        connection = DriverManager.getConnection(
                config.getJdbcUrl(),
                config.getUsername(),
                config.getPassword()
        );

        System.out.println("Conectado ao banco: " + config.getJdbcUrl());
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("Conexão fechada.");
        }
    }

    public List<String> getSchemas() throws SQLException {
        List<String> schemas = new ArrayList<>();

        String query = """
                SELECT schema_name 
                FROM information_schema.schemata 
                WHERE schema_name NOT IN ('information_schema', 'pg_catalog', 'pg_toast')
                ORDER BY schema_name
                """;

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                schemas.add(rs.getString("schema_name"));
            }
        }

        return schemas;
    }

    public List<String> getTables(String schema) throws SQLException {
        List<String> tables = new ArrayList<>();

        String query = """
                SELECT table_name 
                FROM information_schema.tables 
                WHERE table_schema = ? AND table_type = 'BASE TABLE'
                ORDER BY table_name
                """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, schema);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tables.add(rs.getString("table_name"));
                }
            }
        }

        return tables;
    }

    public TableInfo getTableInfo(String schema, String tableName) throws SQLException {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setSchema(schema);
        tableInfo.setName(tableName);

        // Get columns
        tableInfo.setColumns(getColumns(schema, tableName));

        // Get primary key
        tableInfo.setPrimaryKey(getPrimaryKey(schema, tableName));

        // Get foreign keys
        tableInfo.setForeignKeys(getForeignKeys(schema, tableName));

        // Get sequences (for serial types)
        tableInfo.setSequences(getSequences(schema, tableName));

        return tableInfo;
    }

    private List<ColumnInfo> getColumns(String schema, String tableName) throws SQLException {
        List<ColumnInfo> columns = new ArrayList<>();

        String query = """
                SELECT 
                    c.column_name,
                    c.data_type,
                    c.character_maximum_length,
                    c.numeric_precision,
                    c.numeric_scale,
                    c.is_nullable,
                    c.column_default,
                    c.ordinal_position
                FROM information_schema.columns c
                WHERE c.table_schema = ? AND c.table_name = ?
                ORDER BY c.ordinal_position
                """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, schema);
            stmt.setString(2, tableName);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ColumnInfo column = new ColumnInfo();
                    column.setName(rs.getString("column_name"));
                    column.setDataType(rs.getString("data_type"));
                    column.setMaxLength(rs.getObject("character_maximum_length", Integer.class));
                    column.setPrecision(rs.getObject("numeric_precision", Integer.class));
                    column.setScale(rs.getObject("numeric_scale", Integer.class));
                    column.setNullable("YES".equals(rs.getString("is_nullable")));
                    column.setDefaultValue(rs.getString("column_default"));
                    column.setOrdinalPosition(rs.getInt("ordinal_position"));

                    columns.add(column);
                }
            }
        }

        return columns;
    }

    private PrimaryKeyInfo getPrimaryKey(String schema, String tableName) throws SQLException {
        String query = """
                SELECT 
                    kcu.column_name,
                    kcu.ordinal_position
                FROM information_schema.table_constraints tc
                JOIN information_schema.key_column_usage kcu 
                    ON tc.constraint_name = kcu.constraint_name 
                    AND tc.table_schema = kcu.table_schema
                WHERE tc.constraint_type = 'PRIMARY KEY'
                    AND tc.table_schema = ?
                    AND tc.table_name = ?
                ORDER BY kcu.ordinal_position
                """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, schema);
            stmt.setString(2, tableName);

            try (ResultSet rs = stmt.executeQuery()) {
                List<String> columnNames = new ArrayList<>();

                while (rs.next()) {
                    columnNames.add(rs.getString("column_name"));
                }

                if (!columnNames.isEmpty()) {
                    PrimaryKeyInfo pk = new PrimaryKeyInfo();
                    pk.setColumnNames(columnNames);
                    return pk;
                }
            }
        }

        return null;
    }

    private List<ForeignKeyInfo> getForeignKeys(String schema, String tableName) throws SQLException {
        List<ForeignKeyInfo> foreignKeys = new ArrayList<>();

        String query = """
                SELECT 
                    kcu.column_name,
                    ccu.table_schema AS referenced_schema,
                    ccu.table_name AS referenced_table,
                    ccu.column_name AS referenced_column,
                    tc.constraint_name
                FROM information_schema.table_constraints tc
                JOIN information_schema.key_column_usage kcu 
                    ON tc.constraint_name = kcu.constraint_name
                    AND tc.table_schema = kcu.table_schema
                JOIN information_schema.constraint_column_usage ccu 
                    ON ccu.constraint_name = tc.constraint_name
                    AND ccu.table_schema = tc.table_schema
                WHERE tc.constraint_type = 'FOREIGN KEY'
                    AND tc.table_schema = ?
                    AND tc.table_name = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, schema);
            stmt.setString(2, tableName);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ForeignKeyInfo fk = new ForeignKeyInfo();
                    fk.setColumnName(rs.getString("column_name"));
                    fk.setReferencedSchema(rs.getString("referenced_schema"));
                    fk.setReferencedTable(rs.getString("referenced_table"));
                    fk.setReferencedColumn(rs.getString("referenced_column"));
                    fk.setConstraintName(rs.getString("constraint_name"));

                    foreignKeys.add(fk);
                }
            }
        }

        return foreignKeys;
    }

    private List<SequenceInfo> getSequences(String schema, String tableName) throws SQLException {
        List<SequenceInfo> sequences = new ArrayList<>();

        String query = """
                SELECT 
                    c.column_name,
                    s.sequence_name,
                    s.sequence_schema
                FROM information_schema.columns c
                LEFT JOIN information_schema.sequences s 
                    ON s.sequence_name = c.table_name || '_' || c.column_name || '_seq'
                    AND s.sequence_schema = c.table_schema
                WHERE c.table_schema = ? 
                    AND c.table_name = ?
                    AND c.column_default LIKE 'nextval%'
                """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, schema);
            stmt.setString(2, tableName);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SequenceInfo seq = new SequenceInfo();
                    seq.setColumnName(rs.getString("column_name"));
                    seq.setSequenceName(rs.getString("sequence_name"));
                    seq.setSequenceSchema(rs.getString("sequence_schema"));

                    sequences.add(seq);
                }
            }
        }

        return sequences;
    }

    public boolean tableExists(String schema, String tableName) throws SQLException {
        String query = """
                SELECT 1 
                FROM information_schema.tables 
                WHERE table_schema = ? AND table_name = ? AND table_type = 'BASE TABLE'
                """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, schema);
            stmt.setString(2, tableName);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}