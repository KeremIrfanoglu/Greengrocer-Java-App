package com.greengrocer.models;

public class CarrierPerformance {
    private int carrierId;
    private String carrierName;
    private int deliveryCount;
    private double averageRating;

    public CarrierPerformance(int carrierId, String carrierName, int deliveryCount, double averageRating) {
        this.carrierId = carrierId;
        this.carrierName = carrierName;
        this.deliveryCount = deliveryCount;
        this.averageRating = averageRating;
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
}
