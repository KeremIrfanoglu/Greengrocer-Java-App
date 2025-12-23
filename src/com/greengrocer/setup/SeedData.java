package com.greengrocer.setup;

import com.greengrocer.dao.DatabaseAdapter;
import com.greengrocer.util.PasswordUtils;
import java.sql.*;

/**
 * Populates the database with sample data for testing.
 * Run SetupDatabase first, then run this class.
 */
public class SeedData {

    public static void main(String[] args) {
        System.out.println("🌱 Seeding database with sample data...\n");

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement()) {

            // Clear existing data
            System.out.println("Clearing existing data...");
            clearTables(stmt);

            // ========== USERS ==========
            System.out.println("Adding users...");
            seedUsers(conn);

            // ========== PRODUCTS ==========
            System.out.println("Adding products...");
            seedProducts(conn);

            // ========== ORDERS ==========
            System.out.println("Adding orders...");
            seedOrders(conn);

            // ========== FAVORITES ==========
            System.out.println("Adding favorites...");
            seedFavorites(conn);

            // ========== COUPONS ==========
            System.out.println("Adding coupons...");
            seedCoupons(conn);

            System.out.println("\nDatabase seeded successfully!");
            System.out.println("\nLogin credentials:");
            System.out.println("Customer: cust / cust");
            System.out.println("Carrier: carr / carr");
            System.out.println("Owner: own / own");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void clearTables(Statement stmt) throws SQLException {
        stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
        stmt.executeUpdate("TRUNCATE TABLE CouponUsage");
        stmt.executeUpdate("TRUNCATE TABLE Coupons");
        stmt.executeUpdate("TRUNCATE TABLE Favorites");
        stmt.executeUpdate("TRUNCATE TABLE OrderItems");
        stmt.executeUpdate("TRUNCATE TABLE OrderInfo");
        stmt.executeUpdate("TRUNCATE TABLE ProductInfo");
        stmt.executeUpdate("TRUNCATE TABLE UserInfo");
        stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
    }

    private static void seedUsers(Connection conn) throws SQLException {
        String sql = "INSERT INTO UserInfo (username, password, role, first_name, last_name, address, phone, g_points) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);

        // Required users
        addUser(ps, "cust", "cust", "customer", "Customer", "User", "123 Main St", "555-1000", 100.0);
        addUser(ps, "carr", "carr", "carrier", "Carrier", "User", "456 Delivery Rd", "555-2000", 0);
        addUser(ps, "own", "own", "owner", "Owner", "Admin", "789 HQ Building", "555-3000", 0);

        // Extra users
        addUser(ps, "john", "123456", "customer", "John", "Smith", "123 Oak Street", "555-0101", 150.0);
        addUser(ps, "emily", "123456", "customer", "Emily", "Johnson", "456 Maple Ave", "555-0102", 75.0);
        addUser(ps, "carrier1", "123456", "carrier", "Alex", "Fast", "111 Speed Blvd", "555-0201", 0);
        addUser(ps, "carrier2", "123456", "carrier", "Mike", "Quick", "222 Express Way", "555-0202", 0);

        ps.close();
    }

    private static void addUser(PreparedStatement ps, String username, String password, String role,
            String firstName, String lastName, String address, String phone, double gPoints) throws SQLException {
        ps.setString(1, username);
        ps.setString(2, PasswordUtils.hashPassword(password));
        ps.setString(3, role);
        ps.setString(4, firstName);
        ps.setString(5, lastName);
        ps.setString(6, address);
        ps.setString(7, phone);
        ps.setDouble(8, gPoints);
        ps.executeUpdate();
    }

    private static void seedProducts(Connection conn) throws SQLException {
        String sql = "INSERT INTO ProductInfo (name, type, price, cost_price, stock, threshold) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);

        // Vegetables
        addProduct(ps, "Tomato", "Vegetable", 2.99, 1.50, 150, 20);
        addProduct(ps, "Potato", "Vegetable", 1.49, 0.80, 200, 30);
        addProduct(ps, "Carrot", "Vegetable", 1.99, 1.00, 100, 15);
        addProduct(ps, "Onion", "Vegetable", 0.99, 0.50, 180, 25);
        addProduct(ps, "Spinach", "Vegetable", 2.49, 1.20, 8, 15); // Low stock!

        // Fruits
        addProduct(ps, "Apple", "Fruit", 3.49, 2.00, 120, 20);
        addProduct(ps, "Banana", "Fruit", 1.29, 0.60, 200, 30);
        addProduct(ps, "Orange", "Fruit", 2.99, 1.50, 90, 15);
        addProduct(ps, "Strawberry", "Fruit", 4.99, 3.00, 40, 10);

        // Dairy
        addProduct(ps, "Milk", "Dairy", 3.99, 2.50, 100, 20);
        addProduct(ps, "Cheese", "Dairy", 5.99, 4.00, 60, 10);
        addProduct(ps, "Yogurt", "Dairy", 2.49, 1.50, 80, 15);

        // Bakery
        addProduct(ps, "Bread", "Bakery", 2.99, 1.50, 50, 10);
        addProduct(ps, "Croissant", "Bakery", 1.99, 1.00, 30, 5);

        // Meat
        addProduct(ps, "Chicken Breast", "Meat", 8.99, 6.00, 30, 10);
        addProduct(ps, "Ground Beef", "Meat", 9.99, 7.00, 25, 8);

        // Beverages
        addProduct(ps, "Orange Juice", "Beverages", 4.99, 3.00, 60, 10);
        addProduct(ps, "Cola", "Beverages", 1.99, 1.00, 100, 20);

        // Snacks
        addProduct(ps, "Chips", "Snacks", 3.49, 2.00, 70, 15);
        addProduct(ps, "Cookies", "Snacks", 2.99, 1.80, 50, 10);

        ps.close();
    }

    private static void addProduct(PreparedStatement ps, String name, String type, double price,
            double costPrice, double stock, double threshold) throws SQLException {
        ps.setString(1, name);
        ps.setString(2, type);
        ps.setDouble(3, price);
        ps.setDouble(4, costPrice);
        ps.setDouble(5, stock);
        ps.setDouble(6, threshold);
        ps.executeUpdate();
    }

    private static void seedOrders(Connection conn) throws SQLException {
        String orderSql = "INSERT INTO OrderInfo (customer_id, carrier_id, order_date, total_amount, status) VALUES (?, ?, ?, ?, ?)";
        String itemSql = "INSERT INTO OrderItems (order_id, product_id, quantity, price_at_purchase) VALUES (?, ?, ?, ?)";

        PreparedStatement orderPs = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
        PreparedStatement itemPs = conn.prepareStatement(itemSql);

        // Order 1 - Delivered
        int order1 = addOrder(orderPs, 1, 2, "2025-12-08 10:30:00", 45.50, "Delivered");
        addOrderItem(itemPs, order1, 1, 3, 2.99);
        addOrderItem(itemPs, order1, 6, 2, 3.49);

        // Order 2 - Delivering
        int order2 = addOrder(orderPs, 1, 6, "2025-12-15 14:00:00", 32.99, "Delivering");
        addOrderItem(itemPs, order2, 10, 2, 3.99);
        addOrderItem(itemPs, order2, 13, 2, 2.99);

        // Order 3 - Pending
        int order3 = addOrder(orderPs, 4, null, "2025-12-18 09:00:00", 28.50, "Pending");
        addOrderItem(itemPs, order3, 8, 3, 2.99);
        addOrderItem(itemPs, order3, 17, 2, 4.99);

        // Order 4 - Delivered
        int order4 = addOrder(orderPs, 5, 6, "2025-12-10 11:00:00", 55.00, "Delivered");
        addOrderItem(itemPs, order4, 15, 2, 8.99);
        addOrderItem(itemPs, order4, 11, 3, 5.99);

        // Order 5 - Delivered by carrier2
        int order5 = addOrder(orderPs, 4, 7, "2025-12-12 16:00:00", 42.75, "Delivered");
        addOrderItem(itemPs, order5, 16, 1, 9.99);
        addOrderItem(itemPs, order5, 19, 3, 3.49);

        orderPs.close();
        itemPs.close();
    }

    private static int addOrder(PreparedStatement ps, int customerId, Integer carrierId, String date,
            double total, String status) throws SQLException {
        ps.setInt(1, customerId);
        if (carrierId != null) {
            ps.setInt(2, carrierId);
        } else {
            ps.setNull(2, java.sql.Types.INTEGER);
        }
        ps.setString(3, date);
        ps.setDouble(4, total);
        ps.setString(5, status);
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            return rs.getInt(1);
        }
        return -1;
    }

    private static void addOrderItem(PreparedStatement ps, int orderId, int productId, int quantity,
            double price) throws SQLException {
        ps.setInt(1, orderId);
        ps.setInt(2, productId);
        ps.setInt(3, quantity);
        ps.setDouble(4, price);
        ps.executeUpdate();
    }

    private static void seedFavorites(Connection conn) throws SQLException {
        String sql = "INSERT INTO Favorites (user_id, product_id) VALUES (?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);

        // cust's favorites
        addFavorite(ps, 1, 1);
        addFavorite(ps, 1, 5); // Spinach - low stock!
        addFavorite(ps, 1, 6);
        addFavorite(ps, 1, 10);

        // john's favorites
        addFavorite(ps, 4, 6);
        addFavorite(ps, 4, 9);
        addFavorite(ps, 4, 15);

        ps.close();
    }

    private static void addFavorite(PreparedStatement ps, int userId, int productId) throws SQLException {
        ps.setInt(1, userId);
        ps.setInt(2, productId);
        ps.executeUpdate();
    }

    private static void seedCoupons(Connection conn) throws SQLException {
        String sql = "INSERT INTO Coupons (code, discount_percent, max_uses, current_uses, is_active) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);

        addCoupon(ps, "WELCOME10", 10, 100, 5, true);
        addCoupon(ps, "SAVE20", 20, 50, 3, true);
        addCoupon(ps, "VIP50", 50, 10, 1, true);
        addCoupon(ps, "FRESH15", 15, 200, 10, true);

        ps.close();
    }

    private static void addCoupon(PreparedStatement ps, String code, double discount, int maxUses,
            int currentUses, boolean active) throws SQLException {
        ps.setString(1, code);
        ps.setDouble(2, discount);
        ps.setInt(3, maxUses);
        ps.setInt(4, currentUses);
        ps.setBoolean(5, active);
        ps.executeUpdate();
    }
}
