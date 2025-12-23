package com.greengrocer.controllers;

import com.greengrocer.dao.OrderDAO;
import com.greengrocer.dao.CarrierRatingDAO;
import com.greengrocer.models.CarrierRating;
import com.greengrocer.models.Order;
import com.greengrocer.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import com.greengrocer.util.FormatHelper;

/**
 * Controller for the Carrier dashboard with tabs:
 * Available Orders, My Deliveries, Completed, My Ratings, Leaderboard
 */
public class CarrierController {
    private User currentUser;
    private OrderDAO orderDAO;
    private CarrierRatingDAO ratingDAO;
    private ObservableList<Order> availableOrders;
    private ObservableList<Order> myDeliveries;
    private ObservableList<Order> completedOrders;

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label avgRatingLabel;
    @FXML
    private TabPane mainTabPane;

    // Available Orders Tab
    @FXML
    private TableView<Order> availableTable;
    @FXML
    private TableColumn<Order, Integer> colAvId;
    @FXML
    private TableColumn<Order, String> colAvCustomer;
    @FXML
    private TableColumn<Order, Timestamp> colAvDate;
    @FXML
    private TableColumn<Order, Timestamp> colAvScheduled;
    @FXML
    private TableColumn<Order, Double> colAvTotal;

    // Details Panel (using Label instead of TextArea)
    @FXML
    private Label detailCustomerName;
    @FXML
    private Label detailAddress;
    @FXML
    private Label detailScheduledDate;
    @FXML
    private Label detailProductList;
    @FXML
    private Label detailTotal;

    // My Deliveries Tab
    @FXML
    private TableView<Order> deliveryTable;
    @FXML
    private TableColumn<Order, Integer> colDelId;
    @FXML
    private TableColumn<Order, Timestamp> colDelDate;
    @FXML
    private TableColumn<Order, Timestamp> colDelScheduled;
    @FXML
    private TableColumn<Order, Double> colDelTotal;
    @FXML
    private TableColumn<Order, String> colDelStatus;

    // Completed Deliveries Tab
    @FXML
    private TableView<Order> completedTable;
    @FXML
    private TableColumn<Order, Integer> colCompId;
    @FXML
    private TableColumn<Order, Timestamp> colCompDate;
    @FXML
    private TableColumn<Order, Timestamp> colCompDeliveryDate;
    @FXML
    private TableColumn<Order, Double> colCompTotal;

    // My Ratings Tab
    @FXML
    private TableView<CarrierRating> ratingsTable;
    @FXML
    private TableColumn<CarrierRating, Integer> colRatingOrder;
    @FXML
    private TableColumn<CarrierRating, String> colRatingCustomer;
    @FXML
    private TableColumn<CarrierRating, String> colRatingStars;
    @FXML
    private TableColumn<CarrierRating, String> colRatingComment;
    @FXML
    private TableColumn<CarrierRating, Timestamp> colRatingDate;
    @FXML
    private Label ratingsSummaryLabel;

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
    private TableColumn<Object[], Double> colAvgRating;
    @FXML
    private TableColumn<Object[], Integer> colRatingCount;
    @FXML
    private TableColumn<Object[], Double> colTotalValue;
    @FXML
    private Label myRankLabel;

    public CarrierController() {
        this.orderDAO = new OrderDAO();
        this.ratingDAO = new CarrierRatingDAO();
        this.availableOrders = FXCollections.observableArrayList();
        this.myDeliveries = FXCollections.observableArrayList();
        this.completedOrders = FXCollections.observableArrayList();
    }

    public void initData(User user) {
        this.currentUser = user;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Carrier: " + user.getFirstName());
        }

        // Display average rating in header
        updateAverageRatingDisplay();

        setupTables();
        setupSelectionListener();
        loadAvailableOrders();
        loadMyDeliveries();
        loadCompletedOrders();

        // Tab change listener
        if (mainTabPane != null) {
            mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab != null) {
                    String tabText = newTab.getText();
                    if (tabText.contains("Available")) {
                        loadAvailableOrders();
                    } else if (tabText.contains("Deliveries")) {
                        loadMyDeliveries();
                    } else if (tabText.contains("Completed")) {
                        loadCompletedOrders();
                    } else if (tabText.contains("Ratings")) {
                        loadMyRatings();
                    } else if (tabText.contains("Leaderboard")) {
                        loadLeaderboard();
                    }
                }
            });
        }
    }

    private void updateAverageRatingDisplay() {
        try {
            double avg = ratingDAO.getAverageRating(currentUser.getId());
            int count = ratingDAO.getRatingCount(currentUser.getId());
            if (avgRatingLabel != null) {
                if (count > 0) {
                    avgRatingLabel.setText(String.format("⭐ Rating: %.1f (%d reviews)", avg, count));
                } else {
                    avgRatingLabel.setText("⭐ Rating: No reviews yet");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupTables() {
        // Available Orders Table
        colAvId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAvCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colAvDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        if (colAvScheduled != null) {
            colAvScheduled.setCellValueFactory(new PropertyValueFactory<>("deliveryDate"));
        }
        colAvTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        // Deliveries Table
        colDelId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDelDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        if (colDelScheduled != null) {
            colDelScheduled.setCellValueFactory(new PropertyValueFactory<>("deliveryDate"));
        }
        colDelTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colDelStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Completed Table
        colCompId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCompDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        if (colCompDeliveryDate != null) {
            colCompDeliveryDate.setCellValueFactory(new PropertyValueFactory<>("deliveredAt"));
        }
        colCompTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        // Ratings Table
        if (ratingsTable != null) {
            colRatingOrder.setCellValueFactory(new PropertyValueFactory<>("orderId"));
            colRatingCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
            colRatingStars.setCellValueFactory(cellData -> {
                int rating = cellData.getValue().getRating();
                return new javafx.beans.property.SimpleStringProperty("⭐".repeat(rating));
            });
            colRatingComment.setCellValueFactory(new PropertyValueFactory<>("comment"));
            colRatingDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        }
    }

    private void setupSelectionListener() {
        availableTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                showOrderDetails(newSel);
            } else {
                clearOrderDetails();
            }
        });
    }

    private void showOrderDetails(Order order) {
        detailCustomerName.setText(order.getCustomerName() != null ? order.getCustomerName() : "N/A");
        detailAddress.setText(order.getCustomerAddress() != null ? order.getCustomerAddress() : "N/A");
        detailTotal.setText(String.format("Total: %s", FormatHelper.formatCurrency(order.getTotalAmount())));

        // Show scheduled delivery date
        if (detailScheduledDate != null) {
            if (order.getDeliveryDate() != null) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
                detailScheduledDate.setText(sdf.format(order.getDeliveryDate()));
            } else {
                detailScheduledDate.setText("As soon as possible");
            }
        }

        try {
            String products = orderDAO.getOrderDetailsText(order.getId());
            detailProductList.setText(products.isEmpty() ? "No products" : products);
        } catch (SQLException e) {
            detailProductList.setText("Error loading products");
        }
    }

    private void clearOrderDetails() {
        detailCustomerName.setText("-");
        detailAddress.setText("-");
        if (detailScheduledDate != null)
            detailScheduledDate.setText("-");
        detailProductList.setText("-");
        detailTotal.setText("Total: " + FormatHelper.formatCurrency(0));
    }

    @FXML
    public void handleRefresh() {
        loadAvailableOrders();
        loadMyDeliveries();
        loadCompletedOrders();
        updateAverageRatingDisplay();
    }

    private void loadAvailableOrders() {
        try {
            availableOrders = FXCollections.observableArrayList(orderDAO.getPendingOrdersWithDetails());
            availableTable.setItems(availableOrders);
        } catch (SQLException e) {
            e.printStackTrace();
            if (statusLabel != null)
                statusLabel.setText("Error loading available orders.");
        }
    }

    private void loadMyDeliveries() {
        try {
            myDeliveries = FXCollections.observableArrayList(
                    orderDAO.getOrdersByCarrierAndStatus(currentUser.getId(), "Delivering"));
            deliveryTable.setItems(myDeliveries);
        } catch (SQLException e) {
            e.printStackTrace();
            if (statusLabel != null)
                statusLabel.setText("Error loading your deliveries.");
        }
    }

    private void loadCompletedOrders() {
        try {
            completedOrders = FXCollections.observableArrayList(
                    orderDAO.getCompletedDeliveriesByCarrier(currentUser.getId()));
            completedTable.setItems(completedOrders);
        } catch (SQLException e) {
            e.printStackTrace();
            if (statusLabel != null)
                statusLabel.setText("Error loading completed orders.");
        }
    }

    // ==================== MY RATINGS ====================

    @FXML
    public void handleRefreshRatings() {
        loadMyRatings();
    }

    private void loadMyRatings() {
        if (ratingsTable == null)
            return;

        try {
            List<CarrierRating> ratings = ratingDAO.getRatingsForCarrier(currentUser.getId());
            ratingsTable.setItems(FXCollections.observableArrayList(ratings));

            // Update summary
            double avg = ratingDAO.getAverageRating(currentUser.getId());
            int count = ratings.size();
            if (ratingsSummaryLabel != null) {
                if (count > 0) {
                    ratingsSummaryLabel.setText(String.format("Average: %.1f ⭐ from %d reviews", avg, count));
                    ratingsSummaryLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                } else {
                    ratingsSummaryLabel.setText("No ratings yet");
                    ratingsSummaryLabel.setStyle("-fx-text-fill: #888;");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleTakeOrder() {
        Order selected = availableTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            if (statusLabel != null)
                statusLabel.setText("Select an order to take.");
            return;
        }

        try {
            if (orderDAO.assignCarrier(selected.getId(), currentUser.getId())) {
                if (statusLabel != null) {
                    statusLabel.setText("Order #" + selected.getId() + " assigned to you!");
                    statusLabel.setStyle("-fx-text-fill: green;");
                }
                loadAvailableOrders();
                loadMyDeliveries();
                clearOrderDetails();
            } else {
                if (statusLabel != null) {
                    statusLabel.setText("Failed to assign order (maybe already taken).");
                    statusLabel.setStyle("-fx-text-fill: red;");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (statusLabel != null)
                statusLabel.setText("Database error.");
        }
    }

    @FXML
    public void handleCompleteDelivery() {
        Order selected = deliveryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            if (statusLabel != null)
                statusLabel.setText("Select a delivery to complete.");
            return;
        }

        // Check if scheduled delivery date has arrived
        if (selected.getDeliveryDate() != null) {
            java.time.LocalDateTime scheduledDate = selected.getDeliveryDate().toLocalDateTime();
            java.time.LocalDateTime now = java.time.LocalDateTime.now();

            if (now.isBefore(scheduledDate)) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
                if (statusLabel != null) {
                    statusLabel.setText(
                            "Cannot complete before scheduled date: " + sdf.format(selected.getDeliveryDate()));
                    statusLabel.setStyle("-fx-text-fill: orange;");
                }
                return;
            }
        }

        try {
            if (orderDAO.updateOrderStatus(selected.getId(), "Delivered")) {
                if (statusLabel != null) {
                    statusLabel.setText("Order #" + selected.getId() + " marked as Delivered!");
                    statusLabel.setStyle("-fx-text-fill: green;");
                }
                loadMyDeliveries();
                loadCompletedOrders();
            } else {
                if (statusLabel != null) {
                    statusLabel.setText("Failed to update status.");
                    statusLabel.setStyle("-fx-text-fill: red;");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (statusLabel != null)
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
            colAvgRating.setCellValueFactory(
                    data -> new javafx.beans.property.SimpleDoubleProperty((Double) data.getValue()[4]).asObject());
            colRatingCount.setCellValueFactory(
                    data -> new javafx.beans.property.SimpleIntegerProperty((Integer) data.getValue()[5]).asObject());

            // Format rating column to show stars
            colAvgRating.setCellFactory(col -> new TableCell<Object[], Double>() {
                @Override
                protected void updateItem(Double rating, boolean empty) {
                    super.updateItem(rating, empty);
                    if (empty || rating == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.1f ⭐", rating));
                    }
                }
            });

            // Format total value column to show 2 decimal places
            colTotalValue.setCellFactory(col -> new TableCell<Object[], Double>() {
                @Override
                protected void updateItem(Double value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty || value == null) {
                        setText(null);
                    } else {
                        setText(FormatHelper.formatCurrency(value));
                    }
                }
            });

            List<Object[]> leaderboard = ratingDAO.getCarrierLeaderboard();
            leaderboardTable.setItems(FXCollections.observableArrayList(leaderboard));

            // Find current user's rank
            String myName = currentUser.getFirstName() + " " + currentUser.getLastName();
            for (Object[] row : leaderboard) {
                if (myName.equals(row[1])) {
                    int rank = (Integer) row[0];
                    int deliveries = (Integer) row[2];
                    double avgRating = (Double) row[4];
                    String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : "#" + rank;
                    myRankLabel.setText(
                            String.format("%s Rank: %d | %d deliveries | %.1f⭐", medal, rank, deliveries, avgRating));
                    if (rank <= 3) {
                        myRankLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF9800;");
                    }
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
