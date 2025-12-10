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
    private ObservableList<Product> productList;
    private ObservableList<CartItem> cartList;
    private ObservableList<Order> orderList;

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label statusLabel;

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
    private javafx.scene.control.ComboBox<String> filterTypeCombo;
    @FXML
    private javafx.scene.control.ComboBox<String> sortCombo;

    @FXML
    private TextField quantityField;

    private ObservableList<Product> allProducts; // Keep original list for filtering

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

    public CustomerController() {
        this.productDAO = new ProductDAO();
        this.orderDAO = new OrderDAO();
        this.cartList = FXCollections.observableArrayList();
        this.orderList = FXCollections.observableArrayList();
    }

    public void initData(User user) {
        this.currentUser = user;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + user.getFirstName());
        }

        // Shop Setup
        colShopName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colShopType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colShopPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colShopStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // Setup Filter/Sort ComboBoxes
        filterTypeCombo.setItems(FXCollections.observableArrayList("All", "Vegetable", "Fruit"));
        filterTypeCombo.setValue("All");
        sortCombo.setItems(FXCollections.observableArrayList("Default", "Name (A-Z)", "Name (Z-A)", "Price (Low-High)",
                "Price (High-Low)"));
        sortCombo.setValue("Default");

        loadProducts();

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

        double total = 0;
        for (CartItem item : cartList) {
            total += item.getTotal();
        }

        try {
            // Note: createOrder should define default Carrier or NULL. Assuming NULL for
            // open market.
            if (orderDAO.createOrder(currentUser.getId(), cartList, total)) {
                statusLabel.setText("Order placed successfully!");
                cartList.clear();
                updateCartTotal();
                loadOrders();
                loadProducts(); // Stock decreased
            } else {
                statusLabel.setText("Order failed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Database error during checkout.");
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

        for (Product p : allProducts) {
            if ("All".equals(filterType) || p.getType().equals(filterType)) {
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
}
