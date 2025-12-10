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
    // Potentially Customer Name/Address if we join tables, but for now ID reference

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

    public CarrierController() {
        this.orderDAO = new OrderDAO();
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
    }

    @FXML
    public void handleRefresh() {
        loadAvailableOrders();
        loadMyDeliveries();
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
}
