package com.laikapace.hobbymart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class OrderProducts extends AppCompatActivity {

    RecyclerView recyclerView;
    GridLayoutManager manager;
    FirebaseUser user;
    String phoneNumber, key, totalCost;
    DatabaseReference orderReference, profileReference, productReference, statusMessageRef;
    FirebaseRecyclerOptions<CartInfo> options;
    FirebaseRecyclerAdapter<CartInfo, CartViewHolder> adapter;
    int PreviewCost = 0;
    TextView totalView, addressView, deliveryStatusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_products);

        user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        phoneNumber = user.getPhoneNumber();

        key = getIntent().getStringExtra("key");
        totalCost = getIntent().getStringExtra("totalCost");

        recyclerView = findViewById(R.id.recyclerview);
        manager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(manager);

        totalView = findViewById(R.id.total_price);
        addressView = findViewById(R.id.address);
        deliveryStatusView = findViewById(R.id.delivery_status);

        statusMessageRef = FirebaseDatabase.getInstance().getReference("Orders").child(phoneNumber).child(key);

        LoadData();
        GetAddress();

        StatusData();
    }

    private void StatusData() {
        statusMessageRef.child("delivery_message").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    deliveryStatusView.setText(snapshot.getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void GetAddress() {
        profileReference = FirebaseDatabase.getInstance().getReference("Users").child(phoneNumber);
        profileReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String address = snapshot.child("address").child("location").getValue(String.class) + "\n" + snapshot.child("address").child("city").getValue(String.class)
                        + "\n" + snapshot.child("address").child("state").getValue(String.class);
                addressView.setText(address);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void LoadData() {
        orderReference = FirebaseDatabase.getInstance().getReference("Orders").child(phoneNumber).child(key).child("productData");
        productReference = FirebaseDatabase.getInstance().getReference("Products");
        options = new FirebaseRecyclerOptions.Builder<CartInfo>().setQuery(orderReference, CartInfo.class).build();
        adapter = new FirebaseRecyclerAdapter<CartInfo, CartViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CartViewHolder holder, int position, @NonNull CartInfo model) {
                String id = model.getId();

                productReference.child(id).addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name, price, url;
                        name = snapshot.child("title").getValue(String.class);
                        price = snapshot.child("price").getValue(String.class);
                        url = snapshot.child("url").getValue(String.class);


                        assert price != null;
                        int total = Integer.parseInt(price) * Integer.parseInt(model.getQuantity());
                        PreviewCost += total;

                        holder.CartProductPrice.setText("₹ " + total);
                        holder.CartProductQuantity.setText("Qty: " + model.getQuantity());
                        Picasso.get().load(url).into(holder.CartProductImage);

                        assert name != null;
                        if(name.length() >= 25) {
                            String newText = name.substring(0, 25) + "...";
                            holder.CartProductName.setText(newText);
                        } else {
                            holder.CartProductName.setText(name);
                        }

                        totalView.setText("₹ " + totalCost);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_products, parent, false);
                return new CartViewHolder(view);
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    public void Back(View view) {
        finish();
    }
}
