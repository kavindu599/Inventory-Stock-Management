package com.yourcompany.itemcrud.model;

public class PerishableItem extends Item {
    private String expiryDate;

    public PerishableItem(String id, String name, int quantity, String expiryDate) {
        super(id, name, quantity);
        this.expiryDate = expiryDate;
    }

    @Override
    public String display() {
        return String.format("ID: %s, Name: %s, Quantity: %d, Expiry Date: %s",
                id, name, quantity, expiryDate);
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
}