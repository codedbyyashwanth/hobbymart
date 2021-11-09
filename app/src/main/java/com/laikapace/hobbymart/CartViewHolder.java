package com.laikapace.hobbymart;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class CartViewHolder extends RecyclerView.ViewHolder {
    ImageView CartProductImage;
    TextView CartProductName, CartProductPrice, CartProductQuantity;
    CardView Cartclose;

    public CartViewHolder(@NonNull View itemView) {
        super(itemView);

        CartProductImage = itemView.findViewById(R.id.cart_product_image);
        CartProductName = itemView.findViewById(R.id.cart_product_name);
        CartProductPrice = itemView.findViewById(R.id.cart_product_price);
        CartProductQuantity = itemView.findViewById(R.id.cart_product_quantity);
        Cartclose = itemView.findViewById(R.id.cart_close);
    }
}
