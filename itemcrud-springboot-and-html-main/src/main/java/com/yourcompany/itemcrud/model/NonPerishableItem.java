package com.yourcompany.itemcrud.model;

public class NonPerishableItem extends Item {

    public NonPerishableItem(String id, String name, int quantity) {
        super(id, name, quantity);
    }

    @Override
    public String display() {
        return String.format("ID: %s, Name: %s, Quantity: %d", id, name, quantity);
    }

    // This is needed for Jackson serialization
    public String getExpiryDate() {
        return null;
    }
}