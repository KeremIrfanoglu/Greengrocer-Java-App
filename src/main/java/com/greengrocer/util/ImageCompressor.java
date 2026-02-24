package com.greengrocer.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/**
 * Utility class for compressing and resizing product images before storing
 * them in the database. This keeps the LONGBLOB column small and improves
 * performance when loading product lists.
 * 
 * @author Group10
 * @version 1.0
 */
public class ImageCompressor {

    private static final int MAX_WIDTH = 500;
    private static final int MAX_HEIGHT = 500;
    private static final float JPEG_QUALITY = 0.7f;

    /**
     * Compresses and resizes an image byte array to a max of 200x200 pixels
     * at 70% JPEG quality. Typical output is 10-30KB per image.
     *
     * @param imageData Original image bytes (any format: PNG, JPEG, GIF, etc.)
     * @return Compressed JPEG byte array, or the original data if compression fails
     */
    public static byte[] compress(byte[] imageData) {
        if (imageData == null || imageData.length == 0) {
            return imageData;
        }

        try {
            // Read the original image
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageData));
            if (originalImage == null) {
                return imageData; // Unrecognized format, return as-is
            }

            // Calculate new dimensions maintaining aspect ratio
            int origWidth = originalImage.getWidth();
            int origHeight = originalImage.getHeight();

            // Skip if already small enough
            if (origWidth <= MAX_WIDTH && origHeight <= MAX_HEIGHT && imageData.length < 50000) {
                return imageData;
            }

            double scale = Math.min((double) MAX_WIDTH / origWidth, (double) MAX_HEIGHT / origHeight);
            int newWidth = (int) (origWidth * scale);
            int newHeight = (int) (origHeight * scale);

            if (newWidth < 1)
                newWidth = 1;
            if (newHeight < 1)
                newHeight = 1;

            // Resize the image with high quality
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g2d.dispose();

            // Compress to JPEG
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageWriter jpegWriter = ImageIO.getImageWritersByFormatName("jpeg").next();
            ImageWriteParam param = jpegWriter.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(JPEG_QUALITY);

            ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
            jpegWriter.setOutput(ios);
            jpegWriter.write(null, new IIOImage(resizedImage, null, null), param);
            jpegWriter.dispose();
            ios.close();

            byte[] compressed = baos.toByteArray();

            // Only use compressed version if it's actually smaller
            return compressed.length < imageData.length ? compressed : imageData;

        } catch (IOException e) {
            System.err.println("Image compression failed: " + e.getMessage());
            return imageData; // Return original on error
        }
    }
}
