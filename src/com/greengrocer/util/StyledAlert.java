package com.greengrocer.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.Region;
import javafx.scene.Node;
import javafx.geometry.Side;
import java.util.Optional;

/**
 * Utility class for creating styled popup dialogs that match the application
 * theme
 */
public class StyledAlert {

    // Theme colors
    private static final String BG_PRIMARY = "#1a1a2e";
    private static final String BG_SECONDARY = "#16213e";
    private static final String TEXT_PRIMARY = "#eee";
    private static final String ACCENT_COLOR = "#6366f1";
    private static final String SUCCESS_COLOR = "#4CAF50";
    private static final String WARNING_COLOR = "#ff9800";
    private static final String ERROR_COLOR = "#f44336";

    private static final String BASE_STYLE = "-fx-background-color: linear-gradient(to bottom, " + BG_PRIMARY + ", "
            + BG_SECONDARY + ");" +
            "-fx-font-family: 'Segoe UI', Arial, sans-serif;";

    private static final String HEADER_STYLE = "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;";

    private static final String CONTENT_STYLE = "-fx-text-fill: #cccccc;" +
            "-fx-font-size: 13px;";

    /**
     * Show an information popup with styled design
     */
    public static void showInfo(String title, String header, String content) {
        Alert alert = createStyledAlert(Alert.AlertType.INFORMATION, title, header, content);
        styleInfoAlert(alert);
        alert.showAndWait();
    }

    /**
     * Show a success popup with green accent
     */
    public static void showSuccess(String title, String header, String content) {
        Alert alert = createStyledAlert(Alert.AlertType.INFORMATION, title, header, content);
        styleSuccessAlert(alert);
        alert.showAndWait();
    }

    /**
     * Show a warning popup with orange accent
     */
    public static void showWarning(String title, String header, String content) {
        Alert alert = createStyledAlert(Alert.AlertType.WARNING, title, header, content);
        styleWarningAlert(alert);
        alert.showAndWait();
    }

    /**
     * Show an error popup with red accent
     */
    /**
     * Shows a styled error alert.
     * 
     * @param title   The title of the alert.
     * @param content The error message content.
     */
    public static void showError(String title, String header, String content) {
        Alert alert = createStyledAlert(Alert.AlertType.ERROR, title, header, content);
        styleErrorAlert(alert);
        alert.showAndWait();
    }

    /**
     * Show a confirmation popup and return true if user clicks OK
     */
    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = createStyledAlert(Alert.AlertType.CONFIRMATION, title, header, content);
        styleInfoAlert(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Show a detailed popup with scrollable TextArea
     */
    public static void showDetailed(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);

        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefWidth(500);
        textArea.setPrefHeight(400);
        textArea.setStyle(
                "-fx-control-inner-background: " + BG_SECONDARY + ";" +
                        "-fx-text-fill: #eee;" +
                        "-fx-font-family: 'Consolas', 'Courier New', monospace;" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: " + ACCENT_COLOR + ";" +
                        "-fx-border-width: 1;");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setContent(textArea);
        applyBaseStyle(dialogPane);
        styleInfoAlert(alert);

        dialogPane.setPrefWidth(550);
        dialogPane.setMinHeight(Region.USE_PREF_SIZE);

        alert.showAndWait();
    }

    /**
     * Create a basic styled alert
     */
    private static Alert createStyledAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        DialogPane dialogPane = alert.getDialogPane();
        applyBaseStyle(dialogPane);
        applyIcon(alert);

        return alert;
    }

    /**
     * Apply application icon to alert stage
     */
    private static void applyIcon(Alert alert) {
        StyleHelper.applyAppIcon(alert);
    }

    /**
     * Apply base styling to dialog pane
     */
    private static void applyBaseStyle(DialogPane dialogPane) {
        dialogPane.setStyle(BASE_STYLE);
        dialogPane.setPrefWidth(420);
        dialogPane.setMinHeight(Region.USE_PREF_SIZE);

        // Style the header - add null checks to prevent NPE
        Node headerPanel = dialogPane.lookup(".header-panel");
        if (headerPanel != null) {
            headerPanel.setStyle("-fx-background-color: transparent;");
        }

        // Style content
        Node content = dialogPane.lookup(".content");
        if (content != null) {
            content.setStyle(CONTENT_STYLE);
        }
    }

    /**
     * Style for info/general alerts with purple accent
     */
    private static void styleInfoAlert(Alert alert) {
        DialogPane dp = alert.getDialogPane();
        dp.setStyle(dp.getStyle() +
                "-fx-border-color: " + ACCENT_COLOR + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;");
        styleButtons(dp, ACCENT_COLOR);
    }

    /**
     * Style for success alerts with green accent
     */
    private static void styleSuccessAlert(Alert alert) {
        DialogPane dp = alert.getDialogPane();
        dp.setStyle(dp.getStyle() +
                "-fx-border-color: " + SUCCESS_COLOR + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;");
        styleButtons(dp, SUCCESS_COLOR);
    }

    /**
     * Style for warning alerts with orange accent
     */
    private static void styleWarningAlert(Alert alert) {
        DialogPane dp = alert.getDialogPane();
        dp.setStyle(dp.getStyle() +
                "-fx-border-color: " + WARNING_COLOR + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;");
        styleButtons(dp, WARNING_COLOR);
    }

    /**
     * Style for error alerts with red accent
     */
    private static void styleErrorAlert(Alert alert) {
        DialogPane dp = alert.getDialogPane();
        dp.setStyle(dp.getStyle() +
                "-fx-border-color: " + ERROR_COLOR + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;");
        styleButtons(dp, ERROR_COLOR);
    }

    /**
     * Style buttons with accent color
     */
    private static void styleButtons(DialogPane dp, String accentColor) {
        dp.lookupAll(".button").forEach(node -> {
            node.setStyle(
                    "-fx-background-color: " + accentColor + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 8 20;" +
                            "-fx-background-radius: 5;" +
                            "-fx-cursor: hand;");
        });
    }
}
