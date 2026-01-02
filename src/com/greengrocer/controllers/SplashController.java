package com.greengrocer.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import com.greengrocer.util.StyleHelper;
import com.greengrocer.util.BackgroundMusicService;

import java.io.File;
import java.net.URL;

/**
 * Controller for the splash screen with video playback.
 * Plays a video on application startup before showing the login screen.
 * 
 * <p>
 * Features:
 * </p>
 * <ul>
 * <li>Auto-advances to login when video ends</li>
 * <li>Skip button to bypass video</li>
 * <li>Enter key to skip video</li>
 * <li>Fallback to login if video fails to load</li>
 * </ul>
 * 
 * @author Group10
 * @version 1.0
 */
public class SplashController {

    @FXML
    private MediaView mediaView;

    @FXML
    private Button skipButton;

    private MediaPlayer mediaPlayer;
    private Stage stage;
    private boolean hasNavigated = false;

    /**
     * Sets the stage reference for navigation.
     * 
     * @param stage The primary stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Initializes the splash screen and starts video playback.
     * Called automatically after FXML loading.
     */
    @FXML
    public void initialize() {
        try {
            // Load video from assets
            File videoFile = new File("src/com/greengrocer/assets/intro.mp4");
            if (!videoFile.exists()) {
                System.out.println("Splash video not found, skipping to login...");
                // Delay navigation slightly to let stage be set
                javafx.application.Platform.runLater(() -> {
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                    }
                    navigateToLogin();
                });
                return;
            }

            Media media = new Media(videoFile.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);

            // Auto-navigate when video ends
            mediaPlayer.setOnEndOfMedia(this::navigateToLogin);

            // Handle errors
            mediaPlayer.setOnError(() -> {
                System.err.println("Media error: " + mediaPlayer.getError());
                navigateToLogin();
            });

            // Start playing
            mediaPlayer.play();

        } catch (Exception e) {
            System.err.println("Error loading splash video: " + e.getMessage());
            javafx.application.Platform.runLater(this::navigateToLogin);
        }
    }

    /**
     * Sets up keyboard listener for Enter key to skip.
     * Must be called after scene is set.
     */
    public void setupKeyListener() {
        if (mediaView.getScene() != null) {
            mediaView.getScene().setOnKeyPressed(this::handleKeyPress);
        }
    }

    /**
     * Handle key press events.
     * 
     * @param event The key event
     */
    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.SPACE) {
            handleSkip();
        }
    }

    /**
     * Handles skip button click or Enter key press.
     * Stops video and navigates to login screen.
     */
    @FXML
    public void handleSkip() {
        navigateToLogin();
    }

    /**
     * Navigates to the login screen.
     * Disposes media player and loads login FXML.
     */
    private void navigateToLogin() {
        if (hasNavigated)
            return; // Prevent double navigation
        hasNavigated = true;

        // Clean up media player
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        javafx.application.Platform.runLater(() -> {
            try {
                // Start background music
                BackgroundMusicService.getInstance().play();

                // Load login screen
                URL url = new File("src/com/greengrocer/views/login.fxml").toURI().toURL();
                Parent root = FXMLLoader.load(url);

                Scene scene = new Scene(root, 960, 540);

                // Load CSS
                URL cssUrl = new File("src/com/greengrocer/views/styles.css").toURI().toURL();
                scene.getStylesheets().add(cssUrl.toExternalForm());

                stage.setTitle("Greengrocer Login");
                stage.setScene(scene);
                StyleHelper.applyAppIcon(stage);
                stage.centerOnScreen();

            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error loading login screen: " + e.getMessage());
            }
        });
    }
}
