package com.yourcompany.itemcrud.model;

public abstract class Item {


    protected String id;
    protected String name;
    protected int quantity;




    public Item(String id, String name, int quantity) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
    }

    public abstract String display();
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

}

