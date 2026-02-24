package com.greengrocer.setup;

import com.greengrocer.dao.DatabaseAdapter;
import com.greengrocer.util.ImageCompressor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.sql.*;
import javax.imageio.ImageIO;

/**
 * Generates colorful product icons and uploads them to the database.
 * Run this once to populate all products with sample images.
 * 
 * Usage: mvnw exec:java
 * -Dexec.mainClass="com.greengrocer.setup.UploadProductImages"
 */
public class UploadProductImages {

    public static void main(String[] args) {
        System.out.println("🖼️  Generating and uploading product images...\n");

        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT id, name, type FROM ProductInfo")) {

            int count = 0;
            PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE ProductInfo SET image = ? WHERE id = ?");

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String type = rs.getString("type");

                byte[] imageData = generateProductIcon(name, type);
                byte[] compressed = ImageCompressor.compress(imageData);

                updateStmt.setBytes(1, compressed);
                updateStmt.setInt(2, id);
                updateStmt.executeUpdate();

                System.out.printf("  ✅ [%d] %s (%s) - %d bytes%n", id, name, type, compressed.length);
                count++;
            }

            updateStmt.close();
            System.out.printf("\n🎉 Done! %d product images uploaded.%n", count);

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generates a 200x200 colored icon with the product name and a category symbol.
     */
    private static byte[] generateProductIcon(String name, String type) {
        int size = 200;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Background color based on category
        Color bgColor = getCategoryColor(type);
        Color darkBg = bgColor.darker().darker();

        // Gradient background
        GradientPaint gradient = new GradientPaint(0, 0, bgColor, size, size, darkBg);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, size, size);

        // Draw a circle in the center
        Color circleColor = new Color(255, 255, 255, 40);
        g2d.setColor(circleColor);
        g2d.fillOval(30, 20, 140, 140);

        // Category emoji/symbol
        String emoji = getCategoryEmoji(type);
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        FontMetrics emojiMetrics = g2d.getFontMetrics();
        int emojiX = (size - emojiMetrics.stringWidth(emoji)) / 2;
        g2d.drawString(emoji, emojiX, 100);

        // Product name text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = g2d.getFontMetrics();

        // Word wrap if needed
        String displayName = name.length() > 14 ? name.substring(0, 12) + "..." : name;
        int textX = (size - fm.stringWidth(displayName)) / 2;
        g2d.drawString(displayName, textX, 160);

        // Category label at bottom
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.setColor(new Color(255, 255, 255, 180));
        FontMetrics smallFm = g2d.getFontMetrics();
        int typeX = (size - smallFm.stringWidth(type)) / 2;
        g2d.drawString(type, typeX, 185);

        g2d.dispose();

        // Convert to PNG bytes
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private static Color getCategoryColor(String type) {
        if (type == null)
            return new Color(96, 125, 139); // Blue Grey
        return switch (type.toLowerCase()) {
            case "vegetable" -> new Color(76, 175, 80); // Green
            case "fruit" -> new Color(233, 30, 99); // Pink/Red
            case "dairy" -> new Color(33, 150, 243); // Blue
            case "bakery" -> new Color(255, 152, 0); // Orange
            case "meat" -> new Color(121, 85, 72); // Brown
            case "beverages" -> new Color(0, 188, 212); // Cyan
            case "snacks" -> new Color(156, 39, 176); // Purple
            default -> new Color(96, 125, 139); // Blue Grey
        };
    }

    private static String getCategoryEmoji(String type) {
        if (type == null)
            return "📦";
        return switch (type.toLowerCase()) {
            case "vegetable" -> "🥬";
            case "fruit" -> "🍎";
            case "dairy" -> "🥛";
            case "bakery" -> "🍞";
            case "meat" -> "🥩";
            case "beverages" -> "🥤";
            case "snacks" -> "🍿";
            default -> "📦";
        };
    }
}
