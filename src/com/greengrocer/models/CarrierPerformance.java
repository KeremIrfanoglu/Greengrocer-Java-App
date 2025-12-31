package com.greengrocer.models;

public class CarrierPerformance {
    private int carrierId;
    private String carrierName;
    private int deliveryCount;
    private double averageRating;

    private int reviewCount;
    private double totalValue;

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
