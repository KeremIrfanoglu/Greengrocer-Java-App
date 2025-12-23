package com.greengrocer.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseAdapter {
    private static final String DB_URL = System.getenv().getOrDefault("DB_URL",
            "jdbc:mysql://localhost:3306/cmpe343_project");
    private static final String USER = System.getenv().getOrDefault("DB_USER", "myuser");
    private static final String PASS = System.getenv().getOrDefault("DB_PASS", "1234");

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // Log the error message without printing full stack trace to console in
            // production
            System.err.println("Database Driver Error: " + e.getMessage());
            throw new SQLException("MySQL JDBC Driver not found.", e);
        }
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }
}
