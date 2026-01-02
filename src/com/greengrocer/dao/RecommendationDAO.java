package com.greengrocer.dao;

import com.greengrocer.models.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for product recommendations.
 * Implements "Customers who bought this also bought" functionality
 * by analyzing past order patterns to find frequently co-purchased products.
 * 
 * <p>
 * Recommendation strategies:
 * </p>
 * <ul>
 * <li>Also-bought analysis: products frequently in same orders</li>
 * <li>Top-selling fallback: popular products when no co-purchase data</li>
 * </ul>
 * 
 * @author Group10
 * @version 1.0
 * @see Product
 */
public class RecommendationDAO {

    /**
     * Get products that are frequently bought together with the given product
     * 
     * @param productId The product to find recommendations for
     * @return List of up to 5 recommended products
     */
    public List<Product> getAlsoBoughtProducts(int productId) throws SQLException {
        List<Product> recommendations = new ArrayList<>();

        // Query to find products that appear in the same orders as the given product
        // Ordered by frequency (how often they appear together)
        String query = """
                SELECT p.*, COUNT(*) as frequency
                FROM ProductInfo p
                JOIN OrderItems oi1 ON p.id = oi1.product_id
                JOIN OrderItems oi2 ON oi1.order_id = oi2.order_id
                WHERE oi2.product_id = ? AND p.id != ?
                GROUP BY p.id
                ORDER BY frequency DESC
                LIMIT 5
                """;

        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, productId);
            stmt.setInt(2, productId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    recommendations.add(createProductFromResultSet(rs));
                }
            }
        }

        return recommendations;
    }

    /**
     * Get top selling products (can be used as fallback when no recommendations)
     */
    public List<Product> getTopSellingProducts(int limit) throws SQLException {
        List<Product> products = new ArrayList<>();

        String query = """
                SELECT p.*, COALESCE(SUM(oi.quantity), 0) as total_sold
                FROM ProductInfo p
                LEFT JOIN OrderItems oi ON p.id = oi.product_id
                GROUP BY p.id
                ORDER BY total_sold DESC
                LIMIT ?
                """;

        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(createProductFromResultSet(rs));
                }
            }
        }

        return products;
    }

    private Product createProductFromResultSet(ResultSet rs) throws SQLException {
        byte[] imageData = null;
        try {
            imageData = rs.getBytes("image");
        } catch (SQLException e) {
            // Column may not exist
        }

        double costPrice = 0;
        try {
            costPrice = rs.getDouble("cost_price");
        } catch (SQLException e) {
            // Column may not exist
        }

        return new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("type"),
                rs.getDouble("price"),
                costPrice,
                rs.getDouble("stock"),
                rs.getDouble("threshold"),
                imageData);
    }
}
