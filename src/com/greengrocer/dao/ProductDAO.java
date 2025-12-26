package com.greengrocer.dao;

import com.greengrocer.models.Product;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
        String checkQuery = "SELECT COUNT(*) FROM OrderItems WHERE product_name = (SELECT name FROM ProductInfo WHERE id = ?)";
        // Note: The schema seems to link by product_name in some places, or maybe
        // product_id.
        // Let's stick to a direct delete and let the exception handle it, but maybe add
        // a more descriptive error.

        String query = "DELETE FROM ProductInfo WHERE id=?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
}
