package com.greengrocer.models;

import java.sql.Timestamp;

/**
 * Model class representing a message between users.
 * Used for customer-owner communication.
 */
public class Message {
    private int id;
    private int senderId;
    private int receiverId;
    private String senderName;
    private String receiverName;
    private String subject;
    private String content;
    private Timestamp sentAt;
    private boolean isRead;

    // Full constructor
    public Message(int id, int senderId, int receiverId, String subject, String content,
            Timestamp sentAt, boolean isRead) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.subject = subject;
        this.content = content;
        this.sentAt = sentAt;
        this.isRead = isRead;
    }

    // Constructor with names
    public Message(int id, int senderId, int receiverId, String senderName, String receiverName,
            String subject, String content, Timestamp sentAt, boolean isRead) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.subject = subject;
        this.content = content;
        this.sentAt = sentAt;
        this.isRead = isRead;
    }

    // Simple constructor for sending new messages
    public Message(int senderId, int receiverId, String subject, String content) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.subject = subject;
        this.content = content;
        this.isRead = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getSentAt() {
        return sentAt;
    }

    public void setSentAt(Timestamp sentAt) {
        this.sentAt = sentAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    // For display purposes
    public String getReadStatus() {
        return isRead ? "Read" : "Unread";
    }
}
