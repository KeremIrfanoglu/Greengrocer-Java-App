package com.greengrocer.util;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;

/**
 * Singleton service for managing background music playback.
 * Provides play, pause, and mute functionality across all application screens.
 */
public class BackgroundMusicService {

    private static BackgroundMusicService instance;
    private MediaPlayer mediaPlayer;
    private boolean muted = false;
    private boolean initialized = false;

    private static final String MUSIC_FILE_PATH = "src/com/greengrocer/assets/background.mp3";
    private static final double DEFAULT_VOLUME = 0.3; // 30% volume for background music

    private BackgroundMusicService() {
        // Private constructor for singleton
    }

    /**
     * Get the singleton instance of BackgroundMusicService.
     */
    public static synchronized BackgroundMusicService getInstance() {
        if (instance == null) {
            instance = new BackgroundMusicService();
        }
        return instance;
    }

    /**
     * Initialize and start playing background music.
     * Music will loop indefinitely.
     */
    public void play() {
        if (initialized && mediaPlayer != null) {
            mediaPlayer.play();
            return;
        }

        try {
            File musicFile = new File(MUSIC_FILE_PATH);
            if (!musicFile.exists()) {
                System.err.println("Background music file not found: " + MUSIC_FILE_PATH);
                return;
            }

            Media media = new Media(musicFile.toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            // Set to loop indefinitely
            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.seek(Duration.ZERO);
                mediaPlayer.play();
            });

            // Set default volume
            mediaPlayer.setVolume(DEFAULT_VOLUME);

            // Start playing
            mediaPlayer.play();
            initialized = true;

            System.out.println("🎵 Background music started");

        } catch (Exception e) {
            System.err.println("Error initializing background music: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Stop the background music.
     */
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    /**
     * Pause the background music.
     */
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    /**
     * Resume the background music.
     */
    public void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    /**
     * Toggle mute state. When muted, volume is set to 0.
     * When unmuted, volume returns to default.
     */
    public void toggleMute() {
        if (mediaPlayer == null)
            return;

        muted = !muted;
        if (muted) {
            mediaPlayer.setVolume(0);
        } else {
            mediaPlayer.setVolume(DEFAULT_VOLUME);
        }
    }

    /**
     * Check if the music is currently muted.
     */
    public boolean isMuted() {
        return muted;
    }

    /**
     * Set the volume (0.0 to 1.0).
     */
    public void setVolume(double volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(Math.max(0, Math.min(1, volume)));
        }
    }

    /**
     * Get the current volume.
     */
    public double getVolume() {
        return mediaPlayer != null ? mediaPlayer.getVolume() : 0;
    }

    /**
     * Check if the service is initialized and ready.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Dispose of the media player resources.
     * Call this when the application is closing.
     */
    public void dispose() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
            initialized = false;
        }
    }
}
