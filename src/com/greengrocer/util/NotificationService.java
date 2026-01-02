package com.greengrocer.util;

import com.greengrocer.dao.FavoritesDAO;
import com.greengrocer.dao.ProductDAO;
import com.greengrocer.models.Product;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.greengrocer.util.FormatHelper;

/**
 * Service for checking price drops and low stock on favorite products
 * and displaying notifications to the user
 */
public class NotificationService {

    private FavoritesDAO favoritesDAO;
    private ProductDAO productDAO;

    // Store last known prices (in real app, this would be in database)
    private static java.util.Map<Integer, Double> lastKnownPrices = new java.util.HashMap<>();

    public NotificationService() {
        this.favoritesDAO = new FavoritesDAO();
        this.productDAO = new ProductDAO();
    }

    /**
     * Check for price drops in user's favorite products
     * Returns list of products with dropped prices
     */
    public List<Product> checkPriceDrops(int userId) throws SQLException {
        List<Product> droppedProducts = new ArrayList<>();
        List<Integer> favoriteIds = favoritesDAO.getFavoriteProductIds(userId);
        List<Product> allProducts = productDAO.getAllProducts();

        for (Product product : allProducts) {
            if (favoriteIds.contains(product.getId())) {
                Double lastPrice = lastKnownPrices.get(product.getId());
                if (lastPrice != null && product.getPrice() < lastPrice) {
                    droppedProducts.add(product);
                }
                // Update last known price
                lastKnownPrices.put(product.getId(), product.getPrice());
            }
        }

        return droppedProducts;
    }

    /**
     * Check for low stock in user's favorite products
     * Returns list of products below threshold
     */
    public List<Product> checkLowStock(int userId) throws SQLException {
        List<Product> lowStockProducts = new ArrayList<>();
        List<Integer> favoriteIds = favoritesDAO.getFavoriteProductIds(userId);
        List<Product> allProducts = productDAO.getAllProducts();

        for (Product product : allProducts) {
            if (favoriteIds.contains(product.getId())) {
                if (product.getStock() < product.getThreshold()) {
                    lowStockProducts.add(product);
                }
            }
        }

        return lowStockProducts;
    }

    /**
     * Initialize last known prices (call on login)
     */
    public void initializePrices(int userId) throws SQLException {
        List<Integer> favoriteIds = favoritesDAO.getFavoriteProductIds(userId);
        List<Product> allProducts = productDAO.getAllProducts();

        for (Product product : allProducts) {
            if (favoriteIds.contains(product.getId())) {
                lastKnownPrices.put(product.getId(), product.getPrice());
            }
        }
    }

    /**
     * Displays a popup alert for price drops on favorite products.
     * The alert will list the products that have dropped in price and the discount
     * amount.
     *
     * @param products A list of products that have experienced a price drop.
     */
    public static void showPriceDropAlert(List<Product> products) {
        if (products.isEmpty())
            return;

        StringBuilder message = new StringBuilder();
        message.append("🎉 Price drop on your favorite products!\n\n");

        for (Product p : products) {
            Double oldPrice = lastKnownPrices.get(p.getId());
            double newPrice = p.getPrice();
            double discount = oldPrice != null ? oldPrice - newPrice : 0;
            message.append("• ").append(p.getName())
                    .append(": ").append(FormatHelper.formatCurrency(newPrice))
                    .append(" (").append(FormatHelper.formatCurrency(discount)).append(" off)\n");
        }

        StyledAlert.showSuccess("💰 Price Drop!", "Your favorite products are on sale!", message.toString());
    }

    /**
     * Show low stock warning popup
     */
    public static void showLowStockAlert(List<Product> products) {
        if (products.isEmpty())
            return;

        StringBuilder message = new StringBuilder();
        message.append("Low stock on your favorite products!\n\n");

        for (Product p : products) {
            message.append("• ").append(p.getName())
                    .append(": Only ").append(String.format("%.0f", p.getStock()))
                    .append(" left!\n");
        }

        StyledAlert.showWarning("Low Stock Alert!", "Your favorite products are running low!", message.toString());
    }

    /**
     * Check all notifications and display if any
     */
    public void checkAndNotify(int userId) {
        try {
            List<Product> droppedPrices = checkPriceDrops(userId);
            List<Product> lowStock = checkLowStock(userId);

            if (!droppedPrices.isEmpty()) {
                showPriceDropAlert(droppedPrices);
            }

            if (!lowStock.isEmpty()) {
                showLowStockAlert(lowStock);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
