package com.greengrocer.controllers;

import com.greengrocer.dao.ProductDAO;
import com.greengrocer.dao.ReportDAO;
import com.greengrocer.models.Product;
import com.greengrocer.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

public class OwnerController {

    private User currentUser;
    private ProductDAO productDAO;
    private ObservableList<Product> productList;
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
    private TableColumn<com.greengrocer.models.Order, String> colAllOrderDate;
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
    }

    public void initData(User user) {
        this.currentUser = user;
        System.out.println("Owner initialized: " + user.getUsername());

        // Welcome message
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFirstName() + " " + currentUser.getLastName() + "!");
        }

        prodTypeCombo.setItems(FXCollections.observableArrayList("Vegetable", "Fruit", "Dairy", "Bakery", "Meat",
                "Beverages", "Snacks"));

        // Setup tab change listener for auto-refresh
        if (mainTabPane != null) {
            mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab != null) {
                    String tabText = newTab.getText();
                    if (tabText.contains("Products")) {
                        loadProducts();
                    } else if (tabText.contains("Reports")) {
                        handleRefreshReports();
                    } else if (tabText.contains("Orders")) {
                        handleRefreshAllOrders();
                    } else if (tabText.contains("Alerts")) {
                        handleRefreshAlerts();
                    } else if (tabText.contains("Coupons")) {
                        loadCoupons();
                        loadCouponHistory();
                    }
                }
            });
        }

        // Grid View is now used - no table setup needed

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
        colAllOrderTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colAllOrderStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        orderStatusFilter
                .setItems(FXCollections.observableArrayList("All", "Pending", "Delivering", "Delivered", "Cancelled"));
        orderStatusFilter.setValue("All");

        loadAllOrders();
    }

    @FXML
    public void handleRefreshProducts() {
        loadProducts();
    }

    private void loadProducts() {
        try {
            productList = FXCollections.observableArrayList(productDAO.getAllProducts());
            refreshProductGrid();
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading products.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
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
        VBox card = new VBox(8);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(160);
        card.setPadding(new Insets(12));
        card.setAlignment(Pos.CENTER);

        // Image
        javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView();
        iv.setFitHeight(70);
        iv.setFitWidth(70);
        iv.setPreserveRatio(true);
        if (product.getImage() != null) {
            iv.setImage(product.getImage());
        }

        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #F8FAFC;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(140);

        Label typeLabel = new Label(product.getType());
        typeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");

        Label priceLabel = new Label(FormatHelper.formatCurrency(product.getPrice()));
        priceLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 14px;");

        Label stockLabel = new Label("Stock: " + String.format("%.1f", product.getStock()));
        if (product.getStock() <= product.getThreshold()) {
            stockLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
        } else {
            stockLabel.setStyle("-fx-text-fill: #94A3B8;");
        }

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("button-secondary");
        editBtn.setPrefWidth(120);
        editBtn.setOnAction(e -> {
            selectedProduct = product;
            selectedProductId = product.getId();
            prodNameField.setText(product.getName());
            prodTypeCombo.setValue(product.getType());
            prodPriceField.setText(String.valueOf(product.getPrice()));
            prodCostField.setText(String.valueOf(product.getCostPrice()));
            prodStockField.setText(String.valueOf(product.getStock()));
            prodThresholdField.setText(String.valueOf(product.getThreshold()));
            statusLabel.setText("Product #" + product.getId() + " loaded. Edit and click Update.");
            statusLabel.setStyle("-fx-text-fill: #6366f1;");
        });

        card.getChildren().addAll(iv, nameLabel, typeLabel, priceLabel, stockLabel, editBtn);

        // Low stock border
        if (product.getStock() <= product.getThreshold()) {
            card.setStyle("-fx-border-color: #800000; -fx-border-width: 2; -fx-border-radius: 12;");
        }

        return card;
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
            statusLabel.setStyle("-fx-text-fill: blue;");

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
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            double costPrice = costStr.isEmpty() ? 0 : Double.parseDouble(costStr);
            double stock = Double.parseDouble(stockStr);
            double threshold = Double.parseDouble(thresholdStr);

            if (price < 0 || stock < 0 || threshold < 0 || costPrice < 0) {
                statusLabel.setText("Values cannot be negative.");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            FileInputStream fis = null;
            if (selectedImageFile != null) {
                fis = new FileInputStream(selectedImageFile);
            }

            boolean success = productDAO.addProduct(name, type, price, costPrice, stock, threshold, fis, "kg");
            if (success) {
                statusLabel.setText("Product added successfully!");
                statusLabel.setStyle("-fx-text-fill: green;");
                loadProducts();
                clearFields();
            } else {
                statusLabel.setText("Failed to add product.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }

        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid number format.");
            statusLabel.setStyle("-fx-text-fill: red;");
        } catch (FileNotFoundException e) {
            statusLabel.setText("Image file not found.");
            statusLabel.setStyle("-fx-text-fill: red;");
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Database error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    public void handleDeleteProduct() {
        if (selectedProduct == null) {
            statusLabel.setText("Click 'Edit' on a product card first.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        boolean confirm = StyledAlert.showConfirmation("Delete Product", null,
                "Are you sure you want to delete " + selectedProduct.getName() + "?");

        if (confirm) {
            try {
                if (productDAO.deleteProduct(selectedProduct.getId())) {
                    statusLabel.setText("Product deleted.");
                    statusLabel.setStyle("-fx-text-fill: green;");
                    selectedProduct = null;
                    selectedProductId = -1;
                    loadProducts();
                } else {
                    statusLabel.setText("Delete failed.");
                    statusLabel.setStyle("-fx-text-fill: red;");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                // Check if it's a foreign key constraint error
                String msg = e.getMessage().toLowerCase();
                if (msg.contains("foreign key") || msg.contains("constraint")) {
                    statusLabel.setText("Cannot delete: Product has order history.");
                } else {
                    statusLabel.setText("Database error: " + e.getMessage());
                }
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }

    private void clearFields() {
        prodNameField.clear();
        prodTypeCombo.getSelectionModel().clearSelection();
        prodPriceField.clear();
        prodStockField.clear();
        prodThresholdField.clear();
        selectedImageFile = null;
        if (previewImageView != null) {
            previewImageView.setImage(null);
        }
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
            statusLabel.setStyle("-fx-text-fill: red;");
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
            statusLabel.setText("Username and Password required for carrier.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            if (userDAO.register(username, password, "carrier", name, surname, "", phone)) {
                statusLabel.setText("Carrier added successfully.");
                statusLabel.setStyle("-fx-text-fill: green;");
                loadCarriers();
                carrUsernameField.clear();
                carrPasswordField.clear();
                carrNameField.clear();
                carrSurnameField.clear();
                carrPhoneField.clear();
            } else {
                statusLabel.setText("Failed to add carrier (Username used?).");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Database error.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    public void handleFireCarrier() {
        User selected = carrierTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a carrier to fire.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        try {
            if (userDAO.deleteUser(selected.getId())) {
                statusLabel.setText("Carrier fired.");
                statusLabel.setStyle("-fx-text-fill: green;");
                loadCarriers();
            } else {
                statusLabel.setText("Failed to delete carrier.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Database error.");
            statusLabel.setStyle("-fx-text-fill: red;");
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
            productTypeChart.setLabelsVisible(true);
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
                totalProfitLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            } else {
                totalProfitLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
            inventoryCostLabel.setText("Inventory Cost: " + FormatHelper.formatCurrency(inventoryCost));

            // Profit/Loss Table
            loadProfitLossTable();

            // Cost Analysis Table
            loadCostAnalysisTable();

            reportStatusLabel.setText("Reports loaded.");
            reportStatusLabel.setStyle("-fx-text-fill: green;");
        } catch (SQLException e) {
            e.printStackTrace();
            reportStatusLabel.setText("Error loading reports.");
            reportStatusLabel.setStyle("-fx-text-fill: red;");
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
            orderManagementStatus.setStyle("-fx-text-fill: blue;");
        } catch (SQLException e) {
            e.printStackTrace();
            orderManagementStatus.setText("Error filtering orders.");
            orderManagementStatus.setStyle("-fx-text-fill: red;");
        }
    }

    private void loadAllOrders() {
        try {
            ObservableList<com.greengrocer.models.Order> orders = FXCollections
                    .observableArrayList(orderDAO.getAllOrders());
            allOrdersTable.setItems(orders);
            orderManagementStatus.setText("Loaded " + orders.size() + " orders.");
            orderManagementStatus.setStyle("-fx-text-fill: blue;");
        } catch (SQLException e) {
            e.printStackTrace();
            orderManagementStatus.setText("Error loading orders.");
            orderManagementStatus.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    public void handleCancelOrder() {
        com.greengrocer.models.Order selected = allOrdersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            orderManagementStatus.setText("Select an order to cancel.");
            orderManagementStatus.setStyle("-fx-text-fill: red;");
            return;
        }

        if ("Cancelled".equals(selected.getStatus()) || "Delivered".equals(selected.getStatus())) {
            orderManagementStatus.setText("Cannot cancel this order.");
            orderManagementStatus.setStyle("-fx-text-fill: red;");
            return;
        }

        boolean confirm = StyledAlert.showConfirmation("Cancel Order", null,
                "Are you sure you want to cancel Order #" + selected.getId() + "?");

        if (confirm) {
            try {
                if (orderDAO.cancelOrder(selected.getId())) {
                    orderManagementStatus.setText("Order #" + selected.getId() + " cancelled.");
                    orderManagementStatus.setStyle("-fx-text-fill: green;");
                    loadAllOrders();
                } else {
                    orderManagementStatus.setText("Failed to cancel order.");
                    orderManagementStatus.setStyle("-fx-text-fill: red;");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                orderManagementStatus.setText("Database error.");
                orderManagementStatus.setStyle("-fx-text-fill: red;");
            }
        }
    }

    @FXML
    public void handleLoadProductToForm() {
        // This method is now handled by the Edit button on each card
        if (selectedProduct == null) {
            statusLabel.setText("Click 'Edit' on a product card first.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        // Product already loaded when Edit was clicked
        statusLabel.setText("Product already loaded from card.");
        statusLabel.setStyle("-fx-text-fill: blue;");
    }

    @FXML
    public void handleUpdateProduct() {
        if (selectedProductId == -1) {
            statusLabel.setText("First load a product using 'Load to Form'.");
            statusLabel.setStyle("-fx-text-fill: red;");
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
            statusLabel.setStyle("-fx-text-fill: red;");
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
                statusLabel.setStyle("-fx-text-fill: green;");
                loadProducts();
                clearFields();
                selectedProductId = -1;
                selectedImageFile = null;
                if (previewImageView != null)
                    previewImageView.setImage(null);
            } else {
                statusLabel.setText("Failed to update product.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid number format.");
            statusLabel.setStyle("-fx-text-fill: red;");
        } catch (java.io.IOException e) {
            statusLabel.setText("Error reading image file.");
            statusLabel.setStyle("-fx-text-fill: red;");
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Database error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
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
                alertCountLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            } else {
                alertCountLabel.setText(lowStock.size() + " products need restocking!");
                alertCountLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
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
            couponStatusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            double discount = Double.parseDouble(discountStr);
            int maxUses = Integer.parseInt(maxUsesStr);

            if (discount <= 0 || discount > 100) {
                couponStatusLabel.setText("Discount must be between 1-100%!");
                couponStatusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            if (couponDAO.createCoupon(code, discount, maxUses)) {
                couponStatusLabel.setText("Coupon created: " + code.toUpperCase());
                couponStatusLabel.setStyle("-fx-text-fill: green;");
                clearCouponForm();
                loadCoupons();
            } else {
                couponStatusLabel.setText("Failed to create coupon (code may exist)");
                couponStatusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (NumberFormatException e) {
            couponStatusLabel.setText("Invalid number format!");
            couponStatusLabel.setStyle("-fx-text-fill: red;");
        } catch (SQLException e) {
            couponStatusLabel.setText("Database error: " + e.getMessage());
            couponStatusLabel.setStyle("-fx-text-fill: red;");
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
            couponStatusLabel.setStyle("-fx-text-fill: red;");
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
                    couponStatusLabel.setStyle("-fx-text-fill: green;");
                    loadCoupons();
                }
            } catch (NumberFormatException e) {
                couponStatusLabel.setText("Invalid number!");
                couponStatusLabel.setStyle("-fx-text-fill: red;");
            } catch (SQLException e) {
                couponStatusLabel.setText("Update error!");
                couponStatusLabel.setStyle("-fx-text-fill: red;");
            }
        });
    }

    @FXML
    public void handleToggleCouponActive() {
        com.greengrocer.models.Coupon selected = couponTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            couponStatusLabel.setText("Select a coupon!");
            couponStatusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            boolean newStatus = !selected.isActive();
            if (couponDAO.updateCoupon(selected.getId(), selected.getMaxUses(), newStatus)) {
                String status = newStatus ? "activated" : "deactivated";
                couponStatusLabel.setText("Coupon " + status + "!");
                couponStatusLabel.setStyle("-fx-text-fill: green;");
                loadCoupons();
            }
        } catch (SQLException e) {
            couponStatusLabel.setText("Status change failed!");
            couponStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    public void handleDeleteCoupon() {
        com.greengrocer.models.Coupon selected = couponTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            couponStatusLabel.setText("Select a coupon!");
            couponStatusLabel.setStyle("-fx-text-fill: red;");
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
                    couponStatusLabel.setStyle("-fx-text-fill: green;");
                    loadCoupons();
                    loadCouponHistory();
                }
            } catch (SQLException e) {
                couponStatusLabel.setText("Delete error!");
                couponStatusLabel.setStyle("-fx-text-fill: red;");
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
            colHistoryDate.setCellValueFactory(
                    data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[1].toString()));
            colHistoryUser.setCellValueFactory(
                    data -> new javafx.beans.property.SimpleStringProperty((String) data.getValue()[2]));
            colHistoryAmount.setCellValueFactory(
                    data -> new javafx.beans.property.SimpleDoubleProperty((Double) data.getValue()[3]).asObject());

            java.util.List<Object[]> history = couponDAO.getAllCouponUsageHistory();
            couponHistoryTable.setItems(FXCollections.observableArrayList(history));
        } catch (SQLException e) {
            e.printStackTrace();
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
                javafx.scene.layout.HBox convItem = createOwnerConversationItem(entry.getKey(), entry.getValue());
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
            com.greengrocer.models.Message lastMessage) {
        javafx.scene.layout.HBox item = new javafx.scene.layout.HBox(10);
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        item.setStyle("-fx-padding: 12; -fx-background-color: #334155; -fx-background-radius: 8; -fx-cursor: hand;");

        javafx.scene.layout.VBox textContent = new javafx.scene.layout.VBox(3);
        textContent.setMaxWidth(220);

        // Customer name
        String customerName = lastMessage.getSenderId() == currentUser.getId() ? lastMessage.getReceiverName()
                : lastMessage.getSenderName();

        Label nameLabel = new Label(customerName != null ? customerName : "Customer");
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 13px;");

        Label subjectLabel = new Label(subject);
        subjectLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 11px;");
        subjectLabel.setMaxWidth(200);

        String preview = lastMessage.getContent();
        if (preview.length() > 25)
            preview = preview.substring(0, 25) + "...";
        Label previewLabel = new Label(preview);
        previewLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 10px;");

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        Label timeLabel = new Label(sdf.format(lastMessage.getSentAt()));
        timeLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 10px;");

        textContent.getChildren().addAll(nameLabel, subjectLabel, previewLabel);

        item.getChildren().addAll(textContent, new javafx.scene.layout.Region(), timeLabel);
        javafx.scene.layout.HBox.setHgrow(item.getChildren().get(1), javafx.scene.layout.Priority.ALWAYS);

        final String finalCustomerName = customerName;
        item.setOnMouseClicked(e -> {
            ownerCurrentConversationSubject = subject;
            ownerCurrentChatPartnerId = lastMessage.getSenderId() == currentUser.getId() ? lastMessage.getReceiverId()
                    : lastMessage.getSenderId();
            ownerChatPartnerLabel
                    .setText("💬 " + (finalCustomerName != null ? finalCustomerName : "Customer") + " - " + subject);
            loadOwnerChatMessages();
        });

        item.setOnMouseEntered(e -> item.setStyle(
                "-fx-padding: 12; -fx-background-color: #475569; -fx-background-radius: 8; -fx-cursor: hand;"));
        item.setOnMouseExited(e -> item.setStyle(
                "-fx-padding: 12; -fx-background-color: #334155; -fx-background-radius: 8; -fx-cursor: hand;"));

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
            setOwnerMsgStatus("Type a message.", "red");
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
                ownerUnreadCountLabel.setText(count + "");
                ownerUnreadCountLabel.setStyle(
                        "-fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #EF4444; -fx-background-radius: 10; -fx-padding: 2 8;");
            } else {
                ownerUnreadCountLabel.setText("");
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
}
