package com.greengrocer.dao;

import com.greengrocer.models.CarrierRating;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for carrier ratings.
 * Handles rating creation and retrieval operations.
 */
public class CarrierRatingDAO {

    /**
     * Rate a carrier for a specific order
     */
    public boolean rateCarrier(int orderId, int customerId, int carrierId, int rating, String comment)
            throws SQLException {
        Connection conn = DatabaseAdapter.getConnection();
        PreparedStatement stmt = null;
        PreparedStatement pointStmt = null;

        try {
            conn.setAutoCommit(false);

            String query = "INSERT INTO CarrierRatings (order_id, customer_id, carrier_id, rating, comment) VALUES (?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, orderId);
            stmt.setInt(2, customerId);
            stmt.setInt(3, carrierId);
            stmt.setInt(4, rating);
            stmt.setString(5, comment);
            int affected = stmt.executeUpdate();

            if (affected > 0) {
                // Points logic removed
            }

            conn.commit();
            return affected > 0;
        } catch (SQLException e) {
            if (conn != null)
                conn.rollback();
            throw e;
        } finally {
            if (stmt != null)
                stmt.close();
            if (pointStmt != null)
                pointStmt.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * Check if an order has already been rated
     */
    public boolean hasBeenRated(int orderId) throws SQLException {
        String query = "SELECT COUNT(*) FROM CarrierRatings WHERE order_id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Get average rating for a carrier
     */
    public double getAverageRating(int carrierId) throws SQLException {
        String query = "SELECT AVG(rating) as avg_rating FROM CarrierRatings WHERE carrier_id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, carrierId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_rating");
                }
            }
        }
        return 0.0;
    }

    /**
     * Get total rating count for a carrier
     */
    public int getRatingCount(int carrierId) throws SQLException {
        String query = "SELECT COUNT(*) FROM CarrierRatings WHERE carrier_id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, carrierId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Get all ratings for a carrier (for display purposes)
     */
    public List<CarrierRating> getRatingsForCarrier(int carrierId) throws SQLException {
        List<CarrierRating> ratings = new ArrayList<>();
        String query = "SELECT cr.*, u.first_name, u.last_name FROM CarrierRatings cr " +
                "JOIN userinfo u ON cr.customer_id = u.id " +
                "WHERE cr.carrier_id = ? ORDER BY cr.created_at DESC";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, carrierId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CarrierRating r = new CarrierRating(
                            rs.getInt("id"),
                            rs.getInt("order_id"),
                            rs.getInt("customer_id"),
                            rs.getInt("carrier_id"),
                            rs.getInt("rating"),
                            rs.getString("comment"),
                            rs.getTimestamp("created_at"));
                    r.setCustomerName(rs.getString("first_name") + " " + rs.getString("last_name"));
                    ratings.add(r);
                }
            }
        }
        return ratings;
    }

    /**
     * Get rating by order ID (for customer order history display)
     */
    public CarrierRating getRatingByOrderId(int orderId) throws SQLException {
        String query = "SELECT * FROM CarrierRatings WHERE order_id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new CarrierRating(
                            rs.getInt("id"),
                            rs.getInt("order_id"),
                            rs.getInt("customer_id"),
                            rs.getInt("carrier_id"),
                            rs.getInt("rating"),
                            rs.getString("comment"),
                            rs.getTimestamp("created_at"));
                }
            }
        }
        return null;
    }

    /**
     * Get carrier leaderboard with both delivery count and average rating
     * Uses subqueries to correctly count deliveries and reviews separately
     */
    public List<Object[]> getCarrierLeaderboard() throws SQLException {
        List<Object[]> leaderboard = new ArrayList<>();
        String query = "SELECT u.id, u.first_name, u.last_name, u.g_points, " +
                "(SELECT COUNT(*) FROM OrderInfo o WHERE o.carrier_id = u.id AND o.status = 'Delivered') as deliveries, "
                +
                "(SELECT COALESCE(SUM(o.total_amount), 0) FROM OrderInfo o WHERE o.carrier_id = u.id AND o.status = 'Delivered') as total_value, "
                +
                "(SELECT COALESCE(AVG(cr.rating), 0) FROM CarrierRatings cr WHERE cr.carrier_id = u.id) as avg_rating, "
                +
                "(SELECT COUNT(*) FROM CarrierRatings cr WHERE cr.carrier_id = u.id) as rating_count " +
                "FROM userinfo u " +
                "WHERE u.role = 'carrier' " +
                "ORDER BY deliveries DESC, avg_rating DESC";

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            int rank = 1;
            while (rs.next()) {
                leaderboard.add(new Object[] {
                        rank++,
                        rs.getString("first_name") + " " + rs.getString("last_name"),
                        rs.getInt("deliveries"),
                        rs.getDouble("total_value"), // Reverted to total_value
                        rs.getDouble("avg_rating"),
                        rs.getInt("rating_count")
                });
            }
        }
        return leaderboard;
    }
}
