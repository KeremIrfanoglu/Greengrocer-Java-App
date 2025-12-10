package com.greengrocer.dao;

import com.greengrocer.models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public User authenticate(String username, String password) throws SQLException {
        String query = "SELECT * FROM UserInfo WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

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

    public boolean register(String username, String password, String role, String firstName, String lastName,
            String address, String phone) throws SQLException {
        // Validation check for duplicate username should be done before calling this or
        // handled via exception
        String query = "INSERT INTO UserInfo (username, password, role, first_name, last_name, address, phone) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
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
}
