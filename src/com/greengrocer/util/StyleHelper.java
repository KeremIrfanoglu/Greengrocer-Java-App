package com.greengrocer.util;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.control.Dialog;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Utility class for applying the application's CSS stylesheet to scenes.
 */
public class StyleHelper {

    private static final String CSS_PATH = "src/com/greengrocer/views/styles.css";
    private static final String ICON_PATH = "src/com/greengrocer/assets/icon.png";

    /**
     * Applies the application's CSS stylesheet to the given scene.
     * 
     * @param scene The scene to apply styles to
     */
    public static void applyStyles(Scene scene) {
        try {
            String cssUrl = new File(CSS_PATH).toURI().toURL().toExternalForm();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(cssUrl);
        } catch (MalformedURLException e) {
            System.err.println("Could not load CSS stylesheet: " + e.getMessage());
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
            URL iconUrl = new File(ICON_PATH).toURI().toURL();
            stage.getIcons().add(new Image(iconUrl.toExternalForm()));
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
