package com.greengrocer.setup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Handles the initialization of the database schema and default tables.
 * Checks if tables exist and creates them if they don't.
 */
public class SetupDatabase {
        // Database credentials
        private static final String DB_URL = "jdbc:mysql://localhost:3306/"; // Connect to server first
        private static final String DB_NAME = "cmpe343_project";
        private static final String FULL_DB_URL = "jdbc:mysql://localhost:3306/" + DB_NAME;
        private static final String USER = "myuser";
        private static final String PASS = "1234";

        /**
         * Main method to execute the database verification and setup process.
         * 
         * @param args Command line arguments (not used).
         */
        public static void main(String[] args) {
                try {
                        // 1. Create Database if not exists
                        try (Connection rootConn = DriverManager.getConnection(DB_URL, USER, PASS);
                                        Statement rootStmt = rootConn.createStatement()) {
                                rootStmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
                                System.out.println("Database '" + DB_NAME + "' checked/created.");
                        }

                        // 2. Connect to the specific database to create tables
                        try (Connection conn = DriverManager.getConnection(FULL_DB_URL, USER, PASS);
                                        Statement stmt = conn.createStatement()) {

                                // Create UserInfo Table
                                String createUsers = "CREATE TABLE IF NOT EXISTS UserInfo (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY," +
                                                "username VARCHAR(50) UNIQUE NOT NULL," +
                                                "password VARCHAR(100) NOT NULL," +
                                                "role VARCHAR(20) NOT NULL," +
                                                "full_name VARCHAR(100)," +
                                                "phone_number VARCHAR(20)," +
                                                "address TEXT," +
                                                "g_points DECIMAL(10, 2) DEFAULT 0.0" +
                                                ")";
                                stmt.executeUpdate(createUsers);
                                System.out.println("Table 'UserInfo' checked/created.");

                                // Create ProductInfo Table
                                String createProducts = "CREATE TABLE IF NOT EXISTS ProductInfo (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY," +
                                                "name VARCHAR(100) UNIQUE NOT NULL," +
                                                "type VARCHAR(50)," +
                                                "price DECIMAL(10, 2) NOT NULL," +
                                                "cost_price DECIMAL(10, 2) DEFAULT 0.0," +
                                                "stock DECIMAL(10, 2) DEFAULT 0.0," +
                                                "threshold DECIMAL(10, 2) DEFAULT 10.0," +
                                                "image LONGBLOB," +
                                                "unit_type VARCHAR(20) DEFAULT 'kg'" +
                                                ")";
                                stmt.executeUpdate(createProducts);
                                System.out.println("Table 'ProductInfo' checked/created.");

                                // Create Favorites Table
                                String createFavorites = "CREATE TABLE IF NOT EXISTS Favorites (" +
                                                "user_id INT," +
                                                "product_id INT," +
                                                "PRIMARY KEY (user_id, product_id)," +
                                                "FOREIGN KEY (user_id) REFERENCES UserInfo(id)," +
                                                "FOREIGN KEY (product_id) REFERENCES ProductInfo(id)" +
                                                ")";
                                stmt.executeUpdate(createFavorites);
                                System.out.println("Table 'Favorites' checked/created.");

                                // New Tables for Order System
                                String createOrders = "CREATE TABLE IF NOT EXISTS OrderInfo (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY," +
                                                "customer_username VARCHAR(50)," +
                                                "carrier_username VARCHAR(50)," +
                                                "status VARCHAR(50) DEFAULT 'Pending'," +
                                                "total_price DECIMAL(10, 2)," +
                                                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                                                "delivery_time TIMESTAMP NULL," +
                                                "FOREIGN KEY (customer_username) REFERENCES UserInfo(username)" +
                                                ")";
                                stmt.executeUpdate(createOrders);
                                System.out.println("Table 'OrderInfo' checked/created.");

                                String createOrderItems = "CREATE TABLE IF NOT EXISTS OrderItems (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY," +
                                                "order_id INT," +
                                                "product_name VARCHAR(100)," +
                                                "quantity DECIMAL(10, 2)," +
                                                "price_at_purchase DECIMAL(10, 2)," +
                                                "FOREIGN KEY (order_id) REFERENCES OrderInfo(id)" +
                                                ")";
                                stmt.executeUpdate(createOrderItems);
                                System.out.println("Table 'OrderItems' checked/created.");

                                // Coupon Table
                                String createCoupons = "CREATE TABLE IF NOT EXISTS Coupons (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY," +
                                                "code VARCHAR(50) UNIQUE NOT NULL," +
                                                "discount_percentage DECIMAL(5, 2) NOT NULL," +
                                                "valid_until TIMESTAMP," +
                                                "usage_limit INT DEFAULT 1," +
                                                "usage_count INT DEFAULT 0," +
                                                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                                                "is_active BOOLEAN DEFAULT TRUE" +
                                                ")";
                                stmt.executeUpdate(createCoupons);
                                System.out.println("Table 'Coupons' checked/created.");

                                // Coupon Usage History (Who used which coupon)
                                String createCouponUsage = "CREATE TABLE IF NOT EXISTS CouponUsage (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY," +
                                                "coupon_id INT," +
                                                "user_id INT," +
                                                "used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                                                "FOREIGN KEY (coupon_id) REFERENCES Coupons(id)," +
                                                "FOREIGN KEY (user_id) REFERENCES UserInfo(id)" +
                                                ")";
                                stmt.executeUpdate(createCouponUsage);
                                System.out.println("Table 'CouponUsage' checked/created.");

                                // Create Messages Table
                                String createMessages = "CREATE TABLE IF NOT EXISTS Messages (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY," +
                                                "sender_username VARCHAR(50)," +
                                                "receiver_username VARCHAR(50)," +
                                                "content TEXT," +
                                                "sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                                                "is_read BOOLEAN DEFAULT FALSE," +
                                                "FOREIGN KEY (sender_username) REFERENCES UserInfo(username)," +
                                                "FOREIGN KEY (receiver_username) REFERENCES UserInfo(username)" +
                                                ")";
                                stmt.executeUpdate(createMessages);
                                System.out.println("Table 'Messages' checked/created.");

                                // Create CarrierRatings Table
                                String createCarrierRatings = "CREATE TABLE IF NOT EXISTS CarrierRatings (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY," +
                                                "order_id INT," +
                                                "customer_id INT," +
                                                "carrier_id INT," +
                                                "rating INT CHECK (rating BETWEEN 1 AND 5)," +
                                                "comment TEXT," +
                                                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                                                "FOREIGN KEY (order_id) REFERENCES OrderInfo(id)," +
                                                "FOREIGN KEY (customer_id) REFERENCES UserInfo(id)," +
                                                "FOREIGN KEY (carrier_id) REFERENCES UserInfo(id)" +
                                                ")";
                                stmt.executeUpdate(createCarrierRatings);
                                System.out.println("Table 'CarrierRatings' checked/created.");

                                // Create Suppliers Table
                                String createSuppliers = "CREATE TABLE IF NOT EXISTS Suppliers (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY," +
                                                "name VARCHAR(100) NOT NULL," +
                                                "contact_person VARCHAR(100)," +
                                                "email VARCHAR(100)," +
                                                "phone VARCHAR(20)," +
                                                "address TEXT," +
                                                "supplied_product_type VARCHAR(50)" +
                                                ")";
                                stmt.executeUpdate(createSuppliers);
                                System.out.println("Table 'Suppliers' checked/created.");

                        }

                } catch (Exception e) {
                        e.printStackTrace();
                }
        }
}
