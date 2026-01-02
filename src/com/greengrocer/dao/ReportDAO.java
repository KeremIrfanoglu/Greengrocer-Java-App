package com.greengrocer.dao;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data Access Object for generating business reports.
 * Provides methods for sales analytics, revenue tracking, profit/loss analysis,
 * and carrier performance metrics.
 * 
 * <p>
 * Report Types:
 * </p>
 * <ul>
 * <li>Sales by product type (pie chart data)</li>
 * <li>Daily/Weekly/Monthly revenue trends</li>
 * <li>Profit/Loss per product</li>
 * <li>Cost analysis with margin calculation</li>
 * <li>Inventory cost valuation</li>
 * <li>Carrier leaderboard</li>
 * </ul>
 * 
 * @author Group10
 * @version 1.0
 */
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

    /**
     * Get profit/loss per product (based on sold items)
     * Returns: Product Name -> [Revenue, Cost, Profit]
     */
    public java.util.List<Object[]> getProfitLossPerProduct() throws SQLException {
        java.util.List<Object[]> data = new java.util.ArrayList<>();
        String query = """
                SELECT p.name,
                       SUM(oi.quantity * oi.price_at_purchase) as revenue,
                       SUM(oi.quantity * COALESCE(p.cost_price, 0)) as cost,
                       SUM(oi.quantity * (oi.price_at_purchase - COALESCE(p.cost_price, 0))) as profit
                FROM OrderItems oi
                JOIN ProductInfo p ON oi.product_id = p.id
                GROUP BY p.id, p.name
                ORDER BY profit DESC
                """;

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                data.add(new Object[] {
                        rs.getString("name"),
                        rs.getDouble("revenue"),
                        rs.getDouble("cost"),
                        rs.getDouble("profit")
                });
            }
        }
        return data;
    }

    /**
     * Get weekly revenue for the last 4 weeks
     */
    public java.util.Map<String, Double> getWeeklyRevenue() throws SQLException {
        java.util.Map<String, Double> data = new LinkedHashMap<>();
        String query = """
                SELECT YEARWEEK(order_date, 1) as week_num, SUM(total_amount) as weekly_total
                FROM OrderInfo
                WHERE order_date >= DATE_SUB(CURDATE(), INTERVAL 4 WEEK)
                GROUP BY YEARWEEK(order_date, 1)
                ORDER BY week_num ASC
                """;

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                data.put("Week " + rs.getString("week_num"), rs.getDouble("weekly_total"));
            }
        }
        return data;
    }

    /**
     * Get monthly revenue for the last 6 months
     */
    public java.util.Map<String, Double> getMonthlyRevenue() throws SQLException {
        java.util.Map<String, Double> data = new LinkedHashMap<>();
        String query = """
                SELECT DATE_FORMAT(order_date, '%Y-%m') as month, SUM(total_amount) as monthly_total
                FROM OrderInfo
                WHERE order_date >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH)
                GROUP BY DATE_FORMAT(order_date, '%Y-%m')
                ORDER BY month ASC
                """;

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                data.put(rs.getString("month"), rs.getDouble("monthly_total"));
            }
        }
        return data;
    }

    /**
     * Get total cost (sum of cost_price * stock for all products)
     */
    public double getTotalInventoryCost() throws SQLException {
        String query = "SELECT COALESCE(SUM(cost_price * stock), 0) as total FROM ProductInfo";

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
     * Get total profit from all sales
     */
    public double getTotalProfit() throws SQLException {
        String query = """
                SELECT COALESCE(SUM(oi.quantity * (oi.price_at_purchase - COALESCE(p.cost_price, 0))), 0) as profit
                FROM OrderItems oi
                JOIN ProductInfo p ON oi.product_id = p.id
                """;

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getDouble("profit");
            }
        }
        return 0;
    }

    /**
     * Get cost analysis per product
     * Returns: Product Name -> [Price, Cost, Margin %]
     */
    public java.util.List<Object[]> getCostAnalysis() throws SQLException {
        java.util.List<Object[]> data = new java.util.ArrayList<>();
        String query = """
                SELECT name, price, COALESCE(cost_price, 0) as cost_price,
                       CASE WHEN price > 0 THEN ((price - COALESCE(cost_price, 0)) / price * 100) ELSE 0 END as margin
                FROM ProductInfo
                ORDER BY margin DESC
                """;

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                data.add(new Object[] {
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getDouble("cost_price"),
                        rs.getDouble("margin")
                });
            }
        }
        return data;
    }

    /**
     * Get carrier leaderboard - carriers ranked by completed deliveries
     * Returns: Rank, Carrier Name, Completed Deliveries, Total Earnings
     */
    public java.util.List<Object[]> getCarrierLeaderboard() throws SQLException {
        java.util.List<Object[]> data = new java.util.ArrayList<>();
        String query = """
                SELECT u.id,
                       CONCAT(u.first_name, ' ', u.last_name) as carrier_name,
                       COUNT(o.id) as completed_deliveries,
                       COALESCE(SUM(o.total_amount), 0) as total_value
                FROM UserInfo u
                LEFT JOIN OrderInfo o ON u.id = o.carrier_id AND o.status = 'Delivered'
                WHERE u.role = 'carrier'
                GROUP BY u.id, u.first_name, u.last_name
                ORDER BY completed_deliveries DESC, total_value DESC
                """;

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            int rank = 1;
            while (rs.next()) {
                data.add(new Object[] {
                        rank++,
                        rs.getString("carrier_name"),
                        rs.getInt("completed_deliveries"),
                        rs.getDouble("total_value")
                });
            }
        }
        return data;
    }
}
