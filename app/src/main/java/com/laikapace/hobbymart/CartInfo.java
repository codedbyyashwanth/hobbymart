package com.laikapace.hobbymart;

public class CartInfo {
     String id, quantity, price;

    public CartInfo(String id, String quantity) {
        this.id = id;
        this.quantity = quantity;
    }

    public CartInfo() {
    }

    public String getPrice() {
        return price;
    }

    public String getId() {
        return id;
    }

    public String getQuantity() {
        return quantity;
    }

}
