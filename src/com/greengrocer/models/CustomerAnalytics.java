package com.greengrocer.models;

public class CustomerAnalytics {
    private int userId;
    private String username;
    private String fullName;
    private String phone;
    private int orderCount;
    private double totalSpent;

    public CustomerAnalytics(int userId, String username, String fullName, String phone, int orderCount,
            double totalSpent) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.phone = phone;
        this.orderCount = orderCount;
        this.totalSpent = totalSpent;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public double getTotalSpent() {
        return totalSpent;
    }
}
