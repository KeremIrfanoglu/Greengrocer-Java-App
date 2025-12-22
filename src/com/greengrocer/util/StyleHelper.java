package com.greengrocer.util;

import javafx.scene.Scene;
import java.io.File;
import java.net.MalformedURLException;

/**
 * Utility class for applying the application's CSS stylesheet to scenes.
 */
public class StyleHelper {

    private static final String CSS_PATH = "src/com/greengrocer/views/styles.css";

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
}
