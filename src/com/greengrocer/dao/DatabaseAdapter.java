package com.greengrocer.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseAdapter {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/cmpe343_project";
    private static final String USER = "myuser";
    private static final String PASS = "1234";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new SQLException("MySQL JDBC Driver not found.");
        }
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }
}
