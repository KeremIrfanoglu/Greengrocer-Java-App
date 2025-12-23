package com.greengrocer.controllers;

import com.greengrocer.dao.OrderDAO;
import com.greengrocer.dao.ProductDAO;
import com.greengrocer.dao.CartDAO;
import com.greengrocer.models.CartItem;
import com.greengrocer.models.Order;
import com.greengrocer.models.Product;
import com.greengrocer.models.User;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class CustomerController {
    private User currentUser;
    private ProductDAO productDAO;
    private OrderDAO orderDAO;
    private CartDAO cartDAO;
    private com.greengrocer.dao.UserDAO userDAO;
    private com.greengrocer.dao.RecommendationDAO recommendationDAO;
    private ObservableList<Product> productList;
    private ObservableList<CartItem> cartList;
    private ObservableList<Order> orderList;
    private double gPointsToUse = 0.0; // G Points to apply at checkout

    @FXML
    private javafx.scene.control.TabPane mainTabPane;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label gPointsLabel; // G Points balance display

    // Shop Tab
    @FXML
    private TableView<Product> shopTable;
    @FXML
    private TableColumn<Product, String> colShopName;
    @FXML
    private TableColumn<Product, String> colShopType;
    @FXML
    private TableColumn<Product, Double> colShopPrice;
    @FXML
    private TableColumn<Product, Double> colShopStock;
    @FXML
    private TableColumn<Product, javafx.scene.image.ImageView> colShopImage;
    @FXML
    private TableColumn<Product, String> colShopFav;

    @FXML
    private javafx.scene.control.ComboBox<String> filterTypeCombo;
    @FXML
    private javafx.scene.control.ComboBox<String> sortCombo;
    @FXML
    private TextField searchField;

    @FXML
    private TextField quantityField;
    @FXML
    private Label quantityLabel;
    @FXML
    private Label addToCartStatusLabel;
    @FXML
    private Label updateCartStatusLabel;

    private ObservableList<Product> allProducts; // Keep original list for filtering
    private com.greengrocer.dao.FavoritesDAO favoritesDAO;

    // Cart Tab
    @FXML
    private TableView<CartItem> cartTable;
    @FXML
    private TableColumn<CartItem, String> colCartName;
    @FXML
    private TableColumn<CartItem, Double> colCartPrice;
    @FXML
    private TableColumn<CartItem, Double> colCartQty;
    @FXML
    private TableColumn<CartItem, Double> colCartTotal;

    @FXML
    private Label cartTotalLabel;
    @FXML
    private TextField updateQtyField;

    // Orders Tab
    @FXML
    private TableView<Order> orderTable;
    @FXML
    private TableColumn<Order, Integer> colOrderId;
    @FXML
    private TableColumn<Order, String> colOrderDate;
    @FXML
    private TableColumn<Order, Double> colOrderTotal;
    @FXML
    private TableColumn<Order, String> colOrderStatus;

    // Favorites Tab
    @FXML
    private TableView<Product> favoritesTable;
    @FXML
    private TableColumn<Product, String> colFavName;
    @FXML
    private TableColumn<Product, String> colFavType;
    @FXML
    private TableColumn<Product, Double> colFavPrice;
    @FXML
    private TableColumn<Product, Double> colFavStock;
    @FXML
    private TextField favQuantityField;
    @FXML
    private Button favButton;

    // G Points Cart Controls
    @FXML
    private TextField gPointsField;
    @FXML
    private Label discountLabel;
    @FXML
    private Label finalTotalLabel;

    // Recommendation List
    @FXML
    private ListView<String> recommendationList;

    // Coupon fields
    @FXML
    private TextField couponCodeField;
    @FXML
    private Label couponStatusLabel;
    private com.greengrocer.dao.CouponDAO couponDAO;
    private com.greengrocer.models.Coupon appliedCoupon = null;
    private double couponDiscount = 0.0;

    // Profile Tab fields
    @FXML
    private Label profileUsernameLabel;
    @FXML
    private TextField profileFirstNameField;
    @FXML
    private TextField profileLastNameField;
    @FXML
    private TextField profileAddressField;
    @FXML
    private TextField profilePhoneField;
    @FXML
    private Label profileStatusLabel;
    @FXML
    private javafx.scene.control.PasswordField currentPasswordField;
    @FXML
    private javafx.scene.control.PasswordField newPasswordField;
    @FXML
    private javafx.scene.control.PasswordField confirmNewPasswordField;
    @FXML
    private Label passwordStatusLabel;

    private com.greengrocer.util.NotificationService notificationService;

    public CustomerController() {
        this.productDAO = new ProductDAO();
        this.orderDAO = new OrderDAO();
        this.userDAO = new com.greengrocer.dao.UserDAO();
        this.recommendationDAO = new com.greengrocer.dao.RecommendationDAO();
        this.favoritesDAO = new com.greengrocer.dao.FavoritesDAO();
        this.couponDAO = new com.greengrocer.dao.CouponDAO();
        this.cartDAO = new CartDAO();
        this.notificationService = new com.greengrocer.util.NotificationService();
        this.cartList = FXCollections.observableArrayList();
        this.orderList = FXCollections.observableArrayList();
    }

    public void initData(User user) {
        this.currentUser = user;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + user.getFirstName());
        }
        updateGPointsDisplay();

        // Load saved cart from database
        loadSavedCart();

        // Check for price drops and low stock alerts
        javafx.application.Platform.runLater(() -> {
            notificationService.checkAndNotify(user.getId());
        });

        // Setup tab change listener for auto-refresh
        if (mainTabPane != null) {
            mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab != null) {
                    String tabText = newTab.getText();
                    if (tabText.contains("Shop")) {
                        loadProducts();
                    } else if (tabText.contains("Cart")) {
                        updateCartTotalWithDiscount();
                    } else if (tabText.contains("Orders")) {
                        handleRefreshOrders();
                    } else if (tabText.contains("Favorites")) {
                        handleRefreshFavorites();
                    } else if (tabText.contains("Profile")) {
                        loadProfileData();
                    }
                }
            });
        }

        // Shop Setup
        colShopName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colShopType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colShopPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colShopStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // Image Column with ImageView
        colShopImage
                .setCellFactory(column -> new javafx.scene.control.TableCell<Product, javafx.scene.image.ImageView>() {
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

        // Favorite Star Column - shows star if product is in favorites
        colShopFav.setCellFactory(column -> new javafx.scene.control.TableCell<Product, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText("");
                    setStyle("");
                } else {
                    Product product = getTableRow().getItem();
                    try {
                        if (favoritesDAO.isFavorite(currentUser.getId(), product.getId())) {
                            setText("*");
                            setStyle("-fx-font-size: 16px; -fx-alignment: CENTER;");
                        } else {
                            setText("");
                            setStyle("");
                        }
                    } catch (java.sql.SQLException e) {
                        setText("");
                    }
                }
            }
        });

        // Setup Filter/Sort ComboBoxes
        filterTypeCombo.setItems(FXCollections.observableArrayList("All", "Vegetable", "Fruit", "Dairy", "Bakery",
                "Meat", "Beverages", "Snacks", "Favorites"));
        filterTypeCombo.setValue("All");
        sortCombo.setItems(FXCollections.observableArrayList("Default", "Name (A-Z)", "Name (Z-A)", "Price (Low-High)",
                "Price (High-Low)"));
        sortCombo.setValue("Default");

        loadProducts();

        // Update favorite button and quantity label when selection changes
        shopTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateFavButton();
            updateQuantityLabelForProduct(newSelection);
        });

        // Cart Setup
        colCartName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductName()));
        colCartPrice
                .setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());
        colCartQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colCartTotal
                .setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotal()).asObject());

        cartTable.setItems(cartList);

        // Orders Setup
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colOrderDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colOrderTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colOrderStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadOrders();
    }

    private void loadProducts() {
        try {
            allProducts = FXCollections.observableArrayList(productDAO.getAllProducts());
            // Sort products by name alphabetically (A-Z)
            allProducts.sort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
            productList = FXCollections.observableArrayList(allProducts);
            shopTable.setItems(productList);
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading products.");
        }
    }

    private void loadOrders() {
        try {
            orderList = FXCollections.observableArrayList(orderDAO.getOrdersByCustomer(currentUser.getId()));
            orderTable.setItems(orderList);
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading orders.");
        }
    }

    /**
     * Load saved cart from database (persist across sessions)
     */
    private void loadSavedCart() {
        if (currentUser == null)
            return;
        try {
            java.util.List<CartItem> savedItems = cartDAO.getCartByUserId(currentUser.getId());
            cartList.clear();
            cartList.addAll(savedItems);
            if (cartTable != null) {
                cartTable.refresh();
            }
            updateCartTotal();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRefreshOrders() {
        loadOrders();
    }

    @FXML
    public void handleAddToCart() {
        Product selected = shopTable.getSelectionModel().getSelectedItem();
        String qtyStr = quantityField.getText();

        if (selected == null || qtyStr.isEmpty()) {
            setLocalStatus(addToCartStatusLabel, "Select product and enter quantity.", "#FF9800");
            return;
        }

        try {
            double qty = Double.parseDouble(qtyStr);
            if (qty <= 0) {
                setLocalStatus(addToCartStatusLabel, "Quantity must be positive.", "red");
                return;
            }

            // Validate quantity based on unit type
            if (selected.isSoldByPiece()) {
                // For piece-based products, only allow whole numbers
                if (qty != Math.floor(qty)) {
                    setLocalStatus(addToCartStatusLabel, "Whole numbers only!", "red");
                    return;
                }
            }

            if (qty > selected.getStock()) {
                setLocalStatus(addToCartStatusLabel, "Not enough stock!", "red");
                return;
            }

            // Check if already in cart
            boolean exists = false;
            for (CartItem item : cartList) {
                if (item.getProduct().getId() == selected.getId()) {
                    if (item.getQuantity() + qty > selected.getStock()) {
                        setLocalStatus(addToCartStatusLabel, "Exceeds stock!", "red");
                        return;
                    }
                    item.setQuantity(item.getQuantity() + qty);
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                cartList.add(new CartItem(selected, qty));
            }

            // Persist to database
            try {
                cartDAO.addToCart(currentUser.getId(), selected.getId(), qty);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            cartTable.refresh();
            updateCartTotal();

            // Check for threshold discount
            CartItem addedItem = null;
            for (CartItem item : cartList) {
                if (item.getProduct().getId() == selected.getId()) {
                    addedItem = item;
                    break;
                }
            }
            if (addedItem != null && addedItem.isDiscounted()) {
                setLocalStatus(addToCartStatusLabel, "Added +10% OFF!", "#4CAF50");
            } else {
                setLocalStatus(addToCartStatusLabel, "Added!", "#4CAF50");
            }
            quantityField.clear();

        } catch (NumberFormatException e) {
            setLocalStatus(addToCartStatusLabel, "Invalid qty!", "red");
        }
    }

    @FXML
    public void handleRemoveFromCart() {
        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Remove from database
            try {
                cartDAO.removeFromCart(currentUser.getId(), selected.getProduct().getId());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            cartList.remove(selected);
            updateCartTotal();
            setLocalStatus(updateCartStatusLabel, "Removed!", "#F44336");
        }
    }

    @FXML
    public void handleUpdateCartQty() {
        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setLocalStatus(updateCartStatusLabel, "Select item!", "#FF9800");
            return;
        }

        String qtyStr = updateQtyField != null ? updateQtyField.getText().trim() : "";
        if (qtyStr.isEmpty()) {
            setLocalStatus(updateCartStatusLabel, "Enter qty!", "#FF9800");
            return;
        }

        try {
            double newQty = Double.parseDouble(qtyStr);
            if (newQty <= 0) {
                setLocalStatus(updateCartStatusLabel, "Must be positive!", "red");
                return;
            }

            Product product = selected.getProduct();

            // Validate integer for piece-based products
            if (product.isSoldByPiece() && newQty != Math.floor(newQty)) {
                setLocalStatus(updateCartStatusLabel, "Whole numbers!", "red");
                return;
            }

            // Check stock
            if (newQty > product.getStock()) {
                setLocalStatus(updateCartStatusLabel, "Not enough stock!", "red");
                return;
            }

            // Update local cart
            selected.setQuantity(newQty);

            // Update database
            try {
                cartDAO.updateQuantity(currentUser.getId(), product.getId(), newQty);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            cartTable.refresh();
            updateCartTotal();
            updateQtyField.clear();
            setLocalStatus(updateCartStatusLabel, "Updated!", "#4CAF50");

        } catch (NumberFormatException e) {
            setLocalStatus(updateCartStatusLabel, "Invalid qty!", "red");
        }
    }

    @FXML
    public void handleCheckout() {
        if (cartList.isEmpty()) {
            statusLabel.setText("Cart is empty.");
            return;
        }

        double subtotal = getCartTotal();

        // Minimum cart value requirement
        double MINIMUM_CART_VALUE = 20.0;
        if (subtotal < MINIMUM_CART_VALUE) {
            statusLabel.setText(
                    String.format("Minimum order is TL%.2f. Your cart: TL%.2f", MINIMUM_CART_VALUE, subtotal));
            statusLabel.setStyle("-fx-text-fill: #FF9800;");
            return;
        }

        double couponDiscountAmount = appliedCoupon != null ? subtotal * (appliedCoupon.getDiscountPercent() / 100.0)
                : 0;
        double afterDiscounts = subtotal - gPointsToUse - couponDiscountAmount;
        if (afterDiscounts < 0)
            afterDiscounts = 0;

        // Calculate VAT
        double vatAmount = afterDiscounts * 0.20;
        double finalTotal = afterDiscounts + vatAmount;

        // Show delivery date/time selection dialog
        javafx.scene.control.Dialog<java.time.LocalDateTime> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Select Delivery Date & Time");
        dialog.setHeaderText("Choose when you want your order delivered\n(Within 48 hours from now)");

        // Create date/time pickers
        javafx.scene.control.DatePicker datePicker = new javafx.scene.control.DatePicker();
        datePicker.setValue(java.time.LocalDate.now().plusDays(1));

        // Restrict to next 48 hours
        java.time.LocalDate minDate = java.time.LocalDate.now();
        java.time.LocalDate maxDate = java.time.LocalDate.now().plusDays(2);
        datePicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(java.time.LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(minDate) || date.isAfter(maxDate));
            }
        });

        // Time slot selection
        javafx.scene.control.ComboBox<String> timeCombo = new javafx.scene.control.ComboBox<>();
        timeCombo.getItems().addAll(
                "09:00 - 11:00", "11:00 - 13:00", "13:00 - 15:00",
                "15:00 - 17:00", "17:00 - 19:00", "19:00 - 21:00");
        timeCombo.setValue("11:00 - 13:00");

        // Layout
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new javafx.geometry.Insets(20));
        grid.add(new javafx.scene.control.Label("Delivery Date:"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(new javafx.scene.control.Label("Time Slot:"), 0, 1);
        grid.add(timeCombo, 1, 1);

        // Order summary
        StringBuilder summary = new StringBuilder();
        summary.append("\n━━━━━━━━ ORDER SUMMARY ━━━━━━━━\n");
        for (CartItem item : cartList) {
            summary.append(String.format("• %s x%.2f kg = ₺%.2f\n",
                    item.getProductName(), item.getQuantity(), item.getTotal()));
        }
        summary.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        summary.append(String.format("Subtotal: ₺%.2f\n", subtotal));
        if (gPointsToUse > 0)
            summary.append(String.format("G Points: -₺%.2f\n", gPointsToUse));
        if (couponDiscountAmount > 0)
            summary.append(String.format("Coupon: -₺%.2f\n", couponDiscountAmount));
        summary.append(String.format("VAT (20%%): +₺%.2f\n", vatAmount));
        summary.append(String.format("TOTAL: ₺%.2f", finalTotal));

        javafx.scene.control.TextArea summaryArea = new javafx.scene.control.TextArea(summary.toString());
        summaryArea.setEditable(false);
        summaryArea.setPrefHeight(180);
        summaryArea.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px;");
        grid.add(summaryArea, 0, 2, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(
                javafx.scene.control.ButtonType.OK,
                javafx.scene.control.ButtonType.CANCEL);

        // Convert result
        final double finalTotalForLambda = finalTotal;
        dialog.setResultConverter(button -> {
            if (button == javafx.scene.control.ButtonType.OK) {
                java.time.LocalDate date = datePicker.getValue();
                String timeSlot = timeCombo.getValue();
                int hour = Integer.parseInt(timeSlot.split(":")[0]);
                return java.time.LocalDateTime.of(date, java.time.LocalTime.of(hour, 0));
            }
            return null;
        });

        java.util.Optional<java.time.LocalDateTime> result = dialog.showAndWait();

        if (!result.isPresent()) {
            statusLabel.setText("Checkout cancelled.");
            return;
        }

        java.time.LocalDateTime deliveryDateTime = result.get();

        try {
            // Use G Points if any
            if (gPointsToUse > 0) {
                if (!userDAO.useGPoints(currentUser.getId(), gPointsToUse)) {
                    statusLabel.setText("G Points could not be used!");
                    statusLabel.setStyle("-fx-text-fill: red;");
                    return;
                }
            }

            // Create order with the subtotal (full amount for records)
            int orderId = orderDAO.createOrder(currentUser.getId(), cartList, subtotal);
            if (orderId > 0) {
                // Award G Points based on actual payment (1/5 of finalTotal)
                double pointsEarned = finalTotal / 5.0;
                userDAO.addGPoints(currentUser.getId(), finalTotal);

                // Generate and save invoice
                byte[] invoiceData = com.greengrocer.util.InvoiceGenerator.generateInvoiceBytes(
                        currentUser, new java.util.ArrayList<>(cartList),
                        subtotal, gPointsToUse, couponDiscountAmount, vatAmount, finalTotal, deliveryDateTime);

                // Save invoice to database
                try {
                    orderDAO.saveInvoice(orderId, invoiceData);
                } catch (SQLException ex) {
                    System.err.println("Failed to save invoice to database: " + ex.getMessage());
                }

                // Save invoice to file (Downloads folder)
                String savedPath = com.greengrocer.util.InvoiceGenerator.saveInvoiceToFile(invoiceData, orderId);

                // Show invoice to customer
                com.greengrocer.util.InvoiceGenerator.showInvoiceDialog(invoiceData);

                // Open PDF invoice with default application
                if (savedPath != null) {
                    try {
                        java.awt.Desktop.getDesktop().open(new java.io.File(savedPath));
                    } catch (Exception ex) {
                        // Ignore if can't open PDF
                    }
                }

                // Build success message
                StringBuilder msg = new StringBuilder();
                msg.append("Order placed successfully! ");
                msg.append("Delivery: ").append(deliveryDateTime.toLocalDate()).append(" ");
                msg.append(deliveryDateTime.getHour()).append(":00. ");
                if (gPointsToUse > 0) {
                    msg.append(String.format("%.0f G Points used. ", gPointsToUse));
                }
                msg.append(String.format("Earned %.0f G Points!", pointsEarned));
                if (savedPath != null) {
                    msg.append(" Invoice opened in browser!");
                }

                statusLabel.setText(msg.toString());
                statusLabel.setStyle("-fx-text-fill: green;");

                // Clear cart from database
                try {
                    cartDAO.clearCart(currentUser.getId());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                // Reset local cart
                cartList.clear();
                gPointsToUse = 0;
                appliedCoupon = null;
                couponDiscount = 0;
                if (gPointsField != null)
                    gPointsField.clear();
                if (couponCodeField != null)
                    couponCodeField.clear();
                if (couponStatusLabel != null)
                    couponStatusLabel.setText("");
                updateCartTotal();
                updateGPointsDisplay();
                loadOrders();
                loadProducts();
            } else {
                statusLabel.setText("Order failed.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Database error!");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void updateCartTotal() {
        updateCartTotalWithDiscount();
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
    public void handleResetFilter() {
        filterTypeCombo.setValue("All");
        sortCombo.setValue("Default");
        applyFilterAndSort();
        statusLabel.setText("Filters reset.");
    }

    private void applyFilterAndSort() {
        if (allProducts == null)
            return;

        // Filter
        String filterType = filterTypeCombo.getValue();
        java.util.List<Product> filtered = new java.util.ArrayList<>();

        // Get favorites list if needed
        java.util.List<Integer> favoriteIds = new java.util.ArrayList<>();
        if ("Favorites".equals(filterType)) {
            try {
                favoriteIds = favoritesDAO.getFavoriteProductIds(currentUser.getId());
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }

        for (Product p : allProducts) {
            if ("All".equals(filterType)) {
                filtered.add(p);
            } else if ("Favorites".equals(filterType)) {
                if (favoriteIds.contains(p.getId())) {
                    filtered.add(p);
                }
            } else if (p.getType().equals(filterType)) {
                filtered.add(p);
            }
        }

        // Sort
        String sortOption = sortCombo.getValue();
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
                default:
                    // Default order
                    break;
            }
        }

        productList = FXCollections.observableArrayList(filtered);
        shopTable.setItems(productList);
    }

    /**
     * Set local status label with message and color
     */
    private void setLocalStatus(Label label, String message, String color) {
        if (label != null) {
            label.setText(message);
            label.setStyle("-fx-text-fill: " + color + ";");
        }
    }

    /**
     * Update quantity label and promptText based on product unit type
     */
    private void updateQuantityLabelForProduct(Product product) {
        if (quantityLabel == null || quantityField == null)
            return;

        if (product == null) {
            quantityLabel.setText("Quantity:");
            quantityField.setPromptText("e.g. 1, 2.5");
            return;
        }

        if (product.isSoldByPiece()) {
            quantityLabel.setText("Pieces:");
            quantityField.setPromptText("e.g. 1, 2, 3");
        } else {
            quantityLabel.setText("Kilograms:");
            quantityField.setPromptText("e.g. 0.5, 1, 2.3");
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

    @FXML
    public void handleSearch() {
        String searchText = searchField.getText().toLowerCase().trim();
        applyFilterAndSort();

        if (!searchText.isEmpty()) {
            java.util.List<Product> filtered = productList.stream()
                    .filter(p -> p.getName().toLowerCase().contains(searchText))
                    .collect(java.util.stream.Collectors.toList());
            productList = FXCollections.observableArrayList(filtered);
            shopTable.setItems(productList);
        }
    }

    @FXML
    public void handleAddToFavorites() {
        Product selected = shopTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a product first.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            if (favoritesDAO.isFavorite(currentUser.getId(), selected.getId())) {
                // Already favorite, remove it
                favoritesDAO.removeFavorite(currentUser.getId(), selected.getId());
                statusLabel.setText(selected.getName() + " removed from favorites.");
                statusLabel.setStyle("-fx-text-fill: orange;");
            } else {
                // Add to favorites
                favoritesDAO.addFavorite(currentUser.getId(), selected.getId());
                statusLabel.setText(selected.getName() + " added to favorites!");
                statusLabel.setStyle("-fx-text-fill: green;");
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Database error.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
        updateFavButton();
    }

    // Update favorite button text based on selection
    private void updateFavButton() {
        Product selected = shopTable.getSelectionModel().getSelectedItem();
        if (favButton == null)
            return;

        if (selected == null) {
            // No product selected - hide button
            favButton.setVisible(false);
            favButton.setManaged(false);
            return;
        }

        // Product selected - show button
        favButton.setVisible(true);
        favButton.setManaged(true);

        try {
            if (favoritesDAO.isFavorite(currentUser.getId(), selected.getId())) {
                favButton.setText("Remove Favorite");
                favButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            } else {
                favButton.setText("Add Favorite");
                favButton.setStyle("-fx-background-color: #FFC107;");
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    // Favorites Tab Methods
    @FXML
    public void handleRefreshFavorites() {
        loadFavorites();
    }

    private void loadFavorites() {
        try {
            // Setup columns
            colFavName.setCellValueFactory(new PropertyValueFactory<>("name"));
            colFavType.setCellValueFactory(new PropertyValueFactory<>("type"));
            colFavPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
            colFavStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

            java.util.List<Integer> favIds = favoritesDAO.getFavoriteProductIds(currentUser.getId());
            java.util.List<Product> allProducts = productDAO.getAllProducts();
            java.util.List<Product> favorites = allProducts.stream()
                    .filter(p -> favIds.contains(p.getId()))
                    .collect(java.util.stream.Collectors.toList());

            favoritesTable.setItems(FXCollections.observableArrayList(favorites));
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRemoveFavorite() {
        Product selected = favoritesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a favorite to remove.");
            return;
        }

        try {
            favoritesDAO.removeFavorite(currentUser.getId(), selected.getId());
            statusLabel.setText(selected.getName() + " removed from favorites.");
            statusLabel.setStyle("-fx-text-fill: orange;");
            loadFavorites();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddFavToCart() {
        Product selected = favoritesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a favorite product.");
            return;
        }

        try {
            double qty = Double.parseDouble(favQuantityField.getText());
            if (qty <= 0 || qty > selected.getStock()) {
                statusLabel.setText("Invalid quantity.");
                return;
            }

            CartItem existing = cartList.stream()
                    .filter(item -> item.getProduct().getId() == selected.getId())
                    .findFirst().orElse(null);

            if (existing != null) {
                existing.setQuantity(existing.getQuantity() + qty);
            } else {
                cartList.add(new CartItem(selected, qty));
            }

            cartTable.refresh();
            updateCartTotal();
            statusLabel.setText(selected.getName() + " added to cart!");
            statusLabel.setStyle("-fx-text-fill: green;");
        } catch (NumberFormatException e) {
            statusLabel.setText("Enter a valid quantity.");
        }
    }

    // ==================== G POINTS METHODS ====================

    /**
     * Update G Points display in the UI
     */
    private void updateGPointsDisplay() {
        if (gPointsLabel != null && currentUser != null) {
            try {
                double points = userDAO.getGPoints(currentUser.getId());
                currentUser.setGPoints(points);
                gPointsLabel.setText("G Points: " + String.format("%.0f", points));
                gPointsLabel.setStyle(
                        "-fx-font-weight: bold; -fx-text-fill: #FF4500; -fx-font-size: 18px; -fx-background-color: linear-gradient(to right, rgba(255,215,0,0.3), rgba(255,69,0,0.3)); -fx-padding: 8 15; -fx-background-radius: 20;");
            } catch (java.sql.SQLException e) {
                gPointsLabel.setText("G Points: 0");
            }
        }
    }

    /**
     * Apply G Points discount when user types in G Points field
     */
    @FXML
    public void handleApplyGPoints() {
        if (gPointsField == null)
            return;

        String pointsStr = gPointsField.getText().trim();
        if (pointsStr.isEmpty()) {
            gPointsToUse = 0;
            updateCartTotalWithDiscount();
            return;
        }

        try {
            double requestedPoints = Double.parseDouble(pointsStr);
            double availablePoints = currentUser.getGPoints();
            double cartTotal = getCartTotal();

            // Can't use more points than available or more than cart total
            if (requestedPoints > availablePoints) {
                statusLabel.setText("Yetersiz G Point! Mevcut: " + String.format("%.0f", availablePoints));
                statusLabel.setStyle("-fx-text-fill: red;");
                gPointsToUse = 0;
            } else if (requestedPoints > cartTotal) {
                statusLabel.setText("Sepet tutarından fazla puan kullanamazsınız!");
                statusLabel.setStyle("-fx-text-fill: red;");
                gPointsToUse = cartTotal;
                gPointsField.setText(String.format("%.0f", cartTotal));
            } else if (requestedPoints < 0) {
                statusLabel.setText("Geçersiz miktar!");
                statusLabel.setStyle("-fx-text-fill: red;");
                gPointsToUse = 0;
            } else {
                gPointsToUse = requestedPoints;
                statusLabel.setText(String.format("%.0f G Point uygulandı!", requestedPoints));
                statusLabel.setStyle("-fx-text-fill: green;");
            }
            updateCartTotalWithDiscount();
        } catch (NumberFormatException e) {
            statusLabel.setText("Geçersiz miktar!");
            statusLabel.setStyle("-fx-text-fill: red;");
            gPointsToUse = 0;
            updateCartTotalWithDiscount();
        }
    }

    private double getCartTotal() {
        double total = 0;
        for (CartItem item : cartList) {
            total += item.getTotal();
        }
        return total;
    }

    private void updateCartTotalWithDiscount() {
        double subtotal = getCartTotal();
        double couponDiscountAmount = 0;

        // Calculate coupon discount if applied
        if (appliedCoupon != null) {
            couponDiscountAmount = subtotal * (appliedCoupon.getDiscountPercent() / 100.0);
            couponDiscount = couponDiscountAmount;
        }

        double afterDiscounts = subtotal - gPointsToUse - couponDiscountAmount;
        if (afterDiscounts < 0)
            afterDiscounts = 0;

        // Add 20% VAT
        double vatRate = 0.20;
        double vatAmount = afterDiscounts * vatRate;
        double finalTotal = afterDiscounts + vatAmount;

        if (discountLabel != null) {
            StringBuilder discText = new StringBuilder();
            discText.append("Subtotal: ₺").append(String.format("%.2f", subtotal));
            if (gPointsToUse > 0) {
                discText.append(" | G Point: -₺").append(String.format("%.2f", gPointsToUse));
            }
            if (couponDiscountAmount > 0) {
                discText.append(" | Coupon: -₺").append(String.format("%.2f", couponDiscountAmount));
            }
            discText.append(" | VAT: +₺").append(String.format("%.2f", vatAmount));

            discountLabel.setText(discText.toString());
            discountLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");
        }

        if (finalTotalLabel != null) {
            finalTotalLabel.setText("Total: ₺" + String.format("%.2f", finalTotal));
            finalTotalLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #FFD700;");
        }
    }

    // ==================== RECOMMENDATION METHODS ====================

    /**
     * Load product recommendations when a product is selected
     */
    private void loadRecommendations(Product product) {
        if (recommendationList == null || product == null)
            return;

        try {
            java.util.List<Product> recommendations = recommendationDAO.getAlsoBoughtProducts(product.getId());
            ObservableList<String> items = FXCollections.observableArrayList();

            if (recommendations.isEmpty()) {
                items.add("Henüz öneri yok");
            } else {
                for (Product p : recommendations) {
                    items.add("• " + p.getName() + " (₺" + String.format("%.2f", p.getPrice()) + ")");
                }
            }

            recommendationList.setItems(items);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle product selection to show recommendations
     */
    @FXML
    public void handleProductSelection() {
        Product selected = shopTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            loadRecommendations(selected);
            updateFavButton();
        }
    }

    // ==================== ORDER STATUS DETAILS ====================

    /**
     * Show order details popup with complete order information
     */
    @FXML
    public void handleViewOrderDetails() {
        Order selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Please select an order.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        StringBuilder details = new StringBuilder();

        // Order header
        details.append("ORDER #").append(selected.getId()).append("\n");
        details.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        // Order date and status
        details.append("Date: ").append(selected.getOrderDate()).append("\n");
        details.append("Status: ").append(selected.getStatus()).append("\n\n");

        // Status timeline - 3 steps: Order Placed, Shipping, Delivered
        String status = selected.getStatus().toLowerCase();

        // Determine icons based on status
        String orderPlacedIcon = "[OK]"; // Always completed once order exists
        String shippingIcon;
        String deliveredIcon;

        if (status.equals("pending")) {
            shippingIcon = "[ ]";
            deliveredIcon = "[ ]";
        } else if (status.equals("delivering") || status.equals("shipped") || status.equals("shipping")) {
            shippingIcon = "[>>]"; // Currently in progress
            deliveredIcon = "[ ]";
        } else if (status.equals("delivered")) {
            shippingIcon = "[OK]";
            deliveredIcon = "[OK]";
        } else {
            shippingIcon = "[ ]";
            deliveredIcon = "[ ]";
        }

        details.append("STATUS TIMELINE:\n");
        details.append("   ").append(orderPlacedIcon).append(" Order Placed\n");
        details.append("   ").append(shippingIcon).append(" Shipping\n");
        details.append("   ").append(deliveredIcon).append(" Delivered\n\n");

        details.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        details.append("PRODUCTS:\n\n");

        // Get order items
        try {
            java.sql.Connection conn = com.greengrocer.dao.DatabaseAdapter.getConnection();
            String sql = "SELECT p.name, oi.quantity, oi.price_at_purchase FROM OrderItems oi " +
                    "JOIN ProductInfo p ON oi.product_id = p.id WHERE oi.order_id = ?";
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, selected.getId());
            java.sql.ResultSet rs = stmt.executeQuery();

            double subtotal = 0;
            while (rs.next()) {
                String productName = rs.getString("name");
                double quantity = rs.getDouble("quantity");
                double unitPrice = rs.getDouble("price_at_purchase");
                double lineTotal = quantity * unitPrice;
                subtotal += lineTotal;
                details.append("   • ").append(productName)
                        .append(" x").append(String.format("%.0f", quantity))
                        .append(" @ ₺").append(String.format("%.2f", unitPrice))
                        .append(" = ₺").append(String.format("%.2f", lineTotal)).append("\n");
            }
            rs.close();
            stmt.close();

            details.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            details.append("PAYMENT DETAILS:\n\n");
            details.append("   Subtotal: ₺").append(String.format("%.2f", subtotal)).append("\n");

            // Get coupon info
            String couponSql = "SELECT c.code, cu.discount_amount FROM CouponUsage cu " +
                    "JOIN Coupons c ON cu.coupon_id = c.id WHERE cu.order_id = ?";
            java.sql.PreparedStatement couponStmt = conn.prepareStatement(couponSql);
            couponStmt.setInt(1, selected.getId());
            java.sql.ResultSet couponRs = couponStmt.executeQuery();

            if (couponRs.next()) {
                String couponCode = couponRs.getString("code");
                double discountAmount = couponRs.getDouble("discount_amount");
                details.append("   Coupon Used: ").append(couponCode).append("\n");
                details.append("   Coupon Discount: -TL").append(String.format("%.2f", discountAmount)).append("\n");
            }
            couponRs.close();
            couponStmt.close();

            // Calculate G Points (estimated - 5% of order total)
            double totalAmount = selected.getTotalAmount();
            double gPointsEarned = totalAmount * 0.05;
            details.append("   G Points Earned: +").append(String.format("%.0f", gPointsEarned)).append(" points\n");

            details.append("\n   ━━━━━━━━━━━━━━━━━━━━━━━━\n");
            details.append("   TOTAL PAID: TL").append(String.format("%.2f", totalAmount)).append("\n");

            conn.close();
        } catch (java.sql.SQLException e) {
            details.append("   Unable to load order items.\n");
            e.printStackTrace();
        }

        // Show styled dialog with download option
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Order Details");
        alert.setHeaderText("Order #" + selected.getId() + " - " + selected.getStatus().toUpperCase());

        javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(details.toString());
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px;");
        textArea.setPrefWidth(500);
        textArea.setPrefHeight(400);

        javafx.scene.control.ButtonType downloadBtn = new javafx.scene.control.ButtonType("Download Invoice",
                javafx.scene.control.ButtonBar.ButtonData.LEFT);
        javafx.scene.control.ButtonType closeBtn = new javafx.scene.control.ButtonType("Close",
                javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(downloadBtn, closeBtn);
        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setMinWidth(550);

        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == downloadBtn) {
            // Try to get PDF invoice from database
            try {
                byte[] pdfData = orderDAO.getInvoice(selected.getId());

                if (pdfData != null && pdfData.length > 0) {
                    // Save PDF to Downloads folder
                    String fileName = "invoice_order_" + selected.getId() + ".pdf";
                    String filePath = System.getProperty("user.home") + "/Downloads/" + fileName;

                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(filePath)) {
                        fos.write(pdfData);
                        statusLabel.setText("PDF Invoice saved to Downloads folder!");
                        statusLabel.setStyle("-fx-text-fill: green;");

                        // Open PDF with default application
                        java.awt.Desktop.getDesktop().open(new java.io.File(filePath));
                    }
                } else {
                    // No PDF in database - inform user
                    statusLabel.setText("Invoice not available for this order.");
                    statusLabel.setStyle("-fx-text-fill: orange;");
                }
            } catch (SQLException | java.io.IOException ex) {
                statusLabel.setText("Failed to download invoice: " + ex.getMessage());
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }

    // ==================== COUPON APPLICATION ====================

    /**
     * Apply coupon code entered by user
     */
    @FXML
    public void handleApplyCouponCode() {
        if (couponCodeField == null)
            return;

        String code = couponCodeField.getText().trim().toUpperCase();
        if (code.isEmpty()) {
            if (couponStatusLabel != null) {
                couponStatusLabel.setText("");
            }
            appliedCoupon = null;
            couponDiscount = 0;
            updateCartTotalWithDiscount();
            return;
        }

        try {
            com.greengrocer.models.Coupon coupon = couponDAO.getCouponByCode(code);

            if (coupon == null) {
                couponStatusLabel.setText("Coupon not found!");
                couponStatusLabel.setStyle("-fx-text-fill: red;");
                appliedCoupon = null;
                couponDiscount = 0;
            } else if (!coupon.canBeUsed()) {
                couponStatusLabel.setText("Coupon invalid or expired!");
                couponStatusLabel.setStyle("-fx-text-fill: red;");
                appliedCoupon = null;
                couponDiscount = 0;
            } else if (couponDAO.hasUserUsedCoupon(currentUser.getId(), coupon.getId())) {
                couponStatusLabel.setText("You already used this coupon!");
                couponStatusLabel.setStyle("-fx-text-fill: red;");
                appliedCoupon = null;
                couponDiscount = 0;
            } else {
                // Valid coupon
                appliedCoupon = coupon;
                double cartTotal = getCartTotal();
                couponDiscount = cartTotal * (coupon.getDiscountPercent() / 100.0);

                couponStatusLabel.setText(String.format("%.0f", coupon.getDiscountPercent()) +
                        "% off! (-$" + String.format("%.2f", couponDiscount) + ")");
                couponStatusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            }

            updateCartTotalWithDiscount();
        } catch (java.sql.SQLException e) {
            couponStatusLabel.setText("Error: " + e.getMessage());
            couponStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    // ==================== PROFILE MANAGEMENT ====================

    /**
     * Load profile data into fields
     */
    private void loadProfileData() {
        if (currentUser == null)
            return;

        if (profileUsernameLabel != null) {
            profileUsernameLabel.setText(currentUser.getUsername());
        }
        if (profileFirstNameField != null) {
            profileFirstNameField.setText(currentUser.getFirstName() != null ? currentUser.getFirstName() : "");
        }
        if (profileLastNameField != null) {
            profileLastNameField.setText(currentUser.getLastName() != null ? currentUser.getLastName() : "");
        }
        if (profileAddressField != null) {
            profileAddressField.setText(currentUser.getAddress() != null ? currentUser.getAddress() : "");
        }
        if (profilePhoneField != null) {
            profilePhoneField.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");
        }
    }

    /**
     * Save profile changes
     */
    @FXML
    public void handleSaveProfile() {
        if (currentUser == null)
            return;

        String firstName = profileFirstNameField.getText().trim();
        String lastName = profileLastNameField.getText().trim();
        String address = profileAddressField.getText().trim();
        String phone = profilePhoneField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            profileStatusLabel.setText("First and last name are required.");
            profileStatusLabel.setStyle("-fx-text-fill: #f44336;");
            return;
        }

        try {
            boolean success = userDAO.updateUserProfile(currentUser.getId(), firstName, lastName, address, phone);
            if (success) {
                // Update local user object
                currentUser.setFirstName(firstName);
                currentUser.setLastName(lastName);
                currentUser.setAddress(address);
                currentUser.setPhone(phone);

                profileStatusLabel.setText("Profile saved successfully!");
                profileStatusLabel.setStyle("-fx-text-fill: #4CAF50;");

                // Update welcome label
                welcomeLabel.setText("Welcome, " + firstName);
            } else {
                profileStatusLabel.setText("Failed to save profile.");
                profileStatusLabel.setStyle("-fx-text-fill: #f44336;");
            }
        } catch (java.sql.SQLException e) {
            profileStatusLabel.setText("Error: " + e.getMessage());
            profileStatusLabel.setStyle("-fx-text-fill: #f44336;");
        }
    }

    /**
     * Handle password change
     */
    @FXML
    public void handleChangePassword() {
        if (currentUser == null)
            return;

        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmNewPasswordField.getText();

        // Validation
        if (currentPassword.isEmpty()) {
            passwordStatusLabel.setText("Current password is required.");
            passwordStatusLabel.setStyle("-fx-text-fill: #f44336;");
            return;
        }

        if (newPassword.isEmpty()) {
            passwordStatusLabel.setText("New password is required.");
            passwordStatusLabel.setStyle("-fx-text-fill: #f44336;");
            return;
        }

        // Strong password validation
        String passwordError = validatePasswordStrength(newPassword);
        if (passwordError != null) {
            passwordStatusLabel.setText(passwordError);
            passwordStatusLabel.setStyle("-fx-text-fill: #f44336;");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            passwordStatusLabel.setText("New passwords do not match.");
            passwordStatusLabel.setStyle("-fx-text-fill: #f44336;");
            return;
        }

        try {
            boolean success = userDAO.changePassword(currentUser.getId(), currentPassword, newPassword);
            if (success) {
                passwordStatusLabel.setText("Password changed successfully!");
                passwordStatusLabel.setStyle("-fx-text-fill: #4CAF50;");
                // Clear fields
                currentPasswordField.clear();
                newPasswordField.clear();
                confirmNewPasswordField.clear();
            } else {
                passwordStatusLabel.setText("Current password is incorrect.");
                passwordStatusLabel.setStyle("-fx-text-fill: #f44336;");
            }
        } catch (java.sql.SQLException e) {
            passwordStatusLabel.setText("Error: " + e.getMessage());
            passwordStatusLabel.setStyle("-fx-text-fill: #f44336;");
        }
    }

    /**
     * Validate password strength
     */
    private String validatePasswordStrength(String password) {
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

        return null;
    }
}
