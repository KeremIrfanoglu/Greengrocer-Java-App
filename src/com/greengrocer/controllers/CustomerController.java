package com.greengrocer.controllers;

import com.greengrocer.dao.OrderDAO;
import com.greengrocer.dao.ProductDAO;
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

    private com.greengrocer.util.NotificationService notificationService;

    public CustomerController() {
        this.productDAO = new ProductDAO();
        this.orderDAO = new OrderDAO();
        this.userDAO = new com.greengrocer.dao.UserDAO();
        this.recommendationDAO = new com.greengrocer.dao.RecommendationDAO();
        this.favoritesDAO = new com.greengrocer.dao.FavoritesDAO();
        this.couponDAO = new com.greengrocer.dao.CouponDAO();
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
                            setText("⭐");
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
                "Meat", "Beverages", "Snacks", "⭐ Favorites"));
        filterTypeCombo.setValue("All");
        sortCombo.setItems(FXCollections.observableArrayList("Default", "Name (A-Z)", "Name (Z-A)", "Price (Low-High)",
                "Price (High-Low)"));
        sortCombo.setValue("Default");

        loadProducts();

        // Update favorite button when selection changes
        shopTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateFavButton();
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

    @FXML
    public void handleRefreshOrders() {
        loadOrders();
    }

    @FXML
    public void handleAddToCart() {
        Product selected = shopTable.getSelectionModel().getSelectedItem();
        String qtyStr = quantityField.getText();

        if (selected == null || qtyStr.isEmpty()) {
            statusLabel.setText("Select product and enter quantity.");
            return;
        }

        try {
            double qty = Double.parseDouble(qtyStr);
            if (qty <= 0) {
                statusLabel.setText("Quantity must be positive.");
                return;
            }
            if (qty > selected.getStock()) {
                statusLabel.setText("Not enough stock.");
                return;
            }

            // Check if already in cart
            boolean exists = false;
            for (CartItem item : cartList) {
                if (item.getProduct().getId() == selected.getId()) {
                    if (item.getQuantity() + qty > selected.getStock()) {
                        statusLabel.setText("Total quantity exceeds stock.");
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
                statusLabel.setText("Added to cart with 10% DISCOUNT! (Low stock)");
                statusLabel.setStyle("-fx-text-fill: green;");
            } else {
                statusLabel.setText("Added to cart.");
                statusLabel.setStyle("-fx-text-fill: blue;");
            }
            quantityField.clear();

        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid quantity.");
        }
    }

    @FXML
    public void handleRemoveFromCart() {
        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            cartList.remove(selected);
            updateCartTotal();
            statusLabel.setText("Removed from cart.");
        }
    }

    @FXML
    public void handleCheckout() {
        if (cartList.isEmpty()) {
            statusLabel.setText("Cart is empty.");
            return;
        }

        double subtotal = getCartTotal();
        double finalTotal = subtotal - gPointsToUse;
        if (finalTotal < 0)
            finalTotal = 0;

        try {
            // Use G Points if any
            if (gPointsToUse > 0) {
                if (!userDAO.useGPoints(currentUser.getId(), gPointsToUse)) {
                    statusLabel.setText("G Points kullanılamadı!");
                    statusLabel.setStyle("-fx-text-fill: red;");
                    return;
                }
            }

            // Create order with the subtotal (full amount for records)
            if (orderDAO.createOrder(currentUser.getId(), cartList, subtotal)) {
                // Award G Points based on actual payment (1/5 of finalTotal)
                double pointsEarned = finalTotal / 5.0;
                userDAO.addGPoints(currentUser.getId(), finalTotal);

                // Build success message
                StringBuilder msg = new StringBuilder();
                msg.append("✅ Sipariş başarılı! ");
                if (gPointsToUse > 0) {
                    msg.append(String.format("%.0f G Point kullanıldı. ", gPointsToUse));
                }
                msg.append(String.format("%.0f G Point kazandınız!", pointsEarned));

                statusLabel.setText(msg.toString());
                statusLabel.setStyle("-fx-text-fill: green;");

                // Reset
                cartList.clear();
                gPointsToUse = 0;
                if (gPointsField != null)
                    gPointsField.clear();
                updateCartTotal();
                updateGPointsDisplay();
                loadOrders();
                loadProducts();
            } else {
                statusLabel.setText("Sipariş başarısız.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Veritabanı hatası!");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void updateCartTotal() {
        double total = 0;
        for (CartItem item : cartList) {
            total += item.getTotal();
        }
        cartTotalLabel.setText("Total: $" + String.format("%.2f", total));
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
        if ("⭐ Favorites".equals(filterType)) {
            try {
                favoriteIds = favoritesDAO.getFavoriteProductIds(currentUser.getId());
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }

        for (Product p : allProducts) {
            if ("All".equals(filterType)) {
                filtered.add(p);
            } else if ("⭐ Favorites".equals(filterType)) {
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
                statusLabel.setText(selected.getName() + " added to favorites! ⭐");
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
                favButton.setText("❌ Remove Favorite");
                favButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            } else {
                favButton.setText("⭐ Add Favorite");
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
                gPointsLabel.setText("🔥 G Points: " + String.format("%.0f", points));
                gPointsLabel.setStyle(
                        "-fx-font-weight: bold; -fx-text-fill: #FF4500; -fx-font-size: 18px; -fx-background-color: linear-gradient(to right, rgba(255,215,0,0.3), rgba(255,69,0,0.3)); -fx-padding: 8 15; -fx-background-radius: 20;");
            } catch (java.sql.SQLException e) {
                gPointsLabel.setText("🔥 G Points: 0");
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
        double total = getCartTotal();
        double couponDiscountAmount = 0;

        // Calculate coupon discount if applied
        if (appliedCoupon != null) {
            couponDiscountAmount = total * (appliedCoupon.getDiscountPercent() / 100.0);
            couponDiscount = couponDiscountAmount;
        }

        double finalTotal = total - gPointsToUse - couponDiscountAmount;
        if (finalTotal < 0)
            finalTotal = 0;

        cartTotalLabel.setText("Subtotal: ₺" + String.format("%.2f", total));

        if (discountLabel != null) {
            StringBuilder discText = new StringBuilder();
            if (gPointsToUse > 0) {
                discText.append("G Point: -₺").append(String.format("%.2f", gPointsToUse));
            }
            if (couponDiscountAmount > 0) {
                if (discText.length() > 0)
                    discText.append(" | ");
                discText.append("Coupon: -$").append(String.format("%.2f", couponDiscountAmount));
            }
            discountLabel.setText(discText.toString());
            discountLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        }

        if (finalTotalLabel != null) {
            finalTotalLabel.setText("Final: ₺" + String.format("%.2f", finalTotal));
            finalTotalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");
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
        details.append("🛒 ORDER #").append(selected.getId()).append("\n");
        details.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        // Order date and status
        details.append("📅 Date: ").append(selected.getOrderDate()).append("\n");
        details.append("📊 Status: ").append(selected.getStatus()).append("\n\n");

        // Status timeline
        String status = selected.getStatus();
        String pendingIcon = !status.equals("pending") ? "✅" : "🔵";
        String processingIcon = status.equals("processing") ? "🔵"
                : (status.equals("shipped") || status.equals("delivered") ? "✅" : "⚪");
        String shippedIcon = status.equals("shipped") ? "🔵" : (status.equals("delivered") ? "✅" : "⚪");
        String deliveredIcon = status.equals("delivered") ? "✅" : "⚪";

        details.append("📋 STATUS TIMELINE:\n");
        details.append("   ").append(pendingIcon).append(" Order Placed\n");
        details.append("   ").append(processingIcon).append(" Processing\n");
        details.append("   ").append(shippedIcon).append(" Shipped\n");
        details.append("   ").append(deliveredIcon).append(" Delivered\n\n");

        details.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        details.append("📦 PRODUCTS:\n\n");

        // Get order items
        try {
            java.sql.Connection conn = com.greengrocer.dao.DatabaseAdapter.getConnection();
            String sql = "SELECT p.name, oi.quantity, oi.unit_price FROM OrderItems oi " +
                    "JOIN ProductInfo p ON oi.product_id = p.id WHERE oi.order_id = ?";
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, selected.getId());
            java.sql.ResultSet rs = stmt.executeQuery();

            double subtotal = 0;
            while (rs.next()) {
                String productName = rs.getString("name");
                double quantity = rs.getDouble("quantity");
                double unitPrice = rs.getDouble("unit_price");
                double lineTotal = quantity * unitPrice;
                subtotal += lineTotal;
                details.append("   • ").append(productName)
                        .append(" x").append(String.format("%.0f", quantity))
                        .append(" @ $").append(String.format("%.2f", unitPrice))
                        .append(" = $").append(String.format("%.2f", lineTotal)).append("\n");
            }
            rs.close();
            stmt.close();

            details.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            details.append("💵 PAYMENT DETAILS:\n\n");
            details.append("   Subtotal: $").append(String.format("%.2f", subtotal)).append("\n");

            // Get coupon info
            String couponSql = "SELECT c.code, cu.discount_amount FROM CouponUsage cu " +
                    "JOIN Coupons c ON cu.coupon_id = c.id WHERE cu.order_id = ?";
            java.sql.PreparedStatement couponStmt = conn.prepareStatement(couponSql);
            couponStmt.setInt(1, selected.getId());
            java.sql.ResultSet couponRs = couponStmt.executeQuery();

            if (couponRs.next()) {
                String couponCode = couponRs.getString("code");
                double discountAmount = couponRs.getDouble("discount_amount");
                details.append("   🎟️ Coupon Used: ").append(couponCode).append("\n");
                details.append("   🎟️ Coupon Discount: -$").append(String.format("%.2f", discountAmount)).append("\n");
            }
            couponRs.close();
            couponStmt.close();

            // Calculate G Points (estimated - 5% of order total)
            double totalAmount = selected.getTotalAmount();
            double gPointsEarned = totalAmount * 0.05;
            details.append("   🔥 G Points Earned: +").append(String.format("%.0f", gPointsEarned)).append(" points\n");

            details.append("\n   ━━━━━━━━━━━━━━━━━━━━━━━━\n");
            details.append("   💰 TOTAL PAID: $").append(String.format("%.2f", totalAmount)).append("\n");

            conn.close();
        } catch (java.sql.SQLException e) {
            details.append("   Unable to load order items.\n");
            e.printStackTrace();
        }

        // Show styled dialog
        com.greengrocer.util.StyledAlert.showDetailed(
                "Order Details",
                "Order #" + selected.getId() + " - " + selected.getStatus().toUpperCase(),
                details.toString());
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
                couponStatusLabel.setText("❌ Coupon not found!");
                couponStatusLabel.setStyle("-fx-text-fill: red;");
                appliedCoupon = null;
                couponDiscount = 0;
            } else if (!coupon.canBeUsed()) {
                couponStatusLabel.setText("❌ Coupon invalid or expired!");
                couponStatusLabel.setStyle("-fx-text-fill: red;");
                appliedCoupon = null;
                couponDiscount = 0;
            } else if (couponDAO.hasUserUsedCoupon(currentUser.getId(), coupon.getId())) {
                couponStatusLabel.setText("❌ You already used this coupon!");
                couponStatusLabel.setStyle("-fx-text-fill: red;");
                appliedCoupon = null;
                couponDiscount = 0;
            } else {
                // Valid coupon
                appliedCoupon = coupon;
                double cartTotal = getCartTotal();
                couponDiscount = cartTotal * (coupon.getDiscountPercent() / 100.0);

                couponStatusLabel.setText("✅ " + String.format("%.0f", coupon.getDiscountPercent()) +
                        "% off! (-$" + String.format("%.2f", couponDiscount) + ")");
                couponStatusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            }

            updateCartTotalWithDiscount();
        } catch (java.sql.SQLException e) {
            couponStatusLabel.setText("Error: " + e.getMessage());
            couponStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }
}
