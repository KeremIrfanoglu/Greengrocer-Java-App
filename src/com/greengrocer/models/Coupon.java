package com.greengrocer.models;

import java.sql.Timestamp;

/**
 * Model class for discount coupons
 */
public class Coupon {
    private int id;
    private String code;
    private double discountPercent;
    private int maxUses;
    private int currentUses;
    private Timestamp createdDate;
    private boolean isActive;

    public Coupon(int id, String code, double discountPercent, int maxUses, int currentUses,
            Timestamp createdDate, boolean isActive) {
        this.id = id;
        this.code = code;
        this.discountPercent = discountPercent;
        this.maxUses = maxUses;
        this.currentUses = currentUses;
        this.createdDate = createdDate;
        this.isActive = isActive;
    }

    // For creating new coupons
    public Coupon(String code, double discountPercent, int maxUses) {
        this.code = code;
        this.discountPercent = discountPercent;
        this.maxUses = maxUses;
        this.currentUses = 0;
        this.isActive = true;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(int maxUses) {
        this.maxUses = maxUses;
    }

    public int getCurrentUses() {
        return currentUses;
    }

    public void setCurrentUses(int currentUses) {
        this.currentUses = currentUses;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // Helper methods
    public int getRemainingUses() {
        return maxUses - currentUses;
    }

    public boolean canBeUsed() {
        return isActive && currentUses < maxUses;
    }
}
