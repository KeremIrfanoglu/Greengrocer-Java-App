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

    public Product(int id, String name, String type, double price, double costPrice, double stock, double threshold,
            byte[] imageData) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.price = price;
        this.costPrice = costPrice;
        this.stock = stock;
        this.threshold = threshold;
        this.imageData = imageData;
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

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    /**
     * Returns a JavaFX Image from the stored byte array.
     * Returns null if no image data exists.
     */
    public Image getImage() {
        if (imageData != null && imageData.length > 0) {
            return new Image(new ByteArrayInputStream(imageData), 50, 50, true, true);
        }
        return null;
    }
}
