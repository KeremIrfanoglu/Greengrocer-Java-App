package com.greengrocer.dao;

import com.greengrocer.models.CartItem;
import com.greengrocer.models.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for persistent shopping cart management.
 * Stores cart items in database so they persist after logout/app close.
 * 
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>Load cart items by user ID</li>
 * <li>Add/update items with upsert support</li>
 * <li>Remove individual items</li>
 * <li>Clear entire cart after checkout</li>
 * </ul>
 * 
 * @author Group10
 * @version 1.0
 * @see CartItem
 * @see Product
 */
public class CartDAO {

    /**
     * Load cart items for a specific user
     */
    public List<CartItem> getCartByUserId(int userId) throws SQLException {
        List<CartItem> items = new ArrayList<>();
        String query = "SELECT c.quantity, p.* FROM Cart c " +
                "JOIN ProductInfo p ON c.product_id = p.id " +
                "WHERE c.user_id = ?";

        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    double cartQuantity = rs.getDouble("quantity");

                    // Build product
                    String unitType = "kg";
                    try {
                        unitType = rs.getString("unit_type");
                    } catch (SQLException e) {
                    }
                    if (unitType == null)
                        unitType = "kg";

                    double costPrice = 0;
                    try {
                        costPrice = rs.getDouble("cost_price");
                    } catch (SQLException e) {
                    }

                    Product product = new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("type"),
                            rs.getDouble("price"),
                            costPrice,
                            rs.getDouble("stock"),
                            rs.getDouble("threshold"),
                            rs.getBytes("image"),
                            unitType);

                    items.add(new CartItem(product, cartQuantity));
                }
            }
        }
        return items;
    }

    /**
     * Add or update item in cart
     */
    public boolean addToCart(int userId, int productId, double quantity) throws SQLException {
        // Use INSERT ... ON DUPLICATE KEY UPDATE for upsert
        String query = "INSERT INTO Cart (user_id, product_id, quantity) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)";

        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            stmt.setDouble(3, quantity);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Update quantity for item in cart
     */
    public boolean updateQuantity(int userId, int productId, double newQuantity) throws SQLException {
        String query = "UPDATE Cart SET quantity = ? WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, newQuantity);
            stmt.setInt(2, userId);
            stmt.setInt(3, productId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Remove item from cart
     */
    public boolean removeFromCart(int userId, int productId) throws SQLException {
        String query = "DELETE FROM Cart WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Clear entire cart for user (after successful checkout)
     */
    public boolean clearCart(int userId) throws SQLException {
        String query = "DELETE FROM Cart WHERE user_id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() >= 0;
        }
    }
}
