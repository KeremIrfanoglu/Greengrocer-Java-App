package com.greengrocer.dao;

import com.greengrocer.models.CartItem;
import com.greengrocer.models.Order;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public boolean createOrder(int customerId, List<CartItem> items, double totalAmount) throws SQLException {
        Connection conn = DatabaseAdapter.getConnection();
        PreparedStatement orderStmt = null;
        PreparedStatement itemStmt = null;
        PreparedStatement stockStmt = null;
        ResultSet rs = null;

        try {
            conn.setAutoCommit(false); // Start Transaction

            // 1. Insert Order
            String orderQuery = "INSERT INTO OrderInfo (customer_id, order_date, status, total_amount) VALUES (?, NOW(), 'Pending', ?)";
            orderStmt = conn.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setInt(1, customerId);
            orderStmt.setDouble(2, totalAmount);
            int affectedRows = orderStmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }

            int orderId;
            rs = orderStmt.getGeneratedKeys();
            if (rs.next()) {
                orderId = rs.getInt(1);
            } else {
                throw new SQLException("Creating order failed, no ID obtained.");
            }

            // 2. Insert Order Items and Update Stock
            String itemQuery = "INSERT INTO OrderItems (order_id, product_id, quantity, price_at_purchase) VALUES (?, ?, ?, ?)";
            String stockQuery = "UPDATE ProductInfo SET stock = stock - ? WHERE id = ?";

            itemStmt = conn.prepareStatement(itemQuery);
            stockStmt = conn.prepareStatement(stockQuery);

            for (CartItem item : items) {
                // Add Item
                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, item.getProduct().getId());
                itemStmt.setDouble(3, item.getQuantity());
                itemStmt.setDouble(4, item.getPrice());
                itemStmt.addBatch();

                // Update Stock
                stockStmt.setDouble(1, item.getQuantity());
                stockStmt.setInt(2, item.getProduct().getId());
                stockStmt.addBatch();
            }

            itemStmt.executeBatch();
            stockStmt.executeBatch();

            conn.commit(); // Commit Transaction
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (rs != null)
                rs.close();
            if (orderStmt != null)
                orderStmt.close();
            if (itemStmt != null)
                itemStmt.close();
            if (stockStmt != null)
                stockStmt.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public List<Order> getOrdersByCustomer(int customerId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM OrderInfo WHERE customer_id = ? ORDER BY order_date DESC";

        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(new Order(
                            rs.getInt("id"),
                            rs.getInt("customer_id"),
                            rs.getInt("carrier_id"),
                            rs.getTimestamp("order_date"),
                            rs.getString("status"),
                            rs.getDouble("total_amount")));
                }
            }
        }
        return orders;
    }

    public List<Order> getPendingOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM OrderInfo WHERE status = 'Pending' AND carrier_id IS NULL ORDER BY order_date ASC";

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                orders.add(new Order(
                        rs.getInt("id"),
                        rs.getInt("customer_id"),
                        rs.getInt("carrier_id"),
                        rs.getTimestamp("order_date"),
                        rs.getString("status"),
                        rs.getDouble("total_amount")));
            }
        }
        return orders;
    }

    public List<Order> getOrdersByCarrierAndStatus(int carrierId, String status) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM OrderInfo WHERE carrier_id = ? AND status = ? ORDER BY order_date DESC";

        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, carrierId);
            stmt.setString(2, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(new Order(
                            rs.getInt("id"),
                            rs.getInt("customer_id"),
                            rs.getInt("carrier_id"),
                            rs.getTimestamp("order_date"),
                            rs.getString("status"),
                            rs.getDouble("total_amount")));
                }
            }
        }
        return orders;
    }

    public boolean assignCarrier(int orderId, int carrierId) throws SQLException {
        String query = "UPDATE OrderInfo SET carrier_id = ?, status = 'Delivering' WHERE id = ? AND carrier_id IS NULL";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, carrierId);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateOrderStatus(int orderId, String status) throws SQLException {
        String query = "UPDATE OrderInfo SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Get all orders (for Owner management)
     */
    public java.util.List<Order> getAllOrders() throws SQLException {
        java.util.List<Order> orders = new java.util.ArrayList<>();
        String query = "SELECT * FROM OrderInfo ORDER BY order_date DESC";

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                orders.add(new Order(
                        rs.getInt("id"),
                        rs.getInt("customer_id"),
                        rs.getInt("carrier_id"),
                        rs.getTimestamp("order_date"),
                        rs.getString("status"),
                        rs.getDouble("total_amount")));
            }
        }
        return orders;
    }

    /**
     * Get orders filtered by status
     */
    public java.util.List<Order> getOrdersByStatus(String status) throws SQLException {
        java.util.List<Order> orders = new java.util.ArrayList<>();
        String query = "SELECT * FROM OrderInfo WHERE status = ? ORDER BY order_date DESC";

        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(new Order(
                            rs.getInt("id"),
                            rs.getInt("customer_id"),
                            rs.getInt("carrier_id"),
                            rs.getTimestamp("order_date"),
                            rs.getString("status"),
                            rs.getDouble("total_amount")));
                }
            }
        }
        return orders;
    }

    /**
     * Cancel an order (Owner only)
     */
    public boolean cancelOrder(int orderId) throws SQLException {
        return updateOrderStatus(orderId, "Cancelled");
    }
}
