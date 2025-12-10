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
                    stage.setScene(new Scene(root, 960, 540));

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
            stage.setScene(new Scene(root, 960, 540));
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
}
