package com.greengrocer.models;

import java.sql.Timestamp;

/**
 * Represents a discount coupon that can be applied to orders.
 */
public class Coupon {
    private int id;
    private String code;
    private double discountPercent; // Renamed back to match usage
    private Timestamp createdDate; // Renamed back/restored
    private int maxUses; // Renamed back
    private int currentUses; // Renamed back
    private boolean isActive; // Restored

    /**
     * Constructs a new Coupon.
     *
     * @param id              The unique identifier of the coupon.
     * @param code            The unique code string for the coupon.
     * @param discountPercent The discount percentage (0-100).
     * @param maxUses         The maximum number of times this coupon can be used
     *                        total.
     * @param currentUses     The number of times this coupon has already been used.
     * @param createdDate     The creation timestamp of the coupon.
     * @param isActive        Whether the coupon is currently active.
     */
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

    /**
     * @return The coupon ID.
     */
    public int getId() {
        return id;
    }

    /**
     * @return The code.
     */
    public String getCode() {
        return code;
    }

    /**
     * @return The discount percentage.
     */
    public double getDiscountPercent() {
        return discountPercent;
    }

    /**
     * @return The creation date.
     */
    public Timestamp getCreatedDate() {
        return createdDate;
    }

    /**
     * @return The maximum usage limit.
     */
    public int getMaxUses() {
        return maxUses;
    }

    /**
     * @return The current usage count.
     */
    public int getCurrentUses() {
        return currentUses;
    }

    /**
     * @return True if the coupon is marked as active, false otherwise.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Checks if the coupon is valid for use.
     * 
     * @return True if active and usage limit not reached.
     */
    public boolean canBeUsed() {
        return isActive && currentUses < maxUses;
    }
}
