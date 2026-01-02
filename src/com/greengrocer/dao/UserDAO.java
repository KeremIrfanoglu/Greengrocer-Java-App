package com.greengrocer.dao;

import com.greengrocer.models.User;
import com.greengrocer.util.PasswordUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.Statement;

/**
 * Data Access Object for User operations.
 * Handles user authentication, registration, profile management, password
 * changes,
 * and G-Points (loyalty points) management.
 * 
 * <p>
 * Key responsibilities:
 * </p>
 * <ul>
 * <li>User authentication with password hashing support</li>
 * <li>User registration for new customers</li>
 * <li>Profile information updates</li>
 * <li>Password management with secure hashing</li>
 * <li>G-Points balance management (earn/spend loyalty points)</li>
 * <li>Customer analytics retrieval for owner dashboard</li>
 * </ul>
 * 
 * @author Group10
 * @version 1.0
 * @see User
 * @see PasswordUtils
 */
public class UserDAO {

    public User authenticate(String username, String password) throws SQLException {
        String query = "SELECT * FROM UserInfo WHERE username = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    // Check both hashed and plain text (for backward compatibility)
                    if (PasswordUtils.verifyPassword(password, storedPassword) || password.equals(storedPassword)) {
                        double gPoints = 0.0;
                        try {
                            gPoints = rs.getDouble("g_points");
                        } catch (SQLException e) {
                            // Column may not exist yet, default to 0
                        }
                        return new User(
                                rs.getInt("id"),
                                rs.getString("username"),
                                storedPassword,
                                rs.getString("role"),
                                rs.getString("first_name"),
                                rs.getString("last_name"),
                                rs.getString("address"),
                                rs.getString("phone"),
                                gPoints);
                    }
                }
            }
        }
        return null;
    }

    public boolean register(String username, String password, String role, String firstName, String lastName,
            String address, String phone) throws SQLException {
        String hashedPassword = PasswordUtils.hashPassword(password);
        String query = "INSERT INTO UserInfo (username, password, role, first_name, last_name, address, phone) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, role);
            stmt.setString(4, firstName);
            stmt.setString(5, lastName);
            stmt.setString(6, address);
            stmt.setString(7, phone);

            return stmt.executeUpdate() > 0;
        }
    }

    public java.util.List<User> getUsersByRole(String role) throws SQLException {
        java.util.List<User> users = new java.util.ArrayList<>();
        String query = "SELECT * FROM UserInfo WHERE role = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, role);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("address"),
                            rs.getString("phone")));
                }
            }
        }
        return users;
    }

    public boolean deleteUser(int id) throws SQLException {
        String query = "DELETE FROM UserInfo WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public User getUserByUsername(String username) throws SQLException {
        String query = "SELECT * FROM UserInfo WHERE username = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("address"),
                            rs.getString("phone"));
                }
            }
        }
        return null;
    }

    public boolean updatePassword(int userId, String newPassword) throws SQLException {
        String hashedPassword = PasswordUtils.hashPassword(newPassword);
        String query = "UPDATE UserInfo SET password = ? WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, hashedPassword);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean verifyOldPassword(int userId, String oldPassword) throws SQLException {
        String query = "SELECT password FROM UserInfo WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    return PasswordUtils.verifyPassword(oldPassword, storedPassword)
                            || oldPassword.equals(storedPassword);
                }
            }
        }
        return false;
    }

    /**
     * Get user's current G Points balance
     */
    public double getGPoints(int userId) throws SQLException {
        String query = "SELECT g_points FROM UserInfo WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("g_points");
                }
            }
        }
        return 0.0;
    }

    /**
     * Update user's G Points balance
     */
    public boolean updateGPoints(int userId, double newBalance) throws SQLException {
        String query = "UPDATE UserInfo SET g_points = ? WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, newBalance);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Add G Points to user's balance (for earning after purchase)
     * Earns 1/5 of the order total as G Points
     */
    public boolean addGPoints(int userId, double orderTotal) throws SQLException {
        double pointsToAdd = orderTotal / 5.0;
        double currentPoints = getGPoints(userId);
        return updateGPoints(userId, currentPoints + pointsToAdd);
    }

    /**
     * Use G Points for discount (1 G Point = 1 TL)
     * Returns true if successful, false if insufficient balance
     */
    public boolean useGPoints(int userId, double pointsToUse) throws SQLException {
        double currentPoints = getGPoints(userId);
        if (pointsToUse > currentPoints) {
            return false; // Insufficient balance
        }
        return updateGPoints(userId, currentPoints - pointsToUse);
    }

    /**
     * Update user profile information
     */
    public boolean updateUserProfile(int userId, String firstName, String lastName, String address, String phone)
            throws SQLException {
        String query = "UPDATE UserInfo SET first_name = ?, last_name = ?, address = ?, phone = ? WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, address);
            stmt.setString(4, phone);
            stmt.setInt(5, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Change user password
     * Verifies old password before updating to new password
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) throws SQLException {
        // First verify old password
        String verifyQuery = "SELECT password FROM UserInfo WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement verifyStmt = conn.prepareStatement(verifyQuery)) {
            verifyStmt.setInt(1, userId);
            try (var rs = verifyStmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    // Check both plain text and hashed password
                    String hashedOld = PasswordUtils.hashPassword(oldPassword);
                    if (!storedPassword.equals(oldPassword) && !storedPassword.equals(hashedOld)) {
                        return false; // Old password doesn't match
                    }
                } else {
                    return false; // User not found
                }
            }
        }

        // Update to new password (hashed)
        String updateQuery = "UPDATE UserInfo SET password = ? WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
            updateStmt.setString(1, PasswordUtils.hashPassword(newPassword));
            updateStmt.setInt(2, userId);
            return updateStmt.executeUpdate() > 0;
        }
    }

    /**
     * Get customer analytics (order count and total spent)
     * For Owner Panel - Customer Analytics Tab
     */
    public java.util.List<com.greengrocer.models.CustomerAnalytics> getCustomerAnalytics() throws SQLException {
        java.util.List<com.greengrocer.models.CustomerAnalytics> analytics = new java.util.ArrayList<>();
        String query = "SELECT u.id, u.username, u.first_name, u.last_name, u.phone, " +
                "COUNT(o.id) as order_count, " +
                "SUM(COALESCE(o.total_amount, 0)) as total_spent " +
                "FROM UserInfo u " +
                "LEFT JOIN OrderInfo o ON u.id = o.customer_id " +
                "WHERE u.role = 'customer' " +
                "GROUP BY u.id " +
                "ORDER BY total_spent DESC";

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String fullName = (rs.getString("first_name") != null ? rs.getString("first_name") : "") +
                        " " +
                        (rs.getString("last_name") != null ? rs.getString("last_name") : "");

                analytics.add(new com.greengrocer.models.CustomerAnalytics(
                        rs.getInt("id"),
                        rs.getString("username"),
                        fullName.trim(),
                        rs.getString("phone"),
                        rs.getInt("order_count"),
                        rs.getDouble("total_spent")));
            }
        }
        return analytics;
    }
}
