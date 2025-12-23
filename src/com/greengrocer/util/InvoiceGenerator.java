package com.greengrocer.util;

import com.greengrocer.models.CartItem;
import com.greengrocer.models.User;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates PDF invoices for orders.
 * Uses simple text-based approach that can be printed as PDF.
 */
public class InvoiceGenerator {

    /**
     * Generate invoice as byte array (for database storage)
     */
    public static byte[] generateInvoiceBytes(User customer, List<CartItem> items,
            double subtotal, double gPointsUsed, double couponDiscount,
            double vatAmount, double finalTotal, LocalDateTime deliveryDateTime) {

        StringBuilder invoice = new StringBuilder();
        String separator = "═══════════════════════════════════════════════════════\n";
        String thinSeparator = "───────────────────────────────────────────────────────\n";

        // Header
        invoice.append("\n");
        invoice.append(separator);
        invoice.append("                    GROUP10 GREENGROCER\n");
        invoice.append("                       INVOICE / FATURA\n");
        invoice.append(separator);
        invoice.append("\n");

        // Invoice Details
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        invoice.append("Invoice Date: ").append(LocalDateTime.now().format(dtf)).append("\n");
        invoice.append("Invoice No: INV-").append(System.currentTimeMillis() % 100000).append("\n");
        invoice.append("\n");

        // Customer Info
        invoice.append(thinSeparator);
        invoice.append("CUSTOMER INFORMATION\n");
        invoice.append(thinSeparator);
        invoice.append("Name: ").append(customer.getFirstName()).append(" ").append(customer.getLastName())
                .append("\n");
        invoice.append("Phone: ").append(customer.getPhone() != null ? customer.getPhone() : "N/A").append("\n");
        invoice.append("Address: ").append(customer.getAddress() != null ? customer.getAddress() : "N/A").append("\n");
        invoice.append("\n");

        // Delivery Info
        invoice.append(thinSeparator);
        invoice.append("DELIVERY INFORMATION\n");
        invoice.append(thinSeparator);
        if (deliveryDateTime != null) {
            invoice.append("Delivery Date: ").append(deliveryDateTime.toLocalDate()).append("\n");
            invoice.append("Delivery Time: ").append(deliveryDateTime.getHour()).append(":00 - ")
                    .append(deliveryDateTime.getHour() + 2).append(":00\n");
        }
        invoice.append("\n");

        // Items
        invoice.append(thinSeparator);
        invoice.append(String.format("%-25s %8s %8s %10s\n", "PRODUCT", "UNIT", "QTY", "TOTAL"));
        invoice.append(thinSeparator);

        for (CartItem item : items) {
            String name = item.getProductName();
            if (name.length() > 25)
                name = name.substring(0, 22) + "...";
            String unit = item.getProduct().getUnitLabel();
            String qty = String.format("%.2f", item.getQuantity());
            String total = String.format("₺%.2f", item.getTotal());
            invoice.append(String.format("%-25s %8s %8s %10s\n", name, unit, qty, total));
        }
        invoice.append("\n");

        // Totals
        invoice.append(thinSeparator);
        invoice.append(String.format("%-40s %14s\n", "Subtotal:", String.format("₺%.2f", subtotal)));

        if (gPointsUsed > 0) {
            invoice.append(String.format("%-40s %14s\n", "G Points Discount:", String.format("-₺%.2f", gPointsUsed)));
        }
        if (couponDiscount > 0) {
            invoice.append(String.format("%-40s %14s\n", "Coupon Discount:", String.format("-₺%.2f", couponDiscount)));
        }

        invoice.append(String.format("%-40s %14s\n", "VAT (20%):", String.format("+₺%.2f", vatAmount)));
        invoice.append(thinSeparator);
        invoice.append(String.format("%-40s %14s\n", "TOTAL:", String.format("₺%.2f", finalTotal)));
        invoice.append(separator);

        // Footer
        invoice.append("\n");
        invoice.append("Thank you for shopping with Group10 GreenGrocer!\n");
        invoice.append("For questions, contact us at: support@greengrocer.com\n");
        invoice.append("\n");
        invoice.append(separator);

        return invoice.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * Save invoice to file and return the file path
     */
    public static String saveInvoiceToFile(byte[] invoiceBytes, int orderId) {
        String fileName = "invoice_" + orderId + "_" + System.currentTimeMillis() + ".txt";
        String filePath = System.getProperty("user.home") + "/Downloads/" + fileName;

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(invoiceBytes);
            return filePath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Display invoice in a dialog
     */
    public static void showInvoiceDialog(byte[] invoiceBytes) {
        String invoiceText = new String(invoiceBytes, java.nio.charset.StandardCharsets.UTF_8);

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Order Invoice");
        alert.setHeaderText("Your Order Invoice");

        javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(invoiceText);
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px;");
        textArea.setPrefWidth(600);
        textArea.setPrefHeight(500);

        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setMinWidth(650);
        alert.showAndWait();
    }
}
