package com.greengrocer.controllers;

import com.greengrocer.dao.OrderDAO;
import com.greengrocer.models.Order;
import com.greengrocer.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class CarrierController {
    private User currentUser;
    private OrderDAO orderDAO;
    private ObservableList<Order> availableOrders;
    private ObservableList<Order> myDeliveries;

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label statusLabel;

    // Available Orders Tab
    @FXML
    private TableView<Order> availableTable;
    @FXML
    private TableColumn<Order, Integer> colAvId;
    @FXML
    private TableColumn<Order, String> colAvDate;
    @FXML
    private TableColumn<Order, Double> colAvTotal;
    @FXML
    private javafx.scene.control.TabPane mainTabPane;

    // My Deliveries Tab
    @FXML
    private TableView<Order> deliveryTable;
    @FXML
    private TableColumn<Order, Integer> colDelId;
    @FXML
    private TableColumn<Order, String> colDelDate;
    @FXML
    private TableColumn<Order, Double> colDelTotal;
    @FXML
    private TableColumn<Order, String> colDelStatus;

    // Leaderboard Tab
    @FXML
    private TableView<Object[]> leaderboardTable;
    @FXML
    private TableColumn<Object[], Integer> colRank;
    @FXML
    private TableColumn<Object[], String> colCarrierName;
    @FXML
    private TableColumn<Object[], Integer> colDeliveries;
    @FXML
    private TableColumn<Object[], Double> colTotalValue;
    @FXML
    private Label myRankLabel;

    private com.greengrocer.dao.ReportDAO reportDAO;

    public CarrierController() {
        this.orderDAO = new OrderDAO();
        this.reportDAO = new com.greengrocer.dao.ReportDAO();
        this.availableOrders = FXCollections.observableArrayList();
        this.myDeliveries = FXCollections.observableArrayList();
    }

    public void initData(User user) {
        this.currentUser = user;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Carrier: " + user.getFirstName());
        }

        // Setup Available Table
        colAvId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAvDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colAvTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        // Setup Delivery Table
        colDelId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDelDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colDelTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colDelStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadAvailableOrders();
        loadMyDeliveries();

        // Setup tab change listener for auto-refresh
        if (mainTabPane != null) {
            mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab != null) {
                    String tabText = newTab.getText();
                    if (tabText.contains("Available")) {
                        loadAvailableOrders();
                    } else if (tabText.contains("Deliveries")) {
                        loadMyDeliveries();
                    } else if (tabText.contains("Leaderboard")) {
                        loadLeaderboard();
                    }
                }
            });
        }
    }

    @FXML
    public void handleRefresh() {
        loadAvailableOrders();
        loadMyDeliveries();
        loadLeaderboard();
    }

    private void loadAvailableOrders() {
        try {
            availableOrders = FXCollections.observableArrayList(orderDAO.getPendingOrders());
            availableTable.setItems(availableOrders);
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading available orders.");
        }
    }

    private void loadMyDeliveries() {
        try {
            myDeliveries = FXCollections
                    .observableArrayList(orderDAO.getOrdersByCarrierAndStatus(currentUser.getId(), "Delivering"));
            deliveryTable.setItems(myDeliveries);
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading your deliveries.");
        }
    }

    @FXML
    public void handleTakeOrder() {
        Order selected = availableTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select an order to take.");
            return;
        }

        try {
            if (orderDAO.assignCarrier(selected.getId(), currentUser.getId())) {
                statusLabel.setText("Order assigned to you!");
                loadAvailableOrders();
                loadMyDeliveries();
            } else {
                statusLabel.setText("Failed to assign order (maybe taken).");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Database error.");
        }
    }

    @FXML
    public void handleCompleteDelivery() {
        Order selected = deliveryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select an order to complete.");
            return;
        }

        try {
            if (orderDAO.updateOrderStatus(selected.getId(), "Delivered")) {
                statusLabel.setText("Order marked as Delivered!");
                loadMyDeliveries();
            } else {
                statusLabel.setText("Failed to update status.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Database error.");
        }
    }

    @FXML
    public void handleLogout() {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(
                    new java.io.File("src/com/greengrocer/views/login.fxml").toURI().toURL());
            javafx.stage.Stage stage = (javafx.stage.Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(com.greengrocer.util.StyleHelper.createStyledScene(root, 960, 540));
            stage.setTitle("Greengrocer Login");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==================== LEADERBOARD ====================

    @FXML
    public void handleRefreshLeaderboard() {
        loadLeaderboard();
    }

    private void loadLeaderboard() {
        if (leaderboardTable == null)
            return;

        try {
            // Setup columns
            colRank.setCellValueFactory(
                    data -> new javafx.beans.property.SimpleIntegerProperty((Integer) data.getValue()[0]).asObject());
            colCarrierName.setCellValueFactory(
                    data -> new javafx.beans.property.SimpleStringProperty((String) data.getValue()[1]));
            colDeliveries.setCellValueFactory(
                    data -> new javafx.beans.property.SimpleIntegerProperty((Integer) data.getValue()[2]).asObject());
            colTotalValue.setCellValueFactory(
                    data -> new javafx.beans.property.SimpleDoubleProperty((Double) data.getValue()[3]).asObject());

            java.util.List<Object[]> leaderboard = reportDAO.getCarrierLeaderboard();
            leaderboardTable.setItems(FXCollections.observableArrayList(leaderboard));

            // Find current user's rank
            String myName = currentUser.getFirstName() + " " + currentUser.getLastName();
            for (Object[] row : leaderboard) {
                if (myName.equals(row[1])) {
                    int rank = (Integer) row[0];
                    int deliveries = (Integer) row[2];
                    String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : "#" + rank;
                    myRankLabel.setText(medal + " Your Rank: " + rank + ". (" + deliveries + " deliveries)");
                    if (rank <= 3) {
                        myRankLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF9800;");
                    }
                    break;
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }
}
