package com.greengrocer.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Utility class for adding a subtle background image to any pane.
 * The image automatically scales with the parent container.
 */
public class BackgroundHelper {

    private static final String BG_PATH = "/com/greengrocer/assets/background.png";
    private static final double DEFAULT_OPACITY = 0.12;

    /**
     * Adds a background image to the given pane at the specified opacity.
     * The image is inserted at index 0 so it renders behind all other children.
     * It binds to the pane's width and height so it always fills the container.
     *
     * @param pane    the parent pane to add the background to
     * @param opacity the opacity of the background image (0.0 to 1.0)
     */
    public static void addBackground(Pane pane, double opacity) {
        try {
            Image bgImage = new Image(BackgroundHelper.class.getResourceAsStream(BG_PATH));
            ImageView bgView = new ImageView(bgImage);
            bgView.setPreserveRatio(false);
            bgView.setOpacity(opacity);
            bgView.setMouseTransparent(true);

            // Bind to parent size so it always fills
            bgView.fitWidthProperty().bind(pane.widthProperty());
            bgView.fitHeightProperty().bind(pane.heightProperty());

            // Insert at index 0 so it's behind everything
            pane.getChildren().add(0, bgView);
        } catch (Exception e) {
            System.err.println("Background image not found: " + BG_PATH);
        }
    }

    /**
     * Adds a background image with default opacity.
     */
    public static void addBackground(Pane pane) {
        addBackground(pane, DEFAULT_OPACITY);
    }

    /**
     * Wraps the scene's current root in a StackPane and adds a background image
     * behind it.
     * This works for any root element type (VBox, AnchorPane, etc.).
     *
     * @param scene   the scene to add the background to
     * @param opacity the opacity of the background image
     */
    public static void addBackgroundToScene(Scene scene, double opacity) {
        try {
            Parent currentRoot = scene.getRoot();

            // If root is already a StackPane we created, skip
            if (currentRoot.getProperties().containsKey("bgApplied")) {
                return;
            }

            Image bgImage = new Image(BackgroundHelper.class.getResourceAsStream(BG_PATH));
            ImageView bgView = new ImageView(bgImage);
            bgView.setPreserveRatio(false);
            bgView.setOpacity(opacity);
            bgView.setMouseTransparent(true);

            // Create wrapper StackPane
            StackPane wrapper = new StackPane();
            wrapper.getProperties().put("bgApplied", true);

            // Transfer stylesheets and style classes
            wrapper.getStyleClass().addAll(currentRoot.getStyleClass());

            // Bind image size to scene
            bgView.fitWidthProperty().bind(scene.widthProperty());
            bgView.fitHeightProperty().bind(scene.heightProperty());

            // Add background then original content
            wrapper.getChildren().addAll(bgView, currentRoot);

            // Replace scene root
            scene.setRoot(wrapper);
        } catch (Exception e) {
            System.err.println("Background image not found: " + BG_PATH);
        }
    }
}
