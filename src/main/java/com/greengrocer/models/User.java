package com.greengrocer.models;

/**
 * Represents a user of the application.
 */

public class User {
    private int id;
    private String username;
    private String password;
    private String role; // customer, carrier, owner
    private String firstName;
    private String lastName;
    private String address;
    private String phone;
    private double gPoints; // G Point loyalty points

    /**
     * Constructs a new User with initial GPoints set to 0.0.
     *
     * @param id        The unique identifier of the user.
     * @param username  The login username.
     * @param password  The login password (hashed).
     * @param role      The role of the user (e.g., "Customer", "Owner", "Carrier").
     * @param firstName The first name of the user.
     * @param lastName  The last name of the user.
     * @param address   The physical address of the user.
     * @param phone     The contact phone number.
     */
    public User(int id, String username, String password, String role, String firstName, String lastName,
            String address, String phone) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.phone = phone;
        this.gPoints = 0.0;
    }

    public User(int id, String username, String password, String role, String firstName, String lastName,
            String address, String phone, double gPoints) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.phone = phone;
        this.gPoints = gPoints;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getGPoints() {
        return gPoints;
    }

    public void setGPoints(double gPoints) {
        this.gPoints = gPoints;
    }
}
