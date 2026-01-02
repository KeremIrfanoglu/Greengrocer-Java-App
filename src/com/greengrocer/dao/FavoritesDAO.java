package com.greengrocer.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for customer favorites management.
 * Handles adding, removing, and querying favorite products for customers.
 * 
 * <p>
 * Features:
 * </p>
 * <ul>
 * <li>Add products to favorites</li>
 * <li>Remove products from favorites</li>
 * <li>Check if a product is favorited</li>
 * <li>Retrieve all favorite product IDs for a customer</li>
 * </ul>
 * 
 * @author Group10
 * @version 1.0
 */
public class FavoritesDAO {

    /**
     * Add a product to customer's favorites
     */
    public boolean addFavorite(int customerId, int productId) throws SQLException {
        String query = "INSERT IGNORE INTO CustomerFavorites (customer_id, product_id) VALUES (?, ?)";

        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, customerId);
            pstmt.setInt(2, productId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Remove a product from customer's favorites
     */
    public boolean removeFavorite(int customerId, int productId) throws SQLException {
        String query = "DELETE FROM CustomerFavorites WHERE customer_id = ? AND product_id = ?";

        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, customerId);
            pstmt.setInt(2, productId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Check if a product is in customer's favorites
     */
    public boolean isFavorite(int customerId, int productId) throws SQLException {
        String query = "SELECT 1 FROM CustomerFavorites WHERE customer_id = ? AND product_id = ?";

        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, customerId);
            pstmt.setInt(2, productId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    /**
     * Get all favorite product IDs for a customer
     */
    public List<Integer> getFavoriteProductIds(int customerId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String query = "SELECT product_id FROM CustomerFavorites WHERE customer_id = ?";

        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ids.add(rs.getInt("product_id"));
            }
        }
        return ids;
    }
}
