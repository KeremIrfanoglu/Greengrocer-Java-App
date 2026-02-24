package com.greengrocer.controllers;

import com.greengrocer.dao.ProductDAO;
import com.greengrocer.dao.ReportDAO;
import com.greengrocer.models.Product;
import com.greengrocer.models.User;
import com.greengrocer.dao.SupplierDAO;
import com.greengrocer.dao.AnalyticsDAO;
import com.greengrocer.dao.CarrierRatingDAO;
import com.greengrocer.models.Supplier;
import com.greengrocer.models.CustomerAnalytics;
import com.greengrocer.models.CarrierPerformance;
import com.greengrocer.models.CarrierRating;
import com.greengrocer.models.ProductSalesStats;
import com.greengrocer.models.HourlyOrderStats;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import com.greengrocer.util.FormatHelper;
import com.greengrocer.util.StyledAlert;

import javafx.geometry.Side;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/**
 * Controller for the Owner/Admin dashboard.
 * Provides comprehensive management capabilities for the greengrocer system
 * including
 * product management, carrier management, order oversight, financial reporting,
 * and analytics.
 * 
 * <p>
 * Key Features:
 * </p>
 * <ul>
 * <li>Products: Add, edit, delete products with image support and stock
 * management</li>
 * <li>Carriers: Manage carrier accounts (add new carriers, view existing)</li>
 * <li>Orders: View and manage all customer orders</li>
 * <li>Reports: Financial reports including revenue, profit/loss, and cost
 * analysis</li>
 * <li>Stock Alerts: Monitor low-stock products</li>
 * <li>Suppliers: Manage supplier information</li>
 * <li>Analytics: Customer analytics, carrier performance, and peak hours
 * analysis</li>
 * <li>Coupons: Create and manage discount coupons</li>
 * <li>Messages: Communication with customers</li>
 * </ul>
 * 
 * @author Group10
 * @version 1.0
 * @see LoginController
 * @see Product
 * @see User
 */
public class OwnerController {

    private User currentUser;
    private ProductDAO productDAO;
    private ObservableList<Product> productList;
    private ObservableList<Product> allProducts; // Master list for filtering
    private File selectedImageFile;

    @FXML
    private javafx.scene.control.TabPane mainTabPane;

    @FXML
    private TextField prodNameField;
    @FXML
    private ComboBox<String> prodTypeCombo;
    @FXML
    private TextField prodPriceField;
    @FXML
    private TextField prodStockField;
    @FXML
    private TextField prodThresholdField;
    @FXML
    private TextField prodCostField;
    @FXML
    private Label statusLabel;
    @FXML
    private javafx.scene.image.ImageView previewImageView;

    private int selectedProductId = -1; // For update functionality
    private Product selectedProduct = null; // For grid view selection

    @FXML
    private FlowPane productFlowPane;

    // Filter/Search Fields
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> filterTypeCombo;
    @FXML
    private ComboBox<String> sortCombo;
    @FXML
    private javafx.scene.control.ScrollPane productScrollPane;

    // Carrier Tab Fields
    @FXML
    private TextField carrUsernameField;
    @FXML
    private PasswordField carrPasswordField;
    @FXML
    private TextField carrNameField;
    @FXML
    private TextField carrSurnameField;
    @FXML
    private TextField carrPhoneField;
    @FXML
    private Label carrierStatusLabel;

    @FXML
    private TableView<User> carrierTable;
    @FXML
    private TableColumn<User, Integer> colCarrId;
    @FXML
    private TableColumn<User, String> colCarrUsername;
    @FXML
    private TableColumn<User, String> colCarrName;
    @FXML
    private TableColumn<User, String> colCarrSurname;
    @FXML
    private TableColumn<User, String> colCarrPhone;

    private com.greengrocer.dao.UserDAO userDAO;
    private ReportDAO reportDAO;

    // Report Tab Fields
    @FXML
    private PieChart productTypeChart;
    @FXML
    private BarChart<String, Number> revenueChart;
    @FXML
    private Label reportStatusLabel;
    @FXML
    private Label totalRevenueLabel;
    @FXML
    private Label totalOrdersLabel;
    @FXML
    private Label totalProductsLabel;
    @FXML
    private Label totalProfitLabel;
    @FXML
    private Label inventoryCostLabel;
    @FXML
    private javafx.scene.control.ComboBox<String> revenueViewCombo;

    // Profit/Loss Table
    @FXML
    private TableView<Object[]> profitLossTable;
    @FXML
    private TableColumn<Object[], String> colPLProduct;
    @FXML
    private TableColumn<Object[], Double> colPLRevenue;
    @FXML
    private TableColumn<Object[], Double> colPLCost;
    @FXML
    private TableColumn<Object[], Double> colPLProfit;

    // Cost Analysis Table
    @FXML
    private TableView<Object[]> costAnalysisTable;
    @FXML
    private TableColumn<Object[], String> colCAProduct;
    @FXML
    private TableColumn<Object[], Double> colCAPrice;
    @FXML
    private TableColumn<Object[], Double> colCACost;
    @FXML
    private TableColumn<Object[], Double> colCAMargin;

    // Order Management Tab Fields
    @FXML
    private TableView<com.greengrocer.models.Order> allOrdersTable;
    @FXML
    private TableColumn<com.greengrocer.models.Order, Integer> colAllOrderId;
    @FXML
    private TableColumn<com.greengrocer.models.Order, Integer> colAllOrderCustomer;
    @FXML
    private TableColumn<com.greengrocer.models.Order, Integer> colAllOrderCarrier;
    @FXML
    private TableColumn<com.greengrocer.models.Order, java.sql.Timestamp> colAllOrderDate;
    @FXML
    private TableColumn<com.greengrocer.models.Order, Double> colAllOrderTotal;
    @FXML
    private TableColumn<com.greengrocer.models.Order, String> colAllOrderStatus;
    @FXML
    private javafx.scene.control.ComboBox<String> orderStatusFilter;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label orderManagementStatus;

    // Stock Alerts Tab
    @FXML
    private TableView<Product> alertsTable;
    @FXML
    private TableColumn<Product, Integer> colAlertId;
    @FXML
    private TableColumn<Product, String> colAlertName;
    @FXML
    private TableColumn<Product, String> colAlertType;
    @FXML
    private TableColumn<Product, Double> colAlertStock;
    @FXML
    private TableColumn<Product, Double> colAlertThreshold;
    @FXML
    private TableColumn<Product, Double> colAlertDiff;
    @FXML
    private Label alertCountLabel;
    // Supplier Fields
    @FXML
    private TextField supplierSearchField;
    @FXML
    private TableView<Supplier> supplierTable;
    @FXML
    private TableColumn<Supplier, Integer> colSupId;
    @FXML
    private TableColumn<Supplier, String> colSupName;
    @FXML
    private TableColumn<Supplier, String> colSupContact;
    @FXML
    private TableColumn<Supplier, String> colSupPhone;
    @FXML
    private TableColumn<Supplier, String> colSupEmail;
    @FXML
    private Label supplierStatusLabel;

    // Customer Analytics Fields
    @FXML
    private TableView<CustomerAnalytics> analyticsTable;
    @FXML
    private TableColumn<CustomerAnalytics, String> colAnUsername;
    @FXML
    private TableColumn<CustomerAnalytics, String> colAnName;
    @FXML
    private TableColumn<CustomerAnalytics, String> colAnPhone;
    @FXML
    private TableColumn<CustomerAnalytics, Integer> colAnOrderCount;
    @FXML
    private TableColumn<CustomerAnalytics, Double> colAnTotalSpent;
    @FXML
    private Label analyticsStatusLabel;

    // Advanced Analytic Fields
    @FXML
    private TabPane analyticsTabPane;

    @FXML
    private TableView<CarrierPerformance> carrierPerformanceTable;
    @FXML
    private TableColumn<CarrierPerformance, Integer> colCpRank;
    @FXML
    private TableColumn<CarrierPerformance, String> colCpName;
    @FXML
    private TableColumn<CarrierPerformance, Integer> colCpDeliveries;
    @FXML
    private TableColumn<CarrierPerformance, Double> colCpRating;
    @FXML
    private TableColumn<CarrierPerformance, Integer> colCpReviews;
    @FXML
    private TableColumn<CarrierPerformance, Double> colCpValue;

    // Reviews Table Fields
    @FXML
    private TableView<CarrierRating> carrierReviewsTable;
    @FXML
    private TableColumn<CarrierRating, Integer> colReviewRating;
    @FXML
    private TableColumn<CarrierRating, String> colReviewComment;
    @FXML
    private TableColumn<CarrierRating, String> colReviewDate;
    @FXML
    private Label carrierReviewsLabel;

    @FXML
    private TableView<ProductSalesStats> topSellingTable;
    @FXML
    private TableColumn<ProductSalesStats, String> colProdName;
    @FXML
    private TableColumn<ProductSalesStats, Double> colProdQty;
    @FXML
    private TableColumn<ProductSalesStats, Double> colProdRev;

    @FXML
    private TableView<ProductSalesStats> deadStockTable;
    @FXML
    private TableColumn<ProductSalesStats, String> colDeadName;

    @FXML
    private BarChart<String, Number> heatmapChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    private SupplierDAO supplierDAO;
    private AnalyticsDAO analyticsDAO;
    private CarrierRatingDAO carrierRatingDAO;
    private com.greengrocer.dao.OrderDAO orderDAO;
    private com.greengrocer.dao.CouponDAO couponDAO;

    // Coupon Tab Fields
    @FXML
    private TextField couponCodeField;
    @FXML
    private TextField couponDiscountField;
    @FXML
    private TextField couponMaxUsesField;
    @FXML
    private TableView<com.greengrocer.models.Coupon> couponTable;
    @FXML
    private TableColumn<com.greengrocer.models.Coupon, String> colCouponCode;
    @FXML
    private TableColumn<com.greengrocer.models.Coupon, Double> colCouponDiscount;
    @FXML
    private TableColumn<com.greengrocer.models.Coupon, Integer> colCouponMaxUses;
    @FXML
    private TableColumn<com.greengrocer.models.Coupon, Integer> colCouponCurrentUses;
    @FXML
    private TableColumn<com.greengrocer.models.Coupon, Boolean> colCouponActive;
    @FXML
    private Label couponStatusLabel;
    @FXML
    private TableView<Object[]> couponHistoryTable;
    @FXML
    private TableColumn<Object[], String> colHistoryCode;
    @FXML
    private TableColumn<Object[], String> colHistoryDate;
    @FXML
    private TableColumn<Object[], String> colHistoryUser;
    @FXML
    private TableColumn<Object[], Double> colHistoryAmount;

    public OwnerController() {
        this.productDAO = new ProductDAO();
        this.userDAO = new com.greengrocer.dao.UserDAO();
        this.reportDAO = new ReportDAO();
        this.orderDAO = new com.greengrocer.dao.OrderDAO();
        this.couponDAO = new com.greengrocer.dao.CouponDAO();
        this.supplierDAO = new SupplierDAO();
        this.analyticsDAO = new AnalyticsDAO();
        this.carrierRatingDAO = new CarrierRatingDAO();
    }

    public void initialize() {
        if (productTypeChart != null) {
            productTypeChart.setLabelsVisible(false);
            productTypeChart.setLegendVisible(true);
        }
    }

    public void initData(User user) {
        this.currentUser = user;
        System.out.println("Owner initialized: " + user.getUsername());

        // Welcome message
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFirstName() + " " + currentUser.getLastName() + "!");
        }

        // prodTypeCombo removed from FXML - dialog now handles type selection

        // Setup tab change listener for auto-refresh
        if (mainTabPane != null) {
            mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab != null) {
                    String tabText = newTab.getText();
                    if (tabText.contains("Products")) {
                        // Just refresh grid, don't reload from database (causes lag)
                        refreshProductGrid();
                        clearFields(); // Clear form when switching tabs
                    } else if (tabText.contains("Reports")) {
                        handleRefreshReports();
                    } else if (tabText.contains("Orders")) {
                        handleRefreshAllOrders();
                    } else if (tabText.contains("Alerts")) {
                        handleRefreshAlerts();
                    } else if (tabText.contains("Suppliers")) {
                        loadSuppliers();
                    } else if (tabText.contains("Customer Analytics")) {
                        loadCustomerAnalytics();
                    } else if (tabText.contains("Coupons")) {
                        loadCoupons();
                        loadCouponHistory();
                    } else if (tabText.contains("Messages")) {
                        handleRefreshOwnerMessages();
                    }
                }
            });
        }

        // Background auto-refresh for messages (every 5 seconds)
        javafx.animation.Timeline msgTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(5), e -> {
                    if (mainTabPane != null && mainTabPane.getSelectionModel().getSelectedItem() != null) {
                        String tabText = mainTabPane.getSelectionModel().getSelectedItem().getText();
                        if (tabText != null && tabText.contains("Messages")) {
                            handleRefreshOwnerMessages();
                        }
                    }
                }));
        msgTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        msgTimeline.play();

        // Grid View is now used - no table setup needed

        // Setup Filter/Sort ComboBoxes
        if (filterTypeCombo != null) {
            filterTypeCombo.setItems(FXCollections.observableArrayList("All", "Vegetable", "Fruit", "Dairy", "Bakery",
                    "Meat", "Beverages", "Snacks"));
            filterTypeCombo.setValue("All");
        }
        if (sortCombo != null) {
            sortCombo.setItems(FXCollections.observableArrayList("Default", "Name (A-Z)", "Name (Z-A)",
                    "Price (Low-High)", "Price (High-Low)", "Stock (Low-High)"));
            sortCombo.setValue("Default");
        }

        // Click on empty area to deselect product and clear form
        if (productFlowPane != null) {
            productFlowPane.setOnMouseClicked(e -> {
                // Only clear if clicking directly on FlowPane, not on a card
                if (e.getTarget() == productFlowPane) {
                    clearFields();
                    statusLabel.setText("Selection cleared.");
                    statusLabel.setStyle("-fx-text-fill: #94A3B8;");
                }
            });
        }

        loadProducts();

        // Setup Carrier Table
        colCarrId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCarrUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colCarrName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colCarrSurname.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colCarrPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        loadCarriers();

        // Setup Orders Table
        colAllOrderId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAllOrderCustomer.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colAllOrderCarrier.setCellValueFactory(new PropertyValueFactory<>("carrierId"));
        colAllOrderDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colAllOrderDate.setCellFactory(column -> new TableCell<com.greengrocer.models.Order, java.sql.Timestamp>() {
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
        colAllOrderTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colAllOrderTotal.setCellFactory(column -> new TableCell<com.greengrocer.models.Order, Double>() {
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
        colAllOrderStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        orderStatusFilter
                .setItems(FXCollections.observableArrayList("All", "Pending", "Delivering", "Delivered", "Cancelled"));
        orderStatusFilter.setValue("All");

        loadAllOrders();

        // Setup Supplier Table
        if (supplierTable != null) {
            colSupId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colSupName.setCellValueFactory(new PropertyValueFactory<>("name"));
            colSupContact.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
            colSupPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
            colSupEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
            loadSuppliers();
        }

        // Setup Analytics Tables
        if (analyticsTabPane != null) {
            setupAnalyticsTabs();
        }

        // Enter-to-send for Owner Messaging
        if (ownerReplyArea != null) {
            ownerReplyArea.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    String text = ownerReplyArea.getText().trim();
                    if (!text.isEmpty()) {
                        handleOwnerSendMessage();
                    }
                }
            });
        }
    }

    /**
     * Helper to add currency formatting to a TableColumn.
     */
    private <T> void formatCurrencyColumn(TableColumn<T, Double> column) {
        column.setCellFactory(col -> new TableCell<T, Double>() {
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
    }

    private void setupAnalyticsTabs() {
        // Customer Columns
        colAnUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colAnName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colAnPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colAnOrderCount.setCellValueFactory(new PropertyValueFactory<>("orderCount"));
        colAnTotalSpent.setCellValueFactory(new PropertyValueFactory<>("totalSpent"));
        colAnTotalSpent.setCellValueFactory(new PropertyValueFactory<>("totalSpent"));
        colAnTotalSpent.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null)
                    setText(null);
                else
                    setText(FormatHelper.formatCurrency(price));
            }
        });

        // Carrier Columns
        colCpRank.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

        colCpName.setCellValueFactory(new PropertyValueFactory<>("carrierName"));
        colCpDeliveries.setCellValueFactory(new PropertyValueFactory<>("deliveryCount"));
        colCpRating.setCellValueFactory(new PropertyValueFactory<>("averageRating"));
        colCpRating.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double rating, boolean empty) {
                super.updateItem(rating, empty);
                if (empty || rating == null)
                    setText(null);
                else
                    setText(String.format("%.1f ★", rating));
            }
        });

        colCpReviews.setCellValueFactory(new PropertyValueFactory<>("reviewCount"));

        colCpValue.setCellValueFactory(new PropertyValueFactory<>("totalValue"));
        colCpValue.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null)
                    setText(null);
                else
                    setText(FormatHelper.formatCurrency(value));
            }
        });

        // Listener for Carrier Selection to Load Reviews
        carrierPerformanceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadCarrierReviews(newVal.getCarrierId(), newVal.getCarrierName());
            } else {
                if (carrierReviewsTable != null)
                    carrierReviewsTable.getItems().clear();
                if (carrierReviewsLabel != null)
                    carrierReviewsLabel.setText("Select a carrier to view reviews");
            }
        });

        // Setup Carrier Reviews Table
        if (carrierReviewsTable != null) {
            colReviewRating.setCellValueFactory(new PropertyValueFactory<>("rating"));
            colReviewComment.setCellValueFactory(new PropertyValueFactory<>("comment"));
            colReviewDate.setCellValueFactory(cellData -> {
                if (cellData.getValue().getCreatedAt() != null) {
                    return new javafx.beans.property.SimpleStringProperty(
                            FormatHelper.formatDate(cellData.getValue().getCreatedAt()));
                }
                return new javafx.beans.property.SimpleStringProperty("");
            });
        }

        // Load Initial Data
        loadCustomerAnalytics();
        loadCarrierAnalytics();
        loadHeatmap();

        // Add Listener to sub-tabs
        analyticsTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                String text = newTab.getText();
                if (text.equals("Customers"))
                    loadCustomerAnalytics();
                else if (text.equals("Carriers"))
                    loadCarrierAnalytics();
                else if (text.equals("Peak Hours"))
                    loadHeatmap();
            }
        });
    }

    @FXML
    public void handleRefreshProducts() {
        loadProducts();
    }

    @FXML
    public void handleClearSelection(javafx.scene.input.MouseEvent event) {
        // Only clear if clicking directly on ScrollPane or FlowPane background, not on
        // a card
        if (event.getTarget() == productScrollPane ||
                event.getTarget() == productFlowPane ||
                event.getTarget() instanceof javafx.scene.control.ScrollPane) {
            clearFields();
            statusLabel.setText("Selection cleared.");
            statusLabel.setStyle("-fx-text-fill: #94A3B8;");
        }
    }

    private void loadProducts() {
        try {
            allProducts = FXCollections.observableArrayList(productDAO.getAllProducts());
            applyFilterAndSort();
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading products.");
            statusLabel.setStyle("-fx-text-fill: #EF4444;");
        }
    }

    // Filter and Sort Methods
    @FXML
    public void handleFilter() {
        applyFilterAndSort();
    }

    @FXML
    public void handleSort() {
        applyFilterAndSort();
    }

    @FXML
    public void handleSearch() {
        applyFilterAndSort();
    }

    @FXML
    public void handleResetProducts() {
        if (filterTypeCombo != null)
            filterTypeCombo.setValue("All");
        if (sortCombo != null)
            sortCombo.setValue("Default");
        if (searchField != null)
            searchField.clear();
        applyFilterAndSort();
        statusLabel.setText("Filters reset.");
        statusLabel.setStyle("-fx-text-fill: #4CAF50;");
    }

    private void applyFilterAndSort() {
        if (allProducts == null)
            return;

        java.util.List<Product> filtered = new java.util.ArrayList<>();

        // Get filter values
        String filterType = filterTypeCombo != null ? filterTypeCombo.getValue() : "All";
        String searchText = searchField != null ? searchField.getText().toLowerCase().trim() : "";

        // Filter by type and search
        for (Product p : allProducts) {
            boolean matchesType = "All".equals(filterType) || p.getType().equals(filterType);
            boolean matchesSearch = searchText.isEmpty() || p.getName().toLowerCase().contains(searchText);
            if (matchesType && matchesSearch) {
                filtered.add(p);
            }
        }

        // Sort
        String sortOption = sortCombo != null ? sortCombo.getValue() : "Default";
        if (sortOption != null) {
            switch (sortOption) {
                case "Name (A-Z)":
                    filtered.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                    break;
                case "Name (Z-A)":
                    filtered.sort((a, b) -> b.getName().compareToIgnoreCase(a.getName()));
                    break;
                case "Price (Low-High)":
                    filtered.sort((a, b) -> Double.compare(a.getPrice(), b.getPrice()));
                    break;
                case "Price (High-Low)":
                    filtered.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice()));
                    break;
                case "Stock (Low-High)":
                    filtered.sort((a, b) -> Double.compare(a.getStock(), b.getStock()));
                    break;
                default:
                    break;
            }
        }

        productList = FXCollections.observableArrayList(filtered);
        refreshProductGrid();
    }

    private void refreshProductGrid() {
        if (productFlowPane == null)
            return;
        productFlowPane.getChildren().clear();

        for (Product product : productList) {
            productFlowPane.getChildren().add(createProductCard(product));
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(6);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(170);
        card.setPrefHeight(280); // Fixed height for consistent alignment
        card.setMinHeight(280);
        card.setMaxHeight(280);
        card.setPadding(new Insets(12));
        card.setAlignment(Pos.TOP_CENTER);

        // Image container with fixed height
        javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView();
        iv.setFitHeight(80);
        iv.setFitWidth(80);
        iv.setPreserveRatio(true);

        // Lazy load image in background thread
        final int productId = product.getId();
        new Thread(() -> {
            try {
                byte[] imgData = new com.greengrocer.dao.ProductDAO().getProductImage(productId);
                if (imgData != null && imgData.length > 0) {
                    javafx.scene.image.Image img = new javafx.scene.image.Image(
                            new java.io.ByteArrayInputStream(imgData), 80, 0, true, true);
                    javafx.application.Platform.runLater(() -> iv.setImage(img));
                }
            } catch (Exception e) {
                // Silently skip - product just won't have an image
            }
        }).start();
        // Wrap in a container with fixed height
        VBox imageContainer = new VBox(iv);
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setPrefHeight(85);
        imageContainer.setMinHeight(85);

        // Name label with fixed height (2 lines max)
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #F8FAFC;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(150);
        nameLabel.setPrefHeight(36);
        nameLabel.setMinHeight(36);
        nameLabel.setAlignment(Pos.CENTER);

        // Type label
        Label typeLabel = new Label(product.getType() + " (" + (product.isSoldByKg() ? "Kg" : "Piece") + ")");
        typeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");
        typeLabel.setPrefHeight(18);

        // Price label
        Label priceLabel = new Label(FormatHelper.formatCurrency(product.getPrice()));
        priceLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 14px;");
        priceLabel.setPrefHeight(20);

        // Stock label
        String stockText;
        if (product.isSoldByKg()) {
            stockText = String.format("%.1f kg", product.getStock());
        } else {
            stockText = String.valueOf((int) product.getStock());
        }
        Label stockLabel = new Label("Stock: " + stockText);

        if (product.getStock() <= product.getThreshold()) {
            stockLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
        } else {
            stockLabel.setStyle("-fx-text-fill: #94A3B8;");
        }
        stockLabel.setPrefHeight(18);

        // Spacer to push button to bottom
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Edit button at bottom
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("button-secondary");
        editBtn.setPrefWidth(120);
        editBtn.setOnAction(e -> {
            selectedProduct = product;
            selectedProductId = product.getId();
            showProductDialog(product);
        });

        card.getChildren().addAll(imageContainer, nameLabel, typeLabel, priceLabel, stockLabel, spacer, editBtn);

        // Low stock border
        if (product.getStock() <= product.getThreshold()) {
            card.setStyle("-fx-border-color: #800000; -fx-border-width: 2; -fx-border-radius: 12;");
        }

        return card;
    }

    @FXML
    public void handleNewProduct() {
        showProductDialog(null);
    }

    /**
     * Show a dialog to add or edit a product.
     * 
     * @param product The product to edit, or null for a new product.
     */
    private void showProductDialog(Product product) {
        boolean isEdit = product != null;

        javafx.scene.control.Dialog<String> dialog = new javafx.scene.control.Dialog<>();
        com.greengrocer.util.StyleHelper.applyAppIcon(dialog);
        dialog.setTitle(isEdit ? "Edit Product" : "Add New Product");

        // Dark theme styling for dialog
        javafx.scene.control.DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1e293b;");

        // Create styled form fields
        TextField nameField = createStyledTextField("Product Name", 220);
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.setItems(FXCollections.observableArrayList("Vegetable", "Fruit", "Dairy", "Bakery", "Meat",
                "Beverages", "Snacks"));
        typeCombo.setPromptText("Select Type");
        typeCombo.setPrefWidth(220);
        typeCombo.setStyle(
                "-fx-background-color: #334155; -fx-text-fill: white; -fx-prompt-text-fill: #94A3B8; -fx-opacity: 1.0;");

        typeCombo.setButtonCell(new javafx.scene.control.ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(typeCombo.getPromptText());
                    setTextFill(javafx.scene.paint.Color.web("#94A3B8"));
                } else {
                    setText(item);
                    setTextFill(javafx.scene.paint.Color.WHITE);
                }
            }
        });

        TextField priceField = createStyledTextField("Price (₺)", 150);
        TextField costField = createStyledTextField("Cost Price", 150);
        TextField stockField = createStyledTextField("Stock", 150);
        TextField thresholdField = createStyledTextField("Low Stock Threshold", 150);

        // Image preview with border
        javafx.scene.image.ImageView previewImage = new javafx.scene.image.ImageView();
        previewImage.setFitHeight(100);
        previewImage.setFitWidth(100);
        previewImage.setPreserveRatio(true);
        previewImage.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");

        final File[] selectedImage = { null };

        Button browseBtn = new Button("📷 Browse Image");
        browseBtn.setStyle(
                "-fx-background-color: #475569; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        browseBtn.setOnMouseEntered(e -> browseBtn.setStyle(
                "-fx-background-color: #64748b; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;"));
        browseBtn.setOnMouseExited(e -> browseBtn.setStyle(
                "-fx-background-color: #475569; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;"));
        browseBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Product Image");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            File file = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (file != null) {
                selectedImage[0] = file;
                try {
                    previewImage.setImage(new javafx.scene.image.Image(new FileInputStream(file)));
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Main layout
        javafx.scene.layout.VBox mainLayout = new javafx.scene.layout.VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #1e293b;");

        // Header with icon
        Label headerLabel = new Label(isEdit ? "✏️ Edit Product Details" : "➕ Add New Product");
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #F8FAFC;");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setStyle("-fx-background-color: #334155; -fx-background-radius: 12; -fx-padding: 20;");

        ComboBox<String> unitCombo = new ComboBox<>();
        unitCombo.setItems(FXCollections.observableArrayList("Kg", "Piece"));
        unitCombo.setValue("Piece"); // Default
        unitCombo.setPrefWidth(220);
        unitCombo.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-opacity: 1.0;");

        unitCombo.setButtonCell(new javafx.scene.control.ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setTextFill(javafx.scene.paint.Color.WHITE);
                }
            }
        });

        // Auto-select unit based on type
        typeCombo.setOnAction(e -> {
            String selectedType = typeCombo.getValue();
            if ("Fruit".equals(selectedType) || "Vegetable".equals(selectedType)) {
                unitCombo.setValue("Kg");
            } else {
                unitCombo.setValue("Piece");
            }
        });

        // Pre-fill fields if editing
        if (isEdit) {
            nameField.setText(product.getName());
            typeCombo.setValue(product.getType());
            priceField.setText(String.valueOf(product.getPrice()));
            costField.setText(String.valueOf(product.getCostPrice()));
            stockField.setText(String.valueOf(product.getStock()));
            thresholdField.setText(String.valueOf(product.getThreshold()));
            if (product.getImage() != null) {
                previewImage.setImage(product.getImage());
            }
            // Set unit type based on stored value (kg/pcs mapped to Kg/Piece)
            if (product.isSoldByKg()) {
                unitCombo.setValue("Kg");
            } else {
                unitCombo.setValue("Piece");
            }
        }

        grid.add(createStyledLabel("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(createStyledLabel("Type:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(createStyledLabel("Unit Type:"), 0, 2);
        grid.add(unitCombo, 1, 2);
        grid.add(createStyledLabel("Price (₺):"), 0, 3);
        grid.add(priceField, 1, 3);
        grid.add(createStyledLabel("Cost Price:"), 0, 4);
        grid.add(costField, 1, 4);
        grid.add(createStyledLabel("Stock:"), 0, 5);
        grid.add(stockField, 1, 5);
        grid.add(createStyledLabel("Threshold:"), 0, 6);
        grid.add(thresholdField, 1, 6);
        grid.add(createStyledLabel("Image:"), 0, 7);

        javafx.scene.layout.VBox imageBox = new javafx.scene.layout.VBox(10, browseBtn, previewImage);
        imageBox.setAlignment(Pos.CENTER_LEFT);
        grid.add(imageBox, 1, 7);

        // Status label for validation messages
        Label dialogStatus = new Label();
        dialogStatus.setStyle("-fx-font-size: 12px;");

        // Button bar
        javafx.scene.layout.HBox buttonBar = new javafx.scene.layout.HBox(10);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        Button saveBtn = new Button(isEdit ? "💾 Save Changes" : "➕ Add Product");
        saveBtn.setStyle(
                "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: bold; -fx-cursor: hand;");
        saveBtn.setOnMouseEntered(e -> saveBtn.setStyle(
                "-fx-background-color: #66BB6A; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: bold; -fx-cursor: hand;"));
        saveBtn.setOnMouseExited(e -> saveBtn.setStyle(
                "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: bold; -fx-cursor: hand;"));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(
                "-fx-background-color: #64748b; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;");
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(
                "-fx-background-color: #94a3b8; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;"));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle(
                "-fx-background-color: #64748b; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;"));
        cancelBtn.setOnAction(e -> dialog.close());

        buttonBar.getChildren().addAll(cancelBtn, saveBtn);

        // Delete button only for edit mode
        if (isEdit) {
            Button deleteBtn = new Button("🗑️ Delete Product");
            deleteBtn.setStyle(
                    "-fx-background-color: #EF4444; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: bold; -fx-cursor: hand;");
            deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(
                    "-fx-background-color: #F87171; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: bold; -fx-cursor: hand;"));
            deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(
                    "-fx-background-color: #EF4444; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: bold; -fx-cursor: hand;"));

            final Product productToDelete = product;
            deleteBtn.setOnAction(e -> {
                boolean confirm = StyledAlert.showConfirmation("Delete Product", null,
                        "Are you sure you want to delete '" + productToDelete.getName() + "'?");
                if (confirm) {
                    try {
                        if (productDAO.deleteProduct(productToDelete.getId())) {
                            statusLabel.setText("Product deleted successfully!");
                            statusLabel.setStyle("-fx-text-fill: #4CAF50;");
                            selectedProduct = null;
                            selectedProductId = -1;
                            loadProducts();
                            dialog.close();
                        }
                    } catch (SQLException ex) {
                        String msg = ex.getMessage().toLowerCase();
                        if (msg.contains("foreign key") || msg.contains("constraint")) {
                            dialogStatus.setText("Cannot delete: Product has order history.");
                        } else {
                            dialogStatus.setText("Delete failed: " + ex.getMessage());
                        }
                        dialogStatus.setStyle("-fx-text-fill: #EF4444;");
                    }
                }
            });

            // Add delete button to the left
            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            buttonBar.getChildren().add(0, deleteBtn);
            buttonBar.getChildren().add(1, spacer);
        }

        // Save button action
        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String type = typeCombo.getValue();
            String unitType = unitCombo.getValue(); // Get selected unit
            String priceStr = priceField.getText().trim();
            String costStr = costField.getText().trim();
            String stockStr = stockField.getText().trim();
            String thresholdStr = thresholdField.getText().trim();

            if (name.isEmpty() || type == null || unitType == null || priceStr.isEmpty() || stockStr.isEmpty()
                    || thresholdStr.isEmpty()) {
                dialogStatus.setText("All fields are required.");
                dialogStatus.setStyle("-fx-text-fill: #EF4444;");
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                double cost = costStr.isEmpty() ? 0 : Double.parseDouble(costStr);
                double stock = Double.parseDouble(stockStr);
                double threshold = Double.parseDouble(thresholdStr);

                if (price < 0 || stock < 0 || threshold < 0 || cost < 0) {
                    dialogStatus.setText("Values cannot be negative.");
                    dialogStatus.setStyle("-fx-text-fill: #EF4444;");
                    return;
                }

                // Unit-based Stock Validation
                if ("Piece".equalsIgnoreCase(unitType)) {
                    if (stock % 1 != 0) {
                        dialogStatus.setText("Stock for 'Piece' must be a whole number.");
                        dialogStatus.setStyle("-fx-text-fill: #EF4444;");
                        return;
                    }
                } else if ("Kg".equalsIgnoreCase(unitType)) {
                    // Start of '1 decimal place' check
                    // Allow e.g. 3.5 but NOT 3.55
                    // Multiply by 10, check if integer (with epsilon)
                    double multiplied = stock * 10;
                    if (Math.abs(multiplied - Math.round(multiplied)) > 0.001) {
                        dialogStatus.setText("Stock for 'Kg' can have at most 1 decimal place.");
                        dialogStatus.setStyle("-fx-text-fill: #EF4444;");
                        return;
                    }
                }

                String dbUnitType = unitType.toLowerCase(); // "piece" or "kg"

                if (isEdit) {
                    product.setName(name);
                    product.setType(type);
                    product.setUnitType(dbUnitType); // Update unit type
                    product.setPrice(price);
                    product.setCostPrice(cost);
                    product.setStock(stock);
                    product.setThreshold(threshold);
                    if (selectedImage[0] != null) {
                        byte[] rawImage = java.nio.file.Files.readAllBytes(selectedImage[0].toPath());
                        product.setImageData(com.greengrocer.util.ImageCompressor.compress(rawImage));
                    }
                    if (productDAO.updateProduct(product)) {
                        statusLabel.setText("Product updated successfully!");
                        statusLabel.setStyle("-fx-text-fill: #4CAF50;");
                        loadProducts();
                        dialog.close();
                    }
                } else {
                    java.io.ByteArrayInputStream imageStream = null;
                    if (selectedImage[0] != null) {
                        byte[] rawImage = java.nio.file.Files.readAllBytes(selectedImage[0].toPath());
                        byte[] compressed = com.greengrocer.util.ImageCompressor.compress(rawImage);
                        imageStream = new java.io.ByteArrayInputStream(compressed);
                    }
                    // Use dbUnitType instead of hardcoded string
                    if (productDAO.addProduct(name, type, price, cost, stock, threshold, imageStream, dbUnitType)) {
                        statusLabel.setText("Product added successfully!");
                        statusLabel.setStyle("-fx-text-fill: #4CAF50;");
                        loadProducts();
                        dialog.close();
                    }
                }
            } catch (NumberFormatException ex) {
                dialogStatus.setText("Invalid numeric input.");
                dialogStatus.setStyle("-fx-text-fill: #EF4444;");
            } catch (IOException | SQLException ex) {
                ex.printStackTrace();
                dialogStatus.setText("Error saving product: " + ex.getMessage());
                dialogStatus.setStyle("-fx-text-fill: #EF4444;");
            }
        });

        mainLayout.getChildren().addAll(headerLabel, grid, dialogStatus, buttonBar);

        dialogPane.setContent(mainLayout);
        dialogPane.getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
        dialogPane.lookupButton(javafx.scene.control.ButtonType.CLOSE).setVisible(false);

        dialog.showAndWait();
    }

    private TextField createStyledTextField(String prompt, double width) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefWidth(width);
        field.setStyle(
                "-fx-background-color: #475569; -fx-text-fill: white; -fx-prompt-text-fill: #94A3B8; -fx-background-radius: 6; -fx-padding: 8;");
        return field;
    }

    private Label createStyledLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #F8FAFC; -fx-font-size: 13px;");
        return label;
    }

    @FXML
    public void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        selectedImageFile = fileChooser.showOpenDialog(prodNameField.getScene().getWindow());
        if (selectedImageFile != null) {
            statusLabel.setText("Image selected: " + selectedImageFile.getName());
            statusLabel.setStyle("-fx-text-fill: #3B82F6;");

            // Show preview
            try {
                previewImageView.setImage(new javafx.scene.image.Image(new FileInputStream(selectedImageFile)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleAddProduct() {
        String name = prodNameField.getText();
        String type = prodTypeCombo.getValue();
        String priceStr = prodPriceField.getText();
        String costStr = prodCostField.getText();
        String stockStr = prodStockField.getText();
        String thresholdStr = prodThresholdField.getText();

        if (name.isEmpty() || type == null || priceStr.isEmpty() || stockStr.isEmpty() || thresholdStr.isEmpty()) {
            statusLabel.setText("All fields are required.");
            statusLabel.setStyle("-fx-text-fill: #EF4444;");
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            double costPrice = costStr.isEmpty() ? 0 : Double.parseDouble(costStr);
            double stock = Double.parseDouble(stockStr);
            double threshold = Double.parseDouble(thresholdStr);

            if (price < 0 || stock < 0 || threshold < 0 || costPrice < 0) {
                statusLabel.setText("Values cannot be negative.");
                statusLabel.setStyle("-fx-text-fill: #EF4444;");
                return;
            }

            FileInputStream fis = null;
            if (selectedImageFile != null) {
                fis = new FileInputStream(selectedImageFile);
            }

            boolean success = productDAO.addProduct(name, type, price, costPrice, stock, threshold, fis, "kg");
            if (success) {
                statusLabel.setText("Product added successfully!");
                statusLabel.setStyle("-fx-text-fill: #10B981;");
                loadProducts();
                clearFields();
            } else {
                statusLabel.setText("Failed to add product.");
                statusLabel.setStyle("-fx-text-fill: #EF4444;");
            }

        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid number format.");
            statusLabel.setStyle("-fx-text-fill: #EF4444;");
        } catch (FileNotFoundException e) {
            statusLabel.setText("Image file not found.");
            statusLabel.setStyle("-fx-text-fill: #EF4444;");
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Database error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #EF4444;");
        }
    }

    @FXML
    public void handleDeleteProduct() {
        if (selectedProduct == null) {
            statusLabel.setText("Click 'Edit' on a product card first.");
            statusLabel.setStyle("-fx-text-fill: #EF4444;");
            return;
        }

        boolean confirm = StyledAlert.showConfirmation("Delete Product", null,
                "Are you sure you want to delete " + selectedProduct.getName() + "?");

        if (confirm) {
            try {
                if (productDAO.deleteProduct(selectedProduct.getId())) {
                    statusLabel.setText("Product deleted.");
                    statusLabel.setStyle("-fx-text-fill: #10B981;");
                    selectedProduct = null;
                    selectedProductId = -1;
                    loadProducts();
                } else {
                    statusLabel.setText("Delete failed.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                // Check if it's a foreign key constraint error
                // String msg = e.getMessage().toLowerCase(); // This line was commented out or
                // intended to be part of an if block
                statusLabel.setText("DB Error: " + e.getMessage()); // TEMPORARY DEBUG
                statusLabel.setStyle("-fx-text-fill: #EF4444;");
            }
        }
    }

    private void clearFields() {
        // UI fields are now handled in Dialogs. Only reset internal state.
        selectedImageFile = null;
        selectedProduct = null;
        selectedProductId = -1;
        // previewImageView is also part of old UI
    }

    // Carrier Management Methods

    @FXML
    public void handleLoadCarriers() {
        loadCarriers();
    }

    private void loadCarriers() {
        try {
            ObservableList<User> carriers = FXCollections.observableArrayList(userDAO.getUsersByRole("carrier"));
            carrierTable.setItems(carriers);
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading carriers.");
            statusLabel.setStyle("-fx-text-fill: #EF4444;");
        }
    }

    @FXML
    public void handleAddCarrier() {
        String username = carrUsernameField.getText();
        String password = carrPasswordField.getText();
        String name = carrNameField.getText();
        String surname = carrSurnameField.getText();
        String phone = carrPhoneField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            setCarrierStatus("Username and Password required for carrier.", "red");
            return;
        }

        // Username validation: length and format
        if (username.length() < 3) {
            setCarrierStatus("Username must be at least 3 characters.", "red");
            return;
        }

        if (username.length() > 20) {
            setCarrierStatus("Username must be at most 20 characters.", "red");
            return;
        }

        if (!username.matches("^[a-z0-9]+$")) {
            setCarrierStatus("Username must contain only lowercase letters and numbers.", "red");
            return;
        }

        // Password strength validation
        if (password.length() < 6) {
            setCarrierStatus("Password must be at least 6 characters.", "red");
            return;
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
            setCarrierStatus("Password must contain at least 1 uppercase letter.", "red");
            return;
        }

        if (!hasNumber) {
            setCarrierStatus("Password must contain at least 1 number.", "red");
            return;
        }

        try {
            if (userDAO.register(username, password, "carrier", name, surname, "", phone)) {
                setCarrierStatus("Carrier added successfully.", "green");
                loadCarriers();
                carrUsernameField.clear();
                carrPasswordField.clear();
                carrNameField.clear();
                carrSurnameField.clear();
                carrPhoneField.clear();
            } else {
                setCarrierStatus("Failed to add carrier (Username used?).", "red");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            setCarrierStatus("Database error." + e.getMessage(), "red");
        }
    }

    @FXML
    public void handleFireCarrier() {
        User selected = carrierTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setCarrierStatus("Select a carrier to fire.", "red");
            return;
        }

        boolean confirm = StyledAlert.showConfirmation("Fire Carrier", null,
                "Are you sure you want to fire " + selected.getUsername() + "?");

        if (!confirm)
            return;

        try {
            if (userDAO.deleteUser(selected.getId())) {
                setCarrierStatus("Carrier fired.", "green");
                loadCarriers();
            } else {
                setCarrierStatus("Failed to delete carrier.", "red");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            setCarrierStatus("Database error: " + e.getMessage(), "red");
        }
    }

    private void setCarrierStatus(String msg, String color) {
        if (carrierStatusLabel != null) {
            carrierStatusLabel.setText(msg);
            if ("red".equals(color)) {
                carrierStatusLabel.setStyle("-fx-text-fill: #EF4444;");
            } else {
                carrierStatusLabel.setStyle("-fx-text-fill: #10B981;");
            }
        }
    }

    // Report Methods
    @FXML
    public void handleRefreshReports() {
        loadReports();
    }

    private void loadReports() {
        try {
            // Initialize revenue view combo if not done
            if (revenueViewCombo.getItems().isEmpty()) {
                revenueViewCombo.setItems(FXCollections.observableArrayList("Daily", "Weekly", "Monthly"));
                revenueViewCombo.setValue("Daily");
            }

            // Pie Chart - Sales by Product Type
            Map<String, Double> salesByType = reportDAO.getSalesByProductType();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

            // Calculate total for percentages
            double totalSales = salesByType.values().stream().mapToDouble(Double::doubleValue).sum();

            for (Map.Entry<String, Double> entry : salesByType.entrySet()) {
                double percentage = totalSales > 0 ? (entry.getValue() / totalSales) * 100 : 0;
                pieData.add(new PieChart.Data(
                        entry.getKey() + " (" + String.format("%.1f%%", percentage) + ")",
                        entry.getValue()));
            }
            productTypeChart.setData(pieData);
            productTypeChart.setLabelsVisible(false);
            productTypeChart.setLegendSide(Side.RIGHT); // Move legend to side to prevent overlapping with labels
            productTypeChart.setStartAngle(90); // Adjust start angle for better label distribution

            // Bar Chart - Revenue based on selected view
            loadRevenueChart();

            // Summary Statistics
            double totalRevenue = reportDAO.getTotalRevenue();
            int totalOrders = reportDAO.getTotalOrders();
            int totalProducts = reportDAO.getTotalProducts();
            double totalProfit = reportDAO.getTotalProfit();
            double inventoryCost = reportDAO.getTotalInventoryCost();

            totalRevenueLabel.setText("Total Revenue: " + FormatHelper.formatCurrency(totalRevenue));
            totalOrdersLabel.setText("Total Orders: " + totalOrders);
            totalProductsLabel.setText("Total Products: " + totalProducts);

            // Profit/Loss Summary
            totalProfitLabel.setText("Total Profit: " + FormatHelper.formatCurrency(totalProfit));
            if (totalProfit >= 0) {
                totalProfitLabel.getStyleClass().removeAll("status-error");
                totalProfitLabel.getStyleClass().add("status-success");
            } else {
                totalProfitLabel.getStyleClass().removeAll("status-success");
                totalProfitLabel.getStyleClass().add("status-error");
            }
            inventoryCostLabel.setText("Inventory Cost: " + FormatHelper.formatCurrency(inventoryCost));

            // Profit/Loss Table
            loadProfitLossTable();

            // Cost Analysis Table
            loadCostAnalysisTable();

            reportStatusLabel.setText("Reports loaded.");
            reportStatusLabel.setStyle("-fx-text-fill: #10B981;");
        } catch (SQLException e) {
            e.printStackTrace();
            reportStatusLabel.setText("Error loading reports.");
            reportStatusLabel.setStyle("-fx-text-fill: #EF4444;");
        }
    }

    @FXML
    public void handleRevenueViewChange() {
        try {
            loadRevenueChart();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadRevenueChart() throws SQLException {
        String view = revenueViewCombo.getValue();
        Map<String, Double> revenueData;

        if ("Weekly".equals(view)) {
            revenueData = reportDAO.getWeeklyRevenue();
        } else if ("Monthly".equals(view)) {
            revenueData = reportDAO.getMonthlyRevenue();
        } else {
            revenueData = reportDAO.getDailyRevenue();
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue");
        for (Map.Entry<String, Double> entry : revenueData.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        revenueChart.getData().clear();
        revenueChart.getData().add(series);
    }

    private void loadProfitLossTable() throws SQLException {
        // Setup columns if not done
        colPLProduct.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty((String) data.getValue()[0]));
        colPLRevenue.setCellValueFactory(
                data -> new javafx.beans.property.SimpleDoubleProperty((Double) data.getValue()[1]).asObject());
        colPLCost.setCellValueFactory(
                data -> new javafx.beans.property.SimpleDoubleProperty((Double) data.getValue()[2]).asObject());
        colPLProfit.setCellValueFactory(
                data -> new javafx.beans.property.SimpleDoubleProperty((Double) data.getValue()[3]).asObject());

        // Format Currency Columns
        formatCurrencyColumn(colPLRevenue);
        formatCurrencyColumn(colPLCost);
        formatCurrencyColumn(colPLProfit);

        java.util.List<Object[]> profitData = reportDAO.getProfitLossPerProduct();
        profitLossTable.setItems(FXCollections.observableArrayList(profitData));
    }

    private void loadCostAnalysisTable() throws SQLException {
        // Setup columns if not done
        colCAProduct.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty((String) data.getValue()[0]));
        colCAPrice.setCellValueFactory(
                data -> new javafx.beans.property.SimpleDoubleProperty((Double) data.getValue()[1]).asObject());
        colCACost.setCellValueFactory(
                data -> new javafx.beans.property.SimpleDoubleProperty((Double) data.getValue()[2]).asObject());
        colCAMargin.setCellValueFactory(
                data -> new javafx.beans.property.SimpleDoubleProperty((Double) data.getValue()[3]).asObject());

        // Format Currency Columns
        formatCurrencyColumn(colCAPrice);
        formatCurrencyColumn(colCACost);
        formatCurrencyColumn(colCAMargin);

        java.util.List<Object[]> costData = reportDAO.getCostAnalysis();
        costAnalysisTable.setItems(FXCollections.observableArrayList(costData));
    }

    // Order Management Methods
    @FXML
    public void handleRefreshAllOrders() {
        loadAllOrders();
    }

    @FXML
    public void handleFilterOrders() {
        String status = orderStatusFilter.getValue();
        try {
            ObservableList<com.greengrocer.models.Order> orders;
            if ("All".equals(status)) {
                orders = FXCollections.observableArrayList(orderDAO.getAllOrders());
            } else {
                orders = FXCollections.observableArrayList(orderDAO.getOrdersByStatus(status));
            }
            allOrdersTable.setItems(orders);
            orderManagementStatus.setText("Showing " + orders.size() + " orders.");
            orderManagementStatus.setStyle("-fx-text-fill: #3B82F6;");
        } catch (SQLException e) {
            e.printStackTrace();
            orderManagementStatus.setText("Error filtering orders.");
            orderManagementStatus.setStyle("-fx-text-fill: #EF4444;");
        }
    }

    private void loadAllOrders() {
        try {
            ObservableList<com.greengrocer.models.Order> orders = FXCollections
                    .observableArrayList(orderDAO.getAllOrders());
            allOrdersTable.setItems(orders);
            orderManagementStatus.setText("Loaded " + orders.size() + " orders.");
            orderManagementStatus.setStyle("-fx-text-fill: #3B82F6;");
        } catch (SQLException e) {
            e.printStackTrace();
            orderManagementStatus.setText("Error loading orders.");
            orderManagementStatus.setStyle("-fx-text-fill: #EF4444;");
        }
    }

    @FXML
    public void handleCancelOrder() {
        com.greengrocer.models.Order selected = allOrdersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            orderManagementStatus.setText("Select an order to cancel.");
            orderManagementStatus.setStyle("-fx-text-fill: #EF4444;");
            return;
        }

        if ("Cancelled".equals(selected.getStatus()) || "Delivered".equals(selected.getStatus())) {
            orderManagementStatus.setText("Cannot cancel this order.");
            orderManagementStatus.setStyle("-fx-text-fill: #EF4444;");
            return;
        }

        boolean confirm = StyledAlert.showConfirmation("Cancel Order", null,
                "Are you sure you want to cancel Order #" + selected.getId() + "?");

        if (confirm) {
            try {
                if (orderDAO.cancelOrder(selected.getId())) {
                    orderManagementStatus.setText("Order #" + selected.getId() + " cancelled.");
                    orderManagementStatus.setStyle("-fx-text-fill: #10B981;");
                    loadAllOrders();
                } else {
                    orderManagementStatus.setText("Failed to cancel order.");
                    orderManagementStatus.setStyle("-fx-text-fill: #EF4444;");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                orderManagementStatus.setText("Database error.");
                orderManagementStatus.setStyle("-fx-text-fill: #EF4444;");
            }
        }
    }

    @FXML
    public void handleUpdateProduct() {
        if (selectedProductId == -1) {
            statusLabel.setText("First select a product to update by clicking 'Edit' on a card.");
            statusLabel.setStyle("-fx-text-fill: #EF4444;");
            return;
        }

        String name = prodNameField.getText();
        String type = prodTypeCombo.getValue();
        String priceStr = prodPriceField.getText();
        String costStr = prodCostField.getText();
        String stockStr = prodStockField.getText();
        String thresholdStr = prodThresholdField.getText();

        if (name.isEmpty() || type == null || priceStr.isEmpty() || stockStr.isEmpty() || thresholdStr.isEmpty()) {
            statusLabel.setText("All fields are required.");
            statusLabel.setStyle("-fx-text-fill: #EF4444;");
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            double costPrice = costStr.isEmpty() ? 0 : Double.parseDouble(costStr);
            double stock = Double.parseDouble(stockStr);
            double threshold = Double.parseDouble(thresholdStr);

            byte[] imageData = null;
            if (selectedImageFile != null) {
                java.nio.file.Path path = selectedImageFile.toPath();
                imageData = java.nio.file.Files.readAllBytes(path);
            }

            Product updatedProduct = new Product(selectedProductId, name, type, price, costPrice, stock, threshold,
                    imageData);

            if (productDAO.updateProduct(updatedProduct)) {
                statusLabel.setText("Product #" + selectedProductId + " updated successfully!");
                statusLabel.setStyle("-fx-text-fill: #10B981;");
                loadProducts();
                clearFields();
                selectedProductId = -1;
                selectedImageFile = null;
                if (previewImageView != null)
                    previewImageView.setImage(null);
            } else {
                statusLabel.setText("Failed to update product.");
                statusLabel.setStyle("-fx-text-fill: #EF4444;");
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid number format.");
            statusLabel.setStyle("-fx-text-fill: #EF4444;");
        } catch (java.io.IOException e) {
            statusLabel.setText("Error reading image file.");
            statusLabel.setStyle("-fx-text-fill: #EF4444;");
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Database error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #EF4444;");
        }
    }

    @FXML
    public void handleLogout() {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(
                    getClass().getResource("/com/greengrocer/views/login.fxml"));
            javafx.stage.Stage stage = (javafx.stage.Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(com.greengrocer.util.StyleHelper.createStyledScene(root, 960, 540));
            stage.setTitle("Greengrocer Login");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Stock Alerts Methods
    @FXML
    public void handleRefreshAlerts() {
        loadAlerts();
    }

    private void loadAlerts() {
        try {
            // Setup columns if not done
            colAlertId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colAlertName.setCellValueFactory(new PropertyValueFactory<>("name"));
            colAlertType.setCellValueFactory(new PropertyValueFactory<>("type"));
            colAlertStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
            colAlertThreshold.setCellValueFactory(new PropertyValueFactory<>("threshold"));

            // Calculate shortage (threshold - stock)
            colAlertDiff.setCellValueFactory(cellData -> {
                Product p = cellData.getValue();
                double shortage = p.getThreshold() - p.getStock();
                return new javafx.beans.property.SimpleDoubleProperty(shortage).asObject();
            });

            // Get products below threshold
            java.util.List<Product> allProducts = productDAO.getAllProducts();
            java.util.List<Product> lowStock = allProducts.stream()
                    .filter(p -> p.getStock() < p.getThreshold())
                    .collect(java.util.stream.Collectors.toList());

            alertsTable.setItems(FXCollections.observableArrayList(lowStock));

            // Update count label
            if (lowStock.isEmpty()) {
                alertCountLabel.setText("All stock levels OK");
                alertCountLabel.getStyleClass().removeAll("status-success", "status-error");
                alertCountLabel.getStyleClass().add("status-success");
            } else {
                alertCountLabel.setText(lowStock.size() + " products need restocking!");
                alertCountLabel.getStyleClass().removeAll("status-success", "status-error");
                alertCountLabel.getStyleClass().add("status-error");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== COUPON MANAGEMENT ====================

    @FXML
    public void handleCreateCoupon() {
        String code = couponCodeField.getText().trim();
        String discountStr = couponDiscountField.getText().trim();
        String maxUsesStr = couponMaxUsesField.getText().trim();

        if (code.isEmpty() || discountStr.isEmpty() || maxUsesStr.isEmpty()) {
            couponStatusLabel.setText("Please fill all fields!");
            couponStatusLabel.setStyle("-fx-text-fill: #EF4444;");
            return;
        }

        try {
            double discount = Double.parseDouble(discountStr);
            int maxUses = Integer.parseInt(maxUsesStr);

            if (discount <= 0 || discount > 100) {
                couponStatusLabel.setText("Discount must be between 1-100%!");
                couponStatusLabel.setStyle("-fx-text-fill: #EF4444;");
                return;
            }

            if (couponDAO.createCoupon(code, discount, maxUses)) {
                couponStatusLabel.setText("Coupon created: " + code.toUpperCase());
                couponStatusLabel.setStyle("-fx-text-fill: #10B981;");
                clearCouponForm();
                loadCoupons();
            } else {
                couponStatusLabel.setText("Failed to create coupon (code may exist)");
                couponStatusLabel.setStyle("-fx-text-fill: #EF4444;");
            }
        } catch (NumberFormatException e) {
            couponStatusLabel.setText("Invalid number format!");
            couponStatusLabel.setStyle("-fx-text-fill: #EF4444;");
        } catch (SQLException e) {
            couponStatusLabel.setText("Database error: " + e.getMessage());
            couponStatusLabel.setStyle("-fx-text-fill: #EF4444;");
        }
    }

    @FXML
    public void handleRefreshCoupons() {
        loadCoupons();
        loadCouponHistory();
    }

    @FXML
    public void handleUpdateCoupon() {
        com.greengrocer.models.Coupon selected = couponTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            couponStatusLabel.setText("Select a coupon!");
            couponStatusLabel.setStyle("-fx-text-fill: #EF4444;");
            return;
        }

        // Show dialog for new max uses
        TextInputDialog dialog = new TextInputDialog(String.valueOf(selected.getMaxUses()));
        dialog.setTitle("Update Coupon");
        dialog.setHeaderText("Coupon: " + selected.getCode());
        dialog.setContentText("New maximum uses:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(maxUsesStr -> {
            try {
                int newMaxUses = Integer.parseInt(maxUsesStr);
                if (couponDAO.updateCoupon(selected.getId(), newMaxUses, selected.isActive())) {
                    couponStatusLabel.setText("Coupon updated!");
                    couponStatusLabel.setStyle("-fx-text-fill: #10B981;");
                    loadCoupons();
                }
            } catch (NumberFormatException e) {
                couponStatusLabel.setText("Invalid number!");
                couponStatusLabel.setStyle("-fx-text-fill: #EF4444;");
            } catch (SQLException e) {
                couponStatusLabel.setText("Update error!");
                couponStatusLabel.setStyle("-fx-text-fill: #EF4444;");
            }
        });
    }

    @FXML
    public void handleToggleCouponActive() {
        com.greengrocer.models.Coupon selected = couponTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            couponStatusLabel.setText("Select a coupon!");
            couponStatusLabel.setStyle("-fx-text-fill: #EF4444;");
            return;
        }

        try {
            boolean newStatus = !selected.isActive();
            if (couponDAO.updateCoupon(selected.getId(), selected.getMaxUses(), newStatus)) {
                String status = newStatus ? "activated" : "deactivated";
                couponStatusLabel.setText("Coupon " + status + "!");
                couponStatusLabel.setStyle("-fx-text-fill: #10B981;");
                loadCoupons();
            }
        } catch (SQLException e) {
            couponStatusLabel.setText("Status change failed!");
            couponStatusLabel.setStyle("-fx-text-fill: #EF4444;");
        }
    }

    @FXML
    public void handleDeleteCoupon() {
        com.greengrocer.models.Coupon selected = couponTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            couponStatusLabel.setText("Select a coupon!");
            couponStatusLabel.setStyle("-fx-text-fill: #EF4444;");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Coupon");
        confirm.setHeaderText("Are you sure you want to delete this coupon?");
        confirm.setContentText("Coupon: " + selected.getCode() + "\nThis action cannot be undone!");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (couponDAO.deleteCoupon(selected.getId())) {
                    couponStatusLabel.setText("Coupon deleted!");
                    couponStatusLabel.setStyle("-fx-text-fill: #10B981;");
                    loadCoupons();
                    loadCouponHistory();
                }
            } catch (SQLException e) {
                couponStatusLabel.setText("Delete error!");
                couponStatusLabel.setStyle("-fx-text-fill: #EF4444;");
            }
        }
    }

    private void loadCoupons() {
        if (couponTable == null)
            return;

        try {
            // Setup columns
            colCouponCode.setCellValueFactory(new PropertyValueFactory<>("code"));
            colCouponDiscount.setCellValueFactory(new PropertyValueFactory<>("discountPercent"));
            colCouponMaxUses.setCellValueFactory(new PropertyValueFactory<>("maxUses"));
            colCouponCurrentUses.setCellValueFactory(new PropertyValueFactory<>("currentUses"));
            colCouponActive.setCellValueFactory(new PropertyValueFactory<>("active"));

            java.util.List<com.greengrocer.models.Coupon> coupons = couponDAO.getAllCoupons();
            couponTable.setItems(FXCollections.observableArrayList(coupons));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCouponHistory() {
        if (couponHistoryTable == null)
            return;

        try {
            // Setup columns
            colHistoryCode.setCellValueFactory(
                    data -> new javafx.beans.property.SimpleStringProperty((String) data.getValue()[0]));
            colHistoryUser.setCellValueFactory(
                    data -> new javafx.beans.property.SimpleStringProperty((String) data.getValue()[2]));
            colHistoryDate.setCellValueFactory(data -> {
                Object timestampObj = data.getValue()[1];
                if (timestampObj != null && timestampObj instanceof java.sql.Timestamp) {
                    return new javafx.beans.property.SimpleStringProperty(
                            FormatHelper.formatDate((java.sql.Timestamp) timestampObj));
                }
                return new javafx.beans.property.SimpleStringProperty("");
            });
            colHistoryAmount.setCellValueFactory(
                    data -> new javafx.beans.property.SimpleDoubleProperty((Double) data.getValue()[3]).asObject());

            formatCurrencyColumn(colHistoryAmount);

            java.util.List<Object[]> history = couponDAO.getAllCouponUsageHistory();
            System.out.println("[DEBUG] Coupon History loaded: " + history.size() + " items");
            couponHistoryTable.setItems(FXCollections.observableArrayList(history));
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DEBUG] Error loading coupon history: " + e.getMessage());
        }
    }

    private void clearCouponForm() {
        couponCodeField.clear();
        couponDiscountField.clear();
        couponMaxUsesField.clear();
    }

    // ==================== MESSAGING FEATURE - WhatsApp Style ====================

    private com.greengrocer.dao.MessageDAO messageDAO = new com.greengrocer.dao.MessageDAO();
    private String ownerCurrentConversationSubject = null;
    private int ownerCurrentChatPartnerId = -1;

    @FXML
    private javafx.scene.layout.VBox ownerConversationListPane;
    @FXML
    private javafx.scene.layout.VBox ownerChatMessagesPane;
    @FXML
    private ScrollPane ownerChatScrollPane;
    @FXML
    private Label ownerChatPartnerLabel;
    @FXML
    private TextArea ownerReplyArea;
    @FXML
    private Label ownerMsgStatusLabel;
    @FXML
    private Label ownerUnreadCountLabel;

    @FXML
    public void handleRefreshOwnerMessages() {
        loadOwnerConversations();
        updateOwnerUnreadCount();
        if (ownerCurrentConversationSubject != null) {
            loadOwnerChatMessages();
        }
    }

    private void loadOwnerConversations() {
        if (ownerConversationListPane == null || currentUser == null)
            return;
        ownerConversationListPane.getChildren().clear();

        try {
            java.util.List<com.greengrocer.models.Message> inbox = messageDAO.getInbox(currentUser.getId());
            java.util.List<com.greengrocer.models.Message> sent = messageDAO.getSentMessages(currentUser.getId());

            // Count unread messages per conversation
            java.util.Map<String, Integer> unreadCounts = new java.util.HashMap<>();
            for (com.greengrocer.models.Message msg : inbox) {
                if (!msg.isRead()) {
                    String subj = msg.getSubject().replaceFirst("^Re: ", "");
                    unreadCounts.put(subj, unreadCounts.getOrDefault(subj, 0) + 1);
                }
            }

            java.util.Map<String, com.greengrocer.models.Message> conversations = new java.util.LinkedHashMap<>();

            java.util.List<com.greengrocer.models.Message> allMessages = new java.util.ArrayList<>();
            allMessages.addAll(inbox);
            allMessages.addAll(sent);

            allMessages.sort((a, b) -> b.getSentAt().compareTo(a.getSentAt()));

            for (com.greengrocer.models.Message msg : allMessages) {
                String subject = msg.getSubject().replaceFirst("^Re: ", "");
                if (!conversations.containsKey(subject)) {
                    conversations.put(subject, msg);
                }
            }

            for (java.util.Map.Entry<String, com.greengrocer.models.Message> entry : conversations.entrySet()) {
                int unread = unreadCounts.getOrDefault(entry.getKey(), 0);
                javafx.scene.layout.HBox convItem = createOwnerConversationItem(entry.getKey(), entry.getValue(),
                        unread);
                ownerConversationListPane.getChildren().add(convItem);
            }

            if (conversations.isEmpty()) {
                Label emptyLabel = new Label("No conversations yet.");
                emptyLabel.setStyle("-fx-text-fill: #94A3B8; -fx-padding: 20;");
                ownerConversationListPane.getChildren().add(emptyLabel);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private javafx.scene.layout.HBox createOwnerConversationItem(String subject,
            com.greengrocer.models.Message lastMessage, int unreadCount) {
        javafx.scene.layout.HBox item = new javafx.scene.layout.HBox(10);
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        boolean isSelected = subject.equals(ownerCurrentConversationSubject);
        String bgStyle = isSelected ? "-fx-background-color: #475569;" : "-fx-background-color: #334155;";
        String borderStyle = isSelected ? "-fx-border-color: #4CAF50; -fx-border-width: 0 0 0 4;" : "";

        item.setStyle("-fx-padding: 12; " + bgStyle + " -fx-background-radius: 8; -fx-cursor: hand; " + borderStyle);

        javafx.scene.layout.VBox textContent = new javafx.scene.layout.VBox(3);
        textContent.setMaxWidth(180);

        // Customer name
        String customerName = lastMessage.getSenderId() == currentUser.getId() ? lastMessage.getReceiverName()
                : lastMessage.getSenderName();

        Label nameLabel = new Label(customerName != null ? customerName : "Customer");
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 13px;");

        Label subjectLabel = new Label(subject);
        subjectLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 11px;");
        subjectLabel.setMaxWidth(170);

        String preview = lastMessage.getContent();
        if (preview.length() > 20)
            preview = preview.substring(0, 20) + "...";

        // Add checkmarks for sent messages
        boolean isSent = lastMessage.getSenderId() == currentUser.getId();
        String checkMark = "";
        if (isSent) {
            checkMark = lastMessage.isRead() ? "✓✓ " : "✓ ";
        }

        Label previewLabel = new Label(checkMark + preview);
        previewLabel.setStyle("-fx-text-fill: " + (isSent && lastMessage.isRead() ? "#4FC3F7" : "#94A3B8")
                + "; -fx-font-size: 10px;");

        textContent.getChildren().addAll(nameLabel, subjectLabel, previewLabel);

        // Right side: time and unread badge
        javafx.scene.layout.VBox rightContent = new javafx.scene.layout.VBox(5);
        rightContent.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        Label timeLabel = new Label(sdf.format(lastMessage.getSentAt()));
        timeLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 10px;");

        rightContent.getChildren().add(timeLabel);

        // Unread count badge
        if (unreadCount > 0) {
            Label unreadBadge = new Label(String.valueOf(unreadCount));
            unreadBadge.setStyle(
                    "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 2 6; -fx-min-width: 18;");
            unreadBadge.setAlignment(javafx.geometry.Pos.CENTER);
            rightContent.getChildren().add(unreadBadge);
        }

        item.getChildren().addAll(textContent, new javafx.scene.layout.Region(), rightContent);
        javafx.scene.layout.HBox.setHgrow(item.getChildren().get(1), javafx.scene.layout.Priority.ALWAYS);

        final String finalCustomerName = customerName;
        item.setOnMouseClicked(e -> {
            ownerCurrentConversationSubject = subject;
            ownerCurrentChatPartnerId = lastMessage.getSenderId() == currentUser.getId() ? lastMessage.getReceiverId()
                    : lastMessage.getSenderId();
            ownerChatPartnerLabel
                    .setText("💬 " + (finalCustomerName != null ? finalCustomerName : "Customer") + " - " + subject);
            loadOwnerChatMessages();
            loadOwnerConversations(); // Refresh to clear unread badge
        });
        // Hover effect
        item.setOnMouseEntered(e -> {
            boolean selected = subject.equals(ownerCurrentConversationSubject);
            if (!selected) {
                item.setStyle(
                        "-fx-padding: 12; -fx-background-color: #475569; -fx-background-radius: 8; -fx-cursor: hand;");
            }
        });
        item.setOnMouseExited(e -> {
            boolean selected = subject.equals(ownerCurrentConversationSubject);
            if (!selected) {
                item.setStyle(
                        "-fx-padding: 12; -fx-background-color: #334155; -fx-background-radius: 8; -fx-cursor: hand;");
            } else {
                // Restore selected style
                item.setStyle(
                        "-fx-padding: 12; -fx-background-color: #475569; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: #4CAF50; -fx-border-width: 0 0 0 4;");
            }
        });

        return item;
    }

    private void loadOwnerChatMessages() {
        if (ownerChatMessagesPane == null || currentUser == null || ownerCurrentConversationSubject == null)
            return;
        ownerChatMessagesPane.getChildren().clear();

        try {
            java.util.List<com.greengrocer.models.Message> inbox = messageDAO.getInbox(currentUser.getId());
            java.util.List<com.greengrocer.models.Message> sent = messageDAO.getSentMessages(currentUser.getId());

            java.util.List<com.greengrocer.models.Message> chatMessages = new java.util.ArrayList<>();

            for (com.greengrocer.models.Message msg : inbox) {
                String subj = msg.getSubject().replaceFirst("^Re: ", "");
                if (subj.equals(ownerCurrentConversationSubject)) {
                    chatMessages.add(msg);
                    if (!msg.isRead()) {
                        messageDAO.markAsRead(msg.getId());
                    }
                }
            }
            for (com.greengrocer.models.Message msg : sent) {
                String subj = msg.getSubject().replaceFirst("^Re: ", "");
                if (subj.equals(ownerCurrentConversationSubject)) {
                    chatMessages.add(msg);
                }
            }

            chatMessages.sort((a, b) -> a.getSentAt().compareTo(b.getSentAt()));

            for (com.greengrocer.models.Message msg : chatMessages) {
                javafx.scene.layout.HBox bubble = createOwnerChatBubble(msg);
                ownerChatMessagesPane.getChildren().add(bubble);
            }

            javafx.application.Platform.runLater(() -> {
                if (ownerChatScrollPane != null) {
                    ownerChatScrollPane.setVvalue(1.0);
                }
            });

            updateOwnerUnreadCount();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private javafx.scene.layout.HBox createOwnerChatBubble(com.greengrocer.models.Message msg) {
        boolean isSent = msg.getSenderId() == currentUser.getId();

        // Use HBox for proper left/right alignment
        javafx.scene.layout.HBox container = new javafx.scene.layout.HBox();
        container.setMaxWidth(Double.MAX_VALUE);

        javafx.scene.layout.VBox bubble = new javafx.scene.layout.VBox(5);
        bubble.setMaxWidth(350);
        bubble.setStyle(isSent ? "-fx-background-color: #4CAF50; -fx-background-radius: 15 15 0 15; -fx-padding: 10;"
                : "-fx-background-color: #334155; -fx-background-radius: 15 15 15 0; -fx-padding: 10;");

        Label contentLabel = new Label(msg.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        contentLabel.setMaxWidth(330);

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        Label timeLabel = new Label(sdf.format(msg.getSentAt()));
        timeLabel.setStyle("-fx-text-fill: " + (isSent ? "#C8E6C9" : "#94A3B8") + "; -fx-font-size: 10px;");

        bubble.getChildren().addAll(contentLabel, timeLabel);

        // Alignment using spacer regions
        if (isSent) {
            // Sent message: spacer on left, bubble on right
            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            container.getChildren().addAll(spacer, bubble);
        } else {
            // Received message: bubble on left, spacer on right
            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            container.getChildren().addAll(bubble, spacer);
        }

        return container;
    }

    @FXML
    public void handleOwnerSendMessage() {
        if (currentUser == null)
            return;

        String content = ownerReplyArea != null ? ownerReplyArea.getText().trim() : "";

        if (content.isEmpty()) {
            setOwnerMsgStatus("Message cannot be empty!", "red");
            return;
        }

        if (ownerCurrentConversationSubject == null || ownerCurrentChatPartnerId == -1) {
            setOwnerMsgStatus("Select a conversation first.", "#FF9800");
            return;
        }

        try {
            String subject = "Re: " + ownerCurrentConversationSubject;
            com.greengrocer.models.Message message = new com.greengrocer.models.Message(
                    currentUser.getId(), ownerCurrentChatPartnerId, subject, content);

            if (messageDAO.sendMessage(message)) {
                ownerReplyArea.clear();
                loadOwnerChatMessages();
                loadOwnerConversations();
            } else {
                setOwnerMsgStatus("Failed to send.", "red");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            setOwnerMsgStatus("Error sending.", "red");
        }
    }

    private void updateOwnerUnreadCount() {
        if (ownerUnreadCountLabel == null || currentUser == null)
            return;

        try {
            int count = messageDAO.getUnreadCount(currentUser.getId());
            if (count > 0) {
                ownerUnreadCountLabel.setText(String.valueOf(count));
                ownerUnreadCountLabel.setStyle(
                        "-fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #EF4444; -fx-background-radius: 10; -fx-padding: 2 8;");
                ownerUnreadCountLabel.setVisible(true);
                ownerUnreadCountLabel.setManaged(true);
            } else {
                ownerUnreadCountLabel.setText("");
                ownerUnreadCountLabel.setVisible(false);
                ownerUnreadCountLabel.setManaged(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setOwnerMsgStatus(String text, String color) {
        if (ownerMsgStatusLabel != null) {
            ownerMsgStatusLabel.setText(text);
            ownerMsgStatusLabel.setStyle("-fx-text-fill: " + color + ";");
        }
    }

    // ==================== SUPPLIER MANAGEMENT ====================

    @FXML
    public void handleNewSupplier() {
        showSupplierDialog(null);
    }

    @FXML
    public void handleEditSupplier() {
        Supplier selected = supplierTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            supplierStatusLabel.setText("Select a supplier to edit.");
            supplierStatusLabel.setStyle("-fx-text-fill: #EF4444;");
            return;
        }
        showSupplierDialog(selected);
    }

    @FXML
    public void handleDeleteSupplier() {
        Supplier selected = supplierTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            supplierStatusLabel.setText("Select a supplier to delete.");
            supplierStatusLabel.setStyle("-fx-text-fill: #EF4444;");
            return;
        }

        boolean confirm = com.greengrocer.util.StyledAlert.showConfirmation("Delete Supplier", null,
                "Are you sure you want to delete " + selected.getName() + "?");
        if (confirm) {
            try {
                if (supplierDAO.deleteSupplier(selected.getId())) {
                    supplierStatusLabel.setText("Supplier deleted.");
                    supplierStatusLabel.setStyle("-fx-text-fill: #10B981;");
                    loadSuppliers();
                } else {
                    supplierStatusLabel.setText("Failed to delete supplier.");
                    supplierStatusLabel.setStyle("-fx-text-fill: #EF4444;");
                }
            } catch (SQLException e) {
                supplierStatusLabel.setText("Database error: " + e.getMessage());
                supplierStatusLabel.setStyle("-fx-text-fill: #EF4444;");
            }
        }
    }

    @FXML
    public void handleSearchSuppliers() {
        loadSuppliers();
    }

    private void loadSuppliers() {
        if (supplierTable == null)
            return;
        try {
            if (supplierDAO == null)
                supplierDAO = new SupplierDAO();
            ObservableList<Supplier> suppliers = FXCollections.observableArrayList(supplierDAO.getAllSuppliers());
            String search = supplierSearchField.getText().toLowerCase().trim();
            if (!search.isEmpty()) {
                ObservableList<Supplier> filtered = FXCollections.observableArrayList();
                for (Supplier s : suppliers) {
                    if (s.getName().toLowerCase().contains(search) ||
                            s.getContactPerson().toLowerCase().contains(search)) {
                        filtered.add(s);
                    }
                }
                supplierTable.setItems(filtered);
            } else {
                supplierTable.setItems(suppliers);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (supplierStatusLabel != null) {
                supplierStatusLabel.setText("Error loading suppliers.");
                supplierStatusLabel.setStyle("-fx-text-fill: #EF4444;");
            }
        }
    }

    private void showSupplierDialog(Supplier supplier) {
        boolean isEdit = supplier != null;
        Dialog<Supplier> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Supplier" : "Add Supplier");
        dialog.setHeaderText(isEdit ? "Update Supplier Details" : "Enter New Supplier Details");
        com.greengrocer.util.StyleHelper.applyAppIcon(dialog);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField name = new TextField();
        name.setPromptText("Company Name");
        TextField contact = new TextField();
        contact.setPromptText("Contact Person");
        TextField email = new TextField();
        email.setPromptText("Email");
        TextField phone = new TextField();
        phone.setPromptText("Phone");
        TextField address = new TextField();
        address.setPromptText("Address");

        if (isEdit) {
            name.setText(supplier.getName());
            contact.setText(supplier.getContactPerson());
            email.setText(supplier.getEmail());
            phone.setText(supplier.getPhone());
            address.setText(supplier.getAddress());
        }

        grid.add(new Label("Company Name:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("Contact Person:"), 0, 1);
        grid.add(contact, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(email, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phone, 1, 3);
        grid.add(new Label("Address:"), 0, 4);
        grid.add(address, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (isEdit) {
                    supplier.setName(name.getText());
                    supplier.setContactPerson(contact.getText());
                    supplier.setEmail(email.getText());
                    supplier.setPhone(phone.getText());
                    supplier.setAddress(address.getText());
                    return supplier;
                } else {
                    return new Supplier(name.getText(), contact.getText(), email.getText(), phone.getText(),
                            address.getText());
                }
            }
            return null;
        });

        Optional<Supplier> result = dialog.showAndWait();

        result.ifPresent(newSupplier -> {
            try {
                if (supplierDAO == null)
                    supplierDAO = new SupplierDAO();
                boolean success;
                if (isEdit) {
                    success = supplierDAO.updateSupplier(newSupplier);
                } else {
                    success = supplierDAO.addSupplier(newSupplier);
                }

                if (success) {
                    supplierStatusLabel.setText(isEdit ? "Supplier updated." : "Supplier added.");
                    supplierStatusLabel.setStyle("-fx-text-fill: #10B981;");
                    loadSuppliers();
                } else {
                    supplierStatusLabel.setText("Operation failed.");
                    supplierStatusLabel.setStyle("-fx-text-fill: #EF4444;");
                }
            } catch (SQLException e) {
                supplierStatusLabel.setText("Database error: " + e.getMessage());
                supplierStatusLabel.setStyle("-fx-text-fill: #EF4444;");
            }
        });
    }

    // ==================== CUSTOMER ANALYTICS ====================

    @FXML
    public void handleRefreshAnalytics() {
        loadCustomerAnalytics();
    }

    private void loadCustomerAnalytics() {
        if (analyticsTable == null)
            return;
        try {
            if (userDAO == null)
                userDAO = new com.greengrocer.dao.UserDAO();
            java.util.List<CustomerAnalytics> data = userDAO.getCustomerAnalytics();
            analyticsTable.setItems(FXCollections.observableArrayList(data));
            if (analyticsStatusLabel != null) {
                analyticsStatusLabel.setText("Analytics loaded.");
                analyticsStatusLabel.setStyle("-fx-text-fill: #10B981;");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (analyticsStatusLabel != null) {
                analyticsStatusLabel.setText("Error loading analytics.");
                analyticsStatusLabel.setStyle("-fx-text-fill: #EF4444;");
            }
        }
    }

    private void loadCarrierAnalytics() {
        if (carrierPerformanceTable == null)
            return;
        try {
            if (analyticsDAO == null)
                analyticsDAO = new AnalyticsDAO();
            java.util.List<CarrierPerformance> data = analyticsDAO.getCarrierPerformance();
            carrierPerformanceTable.setItems(FXCollections.observableArrayList(data));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCarrierReviews(int carrierId, String carrierName) {
        if (carrierReviewsTable == null)
            return;

        if (carrierReviewsLabel != null) {
            carrierReviewsLabel.setText("Reviews for: " + carrierName);
        }

        try {
            if (carrierRatingDAO == null)
                carrierRatingDAO = new CarrierRatingDAO();
            java.util.List<CarrierRating> reviews = carrierRatingDAO.getRatingsForCarrier(carrierId);
            carrierReviewsTable.setItems(FXCollections.observableArrayList(reviews));
        } catch (SQLException e) {
            e.printStackTrace();
            if (carrierReviewsLabel != null) {
                carrierReviewsLabel.setText("Error loading reviews.");
            }
        }
    }

    private void loadProductAnalytics() {
        if (topSellingTable == null || deadStockTable == null)
            return;
        try {
            if (analyticsDAO == null)
                analyticsDAO = new AnalyticsDAO();
            java.util.List<ProductSalesStats> sales = analyticsDAO.getProductSalesAnalysis();
            topSellingTable.setItems(FXCollections.observableArrayList(sales));

            java.util.List<ProductSalesStats> dead = analyticsDAO.getDeadStock();
            deadStockTable.setItems(FXCollections.observableArrayList(dead));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadHeatmap() {
        if (heatmapChart == null)
            return;
        try {
            if (analyticsDAO == null)
                analyticsDAO = new AnalyticsDAO();
            java.util.List<HourlyOrderStats> stats = analyticsDAO.getHourlyOrderStats();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Orders per Hour");

            // Initialize all 24 hours with 0
            java.util.Map<Integer, Integer> hourMap = new java.util.HashMap<>();
            for (int i = 0; i < 24; i++)
                hourMap.put(i, 0);

            for (HourlyOrderStats s : stats) {
                hourMap.put(s.getHourOfDay(), s.getOrderCount());
            }

            for (int i = 0; i < 24; i++) {
                String label = String.format("%02d:00", i);
                series.getData().add(new XYChart.Data<>(label, hourMap.get(i)));
            }

            heatmapChart.getData().clear();
            heatmapChart.getData().add(series);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
