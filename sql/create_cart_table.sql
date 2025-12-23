-- Persistent Cart Table
-- Run this in MySQL to create the cart table

CREATE TABLE IF NOT EXISTS Cart (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity DOUBLE NOT NULL,
    added_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES UserInfo(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES ProductInfo(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_product (user_id, product_id)
);
