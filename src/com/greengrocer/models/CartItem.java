package com.greengrocer.models;

/**
 * Represents an item in the shopping cart.
 * Applies threshold-based pricing: price doubles (2x) when stock is at or below
 * threshold (scarcity pricing).
 */
public class CartItem {
    private Product product;
    private double quantity;
    private static final double THRESHOLD_MULTIPLIER = 2.0; // 2x price when low stock

    public CartItem(Product product, double quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getProductName() {
        return product.getName();
    }

    /**
     * Returns the effective price per unit.
     * If stock is at or below threshold, price is doubled (scarcity pricing).
     */
    public double getPrice() {
        if (isPriceIncreased()) {
            return product.getPrice() * THRESHOLD_MULTIPLIER;
        }
        return product.getPrice();
    }

    /**
     * Returns the original price without increase.
     */
    public double getOriginalPrice() {
        return product.getPrice();
    }

    /**
     * Checks if this product has increased price due to low stock.
     */
    public boolean isPriceIncreased() {
        return product.getStock() <= product.getThreshold();
    }

    /**
     * Returns the price increase percentage (0 or 100 for 2x).
     */
    public double getPriceIncreasePercent() {
        return isPriceIncreased() ? 100 : 0;
    }

    public double getTotal() {
        return getPrice() * quantity;
    }
}
