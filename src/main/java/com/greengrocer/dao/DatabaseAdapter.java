package com.greengrocer.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Database connection adapter using the Adapter design pattern.
 * Provides a centralized connection management for MySQL database access.
 * 
 * <p>
 * Credentials are loaded from {@code db.properties} file in the project root.
 * If the file is not found, falls back to environment variables (DB_URL,
 * DB_USER, DB_PASS).
 * </p>
 * 
 * @author Group10
 * @version 1.0
 */
public class DatabaseAdapter {
    private static final String DB_URL;
    private static final String USER;
    private static final String PASS;

    static {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("db.properties")) {
            props.load(fis);
        } catch (IOException e) {
            // db.properties not found, will use environment variables
        }

        DB_URL = props.getProperty("DB_URL",
                System.getenv().getOrDefault("DB_URL",
                        "jdbc:mysql://localhost:3306/railway?useSSL=false&allowPublicKeyRetrieval=true"));
        USER = props.getProperty("DB_USER",
                System.getenv().getOrDefault("DB_USER", "root"));
        PASS = props.getProperty("DB_PASS",
                System.getenv().getOrDefault("DB_PASS", ""));
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Database Driver Error: " + e.getMessage());
            throw new SQLException("MySQL JDBC Driver not found.", e);
        }
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }
}
