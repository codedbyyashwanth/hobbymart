package com.laikapace.hobbymart;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class CardViewHolder extends RecyclerView.ViewHolder {
        ImageView ProductImage;
        CardView AddToCart;
        TextView ProductName, ProductPrice, ProductDesc;

        public CardViewHolder(@NonNull View itemView) {
                super(itemView);

                ProductImage = itemView.findViewById(R.id.product_image);
                ProductName = itemView.findViewById(R.id.product_name);
                ProductPrice = itemView.findViewById(R.id.product_price);
                AddToCart = itemView.findViewById(R.id.add_to_cart);
                try {
                        ProductDesc = itemView.findViewById(R.id.product_desc);
                } catch (Exception e) {
                        // Do Nothing
                }
        }
}
