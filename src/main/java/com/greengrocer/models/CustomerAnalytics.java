package com.greengrocer.models;

/**
 * Represents analytics data for a specific customer, including their personal
 * details,
 * order count, and total amount spent.
 */
public class CustomerAnalytics {
    private int userId;
    private String username;
    private String fullName;
    private String phone;
    private int orderCount;
    private double totalSpent;

    /**
     * Constructs a new CustomerAnalytics instance.
     *
     * @param userId     The unique identifier for the customer.
     * @param username   The username of the customer.
     * @param fullName   The full name of the customer.
     * @param phone      The phone number of the customer.
     * @param orderCount The total number of orders placed by the customer.
     * @param totalSpent The total amount of money spent by the customer.
     */
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
