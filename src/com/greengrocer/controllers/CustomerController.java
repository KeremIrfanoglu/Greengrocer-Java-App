package com.greengrocer.controllers;

import com.greengrocer.dao.OrderDAO;
import com.greengrocer.dao.ProductDAO;
import com.greengrocer.dao.CartDAO;
import com.greengrocer.models.CartItem;
import com.greengrocer.models.Order;
import com.greengrocer.models.Product;
import com.greengrocer.models.User;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import com.greengrocer.util.FormatHelper;
import com.greengrocer.util.StyledAlert;
import com.greengrocer.util.BackgroundMusicService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

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

    // Rating cache - loaded once, used for all cells
    private java.util.Map<Integer, com.greengrocer.models.CarrierRating> ratingCache = new java.util.HashMap<>();

    // Favorites cache - loaded once, used for all product cards (performance
    // optimization)
    private java.util.Set<Integer> favoriteProductIds = new java.util.HashSet<>();

    @FXML
    private javafx.scene.control.TabPane mainTabPane;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label gPointsLabel; // G Points balance display

    // Shop Tab - Grid View
    @FXML
    private FlowPane shopFlowPane;

    private Product selectedProduct = null; // For grid view selection

    // Product card cache - for efficient single-card updates instead of full grid
    // refresh
    // Product card cache - supports multiple cards per product (Shop + Favorites)
    private java.util.Map<Integer, java.util.List<VBox>> productCardMap = new java.util.HashMap<>();

    @FXML
    private javafx.scene.control.ComboBox<String> filterTypeCombo;
    @FXML
    private javafx.scene.control.ComboBox<String> sortCombo;
    @FXML
    private TextField searchField;

    // Global quantity controls removed

    // Global status label for cart moved to main statusLabel

    @FXML
    private Label updateCartStatusLabel;

    private ObservableList<Product> allProducts; // Keep original list for filtering
    private com.greengrocer.dao.FavoritesDAO favoritesDAO;

    // Cart Tab

    // Orders Tab
    @FXML
    private TableView<Order> orderTable;
    @FXML
    private TableColumn<Order, Integer> colOrderId;
    @FXML
    private TableColumn<Order, java.sql.Timestamp> colOrderDate;
    @FXML
    private TableColumn<Order, Double> colOrderTotal;
    @FXML
    private TableColumn<Order, String> colOrderStatus;
    @FXML
    private TableColumn<Order, String> colOrderRating;
    @FXML
    private TableColumn<Order, String> colOrderComment;

    // Favorites Tab - Grid View
    @FXML
    private FlowPane favoritesFlowPane;
    @FXML
    private Button favButton;
    @FXML
    private Button favRemoveButton; // New button for Favorites tab

    // Favorites Filter Controls
    @FXML
    private TextField favSearchField;
    @FXML
    private javafx.scene.control.ComboBox<String> favFilterTypeCombo;
    @FXML
    private javafx.scene.control.ComboBox<String> favSortCombo;

    private ObservableList<Product> allFavorites; // Master list of favorites
    private ObservableList<Product> displayedFavorites; // Filtered list

    // Cart Redesign Controls
    @FXML
    private FlowPane cartFlowPane;
    @FXML
    private VBox cartSummaryList;
    @FXML
    private Label subtotalLabel;
    @FXML
    private Label taxLabel;

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

    // Scheduled Delivery fields
    @FXML
    private javafx.scene.control.DatePicker deliveryDatePicker;
    @FXML
    private javafx.scene.control.ComboBox<String> deliveryTimeCombo;

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
    @FXML
    private Button musicToggleButton;

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

    /**
     * Update available time slots based on selected date.
     * If today is selected, exclude past hours.
     */
    private void updateAvailableTimeSlots() {
        if (deliveryTimeCombo == null)
            return;

        java.time.LocalDate selectedDate = deliveryDatePicker != null ? deliveryDatePicker.getValue() : null;
        deliveryTimeCombo.setItems(FXCollections.observableArrayList(getAvailableTimeSlots(selectedDate)));

        // Set default value
        if (!deliveryTimeCombo.getItems().isEmpty()) {
            deliveryTimeCombo.setValue(deliveryTimeCombo.getItems().get(0));
        } else {
            deliveryTimeCombo.setValue(null);
        }
    }

    @FXML
    public void initialize() {
        if (gPointsField != null) {
            // Force numeric input only (digits)
            gPointsField.setTextFormatter(new TextFormatter<>(change -> {
                String newText = change.getControlNewText();
                if (newText.matches("\\d*")) {
                    return change;
                }
                return null;
            }));
        }

        // Enter-to-send for Messaging
        if (msgComposeArea != null) {
            msgComposeArea.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    String text = msgComposeArea.getText().trim();
                    if (!text.isEmpty()) {
                        handleSendMessage();
                    }
                }
            });
        }
    }

    public void initData(User user) {
        this.currentUser = user;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + user.getFirstName());
        }
        updateGPointsDisplay();

        // Load saved cart from database
        loadCart();

        // Check for price drops and low stock alerts
        javafx.application.Platform.runLater(() -> {
            notificationService.checkAndNotify(user.getId());
        });

        // Initialize delivery date/time picker with validation
        if (deliveryDatePicker != null) {
            deliveryDatePicker.setValue(java.time.LocalDate.now().plusDays(1)); // Default: tomorrow

            // Block past dates
            deliveryDatePicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
                @Override
                public void updateItem(java.time.LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    // Disable dates before today
                    setDisable(empty || date.isBefore(java.time.LocalDate.now()));
                    if (date.isBefore(java.time.LocalDate.now())) {
                        setStyle("-fx-background-color: #555;");
                    }
                }
            });

            // Update time slots when date changes
            deliveryDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
                updateAvailableTimeSlots();
            });
        }
        if (deliveryTimeCombo != null) {
            updateAvailableTimeSlots();
        }

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
                    } else if (tabText.contains("Messages")) {
                        handleRefreshMessages();
                    }
                }
            });
        }

        // Grid View is now used instead of TableView - setup handled in
        // loadProducts/refreshShopGrid

        // Setup Filter/Sort ComboBoxes
        filterTypeCombo.setItems(FXCollections.observableArrayList("All", "Vegetable", "Fruit", "Dairy", "Bakery",
                "Meat", "Beverages", "Snacks", "Favorites"));
        filterTypeCombo.setValue("All");
        sortCombo.setItems(FXCollections.observableArrayList("Default", "Name (A-Z)", "Name (Z-A)", "Price (Low-High)",
                "Price (High-Low)"));
        sortCombo.setValue("Default");

        // Favorites Filter/Sort Setup (Independent)
        favFilterTypeCombo.setItems(FXCollections.observableArrayList("All", "Vegetable", "Fruit", "Dairy", "Bakery",
                "Meat", "Beverages", "Snacks"));
        favFilterTypeCombo.setValue("All");
        favSortCombo
                .setItems(FXCollections.observableArrayList("Default", "Name (A-Z)", "Name (Z-A)", "Price (Low-High)",
                        "Price (High-Low)"));
        favSortCombo.setValue("Default");

        // Click on empty area to deselect product
        if (shopFlowPane != null) {
            shopFlowPane.setOnMouseClicked(e -> {
                // Only clear if clicking directly on FlowPane, not on a card
                if (e.getTarget() == shopFlowPane) {
                    selectedProduct = null;
                    updateFavButton();
                    statusLabel.setText("Selection cleared.");
                    statusLabel.setStyle("-fx-text-fill: #94A3B8;");
                }
            });
        }

        loadProducts();

        // Cart Setup

        // Orders Setup
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colOrderDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colOrderDate.setCellFactory(
                column -> new javafx.scene.control.TableCell<com.greengrocer.models.Order, java.sql.Timestamp>() {

                    @Override
                    protected void updateItem(java.sql.Timestamp item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(com.greengrocer.util.FormatHelper.formatDate(item));
                        }
                    }
                });
        colOrderTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colOrderTotal
                .setCellFactory(column -> new javafx.scene.control.TableCell<com.greengrocer.models.Order, Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(com.greengrocer.util.FormatHelper.formatCurrency(item));
                        }
                    }
                });
        colOrderStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Rating column - uses cached data (no DB query per cell)
        if (colOrderRating != null) {
            colOrderRating.setCellValueFactory(cellData -> {
                com.greengrocer.models.CarrierRating rating = ratingCache.get(cellData.getValue().getId());
                if (rating != null) {
                    return new SimpleStringProperty("⭐".repeat(rating.getRating()));
                }
                return new SimpleStringProperty("-");
            });
        }

        // Comment column - uses cached data (no DB query per cell)
        if (colOrderComment != null) {
            colOrderComment.setCellValueFactory(cellData -> {
                com.greengrocer.models.CarrierRating rating = ratingCache.get(cellData.getValue().getId());
                if (rating != null && rating.getComment() != null) {
                    String comment = rating.getComment();
                    return new SimpleStringProperty(comment.length() > 20 ? comment.substring(0, 20) + "..." : comment);
                }
                return new SimpleStringProperty("-");
            });
        }

        loadOrders();
    }

    private void loadProducts() {
        // Run database operations in background to prevent UI lag
        javafx.concurrent.Task<ProductLoadResult> task = new javafx.concurrent.Task<>() {
            @Override
            protected ProductLoadResult call() throws Exception {
                // heavy lifting in background
                java.util.List<Product> products = productDAO.getAllProducts();
                // Sort
                products.sort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));

                java.util.List<Integer> favs = favoritesDAO.getFavoriteProductIds(currentUser.getId());
                return new ProductLoadResult(products, favs);
            }
        };

        task.setOnSucceeded(e -> {
            // Update UI on JavaFX Application Thread
            ProductLoadResult result = task.getValue();
            allProducts = FXCollections.observableArrayList(result.products);
            favoriteProductIds.clear();
            if (result.favorites != null) {
                favoriteProductIds.addAll(result.favorites);
            }

            applyFilterAndSort();
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            ex.printStackTrace();
            statusLabel.setText("Error loading products: " + ex.getMessage());
        });

        new Thread(task).start();
    }

    private void refreshShopGrid() {
        if (shopFlowPane == null)
            return;

        // Cleanup existing Shop cards from the map
        for (javafx.scene.Node node : shopFlowPane.getChildren()) {
            if (node instanceof VBox) {
                // We don't have the ID easily, so we have to iterate the map.
                // This is O(N*M), but N is small.
                // Alternatively, we can clear the map if we are reloading everything,
                // BUT we might have Favorites cards we want to keep?
                // Actually, simplest safe way:
                // Just let the map grow? No, memory leak.
                // Better: attach UserData to VBox with ID.
            }
        }
        // Ideally we should use UserData for ID.
        // Let's assume createShopCard sets UserData.

        // Better approach for now to avoid complexity:
        // Since we are reloading the list, we can just clear the map entries for the
        // Shop cards?
        // Let's just create new map and re-populate? No, Favorites tab might be active.

        // Let's just implement the removal logic using UserData in createShopCard.
        // For now, I'll clear the children.

        // Remove Shop cards from map
        shopFlowPane.getChildren().forEach(node -> {
            if (node.getUserData() instanceof Integer) {
                int id = (Integer) node.getUserData();
                java.util.List<VBox> cards = productCardMap.get(id);
                if (cards != null) {
                    cards.remove(node);
                    if (cards.isEmpty()) {
                        productCardMap.remove(id);
                    }
                }
            }
        });

        shopFlowPane.getChildren().clear();

        for (Product product : productList) {
            VBox card = createShopCard(product);
            productCardMap.computeIfAbsent(product.getId(), k -> new java.util.ArrayList<>()).add(card);
            shopFlowPane.getChildren().add(card);
        }
    }

    // Favorites Tab Methods
    @FXML
    public void handleRefreshFavorites() {
        // Reload master list from DB
        reloadFavoritesMasterList();
        // specific filter reset could be done here if desired, but preserving state is
        // better
        applyFavFilterAndSort();
    }

    private void reloadFavoritesMasterList() {
        try {
            java.util.List<Integer> favIds = favoritesDAO.getFavoriteProductIds(currentUser.getId());
            java.util.List<Product> favs = allProducts.stream()
                    .filter(p -> favIds.contains(p.getId()))
                    .collect(java.util.stream.Collectors.toList());
            allFavorites = FXCollections.observableArrayList(favs);
        } catch (SQLException e) {
            e.printStackTrace();
            allFavorites = FXCollections.observableArrayList();
        }
    }

    private void refreshFavoritesGrid() {
        if (favoritesFlowPane == null)
            return;

        // Remove existing Favorites cards from map
        favoritesFlowPane.getChildren().forEach(node -> {
            if (node.getUserData() instanceof Integer) {
                int id = (Integer) node.getUserData();
                java.util.List<VBox> cards = productCardMap.get(id);
                if (cards != null) {
                    cards.remove(node);
                    if (cards.isEmpty()) {
                        productCardMap.remove(id);
                    }
                }
            }
        });

        favoritesFlowPane.getChildren().clear();

        // Use displayedFavorites which is filtered/sorted
        if (displayedFavorites == null) {
            reloadFavoritesMasterList();
            displayedFavorites = FXCollections.observableArrayList(allFavorites);
        }

        for (Product product : displayedFavorites) {
            VBox card = createShopCard(product);
            productCardMap.computeIfAbsent(product.getId(), k -> new java.util.ArrayList<>()).add(card);
            favoritesFlowPane.getChildren().add(card);
        }
    }

    @FXML
    public void handleFavFilter() {
        applyFavFilterAndSort();
    }

    @FXML
    public void handleFavSort() {
        applyFavFilterAndSort();
    }

    @FXML
    public void handleFavSearch() {
        applyFavFilterAndSort();
    }

    @FXML
    public void handleFavResetFilter() {
        favFilterTypeCombo.setValue("All");
        favSortCombo.setValue("Default");
        favSearchField.clear();
        applyFavFilterAndSort();
        statusLabel.setText("Favorites filters reset.");
    }

    private void applyFavFilterAndSort() {
        if (allFavorites == null) {
            reloadFavoritesMasterList();
        }

        // 1. Filter
        String filterType = favFilterTypeCombo.getValue();
        String searchText = favSearchField.getText().toLowerCase().trim();

        java.util.List<Product> filtered = new java.util.ArrayList<>();

        for (Product p : allFavorites) {
            boolean matchesType = "All".equals(filterType) || p.getType().equals(filterType);
            boolean matchesSearch = searchText.isEmpty() || p.getName().toLowerCase().contains(searchText);

            if (matchesType && matchesSearch) {
                filtered.add(p);
            }
        }

        // 2. Sort
        String sortOption = favSortCombo.getValue();
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
                    break;
            }
        }

        displayedFavorites = FXCollections.observableArrayList(filtered);
        refreshFavoritesGrid();
    }

    /**
     * Update only the single product card instead of refreshing entire grid.
     * This provides much better performance for quantity changes.
     */
    private void updateSingleProductCard(Product product) {
        java.util.List<VBox> cards = productCardMap.get(product.getId());
        if (cards == null || cards.isEmpty())
            return;

        for (VBox card : cards) {
            // Find the action container (last child before... wait, logic from before)
            // ActionBox is the last element in the card
            if (card.getChildren().size() > 1) {
                javafx.scene.Node lastChild = card.getChildren().get(card.getChildren().size() - 1);
                if (lastChild instanceof VBox) {
                    VBox actionBox = (VBox) lastChild;
                    updateProductCardControls(product, actionBox);
                }
            }
        }
    }

    private VBox createShopCard(Product product) {
        VBox card = new VBox(5);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(180);
        card.setMinWidth(180);
        card.setMaxWidth(180);
        card.setPrefHeight(300); // Fixed height for consistent alignment
        card.setMinHeight(300);
        card.setMaxHeight(300);
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.TOP_CENTER);

        // Favorite star indicator with fixed height (uses cached data - no DB query)
        Label favLabel = new Label();
        favLabel.setPrefHeight(16);
        favLabel.setMinHeight(16);
        if (favoriteProductIds.contains(product.getId())) {
            favLabel.setText("★");
            favLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #FFD700;");
        }

        // Image container with fixed height
        javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView();
        iv.setFitHeight(60);
        iv.setFitWidth(60);
        iv.setPreserveRatio(true);
        if (product.getImage() != null) {
            iv.setImage(product.getImage());
        }
        VBox imageContainer = new VBox(iv);
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setPrefHeight(65);
        imageContainer.setMinHeight(65);

        // Name label with fixed height (2 lines max)
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #F8FAFC;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(160);
        nameLabel.setPrefHeight(32);
        nameLabel.setMinHeight(32);
        nameLabel.setAlignment(Pos.CENTER);

        // Type label
        Label typeLabel = new Label(product.getType());
        typeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #94A3B8;");
        typeLabel.setPrefHeight(16);

        // Price label
        // Price label
        double displayPrice = product.getPrice();
        String priceStyle = "-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 13px;";

        // Scarcity Pricing Logic
        if (product.getStock() <= product.getThreshold()) {
            displayPrice *= 2.0; // Show doubled price
            priceStyle = "-fx-text-fill: #FF9800; -fx-font-weight: bold; -fx-font-size: 13px;"; // Orange warning
        }

        Label priceLabel = new Label(FormatHelper.formatCurrency(displayPrice) + " / " + product.getUnitLabel());
        priceLabel.setStyle(priceStyle);
        priceLabel.setPrefHeight(18);

        // Stock label
        String stockText;
        if (product.isSoldByKg()) {
            stockText = String.format("%.1f kg", product.getStock());
        } else {
            stockText = String.valueOf((int) product.getStock());
        }
        Label stockLabel = new Label("Stock: " + stockText);

        if (product.getStock() <= product.getThreshold()) {
            stockLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-font-size: 10px;");
        } else {
            stockLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 10px;");
        }
        stockLabel.setPrefHeight(16);

        // Spacer to push button to bottom
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Initialize action container
        VBox actionBox = new VBox(5);
        actionBox.setAlignment(Pos.CENTER);

        // Make card clickable to select product and show top bar favorite button
        card.setOnMouseClicked(e -> {
            selectedProduct = product;
            loadRecommendations(product);
            updateFavButton();
            statusLabel.setText("Selected: " + product.getName());
        });

        updateProductCardControls(product, actionBox);

        card.setUserData(product.getId()); // Store ID for map cleanup management

        card.getChildren().addAll(favLabel, imageContainer, nameLabel, typeLabel, priceLabel, stockLabel, spacer,
                actionBox);

        // Low stock border
        if (product.getStock() <= product.getThreshold()) {
            card.setStyle("-fx-border-color: #800000; -fx-border-width: 2; -fx-border-radius: 12;");
        }

        return card;
    }

    private void addProductToCart(Product product, double quantity) {
        // Local update first
        CartItem existingItem = null;
        for (CartItem item : cartList) {
            if (item.getProduct() != null && item.getProduct().getId() == product.getId()) {
                existingItem = item;
                break;
            }
        }

        if (existingItem != null) {
            double newQty = existingItem.getQuantity() + quantity;
            if (newQty > product.getStock()) {
                statusLabel.setText("Not enough stock for " + product.getName() + "!");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            existingItem.setQuantity(newQty);
            // DB update via cartDAO
            try {
                cartDAO.updateQuantity(currentUser.getId(), product.getId(), newQty);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            cartList.add(new CartItem(product, quantity));
            // DB insert
            try {
                cartDAO.addToCart(currentUser.getId(), product.getId(), quantity);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        updateCartTotalWithDiscount();
        statusLabel.setText("Added " + quantity + " x " + product.getName());
        statusLabel.setStyle("-fx-text-fill: #4CAF50;");

        // Update only this product's card (not entire grid - performance optimization)
        updateSingleProductCard(product);
        refreshCartGrid();

    }

    /**
     * Dynamic controls for product card:
     * - If not in cart: [ Add to Cart ]
     * - If in cart: [ delete/minus ] [ quantity ] [ plus ]
     */
    private void updateProductCardControls(Product product, VBox container) {
        container.getChildren().clear();

        // Check if product is in cart
        CartItem cartItem = null;
        for (CartItem item : cartList) {
            if (item.getProduct().getId() == product.getId()) {
                cartItem = item;
                break;
            }
        }

        if (product.isSoldByKg()) {
            // New Kg Logic
            if (cartItem != null) {
                // In Cart: [Qty] [Update] [Delete]
                javafx.scene.control.TextField qtyField = new javafx.scene.control.TextField(
                        String.valueOf(cartItem.getQuantity()));
                qtyField.setPrefWidth(50);
                qtyField.setPrefHeight(30);
                qtyField.setStyle("-fx-alignment: center; -fx-font-size: 11px;");
                qtyField.setOnAction(e -> handleKgUpdate(product, qtyField.getText()));

                // Restrict input (same as add-to-cart logic)
                qtyField.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue.matches("\\d*([\\.,]\\d{0,1})?")) {
                        qtyField.setText(oldValue);
                    }
                });

                Button updateBtn = new Button("↻");
                updateBtn.getStyleClass().add("button-secondary");
                updateBtn.setTooltip(new Tooltip("Update Quantity"));
                updateBtn.setPrefSize(40, 30);
                updateBtn.setStyle("-fx-padding: 0; -fx-font-size: 16px; -fx-font-weight: bold;");
                updateBtn.setOnAction(e -> handleKgUpdate(product, qtyField.getText()));

                Button removeBtn = new Button("🗑");
                removeBtn.getStyleClass().add("button-danger");
                removeBtn.setPrefSize(40, 30);
                removeBtn.setStyle("-fx-padding: 0; -fx-font-size: 14px;");
                removeBtn.setOnAction(e -> removeFromCart(product));

                HBox qtyBox = new HBox(5, qtyField, updateBtn, removeBtn);
                qtyBox.setAlignment(Pos.CENTER);
                container.getChildren().add(qtyBox);
            } else {
                // Not in Cart: [Qty] above [Add to Cart]
                javafx.scene.control.TextField qtyField = new javafx.scene.control.TextField();
                qtyField.setPromptText("0.5");
                qtyField.setPrefWidth(160);
                qtyField.setPrefHeight(30);
                // Increased font size slightly for readability, reduced padding to fit text
                qtyField.setStyle("-fx-alignment: center; -fx-font-size: 12px; -fx-padding: 0 5;");
                qtyField.setOnAction(e -> handleKgAdd(product, qtyField.getText()));

                // Restrict input
                qtyField.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue.matches("\\d*([\\.,]\\d{0,1})?")) {
                        qtyField.setText(oldValue);
                    }
                });

                Button addBtn = new Button("Add to Cart");
                addBtn.getStyleClass().add("button-primary");
                addBtn.setPrefWidth(160);
                addBtn.setPrefHeight(30);
                addBtn.setStyle("-fx-font-size: 10px;");
                addBtn.setOnAction(e -> handleKgAdd(product, qtyField.getText()));

                VBox addBox = new VBox(5, qtyField, addBtn);
                addBox.setAlignment(Pos.CENTER);
                container.getChildren().add(addBox);
            }

        } else {
            // Original Legacy Logic for Piece
            if (cartItem == null) {
                // Not in cart -> Show "Add to Cart" button (default size)
                Button addBtn = new Button("Add to Cart");
                addBtn.getStyleClass().add("button-primary");
                addBtn.setStyle("-fx-font-size: 10px;");
                addBtn.setPrefWidth(160);
                addBtn.setPrefHeight(30);
                addBtn.setOnAction(e -> addProductToCart(product, 1.0));
                container.getChildren().add(addBtn);
            } else {
                // In cart -> Show Dynamic Controls
                final CartItem finalCartItem = cartItem;
                HBox controls = new HBox(5);
                controls.setAlignment(Pos.CENTER);

                // Left Button: Trash (if qty=1) or Minus (if qty>1)
                Button leftBtn = new Button();
                leftBtn.setPrefWidth(30);
                leftBtn.setPrefHeight(30);
                leftBtn.setMinWidth(30);
                leftBtn.setMinHeight(30);
                leftBtn.setMaxWidth(30);
                leftBtn.setMaxHeight(30);

                // Check if quantity is effectively 1 (or less)
                boolean isSingle = finalCartItem.getQuantity() <= 1.0;

                if (isSingle) {
                    leftBtn.setText("🗑"); // Trash icon
                    leftBtn.setStyle(
                            "-fx-font-size: 14px; -fx-text-fill: white; -fx-background-color: #EF4444; -fx-padding: 0;");
                } else {
                    leftBtn.setText("-");
                    leftBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 0;");
                    leftBtn.getStyleClass().add("button-secondary");
                }

                leftBtn.setOnAction(e -> handleDecrement(product));

                // Center Label: Quantity
                // Center: Editable Quantity Field
                TextField qtyField = new TextField(String.valueOf((int) finalCartItem.getQuantity()));
                qtyField.setStyle(
                        "-fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: transparent; -fx-alignment: center; -fx-padding: 0; -fx-font-size: 13px;");
                qtyField.setPrefWidth(30);
                qtyField.setPrefHeight(30);
                qtyField.setMinWidth(30);
                qtyField.setMinHeight(30);
                qtyField.setMaxWidth(30);
                qtyField.setMaxHeight(30);

                // Limit input to numbers only
                qtyField.setTextFormatter(new TextFormatter<>(change -> {
                    String newText = change.getControlNewText();
                    if (newText.matches("[0-9]*")) {
                        return change;
                    }
                    return null;
                }));

                // Handle Enter key
                qtyField.setOnAction(event -> {
                    String input = qtyField.getText();
                    if (input.isEmpty()) {
                        // Revert to current qty if empty
                        qtyField.setText(String.valueOf((int) finalCartItem.getQuantity()));
                        return;
                    }
                    try {
                        int newQty = Integer.parseInt(input);
                        handleQuantityUpdate(product, (double) newQty);
                    } catch (NumberFormatException ex) {
                        // Revert
                        qtyField.setText(String.valueOf((int) finalCartItem.getQuantity()));
                    }
                });

                // Right Button: Plus
                Button rightBtn = new Button("+");
                rightBtn.setPrefWidth(30);
                rightBtn.setPrefHeight(30);
                rightBtn.setMinWidth(30);
                rightBtn.setMinHeight(30);
                rightBtn.setMaxWidth(30);
                rightBtn.setMaxHeight(30);
                rightBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 0;");
                rightBtn.getStyleClass().add("button-secondary");
                rightBtn.setOnAction(e -> handleIncrement(product));

                controls.getChildren().addAll(leftBtn, qtyField, rightBtn);
                container.getChildren().add(controls);
            }
        }
    }

    private void handleIncrement(Product product) {
        addProductToCart(product, 1.0);
    }

    private void handleDecrement(Product product) {
        CartItem existingItem = null;
        for (CartItem item : cartList) {
            if (item.getProduct().getId() == product.getId()) {
                existingItem = item;
                break;
            }
        }

        if (existingItem == null)
            return;

        double currentQty = existingItem.getQuantity();
        if (currentQty <= 1.0) {
            // Remove item
            cartList.remove(existingItem);
            try {
                cartDAO.removeFromCart(currentUser.getId(), product.getId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            statusLabel.setText("Removed " + product.getName() + " from cart.");
            refreshCartGrid();
        } else {
            // Decrease quantity
            double newQty = currentQty - 1.0;
            existingItem.setQuantity(newQty);
            try {
                cartDAO.updateQuantity(currentUser.getId(), product.getId(), newQty);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        updateCartTotalWithDiscount();
        updateSingleProductCard(product); // Update only this card
    }

    private void handleQuantityUpdate(Product product, double newQty) {
        CartItem existingItem = null;
        for (CartItem item : cartList) {
            if (item.getProduct().getId() == product.getId()) {
                existingItem = item;
                break;
            }
        }

        if (existingItem == null)
            return;

        if (newQty <= 0) {
            // Remove item
            cartList.remove(existingItem);
            try {
                cartDAO.removeFromCart(currentUser.getId(), product.getId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            statusLabel.setText("Removed " + product.getName() + " from cart.");
            refreshCartGrid();
        } else {
            // Check stock
            if (newQty > product.getStock()) {
                statusLabel.setText("Not enough stock! Max: " + (int) product.getStock());
                statusLabel.setStyle("-fx-text-fill: red;");
                // Revert UI by refreshing
                refreshShopGrid();
                return;
            }

            // Update
            existingItem.setQuantity(newQty);
            try {
                cartDAO.updateQuantity(currentUser.getId(), product.getId(), newQty);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            statusLabel.setText("Updated " + product.getName() + " to " + (int) newQty);
            statusLabel.setStyle("-fx-text-fill: #4CAF50;");
        }

        updateCartTotalWithDiscount();
        updateSingleProductCard(product); // Update only this card
    }

    private void loadOrders() {
        try {
            orderList = FXCollections.observableArrayList(orderDAO.getOrdersByCustomer(currentUser.getId()));

            // Pre-load all ratings into cache (single query instead of per-cell queries)
            ratingCache.clear();
            com.greengrocer.dao.CarrierRatingDAO ratingDAO = new com.greengrocer.dao.CarrierRatingDAO();
            for (Order order : orderList) {
                try {
                    com.greengrocer.models.CarrierRating rating = ratingDAO.getRatingByOrderId(order.getId());
                    if (rating != null) {
                        ratingCache.put(order.getId(), rating);
                    }
                } catch (SQLException ex) {
                    /* ignore */ }
            }

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

    // handleAddToCart removed as it's now handled per card

    private void loadCart() {
        if (currentUser == null)
            return;
        try {
            cartList.setAll(cartDAO.getCartByUserId(currentUser.getId()));
            updateCartTotal();
            refreshCartGrid();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to load cart.");
        }
    }

    private void refreshCartGrid() {
        if (cartFlowPane == null)
            return;

        // Cleanup
        cartFlowPane.getChildren().forEach(node -> {
            if (node.getUserData() instanceof Integer) {
                int id = (Integer) node.getUserData();
                java.util.List<VBox> cards = productCardMap.get(id);
                if (cards != null)
                    cards.remove(node);
            }
        });

        cartFlowPane.getChildren().clear();

        for (CartItem item : cartList) {
            // Re-use logic to create product card
            VBox card = createShopCard(item.getProduct());
            // Register card for updates
            productCardMap.computeIfAbsent(item.getProduct().getId(), k -> new java.util.ArrayList<>()).add(card);

            cartFlowPane.getChildren().add(card);
        }
    }

    @FXML
    public void handleClearCart() {
        if (cartList.isEmpty()) {
            return;
        }
        try {
            cartDAO.clearCart(currentUser.getId());
            cartList.clear();
            refreshCartGrid();
            refreshShopGrid();
            updateCartTotal();
            statusLabel.setText("Cart cleared.");
            statusLabel.setStyle("-fx-text-fill: green;");
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error clearing cart.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private javafx.scene.control.Button checkoutButton;

    @FXML
    public void handleCheckout() {
        if (cartList.isEmpty()) {
            statusLabel.setText("Cart is empty.");
            return;
        }

        double subtotal = getCartTotal();
        double couponDiscountAmount = appliedCoupon != null ? subtotal * (appliedCoupon.getDiscountPercent() / 100.0)
                : 0;
        double afterDiscounts = subtotal - gPointsToUse - couponDiscountAmount;
        if (afterDiscounts < 0)
            afterDiscounts = 0;

        // Calculate VAT
        double vatAmount = afterDiscounts * 0.20;
        double finalTotal = afterDiscounts + vatAmount;

        // Minimum cart value requirement (Based on Final Total)
        double MINIMUM_CART_VALUE = 20.0;
        if (finalTotal < MINIMUM_CART_VALUE) {
            statusLabel.setText(
                    String.format("Minimum order is TL%.2f. Your total: TL%.2f", MINIMUM_CART_VALUE, finalTotal));
            statusLabel.setStyle("-fx-text-fill: #FF9800;");
            return;
        }

        // Show delivery date/time selection dialog
        javafx.scene.control.Dialog<java.time.LocalDateTime> dialog = new javafx.scene.control.Dialog<>();
        com.greengrocer.util.StyleHelper.applyAppIcon(dialog);
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

        // Initial population
        timeCombo.setItems(FXCollections.observableArrayList(getAvailableTimeSlots(datePicker.getValue())));

        // Update on date change
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                timeCombo.setItems(FXCollections.observableArrayList(getAvailableTimeSlots(newVal)));
                if (!timeCombo.getItems().isEmpty()) {
                    timeCombo.setValue(timeCombo.getItems().get(0));
                } else {
                    timeCombo.setValue(null);
                }
            }
        });

        if (!timeCombo.getItems().isEmpty()) {
            timeCombo.setValue(timeCombo.getItems().get(0));
        }

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
            String qtyStr = item.getProduct().isSoldByKg() ? String.format("%.2f kg", item.getQuantity())
                    : String.format("%d pc", (int) item.getQuantity());

            summary.append(String.format("• %s x%s = %s\n",
                    item.getProductName(), qtyStr, FormatHelper.formatCurrency(item.getTotal())));
        }
        summary.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        summary.append("Subtotal: ").append(FormatHelper.formatCurrency(subtotal)).append("\n");
        if (gPointsToUse > 0)
            summary.append("G Points: ").append(FormatHelper.formatCurrencyWithPrefix(gPointsToUse, "-")).append("\n");
        if (couponDiscountAmount > 0)
            summary.append("Coupon: ").append(FormatHelper.formatCurrencyWithPrefix(couponDiscountAmount, "-"))
                    .append("\n");
        summary.append("VAT (20%): ").append(FormatHelper.formatCurrencyWithPrefix(vatAmount, "+")).append("\n");
        summary.append("TOTAL: ").append(FormatHelper.formatCurrency(finalTotal));

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

            // Create order with the subtotal and delivery date
            java.sql.Timestamp deliveryTimestamp = java.sql.Timestamp.valueOf(deliveryDateTime);
            int orderId = orderDAO.createOrder(currentUser.getId(), new java.util.ArrayList<>(cartList), finalTotal,
                    deliveryTimestamp);
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
                refreshCartGrid();
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
            if (p.getStock() <= 0) {
                // Out of stock products are hidden from customers
                continue;
            }

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

        refreshShopGrid();

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
    // updateQuantityLabelForProduct removed

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
            refreshShopGrid();
        }
    }

    @FXML
    public void handleAddToFavorites() {
        Product selected = selectedProduct;
        if (selected == null) {
            statusLabel.setText("Click on a product card first.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            boolean isNowFav = false;
            if (favoritesDAO.isFavorite(currentUser.getId(), selected.getId())) {
                // Already favorite, remove it
                favoritesDAO.removeFavorite(currentUser.getId(), selected.getId());
                statusLabel.setText(selected.getName() + " removed from favorites.");
                statusLabel.setStyle("-fx-text-fill: orange;");

                // Update local list
                favoriteProductIds.remove(Integer.valueOf(selected.getId()));
                isNowFav = false;
            } else {
                // Add to favorites
                favoritesDAO.addFavorite(currentUser.getId(), selected.getId());
                statusLabel.setText(selected.getName() + " added to favorites!");
                statusLabel.setStyle("-fx-text-fill: green;");

                // Update local list
                if (!favoriteProductIds.contains(selected.getId())) {
                    favoriteProductIds.add(selected.getId());
                }
                isNowFav = true;
            }

            // Instant UI Update
            updateProductCardVisuals(selected.getId(), isNowFav);

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Database error.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
        updateFavButton();
    }

    private void updateProductCardVisuals(int productId, boolean isFav) {
        // Update Shop Grid
        if (shopFlowPane != null) {
            for (javafx.scene.Node node : shopFlowPane.getChildren()) {
                if (node instanceof VBox && Integer.valueOf(productId).equals(node.getUserData())) {
                    updateCardStar((VBox) node, isFav);
                }
            }
        }

        // Update Favorites Grid
        if (favoritesFlowPane != null) {
            // For favorites tab, usually we might want to refresh, but for instant feedback
            // let's toggle too.
            // If we untoggled it, it will disappear next refresh.
            for (javafx.scene.Node node : favoritesFlowPane.getChildren()) {
                if (node instanceof VBox && Integer.valueOf(productId).equals(node.getUserData())) {
                    updateCardStar((VBox) node, isFav);
                }
            }
        }
    }

    private void updateCardStar(VBox card, boolean isFav) {
        if (card.getChildren().isEmpty())
            return;
        javafx.scene.Node node = card.getChildren().get(0);
        if (node instanceof Label) {
            Label favLabel = (Label) node;
            if (isFav) {
                favLabel.setText("★");
                favLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #FFD700;");
            } else {
                favLabel.setText("");
            }
        }
    }

    // Update favorite button text based on selection
    // Update favorite button text based on selection
    private void updateFavButton() {
        Product selected = selectedProduct;

        // Hide both buttons initially
        if (favButton != null) {
            favButton.setVisible(false);
            favButton.setManaged(false);
        }
        if (favRemoveButton != null) {
            favRemoveButton.setVisible(false);
            favRemoveButton.setManaged(false);
        }

        if (selected == null) {
            return;
        }

        try {
            boolean isFav = favoritesDAO.isFavorite(currentUser.getId(), selected.getId());

            // Check which tab is active to decide which button to show logic
            // But simpler: just update both if they participate in the scene

            // Logic for Shop Tab Button (Add/Remove)
            if (favButton != null) {
                favButton.setVisible(true);
                favButton.setManaged(true);
                if (isFav) {
                    favButton.setText("Remove Favorite");
                    favButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                } else {
                    favButton.setText("Add Favorite");
                    favButton.setStyle("-fx-background-color: #FFC107;");
                }
            }

            // Logic for Favorites Tab Button (Only Remove makes sense usually, OR
            // Add/Remove if we want consistency)
            // User asked for "Remove Favourite tuşu".
            // Let's make it behave identical to the other button for consistency (Toggle)
            if (favRemoveButton != null) {
                // Only show if we are in Favorites tab?
                // Or let the visibility be handled like the other one?
                // The prompt implies it should be visible when an item is selected in Favorites
                // tab.
                // Since selectedProduct is global, if I select in Favorites, this triggers.

                // However, favRemoveButton is inside the Favorites tab toolbar.
                // We should only show it if the user is viewing Favorites?
                // Actually, `selectedProduct` is shared.

                // Let's checking if the button is relevant.
                // For now, simple toggle logic is best.

                favRemoveButton.setVisible(true);
                favRemoveButton.setManaged(true);

                if (isFav) {
                    favRemoveButton.setText("Remove Favorite");
                    favRemoveButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                    favRemoveButton.setDisable(false);
                } else {
                    // If not favorite (e.g. removed), in Favorites tab,
                    // maybe we still want to allow adding it back?
                    favRemoveButton.setText("Add Favorite");
                    favRemoveButton.setStyle("-fx-background-color: #FFC107;");
                }
            }

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    // Favorites Tab Methods

    // Obsolete favorites methods removed (handleAddFavToCart)

    // ==================== KG UNIT HANDLERS ====================

    private void handleKgAdd(Product product, String qtyText) {
        try {
            double qty = parseKgQuantity(qtyText);
            if (qty < 0.5) {
                showError("Minimum quantity is 0.5 kg.");
                return;
            }
            if (qty > product.getStock()) {
                showError("Not enough stock.");
                return;
            }
            cartDAO.addToCart(currentUser.getId(), product.getId(), qty);

            // Update local lists
            boolean found = false;
            for (CartItem item : cartList) {
                if (item.getProduct().getId() == product.getId()) {
                    item.setQuantity(item.getQuantity() + qty);
                    found = true;
                    break;
                }
            }
            if (!found) {
                cartList.add(new CartItem(product, qty));
            }

            updateCartTotal();
            updateSingleProductCard(product);
            refreshCartGrid();
            statusLabel.setText("Added " + qty + " kg to cart.");
        } catch (NumberFormatException e) {
            showError("Invalid quantity.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleKgUpdate(Product product, String qtyText) {
        try {
            double qty = parseKgQuantity(qtyText);
            if (qty < 0.5) {
                showError("Minimum quantity is 0.5 kg.");
                return;
            }
            if (qty > product.getStock()) {
                showError("Not enough stock.");
                return;
            }
            cartDAO.updateQuantity(currentUser.getId(), product.getId(), qty);

            // Reload cart from DB to ensure UI sync
            cartList.setAll(cartDAO.getCartByUserId(currentUser.getId()));

            updateCartTotal();
            updateSingleProductCard(product);
            statusLabel.setText("Updated to " + qty + " kg.");
        } catch (NumberFormatException e) {
            showError("Invalid quantity.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private double parseKgQuantity(String text) {
        if (text == null || text.trim().isEmpty())
            return 0.5;
        text = text.replace(',', '.');
        return Double.parseDouble(text);
    }

    private void removeFromCart(Product product) {
        try {
            cartDAO.removeFromCart(currentUser.getId(), product.getId());

            // local update
            cartList.removeIf(item -> item.getProduct().getId() == product.getId());

            updateCartTotal();
            updateSingleProductCard(product);
            refreshCartGrid();
            statusLabel.setText("Removed " + product.getName() + " from cart.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        StyledAlert.showError("Error", null, msg);
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
            statusLabel.setText("G Points removed.");
            statusLabel.setStyle("-fx-text-fill: #aaa;");
            return;
        }

        try {
            // Requirement provided: "Positive natural number"
            int requestedPoints = Integer.parseInt(pointsStr);
            double availablePoints = currentUser.getGPoints();
            double cartTotal = getCartTotal();

            if (requestedPoints <= 0) {
                statusLabel.setText("Please enter a positive number!");
                statusLabel.setStyle("-fx-text-fill: red;");
                gPointsToUse = 0;
            } else if (requestedPoints > availablePoints) {
                statusLabel.setText("Insufficient G Points! Available: " + (int) availablePoints);
                statusLabel.setStyle("-fx-text-fill: red;");
                gPointsToUse = 0;
            } else if (requestedPoints > cartTotal) {
                statusLabel.setText("Using max usable G Points.");
                statusLabel.setStyle("-fx-text-fill: #4CAF50;");
                gPointsToUse = cartTotal;
                gPointsField.setText(String.format("%.0f", cartTotal));
            } else {
                gPointsToUse = requestedPoints;
                statusLabel.setText((int) requestedPoints + " G Points applied!");
                statusLabel.setStyle("-fx-text-fill: #4CAF50;");
            }
            updateCartTotalWithDiscount();
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid amount! Use numbers only.");
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
        // 1. Calculate Subtotal & Populate Summary List
        double subtotal = 0;

        if (cartSummaryList != null) {
            cartSummaryList.getChildren().clear();
        }

        if (cartList != null) {
            for (CartItem item : cartList) {
                double itemTotal = item.getTotal();
                subtotal += itemTotal;

                if (cartSummaryList != null) {
                    javafx.scene.layout.HBox row = new javafx.scene.layout.HBox();
                    row.setSpacing(10);
                    row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    String qtyStr = item.getProduct().isSoldByKg() ? String.format("%.1f", item.getQuantity())
                            : String.valueOf((int) item.getQuantity());
                    Label name = new Label(
                            item.getProduct().getName() + " x " + qtyStr + item.getProduct().getUnitLabel());
                    name.setStyle("-fx-text-fill: white; -fx-wrap-text: true; -fx-font-size: 11px;");
                    name.setPrefWidth(160);
                    name.setWrapText(true);

                    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                    javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                    Label price = new Label(FormatHelper.formatCurrency(itemTotal));
                    price.setStyle("-fx-text-fill: #ddd; -fx-alignment: CENTER-RIGHT; -fx-font-size: 11px;");

                    row.getChildren().addAll(name, spacer, price);
                    cartSummaryList.getChildren().add(row);
                }
            }
        }

        // 2. Calculate Discounts
        double couponDiscountAmount = 0;
        if (appliedCoupon != null) {
            couponDiscountAmount = subtotal * (appliedCoupon.getDiscountPercent() / 100.0);
            couponDiscount = couponDiscountAmount;
        }

        double totalDiscount = gPointsToUse + couponDiscountAmount;
        double afterDiscounts = subtotal - totalDiscount;
        if (afterDiscounts < 0)
            afterDiscounts = 0;

        // 3. Calculate Tax (20%)
        double vatRate = 0.20;
        double vatAmount = afterDiscounts * vatRate;
        double finalTotal = afterDiscounts + vatAmount;

        // 4. Update Labels
        if (subtotalLabel != null) {
            subtotalLabel.setText(FormatHelper.formatCurrency(subtotal));
        }

        if (discountLabel != null) {
            if (totalDiscount > 0) {
                discountLabel.setText("-" + FormatHelper.formatCurrency(totalDiscount));
                discountLabel.setStyle("-fx-text-fill: #4CAF50;");
            } else {
                discountLabel.setText("0.00 TL");
                discountLabel.setStyle("-fx-text-fill: #ddd;");
            }
        }

        if (taxLabel != null) {
            taxLabel.setText(FormatHelper.formatCurrency(vatAmount));
        }

        if (finalTotalLabel != null) {
            finalTotalLabel.setText(FormatHelper.formatCurrency(finalTotal));
            // finalTotalLabel styling is set in FXML
        }
        // 5. Update Checkout Button State
        if (checkoutButton != null) {
            double MINIMUM_CART_VALUE = 20.0;
            if (cartList.isEmpty()) {
                checkoutButton.setDisable(true);
                statusLabel.setText(""); // Clear status if empty
            } else if (finalTotal < MINIMUM_CART_VALUE) {
                checkoutButton.setDisable(true);
                statusLabel.setText(
                        String.format("Minimum order TL%.2f (Current: TL%.2f)", MINIMUM_CART_VALUE, finalTotal));
                statusLabel.setStyle("-fx-text-fill: #FF9800;");
            } else {
                checkoutButton.setDisable(false);
                statusLabel.setText(""); // Clear warning
            }
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
                items.add("No recommendations yet");
            } else {
                for (Product p : recommendations) {
                    items.add("• " + p.getName() + " (" + FormatHelper.formatCurrency(p.getPrice()) + ")");
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
        if (selectedProduct != null) {
            loadRecommendations(selectedProduct);
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
            String sql = "SELECT p.name, p.unit_type, oi.quantity, oi.price_at_purchase FROM OrderItems oi " +
                    "JOIN ProductInfo p ON oi.product_id = p.id WHERE oi.order_id = ?";
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, selected.getId());
            java.sql.ResultSet rs = stmt.executeQuery();

            double subtotal = 0;
            while (rs.next()) {
                String productName = rs.getString("name");
                String unitType = rs.getString("unit_type");
                double quantity = rs.getDouble("quantity");
                double unitPrice = rs.getDouble("price_at_purchase");
                double lineTotal = quantity * unitPrice;
                subtotal += lineTotal;

                // Format quantity based on unit type
                String qtyStr;
                if ("piece".equalsIgnoreCase(unitType)) {
                    qtyStr = String.format("%.0f", quantity); // x1
                } else {
                    qtyStr = String.format("%.2f", quantity); // x0.50
                }

                details.append("   • ").append(productName)
                        .append(" x").append(qtyStr)
                        .append(" @ ").append(FormatHelper.formatCurrency(unitPrice))
                        .append(" = ").append(FormatHelper.formatCurrency(lineTotal)).append("\n");
            }
            rs.close();
            stmt.close();

            details.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            details.append("PAYMENT DETAILS:\n\n");
            details.append("   Subtotal: ").append(FormatHelper.formatCurrency(subtotal)).append("\n");

            // Get coupon info
            String couponSql = "SELECT c.code, cu.discount_amount FROM CouponUsage cu " +
                    "JOIN Coupons c ON cu.coupon_id = c.id WHERE cu.order_id = ?";
            java.sql.PreparedStatement couponStmt = conn.prepareStatement(couponSql);
            couponStmt.setInt(1, selected.getId());
            java.sql.ResultSet couponRs = couponStmt.executeQuery();

            double couponDiscount = 0;
            if (couponRs.next()) {
                String couponCode = couponRs.getString("code");
                couponDiscount = couponRs.getDouble("discount_amount");
                details.append("   Coupon Used: ").append(couponCode).append("\n");
                details.append("   Coupon Discount: ")
                        .append(FormatHelper.formatCurrencyWithPrefix(couponDiscount, "-")).append("\n");
            }
            couponRs.close();
            couponStmt.close();

            // Calculate VAT
            double taxableAmount = subtotal - couponDiscount;
            if (taxableAmount < 0)
                taxableAmount = 0;
            double vatAmount = taxableAmount * 0.20;
            details.append("   VAT (20%): ").append(FormatHelper.formatCurrency(vatAmount)).append("\n");

            // Calculate G Points (estimated - 5% of order total)
            // Calculate G Points (estimated - 20% of order total, matching UserDAO logic)
            double totalAmount = selected.getTotalAmount();
            double gPointsEarned = totalAmount / 5.0;
            details.append("   G Points Earned: +").append(String.format("%.0f", gPointsEarned)).append(" points\n");

            details.append("\n   ━━━━━━━━━━━━━━━━━━━━━━━━\n");
            details.append("   TOTAL PAID: ").append(FormatHelper.formatCurrency(totalAmount)).append("\n");

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

    // ==================== ORDER CANCELLATION ====================

    private com.greengrocer.dao.CarrierRatingDAO ratingDAO = new com.greengrocer.dao.CarrierRatingDAO();

    /**
     * Cancel an order (only within 30 minutes of placing and if Pending)
     */
    @FXML
    public void handleCancelOrder() {
        Order selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Please select an order to cancel.");
            statusLabel.setStyle("-fx-text-fill: orange;");
            return;
        }

        // Check if status is Pending
        if (!"Pending".equalsIgnoreCase(selected.getStatus())) {
            statusLabel.setText("Only 'Pending' orders can be cancelled.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Check 30-minute limit
        long diffInMinutes = java.time.Duration.between(
                selected.getOrderDate().toLocalDateTime(),
                java.time.LocalDateTime.now()).toMinutes();

        if (diffInMinutes > 30) {
            statusLabel.setText("Cancellation time limit (30 min) exceeded.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Confirm with user
        boolean confirmed = StyledAlert.showConfirmation(
                "Cancel Order",
                "Are you sure you want to cancel Order #" + selected.getId() + "?",
                "Stock will be restored. Time remaining: " + (30 - diffInMinutes) + " minutes.");

        if (confirmed) {
            try {
                if (orderDAO.cancelOrder(selected.getId())) {
                    statusLabel.setText("Order #" + selected.getId() + " cancelled successfully!");
                    statusLabel.setStyle("-fx-text-fill: green;");
                    loadOrders(); // Refresh table
                } else {
                    statusLabel.setText("Failed to cancel order (it may have been processed).");
                    statusLabel.setStyle("-fx-text-fill: red;");
                }
            } catch (SQLException e) {
                statusLabel.setText("Error cancelling order: " + e.getMessage());
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }

    // ==================== CARRIER RATING ====================

    /**
     * Rate a carrier after delivery (1-5 stars)
     */
    @FXML
    public void handleRateCarrier() {
        Order selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Please select an order to rate.");
            statusLabel.setStyle("-fx-text-fill: orange;");
            return;
        }

        if (!"Delivered".equalsIgnoreCase(selected.getStatus())) {
            statusLabel.setText("You can only rate delivered orders.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            if (ratingDAO.hasBeenRated(selected.getId())) {
                statusLabel.setText("You have already rated this delivery.");
                statusLabel.setStyle("-fx-text-fill: orange;");
                return;
            }

            // Create rating dialog
            ChoiceDialog<Integer> dialog = new ChoiceDialog<>(5, 1, 2, 3, 4, 5);
            dialog.setTitle("Rate Carrier");
            dialog.setHeaderText("How was your delivery for Order #" + selected.getId() + "?");
            dialog.setContentText("Choose rating (1-5 stars):");

            java.util.Optional<Integer> result = dialog.showAndWait();
            if (result.isPresent()) {
                int rating = result.get();

                // Ask for optional comment
                TextInputDialog commentDialog = new TextInputDialog();
                commentDialog.setTitle("Add a Comment");
                commentDialog.setHeaderText("Optional: Leave feedback about the delivery");
                commentDialog.setContentText("Comment:");

                String comment = commentDialog.showAndWait().orElse("");

                if (ratingDAO.rateCarrier(selected.getId(), currentUser.getId(), selected.getCarrierId(), rating,
                        comment)) {
                    String stars = "⭐".repeat(rating);
                    statusLabel.setText("Thank you! Rated: " + stars);
                    statusLabel.setStyle("-fx-text-fill: green;");
                }
            }
        } catch (SQLException e) {
            statusLabel.setText("Error saving rating: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
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
                        "% off! (" + FormatHelper.formatCurrencyWithPrefix(couponDiscount, "-") + ")");
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

        // Name length validation
        if (firstName.length() < 2 || firstName.length() > 50) {
            profileStatusLabel.setText("First name must be 2-50 characters.");
            profileStatusLabel.setStyle("-fx-text-fill: #f44336;");
            return;
        }

        if (lastName.length() < 2 || lastName.length() > 50) {
            profileStatusLabel.setText("Last name must be 2-50 characters.");
            profileStatusLabel.setStyle("-fx-text-fill: #f44336;");
            return;
        }

        // Name format validation (letters and spaces only)
        if (!firstName.matches("^[a-zA-ZğüşıöçĞÜŞİÖÇ\\s]+$")) {
            profileStatusLabel.setText("First name can only contain letters.");
            profileStatusLabel.setStyle("-fx-text-fill: #f44336;");
            return;
        }

        if (!lastName.matches("^[a-zA-ZğüşıöçĞÜŞİÖÇ\\s]+$")) {
            profileStatusLabel.setText("Last name can only contain letters.");
            profileStatusLabel.setStyle("-fx-text-fill: #f44336;");
            return;
        }

        // Address length validation
        if (address.length() > 200) {
            profileStatusLabel.setText("Address must be at most 200 characters.");
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

    // ==================== MESSAGING FEATURE - WhatsApp Style ====================

    private com.greengrocer.dao.MessageDAO messageDAO = new com.greengrocer.dao.MessageDAO();
    private String currentConversationSubject = null;
    private int currentChatPartnerId = -1;

    @FXML
    private VBox conversationListPane;
    @FXML
    private VBox chatMessagesPane;
    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    private Label chatPartnerLabel;
    @FXML
    private TextField msgComposeArea;
    @FXML
    private Label msgStatusLabel;
    @FXML
    private Label unreadCountLabel;

    @FXML
    public void handleRefreshMessages() {
        loadConversations();
        updateUnreadCount();
        if (currentConversationSubject != null) {
            loadChatMessages();
        }
    }

    @FXML
    public void handleNewChat() {
        // Show dialog for new conversation
        javafx.scene.control.Dialog<String> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("New Conversation");
        dialog.setHeaderText("Start a new conversation with the Owner");

        javafx.scene.control.ButtonType sendButtonType = new javafx.scene.control.ButtonType("Start",
                javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(sendButtonType, javafx.scene.control.ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 20;");

        TextField subjectField = new TextField();
        subjectField.setPromptText("Conversation subject (e.g., Product Question)");

        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Your message...");
        messageArea.setPrefRowCount(4);
        messageArea.setWrapText(true);

        content.getChildren().addAll(
                new Label("Subject:"),
                subjectField,
                new Label("Message:"),
                messageArea);

        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == sendButtonType) {
                return subjectField.getText().trim() + "|||" + messageArea.getText().trim();
            }
            return null;
        });

        java.util.Optional<String> result = dialog.showAndWait();
        result.ifPresent(data -> {
            String[] parts = data.split("\\|\\|\\|", 2);
            if (parts.length == 2 && !parts[0].isEmpty() && !parts[1].isEmpty()) {
                sendNewMessage(parts[0], parts[1]);
            } else {
                setMsgStatus("Please fill both subject and message.", "red");
            }
        });
    }

    private void sendNewMessage(String subject, String content) {
        try {
            int ownerId = messageDAO.getOwnerId();
            if (ownerId == -1) {
                setMsgStatus("Owner not found.", "red");
                return;
            }

            com.greengrocer.models.Message message = new com.greengrocer.models.Message(
                    currentUser.getId(), ownerId, subject, content);

            if (messageDAO.sendMessage(message)) {
                setMsgStatus("Message sent!", "#4CAF50");
                currentConversationSubject = subject;
                currentChatPartnerId = ownerId;
                loadConversations();
                loadChatMessages();
            } else {
                setMsgStatus("Failed to send.", "red");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            setMsgStatus("Error sending message.", "red");
        }
    }

    private void loadConversations() {
        if (conversationListPane == null || currentUser == null)
            return;
        conversationListPane.getChildren().clear();

        try {
            // Get all messages for this user and group by subject
            java.util.List<com.greengrocer.models.Message> inbox = messageDAO.getInbox(currentUser.getId());
            java.util.List<com.greengrocer.models.Message> sent = messageDAO.getSentMessages(currentUser.getId());

            // Count unread messages per conversation
            java.util.Map<String, Integer> unreadCounts = new java.util.HashMap<>();
            for (com.greengrocer.models.Message msg : inbox) {
                if (!msg.isRead()) {
                    String key = getConversationKey(msg.getSubject());
                    unreadCounts.put(key, unreadCounts.getOrDefault(key, 0) + 1);
                }
            }

            // Combine and group by subject
            java.util.Map<String, com.greengrocer.models.Message> conversations = new java.util.LinkedHashMap<>();

            // Process all messages, keep the latest for each subject
            java.util.List<com.greengrocer.models.Message> allMessages = new java.util.ArrayList<>();
            allMessages.addAll(inbox);
            allMessages.addAll(sent);

            // Sort by date descending
            allMessages.sort((a, b) -> b.getSentAt().compareTo(a.getSentAt()));

            for (com.greengrocer.models.Message msg : allMessages) {
                String key = getConversationKey(msg.getSubject());
                if (!conversations.containsKey(key)) {
                    conversations.put(key, msg);
                }
            }

            // Create conversation items
            for (java.util.Map.Entry<String, com.greengrocer.models.Message> entry : conversations.entrySet()) {
                int unread = unreadCounts.getOrDefault(entry.getKey(), 0);
                HBox convItem = createConversationItem(entry.getKey(), entry.getValue(), unread);
                conversationListPane.getChildren().add(convItem);
            }

            if (conversations.isEmpty()) {
                Label emptyLabel = new Label("No conversations yet.\nStart a new one!");
                emptyLabel.setStyle("-fx-text-fill: #94A3B8; -fx-padding: 20;");
                emptyLabel.setWrapText(true);
                conversationListPane.getChildren().add(emptyLabel);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getConversationKey(String subject) {
        // Regex to find [Order #123]
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\[Order #\\d+\\]");
        java.util.regex.Matcher m = p.matcher(subject);
        if (m.find()) {
            return m.group();
        }
        return subject.replaceFirst("^Re: ", "");
    }

    private HBox createConversationItem(String subject, com.greengrocer.models.Message lastMessage, int unreadCount) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);

        boolean isSelected = subject.equals(currentConversationSubject);
        String bgStyle = isSelected ? "-fx-background-color: #475569;" : "-fx-background-color: #334155;";
        String borderStyle = isSelected ? "-fx-border-color: #4CAF50; -fx-border-width: 0 0 0 4;" : "";

        item.setStyle("-fx-padding: 12; " + bgStyle + " -fx-background-radius: 8; -fx-cursor: hand; " + borderStyle);

        VBox textContent = new VBox(3);
        textContent.setMaxWidth(180);

        Label subjectLabel = new Label(subject);
        subjectLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 13px;");
        subjectLabel.setMaxWidth(170);

        // Preview with read receipt for sent messages
        String preview = lastMessage.getContent();
        if (preview.length() > 25)
            preview = preview.substring(0, 25) + "...";

        // Add checkmarks for sent messages
        boolean isSent = lastMessage.getSenderId() == currentUser.getId();
        String checkMark = "";
        if (isSent) {
            checkMark = lastMessage.isRead() ? "✓✓ " : "✓ ";
        }

        Label previewLabel = new Label(checkMark + preview);
        previewLabel.setStyle("-fx-text-fill: " + (isSent && lastMessage.isRead() ? "#4FC3F7" : "#94A3B8")
                + "; -fx-font-size: 11px;");
        previewLabel.setMaxWidth(170);

        textContent.getChildren().addAll(subjectLabel, previewLabel);

        // Right side: time and unread badge
        VBox rightContent = new VBox(5);
        rightContent.setAlignment(Pos.CENTER_RIGHT);

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        Label timeLabel = new Label(sdf.format(lastMessage.getSentAt()));
        timeLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 10px;");

        rightContent.getChildren().add(timeLabel);

        // Unread count badge
        if (unreadCount > 0) {
            Label unreadBadge = new Label(String.valueOf(unreadCount));
            unreadBadge.setStyle(
                    "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 2 6; -fx-min-width: 18;");
            unreadBadge.setAlignment(Pos.CENTER);
            rightContent.getChildren().add(unreadBadge);
        }

        item.getChildren().addAll(textContent, new Region(), rightContent);
        HBox.setHgrow(item.getChildren().get(1), javafx.scene.layout.Priority.ALWAYS);

        // Click handler
        item.setOnMouseClicked(e -> {
            currentConversationSubject = subject;
            currentChatPartnerId = lastMessage.getSenderId() == currentUser.getId() ? lastMessage.getReceiverId()
                    : lastMessage.getSenderId();
            chatPartnerLabel.setText("💬 " + subject);
            loadChatMessages();
            loadConversations(); // Refresh to clear unread badge
        });

        // Hover effect
        item.setOnMouseEntered(e -> {
            boolean selected = subject.equals(currentConversationSubject);
            if (!selected) {
                item.setStyle(
                        "-fx-padding: 12; -fx-background-color: #475569; -fx-background-radius: 8; -fx-cursor: hand;");
            }
        });
        item.setOnMouseExited(e -> {
            boolean selected = subject.equals(currentConversationSubject);
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

    private void loadChatMessages() {
        if (chatMessagesPane == null || currentUser == null || currentConversationSubject == null)
            return;
        chatMessagesPane.getChildren().clear();

        try {
            java.util.List<com.greengrocer.models.Message> inbox = messageDAO.getInbox(currentUser.getId());
            java.util.List<com.greengrocer.models.Message> sent = messageDAO.getSentMessages(currentUser.getId());

            java.util.List<com.greengrocer.models.Message> chatMessages = new java.util.ArrayList<>();

            for (com.greengrocer.models.Message msg : inbox) {
                String key = getConversationKey(msg.getSubject());
                if (key.equals(currentConversationSubject)) {
                    chatMessages.add(msg);
                    // Mark as read
                    if (!msg.isRead()) {
                        messageDAO.markAsRead(msg.getId());
                    }
                }
            }
            for (com.greengrocer.models.Message msg : sent) {
                String key = getConversationKey(msg.getSubject());
                if (key.equals(currentConversationSubject)) {
                    chatMessages.add(msg);
                }
            }

            // Sort by date ascending (oldest first)
            chatMessages.sort((a, b) -> a.getSentAt().compareTo(b.getSentAt()));

            // Create chat bubbles
            for (com.greengrocer.models.Message msg : chatMessages) {
                HBox bubble = createChatBubble(msg);
                chatMessagesPane.getChildren().add(bubble);
            }

            // Scroll to bottom
            javafx.application.Platform.runLater(() -> {
                if (chatScrollPane != null) {
                    chatScrollPane.setVvalue(1.0);
                }
            });

            updateUnreadCount();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createChatBubble(com.greengrocer.models.Message msg) {
        boolean isSent = msg.getSenderId() == currentUser.getId();

        // Use HBox as container for proper left/right alignment
        HBox container = new HBox();
        container.setMaxWidth(Double.MAX_VALUE);

        // Message bubble
        VBox bubble = new VBox(5);
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
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            container.getChildren().addAll(spacer, bubble);
        } else {
            // Received message: bubble on left, spacer on right
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            container.getChildren().addAll(bubble, spacer);
        }

        return container;
    }

    @FXML
    public void handleSendMessage() {
        if (currentUser == null)
            return;

        String content = msgComposeArea != null ? msgComposeArea.getText().trim() : "";

        if (content.isEmpty()) {
            setMsgStatus("Type a message.", "red");
            return;
        }

        if (currentConversationSubject == null || currentChatPartnerId == -1) {
            // No active conversation, start new one
            handleNewChat();
            return;
        }

        // Restrict messaging if order is delivered
        if (currentConversationSubject.startsWith("[Order #")) {
            try {
                int endIdx = currentConversationSubject.indexOf("]");
                if (endIdx != -1) {
                    String idStr = currentConversationSubject.substring(8, endIdx);
                    int orderId = Integer.parseInt(idStr);
                    Order order = orderDAO.getOrderById(orderId);
                    if (order != null && "Delivered".equalsIgnoreCase(order.getStatus())) {
                        setMsgStatus("Cannot send: Order is Delivered.", "red");
                        com.greengrocer.util.StyledAlert.showError("Order Completed", "Action Restricted",
                                "You cannot send messages for a delivered order.");
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            String subject = "Re: " + currentConversationSubject;
            com.greengrocer.models.Message message = new com.greengrocer.models.Message(
                    currentUser.getId(), currentChatPartnerId, subject, content);

            if (messageDAO.sendMessage(message)) {
                msgComposeArea.clear();
                loadChatMessages();
                loadConversations();
            } else {
                setMsgStatus("Failed to send.", "red");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            setMsgStatus("Error sending.", "red");
        }
    }

    private void updateUnreadCount() {
        if (unreadCountLabel == null || currentUser == null)
            return;

        try {
            int count = messageDAO.getUnreadCount(currentUser.getId());
            if (count > 0) {
                unreadCountLabel.setText(String.valueOf(count));
                unreadCountLabel.setStyle(
                        "-fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #EF4444; -fx-background-radius: 10; -fx-padding: 2 8;");
                unreadCountLabel.setVisible(true);
                unreadCountLabel.setManaged(true);
            } else {
                unreadCountLabel.setText("");
                unreadCountLabel.setVisible(false);
                unreadCountLabel.setManaged(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setMsgStatus(String text, String color) {
        if (msgStatusLabel != null) {
            msgStatusLabel.setText(text);
            msgStatusLabel.setStyle("-fx-text-fill: " + color + ";");
        }
    }

    private java.util.List<String> getAvailableTimeSlots(java.time.LocalDate date) {
        java.util.List<String> slots = new java.util.ArrayList<>();
        if (date == null)
            return slots;

        boolean isToday = date.equals(java.time.LocalDate.now());
        java.time.LocalTime now = java.time.LocalTime.now();

        for (int h = 9; h < 21; h += 2) {
            java.time.LocalTime slotEnd = java.time.LocalTime.of(h + 2, 0);

            if (isToday) {
                // strict validation: now < slotEnd - 30m
                java.time.LocalTime cutoff = slotEnd.minusMinutes(30);
                if (now.isAfter(cutoff)) {
                    continue;
                }
            }
            slots.add(String.format("%02d:00 - %02d:00", h, h + 2));
        }
        return slots;
    }

    // Helper class for async loading
    private static class ProductLoadResult {
        final java.util.List<Product> products;
        final java.util.List<Integer> favorites;

        public ProductLoadResult(java.util.List<Product> products, java.util.List<Integer> favorites) {
            this.products = products;
            this.favorites = favorites;
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
