package com.greengrocer.main;

import com.greengrocer.dao.DatabaseAdapter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SetupDatabase {
        public static void main(String[] args) {
                // Try connecting directly to the database. Assumes DB exists if user can't
                // create it.
                // Connect to MySQL Server to create DB if not exists
                String rootUrl = "jdbc:mysql://localhost:3306/";
                String dbName = "cmpe343_project";
                String dbUrl = "jdbc:mysql://localhost:3306/" + dbName;
                String user = "myuser";
                String pass = "1234";

                try {
                        try (Connection rootConn = DriverManager.getConnection(rootUrl, user, pass);
                                        Statement rootStmt = rootConn.createStatement()) {
                                rootStmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
                                System.out.println("Database '" + dbName + "' checked/created.");
                        }

                        // Connect to the database
                        try (Connection conn = DriverManager.getConnection(dbUrl, user, pass);
                                        Statement stmt = conn.createStatement()) {

                                System.out.println(
                                                "Connected to 'cmpe343_project'. Creating tables if they don't exist...");

                                // UserInfo
                                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS UserInfo (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                                "username VARCHAR(50) UNIQUE NOT NULL, " +
                                                "password VARCHAR(255) NOT NULL, " +
                                                "role ENUM('customer', 'carrier', 'owner') NOT NULL, " +
                                                "first_name VARCHAR(100), " +
                                                "last_name VARCHAR(100), " +
                                                "address VARCHAR(255), " +
                                                "phone VARCHAR(20))");

                                // ProductInfo
                                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ProductInfo (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                                "name VARCHAR(100) NOT NULL, " +
                                                "type ENUM('Vegetable', 'Fruit') NOT NULL, " +
                                                "price DOUBLE NOT NULL, " +
                                                "stock DOUBLE NOT NULL, " +
                                                "threshold DOUBLE NOT NULL, " +
                                                "imagelocation LONGBLOB)"); // BLOB for images

                                // OrderInfo
                                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS OrderInfo (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                                "customer_id INT, " +
                                                "carrier_id INT, " +
                                                "order_time DATETIME, " +
                                                "delivery_time DATETIME, " +
                                                "status ENUM('Pending', 'In Progress', 'Delivered', 'Cancelled'), " +
                                                "total_cost DOUBLE, " +
                                                "invoice LONGTEXT, " + // CLOB in MySQL is TEXT/LONGTEXT
                                                "FOREIGN KEY (customer_id) REFERENCES UserInfo(id), " +
                                                "FOREIGN KEY (carrier_id) REFERENCES UserInfo(id))");

                                // OrderItems
                                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS OrderItems (" +
                                                "order_id INT, " +
                                                "product_id INT, " +
                                                "amount DOUBLE, " +
                                                "purchase_price DOUBLE, " +
                                                "FOREIGN KEY (order_id) REFERENCES OrderInfo(id), " +
                                                "FOREIGN KEY (product_id) REFERENCES ProductInfo(id))");

                                // Seed Initial Users
                                seedUser(stmt, "cust", "cust", "customer");
                                seedUser(stmt, "carr", "carr", "carrier");
                                seedUser(stmt, "own", "own", "owner");

                                System.out.println("Tables created and initial data seeded.");
                        }

                } catch (SQLException e) {
                        System.err.println("Database Connection/Setup Error: " + e.getMessage());
                        e.printStackTrace();
                }
        }

        private static void seedUser(Statement stmt, String username, String password, String role)
                        throws SQLException {
                String sql = "INSERT IGNORE INTO UserInfo (username, password, role) VALUES ('" + username + "', '"
                                + password + "', '" + role + "')";
                stmt.executeUpdate(sql);
        }
}
