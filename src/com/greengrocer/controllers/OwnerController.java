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

public class OwnerController {

    private User currentUser;
    private ProductDAO productDAO;
    private ObservableList<Product> productList;
    private File selectedImageFile;

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

    private int selectedProductId = -1; // For update functionality

    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, Integer> colId;
    @FXML
    private TableColumn<Product, String> colName;
    @FXML
    private TableColumn<Product, String> colType;
    @FXML
    private TableColumn<Product, Double> colPrice;
    @FXML
    private TableColumn<Product, Double> colStock;
    @FXML
    private TableColumn<Product, Double> colThreshold;
    @FXML
    private TableColumn<Product, Double> colCost;
    @FXML
    private TableColumn<Product, javafx.scene.image.ImageView> colImage;

    // Carrier Tab Fields
    @FXML
    private TextField carrUsernameField;
    @FXML
    private TextField carrPasswordField;
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
    private Label orderManagementStatus;

    private com.greengrocer.dao.OrderDAO orderDAO;

    public OwnerController() {
        this.productDAO = new ProductDAO();
        this.userDAO = new com.greengrocer.dao.UserDAO();
        this.reportDAO = new ReportDAO();
        this.orderDAO = new com.greengrocer.dao.OrderDAO();
    }

    public void initData(User user) {
        this.currentUser = user;
        System.out.println("Owner initialized: " + user.getUsername());

        prodTypeCombo.setItems(FXCollections.observableArrayList("Vegetable", "Fruit"));

        // Setup Table
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colThreshold.setCellValueFactory(new PropertyValueFactory<>("threshold"));
        colCost.setCellValueFactory(new PropertyValueFactory<>("costPrice"));

        // Image Column with ImageView
        colImage.setCellFactory(column -> new javafx.scene.control.TableCell<Product, javafx.scene.image.ImageView>() {
            private final javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();

            @Override
            protected void updateItem(javafx.scene.image.ImageView item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Product product = getTableRow().getItem();
                    javafx.scene.image.Image img = product.getImage();
                    if (img != null) {
                        imageView.setImage(img);
                        imageView.setFitWidth(50);
                        imageView.setFitHeight(50);
                        imageView.setPreserveRatio(true);
                        setGraphic(imageView);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

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
            productTable.setItems(productList);
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading products.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
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

            boolean success = productDAO.addProduct(name, type, price, costPrice, stock, threshold, fis);
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
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a product to delete.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Product");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete " + selected.getName() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (productDAO.deleteProduct(selected.getId())) {
                    statusLabel.setText("Product deleted.");
                    statusLabel.setStyle("-fx-text-fill: green;");
                    loadProducts();
                } else {
                    statusLabel.setText("Delete failed.");
                    statusLabel.setStyle("-fx-text-fill: red;");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                statusLabel.setText("Database error.");
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
            for (Map.Entry<String, Double> entry : salesByType.entrySet()) {
                pieData.add(new PieChart.Data(entry.getKey() + " ($" + String.format("%.2f", entry.getValue()) + ")",
                        entry.getValue()));
            }
            productTypeChart.setData(pieData);

            // Bar Chart - Revenue based on selected view
            loadRevenueChart();

            // Summary Statistics
            double totalRevenue = reportDAO.getTotalRevenue();
            int totalOrders = reportDAO.getTotalOrders();
            int totalProducts = reportDAO.getTotalProducts();
            double totalProfit = reportDAO.getTotalProfit();
            double inventoryCost = reportDAO.getTotalInventoryCost();

            totalRevenueLabel.setText("Total Revenue: $" + String.format("%.2f", totalRevenue));
            totalOrdersLabel.setText("Total Orders: " + totalOrders);
            totalProductsLabel.setText("Total Products: " + totalProducts);

            // Profit/Loss Summary
            totalProfitLabel.setText("Total Profit: $" + String.format("%.2f", totalProfit));
            if (totalProfit >= 0) {
                totalProfitLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            } else {
                totalProfitLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
            inventoryCostLabel.setText("Inventory Cost: $" + String.format("%.2f", inventoryCost));

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

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Order");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to cancel Order #" + selected.getId() + "?");

        java.util.Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
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
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a product to load.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        selectedProductId = selected.getId();
        prodNameField.setText(selected.getName());
        prodTypeCombo.setValue(selected.getType());
        prodPriceField.setText(String.valueOf(selected.getPrice()));
        prodCostField.setText(String.valueOf(selected.getCostPrice()));
        prodStockField.setText(String.valueOf(selected.getStock()));
        prodThresholdField.setText(String.valueOf(selected.getThreshold()));

        statusLabel.setText("Product #" + selected.getId() + " loaded. Edit and click Update.");
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

            Product updatedProduct = new Product(selectedProductId, name, type, price, costPrice, stock, threshold,
                    null);

            if (productDAO.updateProduct(updatedProduct)) {
                statusLabel.setText("Product #" + selectedProductId + " updated successfully!");
                statusLabel.setStyle("-fx-text-fill: green;");
                loadProducts();
                clearFields();
                selectedProductId = -1;
            } else {
                statusLabel.setText("Failed to update product.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid number format.");
            statusLabel.setStyle("-fx-text-fill: red;");
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Database error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
}
