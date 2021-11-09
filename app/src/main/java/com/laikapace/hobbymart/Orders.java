package com.laikapace.hobbymart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Orders extends AppCompatActivity {

    FirebaseUser user;
    String phoneNumber;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    FirebaseRecyclerOptions<OrdersData> options;
    FirebaseRecyclerAdapter<OrdersData, OrdersView> adapter;
    DatabaseReference ordersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        recyclerView = findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        phoneNumber = user.getPhoneNumber();

        ordersReference = FirebaseDatabase.getInstance().getReference("Orders").child(phoneNumber);
        LoadData();
    }

    private void LoadData() {
        options = new FirebaseRecyclerOptions.Builder<OrdersData>().setQuery(ordersReference, OrdersData.class).build();
        adapter = new FirebaseRecyclerAdapter<OrdersData, OrdersView>(options) {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull OrdersView holder, int position, @NonNull OrdersData model) {
                holder.cost.setText("₹ " + model.getTotalCost());
                holder.date.setText("Ordered on: " + model.getDate());
                holder.forward.setOnClickListener(clickView -> {
                    Intent intent = new Intent(Orders.this, OrderProducts.class);
                    intent.putExtra("key", model.getPaymentID());
                    startActivity(intent);
                });
            }

            @NonNull
            @Override
            public OrdersView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_card, parent, false);
                return new OrdersView(view);
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    public void Back(View view) {
        finish();
    }

    public void Cart(View view) {
        startActivity(new Intent(Orders.this, Cart.class));
    }
}