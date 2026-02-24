<div align="center">

# 🥬 Greengrocer Java App

### A Full-Featured Grocery Store Management Platform

**Built with JavaFX · Powered by MySQL · Cross-Platform Ready**

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)](https://adoptium.net)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?logo=java&logoColor=white)](https://openjfx.io)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql&logoColor=white)](https://www.mysql.com)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20macOS%20%7C%20Linux-lightgrey)](.)

</div>

---

## 📖 About

Greengrocer Java App is a full-stack desktop application for managing a grocery store. It supports three different user roles — **Customer**, **Owner**, and **Carrier** — each with their own dedicated dashboard. The entire system is connected through a shared cloud-hosted MySQL database, so there is no need for any local database installation.

The application features real-time inventory management, order processing with delivery tracking, a loyalty rewards system (G-Points), discount coupons, PDF invoice generation, a built-in messaging system, and detailed sales analytics — all wrapped in a modern dark-themed UI.

---

## 📸 Screenshots

<!-- 
  Add your screenshots here. Place image files in a /screenshots folder and reference them like:
  ![Login Screen](screenshots/login.png)
-->

| Login Screen | Customer Dashboard | Owner Panel |
|:---:|:---:|:---:|
| *Coming soon* | *Coming soon* | *Coming soon* |

---

## 🚀 Setup Guide

Follow the steps below to get the application running on your machine. The instructions are provided separately for **Windows** and **macOS**.

---

### Step 1: Install JDK 21

The only software you need to install is **JDK (Java Development Kit) version 21** or higher.

<details>
<summary><b>🪟 Windows</b></summary>

1. Go to [Adoptium (Eclipse Temurin)](https://adoptium.net)
2. Select **Windows x64** and **JDK 21** (LTS)
3. Download the `.msi` installer
4. Run the installer and follow the on-screen instructions
   - ✅ Make sure **"Set JAVA_HOME variable"** is checked during installation
   - ✅ Make sure **"Add to PATH"** is checked
5. Verify the installation by opening **Command Prompt** and running:
   ```
   java -version
   ```
   You should see output containing `openjdk version "21.x.x"` or similar.

</details>

<details>
<summary><b>🍎 macOS</b></summary>

**Option A: Using Homebrew (Recommended)**
```bash
brew install openjdk@21
```

**Option B: Manual Installation**
1. Go to [Adoptium (Eclipse Temurin)](https://adoptium.net)
2. Select **macOS** and your architecture (**aarch64** for Apple Silicon, **x64** for Intel)
3. Download the `.pkg` installer
4. Run the installer and follow the on-screen instructions

**Verify the installation:**
```bash
java -version
```
You should see output containing `openjdk version "21.x.x"` or similar.

</details>

---

### Step 2: Clone the Repository

Open a terminal (Command Prompt on Windows, Terminal on macOS) and run:

```bash
git clone https://github.com/KeremIrfanoglu/Greengrocer-Java-App.git
```

---

### Step 3: Database Configuration

> ⚠️ **Important:** The database credentials (`db.properties`) are **not included** in this repository for security reasons. You need to request this file from the project owner.

**To request access:**
Contact [Kerem İrfanoğlu](https://github.com/KeremIrfanoglu) to receive the `db.properties` file.

**Once you receive the file:**

Place `db.properties` in the **root directory** of the project (the same folder as `pom.xml` and `run.bat`):

```
Greengrocer-Java-App/
├── db.properties       ← Place it here
├── pom.xml
├── run.bat
├── run.sh
├── src/
└── ...
```

---

### Step 4: Run the Application

<details>
<summary><b>🪟 Windows</b></summary>

Simply **double-click** `run.bat` in the project folder.

Alternatively, open **Command Prompt** in the project folder and run:
```
run.bat
```

> 💡 The script automatically detects your JDK installation — no manual `JAVA_HOME` setup required.

</details>

<details>
<summary><b>🍎 macOS</b></summary>

1. Open **Terminal**

2. Navigate to the project folder:
   ```bash
   cd ~/Desktop/Greengrocer-Java-App
   ```
   *(Replace the path with wherever you cloned the repository)*

3. Grant execution permission to the scripts (only needed once):
   ```bash
   chmod +x run.sh mvnw
   ```

4. Run the application:
   ```bash
   ./run.sh
   ```

> 💡 The script automatically detects your JDK installation — no manual `JAVA_HOME` setup required.

</details>

> **📝 Note:** The first launch takes approximately **1–2 minutes** as Maven downloads the required libraries. All subsequent launches will be much faster.

---

### Step 5: Login

Use any of the following test accounts to explore the application:

| Role | Username | Password | Description |
|------|----------|----------|-------------|
| 🏪 Owner | `own` | `own` | Full admin access — manage products, orders, carriers |
| 👤 Customer | `cust` | `cust` | Browse products, place orders, earn rewards |
| 🚚 Carrier | `carr` | `carr` | View and manage delivery assignments |

<details>
<summary><b>Additional test accounts</b></summary>

| Role | Username | Password |
|------|----------|----------|
| 👤 Customer | `john` | `123456` |
| 👤 Customer | `emily` | `123456` |
| 🚚 Carrier | `carrier1` | `123456` |
| 🚚 Carrier | `carrier2` | `123456` |

</details>

---

## ✨ Features

### 👤 Customer Panel
- 🛒 Browse products by category with real-time stock & pricing
- 🛍️ Add to cart, adjust quantities, and place orders
- ⭐ Favorite products for quick access
- 💰 Earn & spend **G-Points** (loyalty rewards system)
- 🎟️ Apply discount coupons at checkout
- 📄 Download **PDF invoices** for completed orders
- 💬 Built-in messaging with the store owner
- 📊 View personal order history and spending analytics

### 🏪 Owner Panel
- 📦 Full product management — add, edit, and delete products with images
- 🖼️ Upload product images (auto-compressed to 500×500px for performance)
- 📊 Sales analytics with revenue charts and profit/loss tracking
- 🚚 Assign carriers to pending orders
- 👥 Manage carriers and supplier records
- 🎟️ Create and distribute discount coupons
- 💬 Messaging system with customers and carriers
- 📈 Inventory tracking with **low-stock alerts**

### 🚚 Carrier Panel
- 📋 View and accept delivery assignments
- 🔄 Update delivery status in real-time
- ⭐ View customer ratings and feedback
- 💬 Communicate with the owner
- 📊 Track delivery history and performance metrics

---

## 🔧 Technical Highlights

| Feature | Description |
|---------|-------------|
| 🖼️ **Lazy Image Loading** | Product images are loaded asynchronously in background threads — the UI never freezes |
| 🗜️ **Auto Image Compression** | Uploaded images are automatically resized to 500×500px and compressed to JPEG 70% quality |
| 🔐 **Password Security** | All passwords are stored with salted SHA-256 hashing |
| ☁️ **Cloud Database** | MySQL hosted on Railway — no local database installation required |
| 🖥️ **Cross-Platform** | Runs on Windows, macOS, and Linux with auto JDK detection |
| 📄 **PDF Generation** | Professional invoices generated with OpenPDF |
| 🏗️ **MVC Architecture** | Clean separation of concerns with DAO pattern for database operations |

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Java 21 |
| **UI Framework** | JavaFX 21 (FXML + CSS) |
| **Database** | MySQL 8.0 (Cloud — Railway) |
| **PDF Generation** | OpenPDF 2.0 |
| **Build Tool** | Apache Maven (via included wrapper — no installation needed) |
| **Architecture** | MVC + DAO Pattern |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│                    Presentation Layer                │
│            JavaFX UI (FXML Views + CSS)             │
├─────────────────────────────────────────────────────┤
│                    Controller Layer                  │
│    CustomerController · OwnerController · Carrier   │
│    LoginController · RegisterController             │
├─────────────────────────────────────────────────────┤
│                  Data Access Layer (DAO)             │
│   ProductDAO · OrderDAO · UserDAO · CartDAO · ...   │
├─────────────────────────────────────────────────────┤
│                   Database Adapter                   │
│               JDBC · Connection Pooling             │
├─────────────────────────────────────────────────────┤
│                  MySQL Database (Cloud)              │
│                   Railway Platform                   │
└─────────────────────────────────────────────────────┘
```

---

## 📁 Project Structure

```
Greengrocer-Java-App/
│
├── src/main/java/com/greengrocer/
│   ├── Main.java                       # Application entry point
│   │
│   ├── controllers/                    # UI Controllers (MVC)
│   │   ├── CustomerController.java     # Customer dashboard & shopping
│   │   ├── OwnerController.java        # Admin panel & store management
│   │   ├── CarrierController.java      # Delivery management
│   │   ├── LoginController.java        # Authentication
│   │   └── RegisterController.java     # New user registration
│   │
│   ├── dao/                            # Data Access Objects
│   │   ├── DatabaseAdapter.java        # Database connection manager
│   │   ├── ProductDAO.java             # Product CRUD + lazy image loading
│   │   ├── OrderDAO.java               # Order processing & history
│   │   ├── UserDAO.java                # User management & authentication
│   │   ├── CartDAO.java                # Shopping cart operations
│   │   ├── FavoritesDAO.java           # Customer favorites
│   │   └── ...                         # Reports, Coupons, etc.
│   │
│   ├── models/                         # Data Models
│   │   ├── Product.java                # Product with image caching
│   │   ├── User.java                   # User entity
│   │   ├── Order.java                  # Order entity
│   │   ├── CartItem.java               # Shopping cart item
│   │   └── ...                         # Supplier, Coupon, etc.
│   │
│   ├── util/                           # Utilities
│   │   ├── ImageCompressor.java        # Auto image compression (500×500)
│   │   ├── InvoiceGenerator.java       # PDF invoice generation
│   │   ├── PasswordUtils.java          # Secure password hashing
│   │   ├── StyleHelper.java            # UI theming & styling
│   │   └── StyledAlert.java            # Custom styled dialogs
│   │
│   └── setup/                          # Database Setup
│       ├── SetupDatabase.java          # Creates tables & schema
│       └── SeedData.java               # Populates initial test data
│
├── src/main/resources/com/greengrocer/
│   ├── views/                          # FXML layout files
│   │   ├── login.fxml
│   │   ├── register.fxml
│   │   ├── customer.fxml
│   │   ├── owner.fxml
│   │   └── carrier.fxml
│   └── views/styles.css                # Application stylesheet
│
├── pom.xml                             # Maven build configuration
├── run.bat                             # Windows launcher (auto-detects JDK)
├── run.sh                              # macOS/Linux launcher (auto-detects JDK)
├── mvnw / mvnw.cmd                     # Maven wrapper (no Maven install needed)
└── db.properties.example               # Database config template
```

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

```
MIT License — Copyright (c) 2025 Kerem İrfanoğlu

Permission is hereby granted, free of charge, to any person obtaining a copy of this 
software to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
copies, subject to the following conditions: the above copyright notice shall be 
included in all copies. The software is provided "as is", without warranty of any kind.
```
