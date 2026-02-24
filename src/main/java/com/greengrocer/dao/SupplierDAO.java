package com.greengrocer.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.greengrocer.models.Supplier;

/**
 * Data Access Object for Supplier management.
 * Handles CRUD operations for supplier records.
 * 
 * <p>
 * Suppliers provide products to the greengrocer and this DAO
 * manages their contact information and business details.
 * </p>
 * 
 * @author Group10
 * @version 1.0
 * @see Supplier
 */
public class SupplierDAO {

    public boolean addSupplier(Supplier supplier) throws SQLException {
        String sql = "INSERT INTO Suppliers (name, contact_person, email, phone, address) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, supplier.getName());
            pstmt.setString(2, supplier.getContactPerson());
            pstmt.setString(3, supplier.getEmail());
            pstmt.setString(4, supplier.getPhone());
            pstmt.setString(5, supplier.getAddress());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean updateSupplier(Supplier supplier) throws SQLException {
        String sql = "UPDATE Suppliers SET name=?, contact_person=?, email=?, phone=?, address=? WHERE id=?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, supplier.getName());
            pstmt.setString(2, supplier.getContactPerson());
            pstmt.setString(3, supplier.getEmail());
            pstmt.setString(4, supplier.getPhone());
            pstmt.setString(5, supplier.getAddress());
            pstmt.setInt(6, supplier.getId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteSupplier(int id) throws SQLException {
        String sql = "DELETE FROM Suppliers WHERE id=?";
        try (Connection conn = DatabaseAdapter.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Supplier> getAllSuppliers() throws SQLException {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM Suppliers";
        try (Connection conn = DatabaseAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                suppliers.add(new Supplier(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("contact_person"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address")));
            }
        }
        return suppliers;
    }
}
