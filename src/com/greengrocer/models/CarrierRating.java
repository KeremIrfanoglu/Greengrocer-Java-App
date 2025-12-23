package com.greengrocer.models;

import java.sql.Timestamp;

/**
 * Represents a carrier rating given by a customer after delivery.
 */
public class CarrierRating {
    private int id;
    private int orderId;
    private int customerId;
    private int carrierId;
    private int rating; // 1-5 stars
    private String comment;
    private Timestamp createdAt;

    public CarrierRating(int id, int orderId, int customerId, int carrierId, int rating, String comment,
            Timestamp createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.carrierId = carrierId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getCarrierId() {
        return carrierId;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    // For display purposes
    private String customerName;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String name) {
        this.customerName = name;
    }
}
