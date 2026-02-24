package com.greengrocer.models;

import java.io.ByteArrayInputStream;
import javafx.scene.image.Image;

/**
 * Represents a product available for purchase in the store.
 * Handles product details, pricing, stock, and image data.
 */
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

    /**
     * Constructs a new Product instance with default unit type "kg".
     *
     * @param id        The unique identifier of the product.
     * @param name      The name of the product.
     * @param type      The category of the product (e.g., "Fruit", "Vegetable").
     * @param price     The selling price of the product.
     * @param costPrice The cost price of the product for the store.
     * @param stock     The current stock quantity.
     * @param threshold The low stock threshold for scarcity pricing.
     * @param imageData The image of the product as a byte array.
     */
    public Product(int id, String name, String type, double price, double costPrice, double stock, double threshold,
            byte[] imageData) {
        this(id, name, type, price, costPrice, stock, threshold, imageData, "kg"); // Default to kg
    }

    /**
     * Constructs a new Product instance with specified unit type.
     *
     * @param id        The unique identifier of the product.
     * @param name      The name of the product.
     * @param type      The category of the product.
     * @param price     The selling price.
     * @param costPrice The cost price.
     * @param stock     The current stock.
     * @param threshold The low stock threshold.
     * @param imageData The image data.
     * @param unitType  The unit of measurement ("kg" or "piece").
     */
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
     * Includes automatic recovery for UTF-8 double-encoded corrupted data.
     */
    public Image getImage() {
        if (cachedImage != null) {
            return cachedImage;
        }
        if (imageData != null && imageData.length > 0) {
            try {
                // First, try loading the image directly
                Image img = new Image(new ByteArrayInputStream(imageData), 200, 0, true, true);

                // If direct load fails, try to recover from UTF-8 double-encoding corruption
                if (img.isError()) {
                    byte[] recoveredData = tryRecoverCorruptedData(imageData);
                    if (recoveredData != null) {
                        img = new Image(new ByteArrayInputStream(recoveredData), 200, 0, true, true);
                        if (!img.isError()) {
                            cachedImage = img;
                            return cachedImage;
                        }
                    }
                    return null;
                }

                cachedImage = img;
                return cachedImage;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Attempts to recover image data that was corrupted by UTF-8 double-encoding.
     * This happens when binary data is incorrectly treated as ISO-8859-1 text
     * and then encoded to UTF-8, causing bytes like FF to become C3 BF.
     */
    private byte[] tryRecoverCorruptedData(byte[] corruptedData) {
        try {
            // Convert bytes to string using UTF-8 (how it was incorrectly encoded)
            String asUtf8 = new String(corruptedData, java.nio.charset.StandardCharsets.UTF_8);
            // Convert back to bytes using ISO-8859-1 (the original binary values)
            byte[] recovered = asUtf8.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);

            // Validate that recovered data looks like an image
            if (recovered.length > 4) {
                // Check for JPEG magic bytes (FF D8 FF)
                if ((recovered[0] & 0xFF) == 0xFF && (recovered[1] & 0xFF) == 0xD8 && (recovered[2] & 0xFF) == 0xFF) {
                    return recovered;
                }
                // Check for PNG magic bytes (89 50 4E 47)
                if ((recovered[0] & 0xFF) == 0x89 && recovered[1] == 'P' && recovered[2] == 'N'
                        && recovered[3] == 'G') {
                    return recovered;
                }
                // Check for GIF magic bytes
                if (recovered[0] == 'G' && recovered[1] == 'I' && recovered[2] == 'F') {
                    return recovered;
                }
                // Check for BMP magic bytes
                if (recovered[0] == 'B' && recovered[1] == 'M') {
                    return recovered;
                }
            }
        } catch (Exception e) {
            // Recovery failed
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
