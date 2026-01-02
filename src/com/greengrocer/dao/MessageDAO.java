package com.greengrocer.dao;

import com.greengrocer.models.Message;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Message operations.
 * Handles in-app messaging between customers, carriers, and owner.
 * 
 * <p>
 * Features:
 * </p>
 * <ul>
 * <li>Send messages between users</li>
 * <li>Inbox and sent message retrieval</li>
 * <li>Unread message counting</li>
 * <li>Mark messages as read</li>
 * <li>Message deletion</li>
 * </ul>
 * 
 * @author Group10
 * @version 1.0
 * @see Message
 */
public class MessageDAO {

    /**
     * Send a new message
     */
    public boolean sendMessage(Message message) throws SQLException {
        String query = "INSERT INTO Messages (sender_id, receiver_id, subject, content) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, message.getSenderId());
            stmt.setInt(2, message.getReceiverId());
            stmt.setString(3, message.getSubject());
            stmt.setString(4, message.getContent());
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Get all messages received by a user (inbox)
     */
    public List<Message> getInbox(int userId) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT m.*, " +
                "CONCAT(s.first_name, ' ', s.last_name) as sender_name, " +
                "CONCAT(r.first_name, ' ', r.last_name) as receiver_name " +
                "FROM Messages m " +
                "LEFT JOIN UserInfo s ON m.sender_id = s.id " +
                "LEFT JOIN UserInfo r ON m.receiver_id = r.id " +
                "WHERE m.receiver_id = ? " +
                "ORDER BY m.sent_at DESC";

        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(new Message(
                            rs.getInt("id"),
                            rs.getInt("sender_id"),
                            rs.getInt("receiver_id"),
                            rs.getString("sender_name"),
                            rs.getString("receiver_name"),
                            rs.getString("subject"),
                            rs.getString("content"),
                            rs.getTimestamp("sent_at"),
                            rs.getBoolean("is_read")));
                }
            }
        }
        return messages;
    }

    /**
     * Get all messages sent by a user (sent box)
     */
    public List<Message> getSentMessages(int userId) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT m.*, " +
                "CONCAT(s.first_name, ' ', s.last_name) as sender_name, " +
                "CONCAT(r.first_name, ' ', r.last_name) as receiver_name " +
                "FROM Messages m " +
                "LEFT JOIN UserInfo s ON m.sender_id = s.id " +
                "LEFT JOIN UserInfo r ON m.receiver_id = r.id " +
                "WHERE m.sender_id = ? " +
                "ORDER BY m.sent_at DESC";

        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(new Message(
                            rs.getInt("id"),
                            rs.getInt("sender_id"),
                            rs.getInt("receiver_id"),
                            rs.getString("sender_name"),
                            rs.getString("receiver_name"),
                            rs.getString("subject"),
                            rs.getString("content"),
                            rs.getTimestamp("sent_at"),
                            rs.getBoolean("is_read")));
                }
            }
        }
        return messages;
    }

    /**
     * Mark a message as read
     */
    public boolean markAsRead(int messageId) throws SQLException {
        String query = "UPDATE Messages SET is_read = TRUE WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, messageId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Get unread message count for a user
     */
    public int getUnreadCount(int userId) throws SQLException {
        String query = "SELECT COUNT(*) FROM Messages WHERE receiver_id = ? AND is_read = FALSE";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Get owner user ID (for customers to send messages to)
     */
    public int getOwnerId() throws SQLException {
        String query = "SELECT id FROM UserInfo WHERE role = 'owner' LIMIT 1";
        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }

    /**
     * Delete a message
     */
    public boolean deleteMessage(int messageId) throws SQLException {
        String query = "DELETE FROM Messages WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, messageId);
            return stmt.executeUpdate() > 0;
        }
    }
}
