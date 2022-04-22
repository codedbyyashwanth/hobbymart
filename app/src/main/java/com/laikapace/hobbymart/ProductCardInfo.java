package com.laikapace.hobbymart;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class ProductCardInfo {
    private String title, price, description, id, url, stock, wBPrice, battery, bPrice;
    ArrayList<String> gallery;
    ArrayList<HashMap<String, String>> prices;

    public ProductCardInfo(String title, String bPrice, String description, String id, String url, String stock) {
        this.title = title;
        this.bPrice = bPrice;
        this.description = description;
        this.id = id;
        this.url = url;
        this.stock = stock;
    }



    public ProductCardInfo() {
    }

    public ArrayList<HashMap<String, String>> getPrices() {
        return prices;
    }

    public ArrayList<String> getGallery() {
        return gallery;
    }

    public String getwBPrice() {
        return wBPrice;
    }

    public String getbPrice() {
        return bPrice;
    }

    public String getBattery() {
        return battery;
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
