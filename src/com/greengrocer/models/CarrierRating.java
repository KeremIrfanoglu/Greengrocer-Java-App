package com.greengrocer.models;

import java.sql.Timestamp;

/**
 * Represents a rating given to a carrier by a customer for a specific order.
 */
public class CarrierRating {
    private int id;
    private int orderId;
    private int customerId;
    private int carrierId;
    private int rating; // 1-5 stars
    private String comment;
    private Timestamp createdAt;

    /**
     * Constructs a new CarrierRating instance.
     *
     * @param id         The unique identifier of the rating.
     * @param orderId    The ID of the order associated with this rating.
     * @param customerId The ID of the customer who gave the rating.
     * @param carrierId  The ID of the carrier being rated.
     * @param rating     The rating score (1-5 stars).
     * @param comment    The text comment associated with the rating.
     * @param createdAt  The timestamp when the rating was submitted.
     */
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
