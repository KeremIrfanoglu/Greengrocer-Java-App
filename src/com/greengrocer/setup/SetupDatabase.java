package com.greengrocer.setup;

import com.greengrocer.dao.DatabaseAdapter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database setup utility - creates tables and seeds initial users.
 * Run this once before using the application.
 */
public class SetupDatabase {
        public static void main(String[] args) {
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
                                                "phone VARCHAR(20), " +
                                                "g_points DOUBLE DEFAULT 0.0)");

                                // ProductInfo
                                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ProductInfo (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                                "name VARCHAR(100) NOT NULL, " +
                                                "type ENUM('Vegetable', 'Fruit', 'Dairy', 'Bakery', 'Meat', 'Beverages', 'Snacks') NOT NULL, "
                                                +
                                                "price DOUBLE NOT NULL, " +
                                                "cost_price DOUBLE DEFAULT 0, " +
                                                "stock DOUBLE NOT NULL, " +
                                                "threshold DOUBLE NOT NULL, " +
                                                "image LONGBLOB)");

                                // CustomerFavorites table
                                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Favorites (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                                "user_id INT NOT NULL, " +
                                                "product_id INT NOT NULL, " +
                                                "UNIQUE KEY unique_favorite (user_id, product_id), " +
                                                "FOREIGN KEY (user_id) REFERENCES UserInfo(id), " +
                                                "FOREIGN KEY (product_id) REFERENCES ProductInfo(id))");

                                // OrderInfo
                                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS OrderInfo (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                                "customer_id INT, " +
                                                "carrier_id INT, " +
                                                "order_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                                                "total_amount DOUBLE, " +
                                                "status VARCHAR(30) DEFAULT 'Pending', " +
                                                "FOREIGN KEY (customer_id) REFERENCES UserInfo(id), " +
                                                "FOREIGN KEY (carrier_id) REFERENCES UserInfo(id))");

                                // OrderItems (stores price and cost at time of purchase)
                                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS OrderItems (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                                "order_id INT, " +
                                                "product_id INT, " +
                                                "quantity INT, " +
                                                "price_at_purchase DOUBLE, " +
                                                "cost_at_purchase DOUBLE, " +
                                                "FOREIGN KEY (order_id) REFERENCES OrderInfo(id), " +
                                                "FOREIGN KEY (product_id) REFERENCES ProductInfo(id))");

                                // Coupons
                                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Coupons (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                                "code VARCHAR(50) UNIQUE NOT NULL, " +
                                                "discount_percent DOUBLE NOT NULL, " +
                                                "max_uses INT NOT NULL, " +
                                                "current_uses INT DEFAULT 0, " +
                                                "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                                "is_active BOOLEAN DEFAULT TRUE)");

                                // CouponUsage
                                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS CouponUsage (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                                "coupon_id INT NOT NULL, " +
                                                "user_id INT NOT NULL, " +
                                                "order_id INT NOT NULL, " +
                                                "used_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                                "discount_amount DOUBLE, " +
                                                "FOREIGN KEY (coupon_id) REFERENCES Coupons(id), " +
                                                "FOREIGN KEY (user_id) REFERENCES UserInfo(id))");

                                // Messages (for customer-owner communication)
                                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Messages (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                                "sender_id INT NOT NULL, " +
                                                "receiver_id INT NOT NULL, " +
                                                "subject VARCHAR(200), " +
                                                "content TEXT NOT NULL, " +
                                                "sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                                "is_read BOOLEAN DEFAULT FALSE, " +
                                                "FOREIGN KEY (sender_id) REFERENCES UserInfo(id), " +
                                                "FOREIGN KEY (receiver_id) REFERENCES UserInfo(id))");

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
