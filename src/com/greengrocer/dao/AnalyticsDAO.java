package com.greengrocer.dao;

import com.greengrocer.models.CarrierPerformance;
import com.greengrocer.models.HourlyOrderStats;
import com.greengrocer.models.ProductSalesStats;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsDAO {

    public List<CarrierPerformance> getCarrierPerformance() throws SQLException {
        List<CarrierPerformance> list = new ArrayList<>();
        String sql = "SELECT u.id, u.first_name, u.last_name, COUNT(o.id) as delivery_count " +
                "FROM UserInfo u " +
                "LEFT JOIN OrderInfo o ON u.id = o.carrier_id " +
                "WHERE u.role = 'carrier' AND o.status = 'DELIVERED' " +
                "GROUP BY u.id " +
                "ORDER BY delivery_count DESC";

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String name = rs.getString("first_name") + " " + rs.getString("last_name");
                list.add(new CarrierPerformance(rs.getInt("id"), name, rs.getInt("delivery_count")));
            }
        }
        return list;
    }

    public List<ProductSalesStats> getProductSalesAnalysis() throws SQLException {
        List<ProductSalesStats> list = new ArrayList<>();
        // Note: Joining OrderItems with ProductInfo. Assuming OrderItems has
        // product_id.
        // Also assuming OrderItems table exists and links to ProductInfo.
        // Adjusting query based on typical schema.
        String sql = "SELECT p.name, SUM(oi.quantity) as total_qty, SUM(oi.price * oi.quantity) as total_rev " +
                "FROM ProductInfo p " +
                "LEFT JOIN OrderItems oi ON p.id = oi.product_id " +
                "GROUP BY p.id " +
                "ORDER BY total_rev DESC";

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String name = rs.getString("name");
                double qty = rs.getDouble("total_qty"); // quantity might be double for weight
                // If quantity is null (no sales), it returns 0
                double rev = rs.getDouble("total_rev");
                list.add(new ProductSalesStats(name, qty, rev));
            }
        }
        return list;
    }

    public List<ProductSalesStats> getDeadStock() throws SQLException {
        List<ProductSalesStats> list = new ArrayList<>();
        String sql = "SELECT p.name, 0 as total_qty, 0 as total_rev " +
                "FROM ProductInfo p " +
                "WHERE p.id NOT IN (SELECT DISTINCT product_id FROM OrderItems)";

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new ProductSalesStats(rs.getString("name"), 0, 0));
            }
        }
        return list;
    }

    public List<HourlyOrderStats> getHourlyOrderStats() throws SQLException {
        List<HourlyOrderStats> list = new ArrayList<>();
        // Extract hour from order_date. Syntax depends on DB (MySQL: HOUR(order_date))
        String sql = "SELECT HOUR(order_date) as hour_of_day, COUNT(id) as order_count " +
                "FROM OrderInfo " +
                "GROUP BY hour_of_day " +
                "ORDER BY hour_of_day";

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new HourlyOrderStats(rs.getInt("hour_of_day"), rs.getInt("order_count")));
            }
        }
        return list;
    }
}
