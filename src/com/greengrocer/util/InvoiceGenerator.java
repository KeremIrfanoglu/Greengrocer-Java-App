package com.greengrocer.util;

import com.greengrocer.models.CartItem;
import com.greengrocer.models.User;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates styled HTML invoices that can be saved as PDF from browser.
 */
public class InvoiceGenerator {

        /**
         * Generate invoice as byte array (for database storage)
         */
        public static byte[] generateInvoiceBytes(User customer, List<CartItem> items,
                        double subtotal, double gPointsUsed, double couponDiscount,
                        double vatAmount, double finalTotal, LocalDateTime deliveryDateTime) {
                return generateHtmlInvoice(customer, items, subtotal, gPointsUsed, couponDiscount,
                                vatAmount, finalTotal, deliveryDateTime)
                                .getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }

        /**
         * Generate styled HTML invoice
         */
        public static String generateHtmlInvoice(User customer, List<CartItem> items,
                        double subtotal, double gPointsUsed, double couponDiscount,
                        double vatAmount, double finalTotal, LocalDateTime deliveryDateTime) {

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                String invoiceNo = "INV-" + (System.currentTimeMillis() % 100000);

                StringBuilder html = new StringBuilder();
                html.append("<!DOCTYPE html>\n");
                html.append("<html>\n<head>\n");
                html.append("<meta charset=\"UTF-8\">\n");
                html.append("<title>Invoice ").append(invoiceNo).append("</title>\n");
                html.append("<style>\n");

                // CSS Styling
                html.append("* { margin: 0; padding: 0; box-sizing: border-box; }\n");
                html.append(
                                "body { font-family: 'Segoe UI', Arial, sans-serif; background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%); color: #333; padding: 40px; min-height: 100vh; }\n");
                html.append(
                                ".invoice { max-width: 800px; margin: 0 auto; background: white; border-radius: 20px; box-shadow: 0 20px 60px rgba(0,0,0,0.3); overflow: hidden; }\n");
                html.append(
                                ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 40px; text-align: center; }\n");
                html.append(
                                ".header h1 { font-size: 2.5em; margin-bottom: 10px; text-shadow: 2px 2px 4px rgba(0,0,0,0.2); }\n");
                html.append(".header p { font-size: 1.2em; opacity: 0.9; }\n");
                html.append(
                                ".invoice-info { display: flex; justify-content: space-between; padding: 30px 40px; background: #f8f9fa; border-bottom: 1px solid #eee; }\n");
                html.append(".invoice-info div { }\n");
                html.append(
                                ".invoice-info h3 { color: #667eea; margin-bottom: 10px; font-size: 0.9em; text-transform: uppercase; letter-spacing: 1px; }\n");
                html.append(".invoice-info p { font-size: 1.1em; color: #555; }\n");
                html.append(".section { padding: 30px 40px; }\n");
                html.append(
                                ".section-title { color: #667eea; font-size: 1.3em; margin-bottom: 20px; padding-bottom: 10px; border-bottom: 2px solid #667eea; }\n");
                html.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }\n");
                html.append(
                                "th { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 15px; text-align: left; font-weight: 600; }\n");
                html.append("td { padding: 15px; border-bottom: 1px solid #eee; }\n");
                html.append("tr:hover { background: #f8f9fa; }\n");
                html.append(".totals { background: #f8f9fa; padding: 30px 40px; }\n");
                html.append(
                                ".totals-row { display: flex; justify-content: space-between; padding: 10px 0; font-size: 1.1em; }\n");
                html.append(".totals-row.discount { color: #4CAF50; }\n");
                html.append(".totals-row.vat { color: #FF9800; }\n");
                html.append(
                                ".totals-row.final { font-size: 1.5em; font-weight: bold; color: #667eea; border-top: 2px solid #667eea; padding-top: 20px; margin-top: 10px; }\n");
                html.append(
                                ".footer { text-align: center; padding: 30px; background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%); color: white; }\n");
                html.append(".footer p { opacity: 0.8; margin-bottom: 5px; }\n");
                html.append(
                                ".badge { display: inline-block; background: #4CAF50; color: white; padding: 5px 15px; border-radius: 20px; font-size: 0.9em; }\n");
                html.append("@media print { body { background: white; padding: 0; } .invoice { box-shadow: none; } }\n");

                html.append("</style>\n");
                html.append("</head>\n<body>\n");

                // Invoice container
                html.append("<div class=\"invoice\">\n");

                // Header
                html.append("<div class=\"header\">\n");
                html.append("<h1>GROUP10 GREENGROCER</h1>\n");
                html.append("<p>Fresh Products, Fast Delivery</p>\n");
                html.append("</div>\n");

                // Invoice Info
                html.append("<div class=\"invoice-info\">\n");
                html.append("<div>\n");
                html.append("<h3>Invoice To</h3>\n");
                html.append("<p><strong>").append(customer.getFirstName()).append(" ").append(customer.getLastName())
                                .append("</strong></p>\n");
                html.append("<p>").append(customer.getPhone() != null ? customer.getPhone() : "N/A").append("</p>\n");
                html.append("<p>").append(customer.getAddress() != null ? customer.getAddress() : "N/A")
                                .append("</p>\n");
                html.append("</div>\n");
                html.append("<div style=\"text-align: right;\">\n");
                html.append("<h3>Invoice Details</h3>\n");
                html.append("<p><strong>").append(invoiceNo).append("</strong></p>\n");
                html.append("<p>Date: ").append(LocalDateTime.now().format(dtf)).append("</p>\n");
                if (deliveryDateTime != null) {
                        html.append("<p>Delivery: ").append(deliveryDateTime.toLocalDate()).append(" (")
                                        .append(deliveryDateTime.getHour()).append(":00-")
                                        .append(deliveryDateTime.getHour() + 2)
                                        .append(":00)</p>\n");
                }
                html.append("</div>\n");
                html.append("</div>\n");

                // Products Section
                html.append("<div class=\"section\">\n");
                html.append("<h2 class=\"section-title\">Order Items</h2>\n");
                html.append("<table>\n");
                html.append("<tr><th>Product</th><th>Unit</th><th>Qty</th><th>Unit Price</th><th>Total</th></tr>\n");

                for (CartItem item : items) {
                        html.append("<tr>");
                        html.append("<td>").append(item.getProductName()).append("</td>");
                        html.append("<td>").append(item.getProduct().getUnitLabel()).append("</td>");
                        html.append("<td>").append(String.format("%.2f", item.getQuantity())).append("</td>");
                        html.append("<td>₺").append(String.format("%.2f", item.getPrice())).append("</td>");
                        html.append("<td><strong>₺").append(String.format("%.2f", item.getTotal()))
                                        .append("</strong></td>");
                        html.append("</tr>\n");
                }

                html.append("</table>\n");
                html.append("</div>\n");

                // Totals
                html.append("<div class=\"totals\">\n");
                html.append("<div class=\"totals-row\"><span>Subtotal:</span><span>₺")
                                .append(String.format("%.2f", subtotal))
                                .append("</span></div>\n");

                if (gPointsUsed > 0) {
                        html.append("<div class=\"totals-row discount\"><span>G Points Discount:</span><span>-TL")
                                        .append(String.format("%.2f", gPointsUsed)).append("</span></div>\n");
                }
                if (couponDiscount > 0) {
                        html.append("<div class=\"totals-row discount\"><span>Coupon Discount:</span><span>-TL")
                                        .append(String.format("%.2f", couponDiscount)).append("</span></div>\n");
                }

                html.append("<div class=\"totals-row vat\"><span>VAT (20%):</span><span>+₺")
                                .append(String.format("%.2f", vatAmount)).append("</span></div>\n");
                html.append("<div class=\"totals-row final\"><span>TOTAL:</span><span>₺")
                                .append(String.format("%.2f", finalTotal)).append("</span></div>\n");
                html.append("</div>\n");

                // Footer
                html.append("<div class=\"footer\">\n");
                html.append("<p>Thank you for shopping with Group10 GreenGrocer!</p>\n");
                html.append("<p>For questions: support@greengrocer.com</p>\n");
                html.append("<p style=\"margin-top: 15px;\"><span class=\"badge\">PAID</span></p>\n");
                html.append("</div>\n");

                html.append("</div>\n");
                html.append("</body>\n</html>");

                return html.toString();
        }

        /**
         * Save invoice as HTML file and return the file path
         */
        public static String saveInvoiceToFile(byte[] invoiceBytes, int orderId) {
                String fileName = "invoice_" + orderId + "_" + System.currentTimeMillis() + ".html";
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

                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Order Invoice");
                alert.setHeaderText("Your order has been placed successfully!");

                javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(15);
                content.setStyle("-fx-padding: 10;");

                javafx.scene.control.Label infoLabel = new javafx.scene.control.Label(
                                "Your invoice has been saved to Downloads folder.\n\n" +
                                                "To save as PDF:\n" +
                                                "1. The invoice will open in your browser\n" +
                                                "2. Press Ctrl+P (or Cmd+P on Mac)\n" +
                                                "3. Select 'Save as PDF' as the printer\n" +
                                                "4. Click Save");
                infoLabel.setWrapText(true);
                infoLabel.setStyle("-fx-font-size: 14px;");

                content.getChildren().add(infoLabel);

                alert.getDialogPane().setContent(content);
                alert.getDialogPane().setMinWidth(400);
                alert.showAndWait();
        }
}
