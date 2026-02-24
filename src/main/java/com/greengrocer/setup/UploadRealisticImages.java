package com.greengrocer.setup;

import com.greengrocer.dao.DatabaseAdapter;
import com.greengrocer.util.ImageCompressor;
import java.io.File;
import java.nio.file.Files;
import java.sql.*;
import java.util.Map;

/**
 * Uploads realistic product images from a local folder to the database.
 * Run with: mvnw exec:java
 * -Dexec.mainClass="com.greengrocer.setup.UploadRealisticImages"
 * -Dexec.args="<folder-path>"
 */
public class UploadRealisticImages {

    // Map image file prefix to product name in the database
    private static final Map<String, String> IMAGE_TO_PRODUCT = Map.ofEntries(
            Map.entry("tomato", "Tomato"),
            Map.entry("potato", "Potato"),
            Map.entry("carrot", "Carrot"),
            Map.entry("onion", "Onion"),
            Map.entry("spinach", "Spinach"),
            Map.entry("apple", "Apple"),
            Map.entry("banana", "Banana"),
            Map.entry("orange_fruit", "Orange"),
            Map.entry("strawberry", "Strawberry"),
            Map.entry("milk", "Milk"),
            Map.entry("cheese", "Cheese"),
            Map.entry("yogurt", "Yogurt"),
            Map.entry("bread", "Bread"),
            Map.entry("croissant", "Croissant"),
            Map.entry("chicken_breast", "Chicken Breast"),
            Map.entry("ground_beef", "Ground Beef"),
            Map.entry("orange_juice", "Orange Juice"),
            Map.entry("cola", "Cola"),
            Map.entry("chips", "Chips"),
            Map.entry("cookies", "Cookies"));

    public static void main(String[] args) {
        String folderPath;
        if (args.length > 0) {
            folderPath = args[0];
        } else {
            folderPath = System.getProperty("user.home") +
                    "/.gemini/antigravity/brain/3badc154-f10d-4b72-8804-8757b5e9bf85";
        }

        System.out.println("🖼️  Uploading realistic product images from: " + folderPath + "\n");

        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("❌ Folder does not exist: " + folderPath);
            return;
        }

        File[] pngFiles = folder.listFiles((dir, name) -> name.endsWith(".png"));
        if (pngFiles == null || pngFiles.length == 0) {
            System.err.println("❌ No PNG files found in: " + folderPath);
            return;
        }

        try (Connection conn = DatabaseAdapter.getConnection()) {
            PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE ProductInfo SET image = ? WHERE name = ?");

            int count = 0;
            for (File file : pngFiles) {
                String fileName = file.getName().toLowerCase();

                // Match file name prefix to product name
                String productName = null;
                for (var entry : IMAGE_TO_PRODUCT.entrySet()) {
                    if (fileName.startsWith(entry.getKey())) {
                        productName = entry.getValue();
                        break;
                    }
                }

                if (productName == null) {
                    System.out.println("  ⏭️  Skipping unknown file: " + fileName);
                    continue;
                }

                byte[] rawImage = Files.readAllBytes(file.toPath());
                byte[] compressed = ImageCompressor.compress(rawImage);

                updateStmt.setBytes(1, compressed);
                updateStmt.setString(2, productName);
                int updated = updateStmt.executeUpdate();

                if (updated > 0) {
                    System.out.printf("  ✅ %s → %s (%d KB → %d KB)%n",
                            fileName, productName,
                            rawImage.length / 1024, compressed.length / 1024);
                    count++;
                } else {
                    System.out.printf("  ⚠️  Product '%s' not found in database%n", productName);
                }
            }

            updateStmt.close();
            System.out.printf("\n🎉 Done! %d product images uploaded.%n", count);

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
