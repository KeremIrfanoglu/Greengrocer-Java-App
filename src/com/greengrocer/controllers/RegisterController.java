package com.greengrocer.controllers;

import com.greengrocer.dao.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.File;
import java.sql.SQLException;

import com.greengrocer.util.StyleHelper;
import com.greengrocer.util.BackgroundMusicService;
import javafx.scene.control.Button;

/**
 * Controller for the Customer Registration screen.
 * Handles new customer account creation with comprehensive validation
 * for username, password, name, address, and phone number fields.
 * 
 * <p>
 * Validation includes:
 * </p>
 * <ul>
 * <li>Username: 3-20 characters, lowercase alphanumeric only</li>
 * <li>Password: minimum 6 characters with uppercase and number requirement</li>
 * <li>Name: 2-50 characters, letters only</li>
 * <li>Phone: Turkish and international format support</li>
 * </ul>
 * 
 * @author Group10
 * @version 1.0
 * @see LoginController
 */
public class RegisterController {

    /** Text field for username input */
    @FXML
    private TextField usernameField;

    /** Password field for password input */
    @FXML
    private PasswordField passwordField;

    /** Password field for confirming password */
    @FXML
    private PasswordField confirmPasswordField;

    /** Text field for first name input */
    @FXML
    private TextField firstNameField;

    /** Text field for last name input */
    @FXML
    private TextField lastNameField;

    /** Text field for address input */
    @FXML
    private TextField addressField;

    /** Text field for phone number input */
    @FXML
    private TextField phoneField;

    /** Label for displaying status/error messages */
    @FXML
    private Label statusLabel;

    /** Button for toggling background music */
    @FXML
    private Button musicToggleButton;

    /** Data access object for user operations */
    private UserDAO userDAO;

    /**
     * Constructs a new RegisterController and initializes the UserDAO.
     */
    public RegisterController() {
        this.userDAO = new UserDAO();
    }

    /**
     * Handles the register button action.
     * Validates all input fields and creates a new customer account.
     * 
     * <p>
     * Performs comprehensive validation including:
     * </p>
     * <ul>
     * <li>Username uniqueness and format validation</li>
     * <li>Password strength requirements</li>
     * <li>Name format and length validation</li>
     * <li>Phone number format validation (optional)</li>
     * </ul>
     */
    @FXML
    public void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();

        // Required fields validation
        if (username.isEmpty()) {
            showError("Username is required.");
            return;
        }

        if (username.length() < 3) {
            showError("Username must be at least 3 characters.");
            return;
        }

        // Username max length check
        if (username.length() > 20) {
            showError("Username must be at most 20 characters.");
            return;
        }

        // Username must be lowercase letters and numbers only (no special chars, no
        // uppercase)
        if (!username.matches("^[a-z0-9]+$")) {
            showError("Username must contain only lowercase letters and numbers (no special characters).");
            return;
        }

        if (password.isEmpty()) {
            showError("Password is required.");
            return;
        }

        // Strong password validation
        String passwordError = validatePassword(password);
        if (passwordError != null) {
            showError(passwordError);
            return;
        }

        // Confirm password check
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }

        if (firstName.isEmpty() || lastName.isEmpty()) {
            showError("First name and last name are required.");
            return;
        }

        // Name length validation
        if (firstName.length() < 2 || firstName.length() > 50) {
            showError("First name must be 2-50 characters.");
            return;
        }

        if (lastName.length() < 2 || lastName.length() > 50) {
            showError("Last name must be 2-50 characters.");
            return;
        }

        // Name format validation (letters and spaces only)
        if (!firstName.matches("^[a-zA-ZğüşıöçĞÜŞİÖÇ\\s]+$")) {
            showError("First name can only contain letters.");
            return;
        }

        if (!lastName.matches("^[a-zA-ZğüşıöçĞÜŞİÖÇ\\s]+$")) {
            showError("Last name can only contain letters.");
            return;
        }

        // Address length validation (optional but if provided, max 200 chars)
        if (address.length() > 200) {
            showError("Address must be at most 200 characters.");
            return;
        }

        // Phone validation (optional but if provided must be valid)
        if (!phone.isEmpty()) {
            String phoneError = validatePhone(phone);
            if (phoneError != null) {
                showError(phoneError);
                return;
            }
        }

        try {
            boolean success = userDAO.register(username, password, "customer", firstName, lastName, address, phone);
            if (success) {
                statusLabel.setText("Registration successful! Please go back to login.");
                statusLabel.setStyle("-fx-text-fill: #4CAF50;");
                // Clear fields
                usernameField.clear();
                passwordField.clear();
                confirmPasswordField.clear();
                firstNameField.clear();
                lastNameField.clear();
                addressField.clear();
                phoneField.clear();
            } else {
                showError("Username already taken. Please choose another.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error: " + e.getMessage());
        }
    }

    /**
     * Validate password strength
     * 
     * @return error message or null if valid
     */
    private String validatePassword(String password) {
        if (password.length() < 6) {
            return "Password must be at least 6 characters.";
        }

        boolean hasUppercase = false;
        boolean hasNumber = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c))
                hasUppercase = true;
            if (Character.isDigit(c))
                hasNumber = true;
        }

        if (!hasUppercase) {
            return "Password must contain at least 1 uppercase letter.";
        }

        if (!hasNumber) {
            return "Password must contain at least 1 number.";
        }

        return null; // Password is valid
    }

    /**
     * Displays an error message in the status label.
     * 
     * @param message the error message to display
     */
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #f44336;");
    }

    /**
     * Validate phone number format
     * Supports Turkish (+90) and international formats
     * 
     * @return error message or null if valid
     */
    private String validatePhone(String phone) {
        // Remove spaces, dashes, parentheses, and dots for validation
        String cleanPhone = phone.replaceAll("[\\s\\-\\(\\)\\.]", "");

        // Empty check (phone is optional)
        if (cleanPhone.isEmpty()) {
            return null;
        }

        // Check for invalid characters (only digits and optional + at start)
        if (!cleanPhone.matches("^\\+?[0-9]+$")) {
            return "Phone number can only contain digits, spaces, dashes, and optional + prefix.";
        }

        // Turkish phone number validation
        if (cleanPhone.startsWith("+90")) {
            // Turkish format: +90 XXX XXX XX XX (10 digits after +90)
            String turkishNumber = cleanPhone.substring(3);
            if (turkishNumber.length() != 10) {
                return "Turkish phone number must be 10 digits after +90 (e.g., +90 555 123 4567).";
            }
        } else if (cleanPhone.startsWith("90") && cleanPhone.length() == 12) {
            // Turkish format without +: 90 XXX XXX XX XX
            // Valid format - 10 digits after 90
        } else if (cleanPhone.startsWith("0") && cleanPhone.length() == 11) {
            // Local Turkish format: 0XXX XXX XX XX
            // Valid format
        } else if (cleanPhone.length() == 10 && !cleanPhone.startsWith("+")) {
            // Short format: XXX XXX XX XX (10 digits)
            // Valid format
        } else if (cleanPhone.startsWith("+")) {
            // Other international format
            String intlNumber = cleanPhone.substring(1);
            if (intlNumber.length() < 10 || intlNumber.length() > 15) {
                return "International phone number must be 10-15 digits (e.g., +1 555 123 4567).";
            }
        } else {
            // Generic format - must be 10-15 digits
            if (cleanPhone.length() < 10 || cleanPhone.length() > 15) {
                return "Phone number must be 10-15 digits.";
            }
        }

        return null; // Phone is valid
    }

    /**
     * Navigates back to the login screen.
     */
    @FXML
    public void handleBack() {
        try {
            // Navigate back to Login
            Stage stage = (Stage) usernameField.getScene().getWindow();
            // Use File URI for consistency
            Parent root = FXMLLoader.load(new File("src/com/greengrocer/views/login.fxml").toURI().toURL());
            stage.setTitle("Greengrocer Login");
            stage.setScene(StyleHelper.createStyledScene(root, 1200, 750));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Toggle background music mute state.
     */
    @FXML
    public void handleToggleMusic() {
        BackgroundMusicService music = BackgroundMusicService.getInstance();
        music.toggleMute();
        updateMusicButtonIcon();
    }

    /**
     * Updates the music toggle button icon based on current mute state.
     */
    private void updateMusicButtonIcon() {
        if (musicToggleButton != null) {
            if (BackgroundMusicService.getInstance().isMuted()) {
                musicToggleButton.setText("🔇");
            } else {
                musicToggleButton.setText("🔊");
            }
        }
    }
}
