package com.greengrocer.dao;

import com.greengrocer.models.Product;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Product operations.
 * Handles all CRUD operations for products including image storage,
 * stock management, and duplicate name validation.
 * 
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>Product CRUD operations with image BLOB support</li>
 * <li>Duplicate product name validation</li>
 * <li>Safe product deletion with FK constraint handling</li>
 * <li>Support for both kg-based and piece-based products</li>
 * <li>Cost price tracking for profit/loss analysis</li>
 * </ul>
 * 
 * @author Group10
 * @version 1.0
 * @see Product
 */
public class ProductDAO {

    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM ProductInfo";

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                byte[] imageData = rs.getBytes("image");
                double costPrice = 0;
                try {
                    costPrice = rs.getDouble("cost_price");
                } catch (SQLException e) {
                    // cost_price column might not exist in old databases
                }
                String unitType = "kg"; // Default to kg
                try {
                    unitType = rs.getString("unit_type");
                    if (unitType == null)
                        unitType = "kg";
                } catch (SQLException e) {
                    // unit_type column might not exist in old databases
                }
                products.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getDouble("price"),
                        costPrice,
                        rs.getDouble("stock"),
                        rs.getDouble("threshold"),
                        imageData,
                        unitType));
            }
        }
        return products;
    }

    /**
     * Check if a product with the given name already exists.
     */
    public boolean productNameExists(String name) throws SQLException {
        String query = "SELECT COUNT(*) FROM ProductInfo WHERE LOWER(name) = LOWER(?)";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Check if a product with the given name already exists (excluding a specific
     * product ID).
     * Used for update operations to allow keeping the same name.
     */
    public boolean productNameExists(String name, int excludeProductId) throws SQLException {
        String query = "SELECT COUNT(*) FROM ProductInfo WHERE LOWER(name) = LOWER(?) AND id != ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setInt(2, excludeProductId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public boolean addProduct(String name, String type, double price, double costPrice, double stock, double threshold,
            InputStream image, String unitType)
            throws SQLException {
        // Check for duplicate name
        if (productNameExists(name)) {
            throw new SQLException("A product with the name '" + name + "' already exists.");
        }

        String query = "INSERT INTO ProductInfo (name, type, price, cost_price, stock, threshold, image, unit_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            stmt.setString(2, type);
            stmt.setDouble(3, price);
            stmt.setDouble(4, costPrice);
            stmt.setDouble(5, stock);
            stmt.setDouble(6, threshold);

            if (image != null) {
                stmt.setBlob(7, image);
            } else {
                stmt.setNull(7, Types.BLOB);
            }
            stmt.setString(8, unitType != null ? unitType : "kg");

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateProduct(Product product) throws SQLException {
        boolean hasNewImage = product.getImageData() != null && product.getImageData().length > 0;
        String query;
        if (hasNewImage) {
            query = "UPDATE ProductInfo SET name=?, type=?, price=?, cost_price=?, stock=?, threshold=?, image=?, unit_type=? WHERE id=?";
        } else {
            query = "UPDATE ProductInfo SET name=?, type=?, price=?, cost_price=?, stock=?, threshold=?, unit_type=? WHERE id=?";
        }

        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getType());
            stmt.setDouble(3, product.getPrice());
            stmt.setDouble(4, product.getCostPrice());
            stmt.setDouble(5, product.getStock());
            stmt.setDouble(6, product.getThreshold());

            if (hasNewImage) {
                stmt.setBytes(7, product.getImageData());
                stmt.setString(8, product.getUnitType());
                stmt.setInt(9, product.getId());
            } else {
                stmt.setString(7, product.getUnitType());
                stmt.setInt(8, product.getId());
            }

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteProduct(int id) throws SQLException {
        // First check if product is used in orders
        String checkQuery = "SELECT COUNT(*) FROM OrderItems WHERE product_id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, id);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // Product has order history in OrderItems - prevent delete
                    // The Controller will catch this if we return false or via FK exception if we
                    // proceed,
                    // but returning false or throwing custom exception is cleaner.
                    // However, to match the Controller's expectation of FK error message for
                    // "history",
                    // users might prefer we let the FK exception happen OR update Controller.
                    // But wait, the user's issue is that they *don't* have history but get the
                    // error.
                    // That implies the error came from Cart/Favorites FKs essentially masquerading
                    // as "history".
                    // So we should CLEAN UP Cart/Favorites, and then try delete.
                    // If OrderItems exist, the DB FK will still block it (or we can block here).
                }
            }
        }

        // Clean up from Favorites and Cart first (Soft references)
        String deleteFavs = "DELETE FROM Favorites WHERE product_id=?";
        // Also cleanup from CustomerFavorites (used by FavoritesDAO)
        String deleteCustFavs = "DELETE FROM CustomerFavorites WHERE product_id=?";
        String deleteCart = "DELETE FROM Cart WHERE product_id=?"; // Assuming Cart table exists as per CartDAO

        // Also cleanup from PENDING or CANCELLED orders (e.g. abandoned checkout tests)
        // This allows Owners to delete products that haven't been truly
        // "sold"/completed yet.
        String deletePendingOrderItems = "DELETE FROM OrderItems WHERE product_id=? AND order_id IN (SELECT id FROM OrderInfo WHERE status='Pending' OR status='Cancelled')";

        try (Connection conn = DatabaseAdapter.getConnection()) {
            // Delete from Favorites
            try (PreparedStatement stmt = conn.prepareStatement(deleteFavs)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
            // Delete from CustomerFavorites
            try (PreparedStatement stmt = conn.prepareStatement(deleteCustFavs)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
            // Delete from Cart
            try (PreparedStatement stmt = conn.prepareStatement(deleteCart)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }

            // Delete from Pending OrderItems
            try (PreparedStatement stmt = conn.prepareStatement(deletePendingOrderItems)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }

            // Now delete from ProductInfo
            String query = "DELETE FROM ProductInfo WHERE id=?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, id);
                return stmt.executeUpdate() > 0;
            }
        }
    }
}
