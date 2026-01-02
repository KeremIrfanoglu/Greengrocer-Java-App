package com.greengrocer.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection adapter using the Adapter design pattern.
 * Provides a centralized connection management for MySQL database access.
 * 
 * <p>
 * Configuration can be provided via environment variables:
 * </p>
 * <ul>
 * <li>DB_URL - Database URL (default:
 * jdbc:mysql://localhost:3306/cmpe343_project)</li>
 * <li>DB_USER - Database username (default: myuser)</li>
 * <li>DB_PASS - Database password (default: 1234)</li>
 * </ul>
 * 
 * @author Group10
 * @version 1.0
 */
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
