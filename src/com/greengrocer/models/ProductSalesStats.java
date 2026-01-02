package com.greengrocer.models;

/**
 * Represents sales statistics for a specific product.
 * Contains average price, average cost, profit margin, etc.
 */
public class ProductSalesStats {
    /**
     * The name of the product.
     */
    private String productName;
    /**
     * The total quantity of the product sold.
     */
    private double quantitySold;
    /**
     * The total revenue generated from the sales of this product.
     */
    private double revenue;
    /**
     * The total cost associated with the quantity sold.
     */
    private double totalCost;
    /**
     * The average selling price per unit of the product.
     */
    private double avgSellingPrice;
    /**
     * The average cost per unit of the product.
     */
    private double avgCost;
    /**
     * The total profit generated from the sales (revenue - totalCost).
     */
    private double profit;
    /**
     * The profit margin as a percentage of revenue.
     */
    private double profitMargin; // percentage

    /**
     * Constructs a new ProductSalesStats instance with detailed sales and cost
     * information.
     *
     * @param productName  The name of the product.
     * @param quantitySold The total quantity of the product sold.
     * @param revenue      The total revenue generated from this product.
     * @param totalCost    The total cost associated with the quantity sold.
     */
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
