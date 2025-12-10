package com.greengrocer.models;

/**
 * Represents an item in the shopping cart.
 * Applies threshold-based pricing: 10% discount when stock is at or below
 * threshold.
 */
public class CartItem {
    private Product product;
    private double quantity;
    private static final double THRESHOLD_DISCOUNT = 0.10; // 10% discount

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
     * If stock is at or below threshold, applies 10% discount.
     */
    public double getPrice() {
        if (isDiscounted()) {
            return product.getPrice() * (1 - THRESHOLD_DISCOUNT);
        }
        return product.getPrice();
    }

    /**
     * Returns the original price without discount.
     */
    public double getOriginalPrice() {
        return product.getPrice();
    }

    /**
     * Checks if this product qualifies for threshold discount.
     */
    public boolean isDiscounted() {
        return product.getStock() <= product.getThreshold();
    }

    /**
     * Returns the discount percentage (0-100).
     */
    public double getDiscountPercent() {
        return isDiscounted() ? THRESHOLD_DISCOUNT * 100 : 0;
    }

    public double getTotal() {
        return getPrice() * quantity;
    }
}
