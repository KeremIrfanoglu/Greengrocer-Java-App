package com.greengrocer.models;

import java.sql.Timestamp;

public class Order {
    private int id;
    private int customerId;
    private int carrierId;
    private Timestamp orderDate;
    private String status;
    private double totalAmount;

    public Order(int id, int customerId, int carrierId, Timestamp orderDate, String status, double totalAmount) {
        this.id = id;
        this.customerId = customerId;
        this.carrierId = carrierId;
        this.orderDate = orderDate;
        this.status = status;
        this.totalAmount = totalAmount;
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

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCarrierId(int carrierId) {
        this.carrierId = carrierId;
    }
}
