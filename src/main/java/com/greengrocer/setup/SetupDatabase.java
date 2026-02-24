package com.greengrocer.setup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Handles the initialization of the database schema and default tables.
 * Drops existing tables and recreates them with the correct schema.
 * Loads credentials from db.properties file.
 */
public class SetupDatabase {

        private static final String DB_URL;
        private static final String USER;
        private static final String PASS;

        static {
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream("db.properties")) {
                        props.load(fis);
                } catch (IOException e) {
                        System.err.println("Warning: db.properties not found. Using environment variables.");
                }

                DB_URL = props.getProperty("DB_URL",
                                System.getenv().getOrDefault("DB_URL",
                                                "jdbc:mysql://localhost:3306/railway?useSSL=false&allowPublicKeyRetrieval=true"));
                USER = props.getProperty("DB_USER",
                                System.getenv().getOrDefault("DB_USER", "root"));
                PASS = props.getProperty("DB_PASS",
                                System.getenv().getOrDefault("DB_PASS", ""));
        }

        public static void main(String[] args) {
                try {
                        System.out.println("Connecting to database...");
                        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                                        Statement stmt = conn.createStatement()) {

                                System.out.println("Connected successfully!\n");

                                // Drop all tables in correct order (respect foreign keys)
                                System.out.println("Dropping existing tables...");
                                stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                                stmt.executeUpdate("DROP TABLE IF EXISTS CouponUsage");
                                stmt.executeUpdate("DROP TABLE IF EXISTS Coupons");
                                stmt.executeUpdate("DROP TABLE IF EXISTS CarrierRatings");
                                stmt.executeUpdate("DROP TABLE IF EXISTS Messages");
                                stmt.executeUpdate("DROP TABLE IF EXISTS Cart");
                                stmt.executeUpdate("DROP TABLE IF EXISTS CustomerFavorites");
                                stmt.executeUpdate("DROP TABLE IF EXISTS OrderItems");
                                stmt.executeUpdate("DROP TABLE IF EXISTS OrderInfo");
                                stmt.executeUpdate("DROP TABLE IF EXISTS Favorites");
                                stmt.executeUpdate("DROP TABLE IF EXISTS Suppliers");
                                stmt.executeUpdate("DROP TABLE IF EXISTS ProductInfo");
                                stmt.executeUpdate("DROP TABLE IF EXISTS UserInfo");
                                stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
                                System.out.println("All existing tables dropped.\n");

                                // ==================== UserInfo ====================
                                // Columns used by: UserDAO, SeedData, ReportDAO, AnalyticsDAO, CarrierRatingDAO
                                stmt.executeUpdate("CREATE TABLE UserInfo (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY," +
                                                "username VARCHAR(50) UNIQUE NOT NULL," +
                                                "password VARCHAR(100) NOT NULL," +
                                                "role VARCHAR(20) NOT NULL," +
                                                "first_name VARCHAR(50)," +
                                                "last_name VARCHAR(50)," +
                                                "address TEXT," +
                                                "phone VARCHAR(20)," +
                                                "g_points DECIMAL(10, 2) DEFAULT 0.0" +
                                                ")");
                                System.out.println("Table 'UserInfo' created.");

                                // ==================== ProductInfo ====================
                                // Columns used by: ProductDAO, OrderDAO, AnalyticsDAO
                                stmt.executeUpdate("CREATE TABLE ProductInfo (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY," +
                                                "name VARCHAR(100) UNIQUE NOT NULL," +
                                                "type VARCHAR(50)," +
                                                "price DECIMAL(10, 2) NOT NULL," +
                                                "cost_price DECIMAL(10, 2) DEFAULT 0.0," +
                                                "stock DECIMAL(10, 2) DEFAULT 0.0," +
                                                "threshold DECIMAL(10, 2) DEFAULT 10.0," +
                                                "image LONGBLOB," +
                                                "unit_type VARCHAR(20) DEFAULT 'kg'" +
                                                ")");
                                System.out.println("Table 'ProductInfo' created.");

                                // ==================== Favorites ====================
                                // Columns used by: FavoriteDAO
                                stmt.executeUpdate("CREATE TABLE Favorites (" +
                                                "user_id INT," +
                                                "product_id INT," +
                                                "PRIMARY KEY (user_id, product_id)," +
                                                "FOREIGN KEY (user_id) REFERENCES UserInfo(id)," +
                                                "FOREIGN KEY (product_id) REFERENCES ProductInfo(id)" +
                                                ")");
                                System.out.println("Table 'Favorites' created.");

                                // ==================== CustomerFavorites ====================
                                // Columns used by: FavoritesDAO (customer_id, product_id)
                                // Also referenced by: ProductDAO.deleteProduct
                                stmt.executeUpdate("CREATE TABLE CustomerFavorites (" +
                                                "customer_id INT," +
                                                "product_id INT," +
                                                "PRIMARY KEY (customer_id, product_id)," +
                                                "FOREIGN KEY (customer_id) REFERENCES UserInfo(id)," +
                                                "FOREIGN KEY (product_id) REFERENCES ProductInfo(id)" +
                                                ")");
                                System.out.println("Table 'CustomerFavorites' created.");

                                // ==================== OrderInfo ====================
                                // Columns used by: OrderDAO.createOrder, getOrdersByCustomer, getPendingOrders,
                                // getActiveDeliveries, getCompletedDeliveriesByCarrier, saveInvoice,
                                // cancelOrder, completeDelivery, getAllOrders, getOrdersByStatus
                                stmt.executeUpdate("CREATE TABLE OrderInfo (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY," +
                                                "customer_id INT," +
                                                "carrier_id INT," +
                                                "order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                                                "status VARCHAR(50) DEFAULT 'Pending'," +
                                                "total_amount DECIMAL(10, 2)," +
                                                "delivery_date TIMESTAMP NULL," +
                                                "delivered_at TIMESTAMP NULL," +
                                                "invoice_data LONGBLOB," +
                                                "FOREIGN KEY (customer_id) REFERENCES UserInfo(id)," +
                                                "FOREIGN KEY (carrier_id) REFERENCES UserInfo(id)" +
                                                ")");
                                System.out.println("Table 'OrderInfo' created.");

                                // ==================== OrderItems ====================
                                // Columns used by: OrderDAO.createOrder (product_id, quantity,
                                // price_at_purchase, cost_at_purchase)
                                // OrderDAO.cancelOrder (product_id, quantity)
                                // OrderDAO.getOrderDetailsText (product_id via JOIN)
                                stmt.executeUpdate("CREATE TABLE OrderItems (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY," +
                                                "order_id INT," +
                                                "product_id INT," +
                                                "quantity DECIMAL(10, 2)," +
                                                "price_at_purchase DECIMAL(10, 2)," +
                                                "cost_at_purchase DECIMAL(10, 2) DEFAULT 0.0," +
                                                "FOREIGN KEY (order_id) REFERENCES OrderInfo(id)," +
                                                "FOREIGN KEY (product_id) REFERENCES ProductInfo(id)" +
                                                ")");
                                System.out.println("Table 'OrderItems' created.");

                                // ==================== Coupons ====================
                                // Columns used by: CouponDAO.createCoupon (code, discount_percent, max_uses)
                                // CouponDAO.createCouponFromResultSet (id, code, discount_percent, max_uses,
                                // current_uses, created_date, is_active)
                                // CouponDAO.applyCoupon (current_uses, max_uses, is_active)
                                stmt.executeUpdate("CREATE TABLE Coupons (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY," +
                                                "code VARCHAR(50) UNIQUE NOT NULL," +
                                                "discount_percent DECIMAL(5, 2) NOT NULL," +
                                                "max_uses INT DEFAULT 1," +
                                                "current_uses INT DEFAULT 0," +
                                                "is_active BOOLEAN DEFAULT TRUE," +
                                                "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                                                ")");
                                System.out.println("Table 'Coupons' created.");

                                // ==================== CouponUsage ====================
                                // Columns used by: CouponDAO.applyCoupon (coupon_id, user_id, order_id,
                                // discount_amount)
                                // CouponDAO.getCouponUsageHistory (used_date, discount_amount, order_id)
                                // CouponDAO.hasUserUsedCoupon (user_id, coupon_id)
                                stmt.executeUpdate("CREATE TABLE CouponUsage (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY," +
                                                "coupon_id INT," +
                                                "user_id INT," +
                                                "order_id INT," +
                                                "discount_amount DECIMAL(10, 2)," +
                                                "used_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                                                "FOREIGN KEY (coupon_id) REFERENCES Coupons(id)," +
                                                "FOREIGN KEY (user_id) REFERENCES UserInfo(id)," +
                                                "FOREIGN KEY (order_id) REFERENCES OrderInfo(id)" +
                                                ")");
                                System.out.println("Table 'CouponUsage' created.");

                                // ==================== Messages ====================
                                // Columns used by: MessageDAO.sendMessage (sender_id, receiver_id, subject,
                                // content)
                                // MessageDAO.getInbox/getSentMessages (id, sender_id, receiver_id, subject,
                                // content, sent_at, is_read)
                                // MessageDAO.getUnreadCount (receiver_id, is_read)
                                stmt.executeUpdate("CREATE TABLE Messages (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY," +
                                                "sender_id INT," +
                                                "receiver_id INT," +
                                                "subject VARCHAR(200)," +
                                                "content TEXT," +
                                                "sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                                                "is_read BOOLEAN DEFAULT FALSE," +
                                                "FOREIGN KEY (sender_id) REFERENCES UserInfo(id)," +
                                                "FOREIGN KEY (receiver_id) REFERENCES UserInfo(id)" +
                                                ")");
                                System.out.println("Table 'Messages' created.");

                                // ==================== CarrierRatings ====================
                                // Columns used by: CarrierRatingDAO
                                stmt.executeUpdate("CREATE TABLE CarrierRatings (" +
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
                                                ")");
                                System.out.println("Table 'CarrierRatings' created.");

                                // ==================== Suppliers ====================
                                // Columns used by: SupplierDAO
                                stmt.executeUpdate("CREATE TABLE Suppliers (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY," +
                                                "name VARCHAR(100) NOT NULL," +
                                                "contact_person VARCHAR(100)," +
                                                "email VARCHAR(100)," +
                                                "phone VARCHAR(20)," +
                                                "address TEXT," +
                                                "supplied_product_type VARCHAR(50)" +
                                                ")");
                                System.out.println("Table 'Suppliers' created.");

                                // ==================== Cart ====================
                                // Columns used by: CartDAO (user_id, product_id, quantity)
                                stmt.executeUpdate("CREATE TABLE Cart (" +
                                                "id INT AUTO_INCREMENT PRIMARY KEY," +
                                                "user_id INT," +
                                                "product_id INT," +
                                                "quantity DECIMAL(10, 2) DEFAULT 1.0," +
                                                "UNIQUE KEY unique_cart_item (user_id, product_id)," +
                                                "FOREIGN KEY (user_id) REFERENCES UserInfo(id)," +
                                                "FOREIGN KEY (product_id) REFERENCES ProductInfo(id)" +
                                                ")");
                                System.out.println("Table 'Cart' created.");

                                System.out.println("\n✅ All 12 tables created successfully!");
                        }

                } catch (Exception e) {
                        System.err.println("❌ Database setup failed:");
                        e.printStackTrace();
                }
        }
}
