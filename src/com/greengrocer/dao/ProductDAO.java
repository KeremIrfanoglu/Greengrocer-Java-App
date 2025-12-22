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
                products.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getDouble("price"),
                        costPrice,
                        rs.getDouble("stock"),
                        rs.getDouble("threshold"),
                        imageData));
            }
        }
        return products;
    }

    public boolean addProduct(String name, String type, double price, double costPrice, double stock, double threshold,
            InputStream image)
            throws SQLException {
        String query = "INSERT INTO ProductInfo (name, type, price, cost_price, stock, threshold, image) VALUES (?, ?, ?, ?, ?, ?, ?)";
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

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateProduct(Product product) throws SQLException {
        String query = "UPDATE ProductInfo SET name=?, type=?, price=?, cost_price=?, stock=?, threshold=? WHERE id=?";

        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getType());
            stmt.setDouble(3, product.getPrice());
            stmt.setDouble(4, product.getCostPrice());
            stmt.setDouble(5, product.getStock());
            stmt.setDouble(6, product.getThreshold());
            stmt.setInt(7, product.getId());

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
