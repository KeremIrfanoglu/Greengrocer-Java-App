package com.greengrocer.util;

import com.greengrocer.models.CartItem;
import com.greengrocer.models.User;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.Color;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.greengrocer.util.FormatHelper;

/**
 * Generates PDF invoices using OpenPDF library.
 * PDFs are stored in database and saved to Downloads folder.
 */
public class InvoiceGenerator {

        // Colors for PDF styling
        private static final Color PRIMARY_COLOR = new Color(102, 126, 234);
        private static final Color SUCCESS_COLOR = new Color(76, 175, 80);
        private static final Color TEXT_COLOR = new Color(51, 51, 51);
        private static final Color LIGHT_GRAY = new Color(248, 249, 250);

        /**
         * Generate PDF invoice as byte array (for database storage)
         */
        public static byte[] generateInvoiceBytes(User customer, List<CartItem> items,
                        double subtotal, double gPointsUsed, double couponDiscount,
                        double vatAmount, double finalTotal, LocalDateTime deliveryDateTime) {

                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                try {
                        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
                        PdfWriter writer = PdfWriter.getInstance(document, baos);
                        document.open();

                        // Fonts
                        Font titleFont = new Font(Font.HELVETICA, 24, Font.BOLD, Color.WHITE);
                        Font subtitleFont = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.WHITE);
                        Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, PRIMARY_COLOR);
                        Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL, TEXT_COLOR);
                        Font boldFont = new Font(Font.HELVETICA, 10, Font.BOLD, TEXT_COLOR);
                        Font totalFont = new Font(Font.HELVETICA, 14, Font.BOLD, PRIMARY_COLOR);
                        Font discountFont = new Font(Font.HELVETICA, 10, Font.NORMAL, SUCCESS_COLOR);

                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                        String invoiceNo = "INV-" + (System.currentTimeMillis() % 100000);

                        // ==================== HEADER ====================
                        PdfPTable headerTable = new PdfPTable(1);
                        headerTable.setWidthPercentage(100);

                        PdfPCell headerCell = new PdfPCell();
                        headerCell.setBackgroundColor(PRIMARY_COLOR);
                        headerCell.setPadding(30);
                        headerCell.setBorder(Rectangle.NO_BORDER);
                        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);

                        Paragraph title = new Paragraph("GROUP10 GREENGROCER", titleFont);
                        title.setAlignment(Element.ALIGN_CENTER);
                        headerCell.addElement(title);

                        Paragraph tagline = new Paragraph("Fresh Products, Fast Delivery", subtitleFont);
                        tagline.setAlignment(Element.ALIGN_CENTER);
                        tagline.setSpacingBefore(5);
                        headerCell.addElement(tagline);

                        headerTable.addCell(headerCell);
                        document.add(headerTable);

                        document.add(new Paragraph(" ")); // Spacer

                        // ==================== INVOICE INFO ====================
                        PdfPTable infoTable = new PdfPTable(2);
                        infoTable.setWidthPercentage(100);
                        infoTable.setWidths(new float[] { 1, 1 });

                        // Left side - Customer Info
                        PdfPCell leftCell = new PdfPCell();
                        leftCell.setBorder(Rectangle.NO_BORDER);
                        leftCell.setPadding(10);
                        leftCell.setBackgroundColor(LIGHT_GRAY);

                        leftCell.addElement(new Paragraph("INVOICE TO", headerFont));
                        leftCell.addElement(new Paragraph(customer.getFirstName() + " " + customer.getLastName(),
                                        boldFont));
                        leftCell.addElement(new Paragraph(customer.getPhone() != null ? customer.getPhone() : "N/A",
                                        normalFont));
                        leftCell.addElement(
                                        new Paragraph(customer.getAddress() != null ? customer.getAddress() : "N/A",
                                                        normalFont));
                        infoTable.addCell(leftCell);

                        // Right side - Invoice Details
                        PdfPCell rightCell = new PdfPCell();
                        rightCell.setBorder(Rectangle.NO_BORDER);
                        rightCell.setPadding(10);
                        rightCell.setBackgroundColor(LIGHT_GRAY);
                        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

                        Paragraph invHeader = new Paragraph("INVOICE DETAILS", headerFont);
                        invHeader.setAlignment(Element.ALIGN_RIGHT);
                        rightCell.addElement(invHeader);

                        Paragraph invNo = new Paragraph(invoiceNo, boldFont);
                        invNo.setAlignment(Element.ALIGN_RIGHT);
                        rightCell.addElement(invNo);

                        Paragraph date = new Paragraph("Date: " + LocalDateTime.now().format(dtf), normalFont);
                        date.setAlignment(Element.ALIGN_RIGHT);
                        rightCell.addElement(date);

                        if (deliveryDateTime != null) {
                                Paragraph delivery = new Paragraph(
                                                "Delivery: " + deliveryDateTime.toLocalDate() + " ("
                                                                + deliveryDateTime.getHour() + ":00)",
                                                normalFont);
                                delivery.setAlignment(Element.ALIGN_RIGHT);
                                rightCell.addElement(delivery);
                        }
                        infoTable.addCell(rightCell);

                        document.add(infoTable);
                        document.add(new Paragraph(" ")); // Spacer

                        // ==================== PRODUCTS TABLE ====================
                        Paragraph productsTitle = new Paragraph("ORDER ITEMS", headerFont);
                        productsTitle.setSpacingBefore(10);
                        document.add(productsTitle);

                        PdfPTable productTable = new PdfPTable(5);
                        productTable.setWidthPercentage(100);
                        productTable.setSpacingBefore(10);
                        productTable.setWidths(new float[] { 3, 1, 1, 1.5f, 1.5f });

                        // Table Headers
                        String[] headers = { "Product", "Unit", "Qty", "Unit Price", "Total" };
                        for (String h : headers) {
                                PdfPCell cell = new PdfPCell(
                                                new Phrase(h, new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE)));
                                cell.setBackgroundColor(PRIMARY_COLOR);
                                cell.setPadding(10);
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                productTable.addCell(cell);
                        }

                        // Table Rows
                        for (CartItem item : items) {
                                productTable.addCell(createCell(item.getProductName(), normalFont));
                                productTable.addCell(createCell(item.getProduct().getUnitLabel(), normalFont));
                                productTable.addCell(createCell(String.format("%.2f", item.getQuantity()), normalFont));
                                productTable.addCell(
                                                createCell(FormatHelper.formatCurrency(item.getPrice()), normalFont));
                                productTable.addCell(
                                                createCell(FormatHelper.formatCurrency(item.getTotal()), boldFont));
                        }

                        document.add(productTable);
                        document.add(new Paragraph(" ")); // Spacer

                        // ==================== TOTALS ====================
                        PdfPTable totalsTable = new PdfPTable(2);
                        totalsTable.setWidthPercentage(50);
                        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        totalsTable.setSpacingBefore(10);

                        // Subtotal
                        addTotalRow(totalsTable, "Subtotal:", FormatHelper.formatCurrency(subtotal), normalFont,
                                        normalFont);

                        // G Points
                        if (gPointsUsed > 0) {
                                addTotalRow(totalsTable, "G Points Discount:",
                                                FormatHelper.formatCurrencyWithPrefix(gPointsUsed, "-"),
                                                normalFont, discountFont);
                        }

                        // Coupon
                        if (couponDiscount > 0) {
                                addTotalRow(totalsTable, "Coupon Discount:",
                                                FormatHelper.formatCurrencyWithPrefix(couponDiscount, "-"),
                                                normalFont, discountFont);
                        }

                        // VAT
                        Font vatFont = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(255, 152, 0));
                        addTotalRow(totalsTable, "VAT (20%):", FormatHelper.formatCurrencyWithPrefix(vatAmount, "+"),
                                        normalFont,
                                        vatFont);

                        // Separator line
                        PdfPCell sepCell = new PdfPCell(new Phrase(""));
                        sepCell.setColspan(2);
                        sepCell.setBorderWidthTop(2);
                        sepCell.setBorderColorTop(PRIMARY_COLOR);
                        sepCell.setBorder(Rectangle.TOP);
                        sepCell.setPaddingTop(10);
                        totalsTable.addCell(sepCell);

                        // Final Total
                        addTotalRow(totalsTable, "TOTAL:", FormatHelper.formatCurrency(finalTotal), totalFont,
                                        totalFont);

                        document.add(totalsTable);

                        // ==================== FOOTER ====================
                        document.add(new Paragraph(" "));
                        document.add(new Paragraph(" "));

                        PdfPTable footerTable = new PdfPTable(1);
                        footerTable.setWidthPercentage(100);

                        PdfPCell footerCell = new PdfPCell();
                        footerCell.setBackgroundColor(new Color(26, 26, 46));
                        footerCell.setPadding(20);
                        footerCell.setBorder(Rectangle.NO_BORDER);
                        footerCell.setHorizontalAlignment(Element.ALIGN_CENTER);

                        Font footerFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.WHITE);
                        Paragraph thanks = new Paragraph("Thank you for shopping with Group10 GreenGrocer!",
                                        footerFont);
                        thanks.setAlignment(Element.ALIGN_CENTER);
                        footerCell.addElement(thanks);

                        Paragraph contact = new Paragraph("For questions: support@greengrocer.com", footerFont);
                        contact.setAlignment(Element.ALIGN_CENTER);
                        contact.setSpacingBefore(5);
                        footerCell.addElement(contact);

                        // PAID Badge
                        Paragraph paid = new Paragraph("PAID", new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE));
                        paid.setAlignment(Element.ALIGN_CENTER);
                        paid.setSpacingBefore(15);
                        footerCell.addElement(paid);

                        footerTable.addCell(footerCell);
                        document.add(footerTable);

                        document.close();

                } catch (DocumentException e) {
                        e.printStackTrace();
                        return null;
                }

                return baos.toByteArray();
        }

        /**
         * Helper method to create table cell
         */
        private static PdfPCell createCell(String text, Font font) {
                PdfPCell cell = new PdfPCell(new Phrase(text, font));
                cell.setPadding(8);
                cell.setBorderColor(LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                return cell;
        }

        /**
         * Helper method to add total row
         */
        private static void addTotalRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
                PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
                labelCell.setBorder(Rectangle.NO_BORDER);
                labelCell.setPadding(5);
                labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(labelCell);

                PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
                valueCell.setBorder(Rectangle.NO_BORDER);
                valueCell.setPadding(5);
                valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(valueCell);
        }

        /**
         * Save PDF invoice to file (Downloads folder)
         * 
         * @return the path of the saved file, or null if failed
         */
        public static String saveInvoiceToFile(byte[] invoiceBytes, int orderId) {
                String userHome = System.getProperty("user.home");
                String downloadsPath = userHome + File.separator + "Downloads";
                String fileName = "invoice_" + orderId + ".pdf";
                String filePath = downloadsPath + File.separator + fileName;

                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                        fos.write(invoiceBytes);
                        return filePath;
                } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                }
        }

        /**
         * Show invoice dialog informing user about the saved PDF
         */
        public static void showInvoiceDialog(byte[] invoiceBytes) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.INFORMATION);
                com.greengrocer.util.StyleHelper.applyAppIcon(alert);
                alert.setTitle("Invoice Generated");
                alert.setHeaderText("Your order has been placed successfully!");

                javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(15);
                content.setStyle("-fx-padding: 10;");

                javafx.scene.control.Label infoLabel = new javafx.scene.control.Label(
                                "Your PDF invoice has been saved to Downloads folder.\n\n" +
                                                "The invoice is also stored in our database for your records.");
                infoLabel.setWrapText(true);
                infoLabel.setStyle("-fx-font-size: 14px;");

                content.getChildren().add(infoLabel);

                alert.getDialogPane().setContent(content);
                alert.getDialogPane().setMinWidth(400);
                alert.showAndWait();
        }
}
