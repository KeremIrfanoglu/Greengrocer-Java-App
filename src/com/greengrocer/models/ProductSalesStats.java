package com.greengrocer.models;

public class ProductSalesStats {
    private String productName;
    private double quantitySold;
    private double revenue;

    public ProductSalesStats(String productName, double quantitySold, double revenue) {
        this.productName = productName;
        this.quantitySold = quantitySold;
        this.revenue = revenue;
    }

    public String getProductName() {
        return productName;
    }

    public double getQuantitySold() {
        return quantitySold;
    }

    public double getRevenue() {
        return revenue;
    }
}
