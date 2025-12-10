package com.greengrocer.dao;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReportDAO {

    /**
     * Get total sales grouped by product type (Vegetable/Fruit)
     */
    public Map<String, Double> getSalesByProductType() throws SQLException {
        Map<String, Double> data = new LinkedHashMap<>();
        String query = """
                SELECT p.type, SUM(oi.quantity * oi.price_at_purchase) as total
                FROM OrderItems oi
                JOIN ProductInfo p ON oi.product_id = p.id
                GROUP BY p.type
                """;

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                data.put(rs.getString("type"), rs.getDouble("total"));
            }
        }
        return data;
    }

    /**
     * Get daily revenue for the last 7 days
     */
    public Map<String, Double> getDailyRevenue() throws SQLException {
        Map<String, Double> data = new LinkedHashMap<>();
        String query = """
                SELECT DATE(order_date) as order_day, SUM(total_amount) as daily_total
                FROM OrderInfo
                WHERE order_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
                GROUP BY DATE(order_date)
                ORDER BY order_day ASC
                """;

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                data.put(rs.getString("order_day"), rs.getDouble("daily_total"));
            }
        }
        return data;
    }

    /**
     * Get total revenue from all orders
     */
    public double getTotalRevenue() throws SQLException {
        String query = "SELECT COALESCE(SUM(total_amount), 0) as total FROM OrderInfo";

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0;
    }

    /**
     * Get total number of orders
     */
    public int getTotalOrders() throws SQLException {
        String query = "SELECT COUNT(*) as count FROM OrderInfo";

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }

    /**
     * Get total number of products
     */
    public int getTotalProducts() throws SQLException {
        String query = "SELECT COUNT(*) as count FROM ProductInfo";

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }
}
