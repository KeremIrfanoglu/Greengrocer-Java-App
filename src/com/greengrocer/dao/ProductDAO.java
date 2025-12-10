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
                products.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getDouble("price"),
                        rs.getDouble("stock"),
                        rs.getDouble("threshold"),
                        rs.getBinaryStream("imagelocation")));
            }
        }
        return products;
    }

    public boolean addProduct(String name, String type, double price, double stock, double threshold, InputStream image)
            throws SQLException {
        String query = "INSERT INTO ProductInfo (name, type, price, stock, threshold, imagelocation) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            stmt.setString(2, type);
            stmt.setDouble(3, price);
            stmt.setDouble(4, stock);
            stmt.setDouble(5, threshold);

            if (image != null) {
                stmt.setBlob(6, image);
            } else {
                stmt.setNull(6, Types.BLOB);
            }

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateProduct(Product product) throws SQLException {
        String query = "UPDATE ProductInfo SET name=?, type=?, price=?, stock=?, threshold=? WHERE id=?";
        // Note: Image update is typically separate or handled if provided
        // For simplicity, we might skipping image update here unless we want to handle
        // it specifically

        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getType());
            stmt.setDouble(3, product.getPrice());
            stmt.setDouble(4, product.getStock());
            stmt.setDouble(5, product.getThreshold());
            stmt.setInt(6, product.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteProduct(int id) throws SQLException {
        String query = "DELETE FROM ProductInfo WHERE id=?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
}
