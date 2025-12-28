package com.greengrocer.models;

/**
 * Statistics for product sales analysis.
 * Contains average price, average cost, profit margin, etc.
 */
public class ProductSalesStats {
    private String productName;
    private double quantitySold;
    private double revenue;
    private double totalCost;
    private double avgSellingPrice;
    private double avgCost;
    private double profit;
    private double profitMargin; // percentage

    public ProductSalesStats(String productName, double quantitySold, double revenue, double totalCost) {
        this.productName = productName;
        this.quantitySold = quantitySold;
        this.revenue = revenue;
        this.totalCost = totalCost;

        // Calculate averages and profit
        if (quantitySold > 0) {
            this.avgSellingPrice = revenue / quantitySold;
            this.avgCost = totalCost / quantitySold;
        } else {
            this.avgSellingPrice = 0;
            this.avgCost = 0;
        }

        this.profit = revenue - totalCost;

        if (revenue > 0) {
            this.profitMargin = (profit / revenue) * 100;
        } else {
            this.profitMargin = 0;
        }
    }

    // Constructor for backward compatibility (dead stock, etc.)
    public ProductSalesStats(String productName, double quantitySold, double revenue) {
        this(productName, quantitySold, revenue, 0);
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

    public double getTotalCost() {
        return totalCost;
    }

    public double getAvgSellingPrice() {
        return avgSellingPrice;
    }

    public double getAvgCost() {
        return avgCost;
    }

    public double getProfit() {
        return profit;
    }

    public double getProfitMargin() {
        return profitMargin;
    }
}
