package com.laikapace.hobbymart;

public class ProductCardInfo {
    private String title, price, description, id, url;

    public ProductCardInfo(String title, String price, String description, String id, String url) {
        this.title = title;
        this.price = price;
        this.description = description;
        this.id = id;
        this.url = url;
    }

    public ProductCardInfo() {
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
