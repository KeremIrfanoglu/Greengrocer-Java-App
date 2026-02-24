package com.greengrocer.util;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.control.Dialog;
import java.net.URL;

/**
 * Helper class for applying consistent CSS styles to UI components.
 */
public class StyleHelper {

    /**
     * Applies a primary button style (green background, white text).
     * 
     * @param button The button to style.
     */
    public static void stylePrimaryButton(Button button) {
        // Implementation for styling primary button would go here
        // For example: button.getStyleClass().add("primary-button");
    }

    private static final String CSS_RESOURCE = "/com/greengrocer/views/styles.css";
    private static final String ICON_RESOURCE = "/com/greengrocer/assets/icon.png";

    /**
     * Applies the application's CSS stylesheet to the given scene.
     * 
     * @param scene The scene to apply styles to
     */
    public static void applyStyles(Scene scene) {
        URL cssUrl = StyleHelper.class.getResource(CSS_RESOURCE);
        if (cssUrl != null) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Could not find CSS stylesheet: " + CSS_RESOURCE);
        }
    }

    /**
     * Creates a new Scene with the CSS stylesheet already applied.
     * 
     * @param root   The root node
     * @param width  Scene width
     * @param height Scene height
     * @return A styled Scene
     */
    public static Scene createStyledScene(javafx.scene.Parent root, double width, double height) {
        Scene scene = new Scene(root, width, height);
        applyStyles(scene);
        return scene;
    }

    /**
     * Applies the application icon to the given stage.
     */
    public static void applyAppIcon(Stage stage) {
        try {
            URL iconUrl = StyleHelper.class.getResource(ICON_RESOURCE);
            if (iconUrl != null) {
                stage.getIcons().add(new Image(iconUrl.toExternalForm()));
            }
        } catch (Exception e) {
            // Silently fail if icon can't be loaded
        }
    }

    /**
     * Applies the application icon to the given dialog.
     */
    public static void applyAppIcon(Dialog<?> dialog) {
        try {
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            applyAppIcon(stage);
        } catch (Exception e) {
            // Silently fail
        }
    }
}
