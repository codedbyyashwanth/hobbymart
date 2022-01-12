package com.laikapace.hobbymart;

public class ProductCardInfo {
    private String title, price, description, id, url, stock;


    public ProductCardInfo(String title, String price, String description, String id, String url, String stock) {
        this.title = title;
        this.price = price;
        this.description = description;
        this.id = id;
        this.url = url;
        this.stock = stock;
    }

    public ProductCardInfo() {
    }

    public String getStock() {
        return stock;
    }

    public String getTitle() {
        return title;
    }

    public String getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }
}
