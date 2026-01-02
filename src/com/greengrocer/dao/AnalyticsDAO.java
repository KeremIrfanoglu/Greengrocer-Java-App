package com.greengrocer.dao;

import com.greengrocer.models.CarrierPerformance;
import com.greengrocer.models.HourlyOrderStats;
import com.greengrocer.models.ProductSalesStats;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for advanced analytics operations.
 * Provides detailed business intelligence data including carrier performance,
 * product sales analysis, dead stock identification, and peak hours analysis.
 * 
 * <p>
 * Analytics available:
 * </p>
 * <ul>
 * <li>Carrier Performance: delivery count, ratings, total value</li>
 * <li>Product Sales Analysis: quantity sold, revenue, cost per product</li>
 * <li>Dead Stock: products with no sales history</li>
 * <li>Peak Hours: order distribution by hour of day</li>
 * </ul>
 * 
 * @author Group10
 * @version 1.0
 * @see CarrierPerformance
 * @see ProductSalesStats
 * @see HourlyOrderStats
 */
public class AnalyticsDAO {

    public List<CarrierPerformance> getCarrierPerformance() throws SQLException {
        List<CarrierPerformance> list = new ArrayList<>();
        String sql = "SELECT u.id, u.first_name, u.last_name, " +
                "(SELECT COUNT(*) FROM OrderInfo o WHERE o.carrier_id = u.id AND o.status = 'DELIVERED') as delivery_count, "
                +
                "(SELECT COALESCE(SUM(o.total_amount), 0) FROM OrderInfo o WHERE o.carrier_id = u.id AND o.status = 'DELIVERED') as total_value, "
                +
                "(SELECT COALESCE(AVG(cr.rating), 0) FROM CarrierRatings cr WHERE cr.carrier_id = u.id) as avg_rating, "
                +
                "(SELECT COUNT(*) FROM CarrierRatings cr WHERE cr.carrier_id = u.id) as review_count "
                +
                "FROM UserInfo u " +
                "WHERE u.role = 'carrier' " +
                "ORDER BY avg_rating DESC, delivery_count DESC";

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String name = rs.getString("first_name") + " " + rs.getString("last_name");
                list.add(new CarrierPerformance(
                        rs.getInt("id"),
                        name,
                        rs.getInt("delivery_count"),
                        rs.getDouble("avg_rating"),
                        rs.getInt("review_count"),
                        rs.getDouble("total_value")));
            }
        }
        return list;
    }

    public List<ProductSalesStats> getProductSalesAnalysis() throws SQLException {
        List<ProductSalesStats> list = new ArrayList<>();
        // Calculate revenue, cost, and quantities for each product
        // Uses price_at_purchase and cost_at_purchase for accurate historical data
        String sql = "SELECT p.name, " +
                "COALESCE(SUM(oi.quantity), 0) as total_qty, " +
                "COALESCE(SUM(oi.price_at_purchase * oi.quantity), 0) as total_rev, " +
                "COALESCE(SUM(oi.cost_at_purchase * oi.quantity), 0) as total_cost " +
                "FROM ProductInfo p " +
                "LEFT JOIN OrderItems oi ON p.id = oi.product_id " +
                "GROUP BY p.id, p.name " +
                "ORDER BY total_rev DESC";

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String name = rs.getString("name");
                double qty = rs.getDouble("total_qty");
                double rev = rs.getDouble("total_rev");
                double cost = rs.getDouble("total_cost");
                list.add(new ProductSalesStats(name, qty, rev, cost));
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
