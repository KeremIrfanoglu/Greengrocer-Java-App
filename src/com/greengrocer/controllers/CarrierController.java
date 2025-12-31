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
import com.greengrocer.util.BackgroundMusicService;

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

    // Messaging
    private com.greengrocer.dao.MessageDAO messageDAO;

    // Communication Tab
    @FXML
    private ListView<Order> communicationOrderList;
    @FXML
    private ListView<Order> communicationDeliveredList;
    private java.util.Map<Integer, Integer> unreadMap = new java.util.HashMap<>();
    @FXML
    private Label commStatusLabel;

    public CarrierController() {
        this.orderDAO = new OrderDAO();
        this.ratingDAO = new CarrierRatingDAO();
        this.messageDAO = new com.greengrocer.dao.MessageDAO();
        this.availableOrders = FXCollections.observableArrayList();
        this.myDeliveries = FXCollections.observableArrayList();
        this.completedOrders = FXCollections.observableArrayList();
    }

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label avgRatingLabel;
    @FXML
    private TabPane mainTabPane;
    @FXML
    private Button musicToggleButton;

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
    @FXML
    private Button statusUpdateBtn;

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
        setupCommunicationListeners(); // Initialize listeners once
        loadCommunicationTab();
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
                    } else if (tabText.contains("Communication")) {
                        loadCommunicationTab();
                    }
                }
            });
        }

        // Enter-to-send for Carrier Messaging
        if (manualMessageInput != null) {
            manualMessageInput.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    String text = manualMessageInput.getText().trim();
                    if (!text.isEmpty()) {
                        handleManualMessageSend();
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
            colAvScheduled.setCellFactory(column -> new TableCell<Order, java.sql.Timestamp>() {
                @Override
                protected void updateItem(java.sql.Timestamp item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(FormatHelper.formatDate(item));
                    }
                }
            });
        }
        colAvDate.setCellFactory(column -> new TableCell<Order, java.sql.Timestamp>() {

            @Override
            protected void updateItem(java.sql.Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(FormatHelper.formatDate(item));
                }
            }
        });
        colAvTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colAvTotal.setCellFactory(column -> new TableCell<Order, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(FormatHelper.formatCurrency(item));
                }
            }
        });

        // Deliveries Table
        colDelId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDelDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        if (colDelScheduled != null) {
            colDelScheduled.setCellValueFactory(new PropertyValueFactory<>("deliveryDate"));
            colDelScheduled.setCellFactory(column -> new TableCell<Order, java.sql.Timestamp>() {
                @Override
                protected void updateItem(java.sql.Timestamp item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(FormatHelper.formatDate(item));
                    }
                }
            });
        }
        colDelDate.setCellFactory(column -> new TableCell<Order, java.sql.Timestamp>() {
            @Override
            protected void updateItem(java.sql.Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(FormatHelper.formatDate(item));
                }
            }
        });
        colDelTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colDelTotal.setCellFactory(column -> new TableCell<Order, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(FormatHelper.formatCurrency(item));
                }
            }
        });

        colDelStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Highlight late deliveries
        deliveryTable.setRowFactory(tv -> new TableRow<Order>() {
            @Override
            protected void updateItem(Order item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    if ("Delivering".equals(item.getStatus()) && item.getDeliveryDate() != null) {
                        if (new java.util.Date().after(item.getDeliveryDate())) {
                            // Deep Red Background for Late
                            setStyle("-fx-background-color: rgba(220, 38, 38, 0.25); -fx-text-fill: #fecaca;");
                        } else {
                            // Normal delivering - slight green tint? Or just default
                            setStyle("");
                        }
                    } else if ("Picked Up".equals(item.getStatus())) {
                        // Deep Blue/Slate Background for Picked Up
                        setStyle("-fx-background-color: rgba(59, 130, 246, 0.15); -fx-text-fill: #bfdbfe;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        // Update Button Logic for Selection
        deliveryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (statusUpdateBtn == null)
                return;

            if (newVal == null) {
                statusUpdateBtn.setText("📦 Update Status");
                statusUpdateBtn.setDisable(true);
                return;
            }

            statusUpdateBtn.setDisable(false);
            String current = newVal.getStatus();
            if ("Picked Up".equals(current)) {
                statusUpdateBtn.setText("🚚 Delivering");
                statusUpdateBtn.getStyleClass().removeAll("button-success", "button-accent");
                if (!statusUpdateBtn.getStyleClass().contains("button-accent"))
                    statusUpdateBtn.getStyleClass().add("button-accent");
            } else if ("Delivering".equals(current)) {
                statusUpdateBtn.setText("✅ Delivered");
                statusUpdateBtn.getStyleClass().removeAll("button-accent", "button-success");
                if (!statusUpdateBtn.getStyleClass().contains("button-success"))
                    statusUpdateBtn.getStyleClass().add("button-success");
            } else {
                statusUpdateBtn.setText("📦 Update Status");
            }
        });

        // Completed Table
        colCompId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCompDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colCompDate.setCellFactory(column -> new TableCell<Order, java.sql.Timestamp>() {
            @Override
            protected void updateItem(java.sql.Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(FormatHelper.formatDate(item));
                }
            }
        });

        if (colCompDeliveryDate != null) {
            colCompDeliveryDate.setCellValueFactory(new PropertyValueFactory<>("deliveredAt")); // Note: was
                                                                                                // deliveryDate, but for
                                                                                                // completed/deliveredAt
                                                                                                // makes more sense or
                                                                                                // keep consistency?
                                                                                                // keeping what is there
                                                                                                // or assuming
                                                                                                // deliveredAt if it
                                                                                                // exists
            // Checking original code: "deliveryDate" was likely used or "deliveredAt".
            // Logic: For completed orders, we probably want the actual completion time
            // (deliveredAt) OR the scheduled time.
            // Let's stick to whatever property was there or use deliveredAt if previously
            // missing.
            // Wait, previous code didn't explicitly set factory for colCompDeliveryDate in
            // the snippet I saw (lines 150-250 only had colAv and colDel).
            // Actually I didn't see colCompDeliveryDate in the snippet of CarrierController
            // I viewed (lines 150-250 only had colAv and colDel).
            // Let's assume standard PropertyValueFactory usage. I will just add the cell
            // factory.
            colCompDeliveryDate.setCellFactory(column -> new TableCell<Order, java.sql.Timestamp>() {
                @Override
                protected void updateItem(java.sql.Timestamp item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(FormatHelper.formatDate(item));
                    }
                }
            });
        }
        if (colCompDeliveryDate != null) {
            colCompDeliveryDate.setCellValueFactory(new PropertyValueFactory<>("deliveredAt"));
        }
        colCompTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colCompTotal.setCellFactory(column -> new TableCell<Order, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(FormatHelper.formatCurrency(item));
                }
            }
        });

        // Ratings Table
        if (ratingsTable != null) {
            colRatingOrder.setCellValueFactory(new PropertyValueFactory<>("orderId"));
            if (colRatingDate != null) {
                colRatingDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
                colRatingDate.setCellFactory(column -> new TableCell<CarrierRating, java.sql.Timestamp>() {
                    @Override
                    protected void updateItem(java.sql.Timestamp item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(FormatHelper.formatDate(item));
                        }
                    }
                });
            }
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
        loadCommunicationTab();
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
                    orderDAO.getActiveDeliveries(currentUser.getId()));
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
    public void handleStatusUpdate() {
        Order selected = deliveryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            if (statusLabel != null)
                statusLabel.setText("Select a delivery to update.");
            return;
        }

        String currentStatus = selected.getStatus();

        try {
            if ("Picked Up".equals(currentStatus)) {
                // Picked Up -> Delivering
                if (orderDAO.updateOrderStatus(selected.getId(), "Delivering")) {
                    if (statusLabel != null) {
                        statusLabel.setText("Delivery started for Order #" + selected.getId());
                        statusLabel.setStyle("-fx-text-fill: green;");
                    }
                    loadMyDeliveries();
                } else {
                    if (statusLabel != null)
                        statusLabel.setText("Failed to start delivery.");
                }
            } else if ("Delivering".equals(currentStatus)) {
                // Delivering -> Delivered
                // Check if scheduled delivery date has arrived
                if (selected.getDeliveryDate() != null) {
                    java.time.LocalDateTime scheduledDate = selected.getDeliveryDate().toLocalDateTime();
                    java.time.LocalDateTime now = java.time.LocalDateTime.now();

                    if (now.isBefore(scheduledDate)) {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
                        if (statusLabel != null) {
                            statusLabel.setText("Wait until " + sdf.format(selected.getDeliveryDate()));
                            statusLabel.setStyle("-fx-text-fill: orange;");
                        }
                        return;
                    }
                }

                if (orderDAO.updateOrderStatus(selected.getId(), "Delivered")) {
                    if (statusLabel != null) {
                        statusLabel.setText("Order #" + selected.getId() + " Delivered!");
                        statusLabel.setStyle("-fx-text-fill: green;");
                    }

                    // Auto-send message to customer
                    try {
                        String subject = String.format("[Order #%d] Delivery Completed", selected.getId());
                        com.greengrocer.models.Message autoMsg = new com.greengrocer.models.Message(
                                currentUser.getId(),
                                selected.getCustomerId(),
                                subject,
                                "Your order has been successfully delivered.");
                        messageDAO.sendMessage(autoMsg);
                    } catch (SQLException e) {
                        e.printStackTrace(); // Log but don't fail the action
                    }

                    loadMyDeliveries();
                    loadCompletedOrders();

                    // Clear selection or re-select if needed. Here we clear since item moves to
                    // completed
                    deliveryTable.getSelectionModel().clearSelection();
                    if (statusUpdateBtn != null) {
                        statusUpdateBtn.setText("📦 Update Status");
                        statusUpdateBtn.setDisable(true);
                    }

                } else {
                    if (statusLabel != null)
                        statusLabel.setText("Failed to complete delivery.");
                }
            }

            // Refresh selection state if still in list
            if (deliveryTable.getSelectionModel().getSelectedItem() != null) {
                // Trigger listener manually or let observable fire
                Order updated = orderDAO.getOrderById(selected.getId()); // improved refresh
                if (updated != null && "Delivering".equals(updated.getStatus())) {
                    // Force refresh of item in table
                    int index = deliveryTable.getItems().indexOf(selected);
                    if (index >= 0) {
                        deliveryTable.getItems().set(index, updated);
                        deliveryTable.getSelectionModel().select(index);
                    }
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
            colTotalValue.setText("💰 Total Value");
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

    // ==================== COMMUNICATION ====================

    @FXML
    private javafx.scene.control.ScrollPane chatScrollPane;
    @FXML
    private javafx.scene.layout.VBox chatMessagesPane;

    @FXML
    private TextField manualMessageInput;

    @FXML
    public void handleSendOnWay() {
        sendPresetMessage("On My Way", "I am on my way to your address.");
    }

    @FXML
    public void handleSendAtAddress() {
        sendPresetMessage("At Address", "I have arrived at your address.");
    }

    @FXML
    public void handleSendDelay() {
        sendPresetMessage("Delay", "There will be a short delay in your delivery. Thank you for your patience.");
    }

    @FXML
    public void handleSendLeftAtDoor() {
        sendPresetMessage("Delivery", "I have left your order at the door.");
    }

    @FXML
    public void handleManualMessageSend() {
        String content = manualMessageInput.getText().trim();
        if (content.isEmpty()) {
            if (commStatusLabel != null) {
                commStatusLabel.setText("Please enter a message.");
                commStatusLabel.setStyle("-fx-text-fill: orange;");
            }
            return;
        }
        // Use a generic subject for manual messages, the threading depends on the tag
        sendPresetMessage("Message", content);
        manualMessageInput.clear();
    }

    private void sendPresetMessage(String subject, String content) {
        if (communicationOrderList == null)
            return;

        Order selected = communicationOrderList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            if (commStatusLabel != null) {
                commStatusLabel.setText("Please select an order first.");
                commStatusLabel.setStyle("-fx-text-fill: red;");
            }
            return;
        }

        try {
            String taggedSubject = String.format("[Order #%d] %s", selected.getId(), subject);
            com.greengrocer.models.Message msg = new com.greengrocer.models.Message(
                    currentUser.getId(),
                    selected.getCustomerId(),
                    taggedSubject,
                    content);
            if (messageDAO.sendMessage(msg)) {
                if (commStatusLabel != null) {
                    commStatusLabel.setText("Message sent: " + subject);
                    commStatusLabel.setStyle("-fx-text-fill: green;");
                }
                loadChatHistory(selected);
                // Refresh the list to show the new message preview
                loadCommunicationTab();
            } else {
                if (commStatusLabel != null) {
                    commStatusLabel.setText("Failed to send message.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (commStatusLabel != null) {
                commStatusLabel.setText("Error sending message.");
            }
        }
    }

    private void loadChatHistory(Order order) {
        if (chatMessagesPane == null || order == null)
            return;

        chatMessagesPane.getChildren().clear();

        try {
            java.util.List<com.greengrocer.models.Message> inbox = messageDAO.getInbox(currentUser.getId());
            java.util.List<com.greengrocer.models.Message> sent = messageDAO.getSentMessages(currentUser.getId());

            // Merge and sort
            java.util.List<com.greengrocer.models.Message> conversation = new java.util.ArrayList<>();
            String orderTag = String.format("[Order #%d]", order.getId());

            for (com.greengrocer.models.Message m : inbox) {
                if (m.getSenderId() == order.getCustomerId() && m.getSubject().contains(orderTag))
                    conversation.add(m);
            }
            for (com.greengrocer.models.Message m : sent) {
                if (m.getReceiverId() == order.getCustomerId() && m.getSubject().contains(orderTag))
                    conversation.add(m);
            }

            conversation.sort(java.util.Comparator.comparing(com.greengrocer.models.Message::getSentAt));

            if (conversation.isEmpty()) {
                Label emptyLabel = new Label("No messages for this order yet.");
                emptyLabel.setStyle("-fx-text-fill: #94A3B8;");
                chatMessagesPane.getChildren().add(emptyLabel);
            } else {
                for (com.greengrocer.models.Message msg : conversation) {
                    chatMessagesPane.getChildren().add(createChatBubble(msg));
                }
            }

            // Scroll to bottom
            javafx.application.Platform.runLater(() -> {
                if (chatScrollPane != null) {
                    chatScrollPane.setVvalue(1.0);
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private javafx.scene.layout.HBox createChatBubble(com.greengrocer.models.Message msg) {
        boolean isSent = msg.getSenderId() == currentUser.getId();

        javafx.scene.layout.HBox container = new javafx.scene.layout.HBox();
        container.setMaxWidth(Double.MAX_VALUE);

        javafx.scene.layout.VBox bubble = new javafx.scene.layout.VBox(5);
        bubble.setMaxWidth(350);
        // Green for me (Sent), Blue/Grey for customer (Received)
        bubble.setStyle(isSent
                ? "-fx-background-color: #4CAF50; -fx-background-radius: 15 15 0 15; -fx-padding: 10;"
                : "-fx-background-color: #334155; -fx-background-radius: 15 15 15 0; -fx-padding: 10;");

        Label contentLabel = new Label(msg.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        contentLabel.setMaxWidth(330);

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        Label timeLabel = new Label(sdf.format(msg.getSentAt()));
        timeLabel.setStyle("-fx-text-fill: " + (isSent ? "#C8E6C9" : "#94A3B8") + "; -fx-font-size: 10px;");

        bubble.getChildren().addAll(contentLabel, timeLabel);

        if (isSent) {
            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            container.getChildren().addAll(spacer, bubble);
        } else {
            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            container.getChildren().addAll(bubble, spacer);
        }

        return container;
    }

    private void loadCommunicationTab() {
        if (communicationOrderList == null || communicationDeliveredList == null)
            return;

        // Save current selection to restore after reload
        int selectedOrderId = -1;
        Order currentActive = communicationOrderList.getSelectionModel().getSelectedItem();
        Order currentDelivered = communicationDeliveredList.getSelectionModel().getSelectedItem();

        if (currentActive != null)
            selectedOrderId = currentActive.getId();
        else if (currentDelivered != null)
            selectedOrderId = currentDelivered.getId();

        try {
            List<com.greengrocer.models.Message> inbox = messageDAO.getInbox(currentUser.getId());
            List<com.greengrocer.models.Message> sent = messageDAO.getSentMessages(currentUser.getId());

            // Map OrderID -> Latest Message
            java.util.Map<Integer, com.greengrocer.models.Message> latestMessageMap = new java.util.HashMap<>();
            // Clear and repopulate the class-level unreadMap
            unreadMap.clear();

            // Process Inbox (for unread counts and latest msg)
            for (com.greengrocer.models.Message msg : inbox) {
                int orderId = extractOrderId(msg.getSubject());
                if (orderId != -1) {
                    if (!msg.isRead()) {
                        unreadMap.put(orderId, unreadMap.getOrDefault(orderId, 0) + 1);
                    }
                    updateLatestMessage(latestMessageMap, orderId, msg);
                }
            }

            // Process Sent (for latest msg)
            for (com.greengrocer.models.Message msg : sent) {
                int orderId = extractOrderId(msg.getSubject());
                if (orderId != -1) {
                    updateLatestMessage(latestMessageMap, orderId, msg);
                }
            }

            // Load Lists
            List<Order> activeOrders = orderDAO.getActiveDeliveries(currentUser.getId());
            communicationOrderList.setItems(FXCollections.observableArrayList(activeOrders));

            List<Order> deliveredOrders = orderDAO.getCompletedDeliveriesByCarrier(currentUser.getId());
            communicationDeliveredList.setItems(FXCollections.observableArrayList(deliveredOrders));

            // Set Custom Cell Factory
            javafx.util.Callback<ListView<Order>, ListCell<Order>> cellFactory = param -> new ListCell<Order>() {
                @Override
                protected void updateItem(Order item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        setStyle("-fx-background-color: transparent;");
                    } else {
                        com.greengrocer.models.Message lastMsg = latestMessageMap.get(item.getId());
                        int unreadCount = unreadMap.getOrDefault(item.getId(), 0);
                        setGraphic(createOrderListCell(item, lastMsg, unreadCount, isSelected()));
                        setText(null);
                        setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                    }
                }
            };

            communicationOrderList.setCellFactory(cellFactory);
            communicationDeliveredList.setCellFactory(cellFactory);

            // Listeners have been moved to setupCommunicationListeners()

            // Restore selection if it existed
            if (selectedOrderId != -1) {
                // Try to find and select in active list
                for (Order o : communicationOrderList.getItems()) {
                    if (o.getId() == selectedOrderId) {
                        communicationOrderList.getSelectionModel().select(o);
                        return;
                    }
                }
                // Try to find and select in delivered list
                for (Order o : communicationDeliveredList.getItems()) {
                    if (o.getId() == selectedOrderId) {
                        communicationDeliveredList.getSelectionModel().select(o);
                        return;
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupCommunicationListeners() {
        if (communicationOrderList == null || communicationDeliveredList == null)
            return;

        communicationOrderList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                communicationDeliveredList.getSelectionModel().clearSelection();
                markMessagesAsRead(newVal);
                loadChatHistory(newVal);
                if (commStatusLabel != null)
                    commStatusLabel.setText("Active: Order #" + newVal.getId());

                // Update badges locally
                unreadMap.put(newVal.getId(), 0);
                communicationOrderList.refresh();
            } else {
                // Only clear if neither is selected (avoid clearing when switching between
                // lists)
                if (communicationDeliveredList.getSelectionModel().getSelectedItem() == null
                        && chatMessagesPane != null)
                    chatMessagesPane.getChildren().clear();
            }
        });

        communicationDeliveredList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                communicationOrderList.getSelectionModel().clearSelection();
                markMessagesAsRead(newVal);
                loadChatHistory(newVal);
                if (commStatusLabel != null)
                    commStatusLabel.setText("Delivered: Order #" + newVal.getId());

                // Update badges locally
                unreadMap.put(newVal.getId(), 0);
                communicationDeliveredList.refresh();
            } else {
                if (communicationOrderList.getSelectionModel().getSelectedItem() == null && chatMessagesPane != null)
                    chatMessagesPane.getChildren().clear();
            }
        });
    }

    private int extractOrderId(String subject) {
        // Case insensitive, optional spaces
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?i)\\[\\s*Order\\s*#\\s*(\\d+)\\s*\\]")
                .matcher(subject);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    private void updateLatestMessage(java.util.Map<Integer, com.greengrocer.models.Message> map, int orderId,
            com.greengrocer.models.Message msg) {
        if (!map.containsKey(orderId) || msg.getSentAt().after(map.get(orderId).getSentAt())) {
            map.put(orderId, msg);
        }
    }

    private javafx.scene.Node createOrderListCell(Order order, com.greengrocer.models.Message lastMsg,
            int unreadCount, boolean isSelected) {
        javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(5);
        // Dark card style
        String bgStyle = isSelected ? "-fx-background-color: #475569;" : "-fx-background-color: #334155;";
        String borderStyle = isSelected ? "-fx-border-color: #4CAF50; -fx-border-width: 0 0 0 4;" : "";
        card.setStyle(
                bgStyle + " -fx-background-radius: 10; -fx-padding: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2); "
                        + borderStyle);

        // Top Row: Title + Time
        javafx.scene.layout.HBox topRow = new javafx.scene.layout.HBox();
        topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label titleLabel = new Label("[Order #" + order.getId() + "]");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label timeLabel = new Label("");
        if (lastMsg != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
            timeLabel.setText(sdf.format(lastMsg.getSentAt()));
        }
        timeLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px;");

        topRow.getChildren().addAll(titleLabel, spacer, timeLabel);

        // Bottom Row: Stats/Preview + Badge
        javafx.scene.layout.HBox bottomRow = new javafx.scene.layout.HBox(5);
        bottomRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        if (lastMsg != null) {
            // Read receipt for sent messages
            if (lastMsg.getSenderId() == currentUser.getId()) {
                Label readStatus = new Label(lastMsg.isRead() ? "✓✓" : "✓");
                // Cyan for read, Gray for sent
                readStatus.setStyle("-fx-text-fill: " + (lastMsg.isRead() ? "#06b6d4" : "#94A3B8")
                        + "; -fx-font-size: 11px; -fx-font-weight: bold;");
                bottomRow.getChildren().add(readStatus);
            }

            String previewText = lastMsg.getContent().replace("\n", " ");
            if (previewText.length() > 25)
                previewText = previewText.substring(0, 25) + "...";
            Label previewLabel = new Label(previewText);
            previewLabel.setStyle("-fx-text-fill: #CBD5E1; -fx-font-size: 12px;");
            bottomRow.getChildren().add(previewLabel);
        } else {
            Label noMsgLabel = new Label("No messages");
            noMsgLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px; -fx-font-style: italic;");
            bottomRow.getChildren().add(noMsgLabel);
        }

        javafx.scene.layout.Region bottomSpacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(bottomSpacer, javafx.scene.layout.Priority.ALWAYS);
        bottomRow.getChildren().add(bottomSpacer);

        // Green Unread Badge
        if (unreadCount > 0) {
            Label badge = new Label(String.valueOf(unreadCount));
            // Green circle badge
            badge.setStyle(
                    "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-background-radius: 10; -fx-min-width: 20; -fx-alignment: center; -fx-padding: 2 6; -fx-font-size: 11px; -fx-font-weight: bold;");
            bottomRow.getChildren().add(badge);
        }

        card.getChildren().addAll(topRow, bottomRow);
        return card;
    }

    private void markMessagesAsRead(Order order) {
        try {
            List<com.greengrocer.models.Message> inbox = messageDAO.getInbox(currentUser.getId());
            String orderTag = String.format("[Order #%d]", order.getId());
            for (com.greengrocer.models.Message msg : inbox) {
                if (!msg.isRead() && msg.getSubject().contains(orderTag)) {
                    messageDAO.markAsRead(msg.getId());
                }
            }
        } catch (SQLException e) {
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
