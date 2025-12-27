package com.greengrocer.models;

public class CarrierPerformance {
    private int carrierId;
    private String carrierName;
    private int deliveryCount;

    public CarrierPerformance(int carrierId, String carrierName, int deliveryCount) {
        this.carrierId = carrierId;
        this.carrierName = carrierName;
        this.deliveryCount = deliveryCount;
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
}
