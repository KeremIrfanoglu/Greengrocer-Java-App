package com.greengrocer.dao;

import com.greengrocer.models.Coupon;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for managing discount coupons.
 * Handles coupon creation, validation, application, and usage tracking.
 * 
 * <p>
 * Features:
 * </p>
 * <ul>
 * <li>Create/update/delete coupons</li>
 * <li>Validate coupon codes</li>
 * <li>Track usage limits and current usage</li>
 * <li>Prevent duplicate usage per user</li>
 * <li>Usage history reporting</li>
 * </ul>
 * 
 * @author Group10
 * @version 1.0
 * @see Coupon
 */
public class CouponDAO {

    /**
     * Create a new coupon
     */
    public boolean createCoupon(String code, double discountPercent, int maxUses) throws SQLException {
        String query = "INSERT INTO Coupons (code, discount_percent, max_uses) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, code.toUpperCase());
            stmt.setDouble(2, discountPercent);
            stmt.setInt(3, maxUses);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Get all coupons
     */
    public List<Coupon> getAllCoupons() throws SQLException {
        List<Coupon> coupons = new ArrayList<>();
        String query = "SELECT * FROM Coupons ORDER BY created_date DESC";

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                coupons.add(createCouponFromResultSet(rs));
            }
        }
        return coupons;
    }

    /**
     * Get active coupons only
     */
    public List<Coupon> getActiveCoupons() throws SQLException {
        List<Coupon> coupons = new ArrayList<>();
        String query = "SELECT * FROM Coupons WHERE is_active = TRUE AND current_uses < max_uses ORDER BY created_date DESC";

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                coupons.add(createCouponFromResultSet(rs));
            }
        }
        return coupons;
    }

    /**
     * Get coupon by code
     */
    public Coupon getCouponByCode(String code) throws SQLException {
        String query = "SELECT * FROM Coupons WHERE code = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, code.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return createCouponFromResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Update coupon (max uses, active status)
     */
    public boolean updateCoupon(int couponId, int maxUses, boolean isActive) throws SQLException {
        String query = "UPDATE Coupons SET max_uses = ?, is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, maxUses);
            stmt.setBoolean(2, isActive);
            stmt.setInt(3, couponId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Delete coupon
     */
    public boolean deleteCoupon(int couponId) throws SQLException {
        // First delete usage history
        String deleteUsage = "DELETE FROM CouponUsage WHERE coupon_id = ?";
        String deleteCoupon = "DELETE FROM Coupons WHERE id = ?";

        try (Connection conn = DatabaseAdapter.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(deleteUsage)) {
                    stmt.setInt(1, couponId);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement(deleteCoupon)) {
                    stmt.setInt(1, couponId);
                    int result = stmt.executeUpdate();
                    conn.commit();
                    return result > 0;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Apply coupon - increment usage and record in history
     */
    public boolean applyCoupon(int couponId, int userId, int orderId, double discountAmount) throws SQLException {
        String updateCoupon = "UPDATE Coupons SET current_uses = current_uses + 1 WHERE id = ? AND current_uses < max_uses AND is_active = TRUE";
        String insertUsage = "INSERT INTO CouponUsage (coupon_id, user_id, order_id, discount_amount) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseAdapter.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Update coupon usage count
                try (PreparedStatement stmt = conn.prepareStatement(updateCoupon)) {
                    stmt.setInt(1, couponId);
                    int updated = stmt.executeUpdate();
                    if (updated == 0) {
                        conn.rollback();
                        return false; // Coupon not available
                    }
                }

                // Record usage
                try (PreparedStatement stmt = conn.prepareStatement(insertUsage)) {
                    stmt.setInt(1, couponId);
                    stmt.setInt(2, userId);
                    stmt.setInt(3, orderId);
                    stmt.setDouble(4, discountAmount);
                    stmt.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Check if user already used this coupon
     */
    public boolean hasUserUsedCoupon(int userId, int couponId) throws SQLException {
        String query = "SELECT COUNT(*) FROM CouponUsage WHERE user_id = ? AND coupon_id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, couponId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Get coupon usage history
     */
    public List<Object[]> getCouponUsageHistory(int couponId) throws SQLException {
        List<Object[]> history = new ArrayList<>();
        String query = """
                SELECT cu.used_date, u.username, CONCAT(u.first_name, ' ', u.last_name) as full_name,
                       cu.discount_amount, cu.order_id
                FROM CouponUsage cu
                JOIN UserInfo u ON cu.user_id = u.id
                WHERE cu.coupon_id = ?
                ORDER BY cu.used_date DESC
                """;

        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, couponId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    history.add(new Object[] {
                            rs.getTimestamp("used_date"),
                            rs.getString("username"),
                            rs.getString("full_name"),
                            rs.getDouble("discount_amount"),
                            rs.getInt("order_id")
                    });
                }
            }
        }
        return history;
    }

    /**
     * Get all coupon usage history (for owner overview)
     */
    public List<Object[]> getAllCouponUsageHistory() throws SQLException {
        List<Object[]> history = new ArrayList<>();
        String query = """
                SELECT c.code, cu.used_date, u.username, CONCAT(u.first_name, ' ', u.last_name) as full_name,
                       cu.discount_amount
                FROM CouponUsage cu
                JOIN Coupons c ON cu.coupon_id = c.id
                JOIN UserInfo u ON cu.user_id = u.id
                ORDER BY cu.used_date DESC
                LIMIT 50
                """;

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                history.add(new Object[] {
                        rs.getString("code"),
                        rs.getTimestamp("used_date"),
                        rs.getString("full_name"),
                        rs.getDouble("discount_amount")
                });
            }
        }
        return history;
    }

    private Coupon createCouponFromResultSet(ResultSet rs) throws SQLException {
        return new Coupon(
                rs.getInt("id"),
                rs.getString("code"),
                rs.getDouble("discount_percent"),
                rs.getInt("max_uses"),
                rs.getInt("current_uses"),
                rs.getTimestamp("created_date"),
                rs.getBoolean("is_active"));
    }
}
