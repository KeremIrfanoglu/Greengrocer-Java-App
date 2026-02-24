package com.greengrocer.models;

/**
 * Represents a supplier who provides products to the store.
 */
public class Supplier {
    private int id;
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private String suppliedProductType;

    /**
     * Constructs a new Supplier.
     *
     * @param id                  The unique identifier of the supplier.
     * @param name                The name of the supplier.
     * @param contactPerson       The name of the contact person.
     * @param email               The email address of the supplier.
     * @param phone               The phone number of the supplier.
     * @param address             The physical address of the supplier.
     * @param suppliedProductType The type of product typically supplied.
     */
    public Supplier(int id, String name, String contactPerson, String email, String phone, String address,
            String suppliedProductType) {
        this.id = id;
        this.name = name;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.suppliedProductType = suppliedProductType;
    }

    /**
     * Constructs a new Supplier without ID (for insertion).
     */
    public Supplier(String name, String contactPerson, String email, String phone, String address,
            String suppliedProductType) {
        this(-1, name, contactPerson, email, phone, address, suppliedProductType);
    }

    /**
     * Constructs a new Supplier (Backward Compatibility).
     */
    public Supplier(int id, String name, String contactPerson, String email, String phone, String address) {
        this(id, name, contactPerson, email, phone, address, null);
    }

    /**
     * Constructs a new Supplier (Backward Compatibility).
     */
    public Supplier(String name, String contactPerson, String email, String phone, String address) {
        this(-1, name, contactPerson, email, phone, address, null);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSuppliedProductType() {
        return suppliedProductType;
    }

    public void setSuppliedProductType(String suppliedProductType) {
        this.suppliedProductType = suppliedProductType;
    }
}
