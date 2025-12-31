package com.greengrocer.models;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javafx.scene.image.Image;

public class Product {
    private int id;
    private String name;
    private String type; // Vegetable, Fruit
    private double price;
    private double costPrice; // Purchase cost for profit calculation
    private double stock;
    private double threshold;
    private byte[] imageData; // Store image as byte array for display
    private String unitType; // "kg" for kilograms (decimal), "pcs" for pieces (integer)

    public Product(int id, String name, String type, double price, double costPrice, double stock, double threshold,
            byte[] imageData) {
        this(id, name, type, price, costPrice, stock, threshold, imageData, "kg"); // Default to kg
    }

    public Product(int id, String name, String type, double price, double costPrice, double stock, double threshold,
            byte[] imageData, String unitType) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.price = price;
        this.costPrice = costPrice;
        this.stock = stock;
        this.threshold = threshold;
        this.imageData = imageData;
        this.unitType = unitType != null ? unitType : "kg";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getStock() {
        return stock;
    }

    public void setStock(double stock) {
        this.stock = stock;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(double costPrice) {
        this.costPrice = costPrice;
    }

    public byte[] getImageData() {
        return imageData;
    }

    private Image cachedImage;

    // ... existing constructor ...

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
        this.cachedImage = null; // Invalidate cache
    }

    /**
     * Returns a JavaFX Image from the stored byte array.
     * Caches the image after first load to prevent expensive re-decoding.
     * Loads at a max width of 200px to optimize memory usage.
     */
    public Image getImage() {
        if (cachedImage != null) {
            return cachedImage;
        }
        if (imageData != null && imageData.length > 0) {
            // Load with requested width 200 to save memory, preserving aspect ratio
            cachedImage = new Image(new ByteArrayInputStream(imageData), 200, 0, true, true);
            return cachedImage;
        }
        return null;
    }

    public String getUnitType() {
        return unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    /**
     * Check if product is sold by kilogram (allows decimal quantities)
     */
    public boolean isSoldByKg() {
        return "kg".equalsIgnoreCase(unitType);
    }

    /**
     * Check if product is sold by piece (integer quantities only)
     */
    public boolean isSoldByPiece() {
        return "piece".equalsIgnoreCase(unitType) || "pcs".equalsIgnoreCase(unitType);
    }

    /**
     * Get display unit label
     */
    public String getUnitLabel() {
        return isSoldByPiece() ? "pc" : "kg";
    }
}
