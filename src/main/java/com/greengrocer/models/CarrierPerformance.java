package com.greengrocer.models;

/**
 * Represents the performance metrics for a carrier.
 */
public class CarrierPerformance {
    private int carrierId;
    private String carrierName;
    private int deliveryCount;
    private double averageRating;

    private int reviewCount;
    private double totalValue;

    /**
     * Constructs a new CarrierPerformance instance.
     *
     * @param carrierId     The unique identifier of the carrier.
     * @param carrierName   The name of the carrier.
     * @param deliveryCount The total number of deliveries made by the carrier.
     * @param averageRating The average rating received by the carrier from
     *                      customers.
     * @param reviewCount   The total number of reviews received by the carrier.
     * @param totalValue    The total monetary value of deliveries handled by the
     *                      carrier.
     */
    public CarrierPerformance(int carrierId, String carrierName, int deliveryCount, double averageRating,
            int reviewCount, double totalValue) {
        this.carrierId = carrierId;
        this.carrierName = carrierName;
        this.deliveryCount = deliveryCount;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
        this.totalValue = totalValue;
    }

    public int getCarrierId() {
        return carrierId;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public int getDeliveryCount() {
        return deliveryCount;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public double getTotalValue() {
        return totalValue;
    }
}
