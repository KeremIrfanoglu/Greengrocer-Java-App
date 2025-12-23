package com.greengrocer.models;

import java.sql.Timestamp;

/**
 * Represents an order in the system with extended fields for
 * cancellation tracking, delivery info, and customer details.
 */
public class Order {
    private int id;
    private int customerId;
    private int carrierId;
    private Timestamp orderDate;
    private String status;
    private double totalAmount;
    private Timestamp cancelledAt;
    private Timestamp deliveryDate; // Scheduled delivery date
    private Timestamp deliveredAt; // Actual delivery completion time
    private String customerName;
    private String customerAddress;

    // Basic constructor (for simple queries)
    public Order(int id, int customerId, int carrierId, Timestamp orderDate, String status, double totalAmount) {
        this(id, customerId, carrierId, orderDate, status, totalAmount, null, null, null, null, null);
    }

    // Constructor without deliveredAt (backward compatibility)
    public Order(int id, int customerId, int carrierId, Timestamp orderDate, String status, double totalAmount,
            Timestamp cancelledAt, Timestamp deliveryDate, String customerName, String customerAddress) {
        this(id, customerId, carrierId, orderDate, status, totalAmount, cancelledAt, deliveryDate, null, customerName,
                customerAddress);
    }

    // Full constructor with deliveredAt
    public Order(int id, int customerId, int carrierId, Timestamp orderDate, String status, double totalAmount,
            Timestamp cancelledAt, Timestamp deliveryDate, Timestamp deliveredAt, String customerName,
            String customerAddress) {
        this.id = id;
        this.customerId = customerId;
        this.carrierId = carrierId;
        this.orderDate = orderDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.cancelledAt = cancelledAt;
        this.deliveryDate = deliveryDate;
        this.deliveredAt = deliveredAt;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
    }

    public int getId() {
        return id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getCarrierId() {
        return carrierId;
    }

    public Timestamp getOrderDate() {
        return orderDate;
    }

    public String getStatus() {
        return status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public Timestamp getCancelledAt() {
        return cancelledAt;
    }

    public Timestamp getDeliveryDate() {
        return deliveryDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCarrierId(int carrierId) {
        this.carrierId = carrierId;
    }

    public void setCancelledAt(Timestamp cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public void setDeliveryDate(Timestamp deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public Timestamp getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Timestamp deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
}
