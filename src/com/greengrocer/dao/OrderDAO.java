package com.greengrocer.dao;

import com.greengrocer.models.CartItem;
import com.greengrocer.models.Order;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public int createOrder(int customerId, List<CartItem> items, double totalAmount, java.sql.Timestamp deliveryDate)
            throws SQLException {
        Connection conn = DatabaseAdapter.getConnection();
        PreparedStatement orderStmt = null;
        PreparedStatement itemStmt = null;
        PreparedStatement stockStmt = null;
        ResultSet rs = null;

        try {
            conn.setAutoCommit(false); // Start Transaction

            // 1. Insert Order with delivery_date
            String orderQuery = "INSERT INTO OrderInfo (customer_id, order_date, status, total_amount, delivery_date) VALUES (?, NOW(), 'Pending', ?, ?)";
            orderStmt = conn.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setInt(1, customerId);
            orderStmt.setDouble(2, totalAmount);
            orderStmt.setTimestamp(3, deliveryDate);
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

            // 2. Insert Order Items (with price and cost at purchase) and Update Stock
            String itemQuery = "INSERT INTO OrderItems (order_id, product_id, quantity, price_at_purchase, cost_at_purchase) VALUES (?, ?, ?, ?, ?)";
            String stockQuery = "UPDATE ProductInfo SET stock = stock - ? WHERE id = ?";

            itemStmt = conn.prepareStatement(itemQuery);
            stockStmt = conn.prepareStatement(stockQuery);

            for (CartItem item : items) {
                // Add Item with both price and cost at time of purchase
                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, item.getProduct().getId());
                itemStmt.setDouble(3, item.getQuantity());
                itemStmt.setDouble(4, item.getPrice());
                itemStmt.setDouble(5, item.getProduct().getCostPrice()); // Store cost at purchase
                itemStmt.addBatch();

                // Update Stock
                stockStmt.setDouble(1, item.getQuantity());
                stockStmt.setInt(2, item.getProduct().getId());
                stockStmt.addBatch();
            }

            itemStmt.executeBatch();
            stockStmt.executeBatch();

            conn.commit(); // Commit Transaction
            return orderId;

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

    /**
     * Save invoice data to order
     */
    public boolean saveInvoice(int orderId, byte[] invoiceData) throws SQLException {
        String query = "UPDATE OrderInfo SET invoice_data = ? WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setBytes(1, invoiceData);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Get invoice data for an order
     */
    public byte[] getInvoice(int orderId) throws SQLException {
        String query = "SELECT invoice_data FROM OrderInfo WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("invoice_data");
                }
            }
        }
        return null;
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

    /**
     * Get pending orders with customer details for carrier view
     */
    public List<Order> getPendingOrdersWithDetails() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT o.id, o.customer_id, o.carrier_id, o.order_date, o.status, o.total_amount, o.delivery_date, "
                +
                "u.first_name, u.last_name, u.address FROM OrderInfo o " +
                "LEFT JOIN userinfo u ON o.customer_id = u.id " +
                "WHERE o.status = 'Pending' AND o.carrier_id IS NULL ORDER BY o.order_date ASC";

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String address = rs.getString("address");

                String customerName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                customerName = customerName.trim().isEmpty() ? "Unknown" : customerName.trim();

                orders.add(new Order(
                        rs.getInt("id"),
                        rs.getInt("customer_id"),
                        rs.getInt("carrier_id"),
                        rs.getTimestamp("order_date"),
                        rs.getString("status"),
                        rs.getDouble("total_amount"),
                        null,
                        rs.getTimestamp("delivery_date"),
                        customerName,
                        address != null ? address : "No address"));
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
        String query = "SELECT id, customer_id, carrier_id, order_date, status, total_amount, delivery_date FROM OrderInfo WHERE carrier_id = ? AND status = ? ORDER BY order_date DESC";

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
                            rs.getDouble("total_amount"),
                            null, // cancelled_at - not needed here
                            rs.getTimestamp("delivery_date"),
                            null, null));
                }
            }
        }
        return orders;
    }

    /**
     * Get completed deliveries by carrier
     */
    public List<Order> getCompletedDeliveriesByCarrier(int carrierId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT id, customer_id, carrier_id, order_date, status, total_amount, delivery_date, delivered_at FROM OrderInfo WHERE carrier_id = ? AND status = 'Delivered' ORDER BY order_date DESC";

        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, carrierId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(new Order(
                            rs.getInt("id"),
                            rs.getInt("customer_id"),
                            rs.getInt("carrier_id"),
                            rs.getTimestamp("order_date"),
                            rs.getString("status"),
                            rs.getDouble("total_amount"),
                            null, // cancelled_at
                            rs.getTimestamp("delivery_date"),
                            rs.getTimestamp("delivered_at"),
                            null, null));
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
        String query;
        if ("Delivered".equals(status)) {
            // Record the actual delivery time when marking as delivered
            query = "UPDATE OrderInfo SET status = ?, delivered_at = NOW() WHERE id = ?";
        } else {
            query = "UPDATE OrderInfo SET status = ? WHERE id = ?";
        }
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
     * Cancel an order and restore stock (with 30 min limit check should be done in
     * controller)
     */
    public boolean cancelOrder(int orderId) throws SQLException {
        Connection conn = DatabaseAdapter.getConnection();
        PreparedStatement updateOrderStmt = null;
        PreparedStatement getItemsStmt = null;
        PreparedStatement restoreStockStmt = null;
        ResultSet rs = null;

        try {
            conn.setAutoCommit(false);

            // 1. Update Order Status to Cancelled
            String updateOrderQuery = "UPDATE OrderInfo SET status = 'Cancelled' WHERE id = ? AND status = 'Pending'";
            updateOrderStmt = conn.prepareStatement(updateOrderQuery);
            updateOrderStmt.setInt(1, orderId);
            int affected = updateOrderStmt.executeUpdate();

            if (affected == 0) {
                conn.rollback();
                return false; // Order not in Pending status
            }

            // 2. Get Order Items to restore stock
            String getItemsQuery = "SELECT product_id, quantity FROM OrderItems WHERE order_id = ?";
            getItemsStmt = conn.prepareStatement(getItemsQuery);
            getItemsStmt.setInt(1, orderId);
            rs = getItemsStmt.executeQuery();

            // 3. Restore Stock
            String restoreStockQuery = "UPDATE ProductInfo SET stock = stock + ? WHERE id = ?";
            restoreStockStmt = conn.prepareStatement(restoreStockQuery);

            while (rs.next()) {
                restoreStockStmt.setDouble(1, rs.getDouble("quantity"));
                restoreStockStmt.setInt(2, rs.getInt("product_id"));
                restoreStockStmt.addBatch();
            }
            restoreStockStmt.executeBatch();

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null)
                conn.rollback();
            throw e;
        } finally {
            if (rs != null)
                rs.close();
            if (updateOrderStmt != null)
                updateOrderStmt.close();
            if (getItemsStmt != null)
                getItemsStmt.close();
            if (restoreStockStmt != null)
                restoreStockStmt.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * Get order details as formatted string for carrier view
     */
    public String getOrderDetailsText(int orderId) throws SQLException {
        StringBuilder sb = new StringBuilder();
        String query = "SELECT oi.quantity, p.name, p.unit_type FROM OrderItems oi " +
                "JOIN ProductInfo p ON oi.product_id = p.id WHERE oi.order_id = ?";

        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sb.append("• ").append(rs.getString("name"))
                            .append(": ").append(String.format("%.1f", rs.getDouble("quantity")))
                            .append(" ").append(rs.getString("unit_type")).append("\n");
                }
            }
        }
        return sb.toString();
    }
}
