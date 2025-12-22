package com.greengrocer.controllers;

import com.greengrocer.dao.UserDAO;
import com.greengrocer.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.sql.SQLException;

import com.greengrocer.util.StyleHelper;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private UserDAO userDAO;

    public LoginController() {
        this.userDAO = new UserDAO();
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username and Password are required!");
            return;
        }

        try {
            User user = userDAO.authenticate(username, password);
            if (user != null) {
                // Successful Login
                System.out.println("Login Successful: " + user.getRole());
                errorLabel.setText("Login Successful! Redirecting...");

                // Redirect based on role
                String fxmlFile = "";
                switch (user.getRole().toLowerCase()) {
                    case "owner":
                        fxmlFile = "src/com/greengrocer/views/owner.fxml";
                        break;
                    case "carrier":
                        fxmlFile = "src/com/greengrocer/views/carrier.fxml";
                        break;
                    case "customer":
                    default:
                        fxmlFile = "src/com/greengrocer/views/customer.fxml";
                        break;
                }

                try {
                    FXMLLoader loader = new FXMLLoader(new java.io.File(fxmlFile).toURI().toURL());
                    Parent root = loader.load();

                    // Pass user data to controller
                    if (user.getRole().equalsIgnoreCase("owner")) {
                        OwnerController controller = loader.getController();
                        controller.initData(user);
                    } else if (user.getRole().equalsIgnoreCase("carrier")) {
                        CarrierController controller = loader.getController();
                        controller.initData(user);
                    } else {
                        CustomerController controller = loader.getController();
                        controller.initData(user);
                    }

                    Stage stage = (Stage) usernameField.getScene().getWindow();
                    stage.setTitle(
                            user.getRole().substring(0, 1).toUpperCase() + user.getRole().substring(1) + " Dashboard");
                    stage.setScene(StyleHelper.createStyledScene(root, 960, 540));

                } catch (IOException e) {
                    e.printStackTrace();
                    errorLabel.setText("Error loading dashboard.");
                }

            } else {
                errorLabel.setText("Invalid credentials!");
                showAlert("Authentication Failed", "Invalid username or password.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Database error!");
        }
    }

    @FXML
    public void handleOpenRegister() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Parent root = FXMLLoader.load(new java.io.File("src/com/greengrocer/views/register.fxml").toURI().toURL());
            stage.setTitle("Customer Registration");
            stage.setScene(StyleHelper.createStyledScene(root, 960, 540));
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Could not load registration form.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void handleForgotPassword() {
        String username = usernameField.getText().trim();

        if (username.isEmpty()) {
            errorLabel.setText("Enter your username first.");
            return;
        }

        try {
            User user = userDAO.getUserByUsername(username);
            if (user != null) {
                // Create password change dialog
                javafx.scene.control.Dialog<String[]> dialog = new javafx.scene.control.Dialog<>();
                dialog.setTitle("Change Password");
                dialog.setHeaderText("Change password for: " + username);

                // Set the button types
                javafx.scene.control.ButtonType changeButtonType = new javafx.scene.control.ButtonType("Change",
                        javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(changeButtonType,
                        javafx.scene.control.ButtonType.CANCEL);

                // Create the fields
                javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

                PasswordField oldPasswordField = new PasswordField();
                oldPasswordField.setPromptText("Old Password");
                PasswordField newPasswordField = new PasswordField();
                newPasswordField.setPromptText("New Password");
                PasswordField confirmPasswordField = new PasswordField();
                confirmPasswordField.setPromptText("Confirm New Password");

                grid.add(new Label("Old Password:"), 0, 0);
                grid.add(oldPasswordField, 1, 0);
                grid.add(new Label("New Password:"), 0, 1);
                grid.add(newPasswordField, 1, 1);
                grid.add(new Label("Confirm Password:"), 0, 2);
                grid.add(confirmPasswordField, 1, 2);

                dialog.getDialogPane().setContent(grid);

                // Convert result
                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == changeButtonType) {
                        return new String[] { oldPasswordField.getText(), newPasswordField.getText(),
                                confirmPasswordField.getText() };
                    }
                    return null;
                });

                java.util.Optional<String[]> result = dialog.showAndWait();

                result.ifPresent(passwords -> {
                    String oldPass = passwords[0];
                    String newPass = passwords[1];
                    String confirmPass = passwords[2];

                    if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                        showAlert("Error", "All fields are required.");
                        return;
                    }

                    if (!newPass.equals(confirmPass)) {
                        showAlert("Error", "New passwords do not match.");
                        return;
                    }

                    try {
                        if (!userDAO.verifyOldPassword(user.getId(), oldPass)) {
                            showAlert("Error", "Old password is incorrect.");
                            return;
                        }

                        if (userDAO.updatePassword(user.getId(), newPass)) {
                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                            successAlert.setTitle("Success");
                            successAlert.setHeaderText(null);
                            successAlert.setContentText(
                                    "Password changed successfully! Please login with your new password.");
                            successAlert.showAndWait();
                        } else {
                            showAlert("Error", "Failed to update password.");
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        showAlert("Error", "Database error.");
                    }
                });
            } else {
                errorLabel.setText("Username not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Database error.");
        }
    }
}
