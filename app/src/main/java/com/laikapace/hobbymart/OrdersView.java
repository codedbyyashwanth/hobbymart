package com.laikapace.hobbymart;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class OrdersView extends RecyclerView.ViewHolder {

    TextView cost, date;
    CardView forward;

    public OrdersView(@NonNull View itemView) {
        super(itemView);

        cost = itemView.findViewById(R.id.cost);
        date = itemView.findViewById(R.id.date);
        forward = itemView.findViewById(R.id.forward);
    }
}
