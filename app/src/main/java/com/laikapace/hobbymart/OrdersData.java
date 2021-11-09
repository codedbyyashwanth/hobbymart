package com.laikapace.hobbymart;

public class OrdersData {
     String date, totalCost, paymentID;

    public OrdersData(String date, String totalCost, String paymentID) {
        this.date = date;
        this.totalCost = totalCost;
        this.paymentID = paymentID;
    }

    public OrdersData() {
    }

    public String getPaymentID() {
        return paymentID;
    }

    public String getDate() {
        return date;
    }

    public String getTotalCost() {
        return totalCost;
    }
}
