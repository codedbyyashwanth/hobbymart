package com.laikapace.hobbymart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

public class Plane extends AppCompatActivity {

    FirebaseUser user;
    String phoneNumber;
    RecyclerView recyclerView;
    GridLayoutManager layoutManager;
    FirebaseRecyclerOptions<ProductCardInfo> options;
    FirebaseRecyclerAdapter<ProductCardInfo, CardViewHolder> adapter;
    DatabaseReference productsReference, cartReference;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plane);

        String tag = "planes";
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        recyclerView = findViewById(R.id.recyclerview);
        layoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setVisibility(View.GONE);

        LoadData(tag);

        user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        phoneNumber = user.getPhoneNumber();
        cartReference = FirebaseDatabase.getInstance().getReference("Cart");
    }

    private void LoadData(String tag) {
        productsReference = FirebaseDatabase.getInstance().getReference(tag);
        options =  new FirebaseRecyclerOptions.Builder<ProductCardInfo>().setQuery(productsReference, ProductCardInfo.class).build();
        adapter = new FirebaseRecyclerAdapter<ProductCardInfo, CardViewHolder>(options) {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull CardViewHolder holder, int position, @NonNull ProductCardInfo model) {
                if(model.getTitle().length() >= 35) {
                    String newText = model.getTitle().substring(0, 35) + "...";
                    holder.ProductName.setText(newText);
                } else {
                    holder.ProductName.setText(model.getTitle());
                }
                if (model.getPrices().size() > 1)
                    holder.ProductPrice.setText("₹ " + model.getPrices().get(0).get("price") + " - ₹ " + model.getPrices().get(model.getPrices().size() - 1).get("price"));
                else
                    holder.ProductPrice.setText("₹ " + model.getPrices().get(0).get("price"));

                Picasso.get().load(model.getUrl()).into(holder.ProductImage);
                holder.ProductDesc.setText(model.getDescription());

                holder.ProductDesc.setOnClickListener(v -> {
                    Intent intent = new Intent(Plane.this, PlaneData.class);
                    intent.putExtra("id", model.getId());
                    intent.putExtra("url", model.getUrl());
                    intent.putExtra("tag", "planes");
                    intent.putExtra("prices", model.getPrices());
                    intent.putExtra("price", model.getwBPrice());
                    intent.putExtra("no", phoneNumber);
                    intent.putExtra("stock", model.getStock());
                    startActivity(intent);
                });

                holder.ProductImage.setOnClickListener(v -> {
                    Intent intent = new Intent(Plane.this, PlaneData.class);
                    intent.putExtra("id", model.getId());
                    intent.putExtra("url", model.getUrl());
                    intent.putExtra("tag", "planes");
                    intent.putExtra("prices", model.getPrices());
                    intent.putExtra("price", model.getwBPrice());
                    intent.putExtra("no", phoneNumber);
                    intent.putExtra("stock", model.getStock());
                    startActivity(intent);
                });

                holder.ProductName.setOnClickListener(v -> {
                    Intent intent = new Intent(Plane.this, PlaneData.class);
                    intent.putExtra("id", model.getId());
                    intent.putExtra("url", model.getUrl());
                    intent.putExtra("tag", "planes");
                    intent.putExtra("prices", model.getPrices());
                    intent.putExtra("price", model.getwBPrice());
                    intent.putExtra("no", phoneNumber);
                    intent.putExtra("stock", model.getStock());
                    startActivity(intent);
                });

                holder.AddToCart.setOnClickListener(v -> {
                    Intent intent = new Intent(Plane.this, PlaneData.class);
                    intent.putExtra("id", model.getId());
                    intent.putExtra("url", model.getUrl());
                    intent.putExtra("tag", "planes");
                    intent.putExtra("prices", model.getPrices());
                    intent.putExtra("price", model.getwBPrice());
                    intent.putExtra("no", phoneNumber);
                    intent.putExtra("stock", model.getStock());
                    startActivity(intent);
//                    BottomSheetDialog dialog = new BottomSheetDialog(Plane.this, R.style.AppBottomSheetDialogTheme);
//                    @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.quantity_selector, null);
//                    dialog.setCanceledOnTouchOutside(false);
//                    dialog.setContentView(view);
//
//                    final int[] counter = {1};
//
//                    ImageView ProductImage;
//                    CardView Add, Remove, Close;
//                    TextView ProductPrice, ProductQuantity, ProductName, Description;
//                    Button AddToCart = view.findViewById(R.id.add_to_cart);
//                    Button ViewDetails = view.findViewById(R.id.view_details);
//
//                    Add = view.findViewById(R.id.more);
//                    Remove = view.findViewById(R.id.less);
//                    Close = view.findViewById(R.id.close);
//                    Description = view.findViewById(R.id.description);
//                    ProductImage  = view.findViewById(R.id.product_image);
//                    ProductName = view.findViewById(R.id.product_name);
//                    ProductPrice = view.findViewById(R.id.product_price);
//                    ProductQuantity = view.findViewById(R.id.product_quantity);
//
//                    Add.setOnClickListener(v2 -> {
//                        counter[0]++;
//                        ProductQuantity.setText(counter[0] + "");
//                        int newPrice = counter[0] * Integer.parseInt(model.getwBPrice());
//                        ProductPrice.setText("Cost: ₹" + newPrice);
//                    });
//
//                    Remove.setOnClickListener(v3 -> {
//                        if(counter[0] > 1) {
//                            counter[0]--;
//                            ProductQuantity.setText(counter[0] + "");
//                            int newPrice = counter[0] * Integer.parseInt(model.getwBPrice());
//                            ProductPrice.setText("Cost: ₹" + newPrice);
//                        } else {
//                            Toast.makeText(Plane.this, "Minimum Quantity is 1", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//
//                    Close.setOnClickListener(v4 -> dialog.dismiss() );
//                    Description.setText(model.getDescription());

//                    Picasso.get().load(model.getUrl()).into(ProductImage);
//                    ProductPrice.setText("Cost: ₹" + model.getwBPrice());
//                    ProductQuantity.setText("1");
//                    ProductName.setText(model.getTitle());

//                    if (model.getStock().equalsIgnoreCase("yes")) {
//                        AddToCart.setText("Add to Cart");
//                        AddToCart.setEnabled(true);
//                    } else {
//                        AddToCart.setText("Out of Stock");
//                        AddToCart.setEnabled(false);
//                    }

//                    ViewDetails.setOnClickListener(v12 -> {
//                        Intent intent = new Intent(Plane.this, PlaneData.class);
//                        intent.putExtra("id", model.getId());
//                        intent.putExtra("url", model.getUrl());
//                        intent.putExtra("tag", tag);
//                        intent.putExtra("price", model.getwBPrice());
//                        intent.putExtra("no", phoneNumber);
//                        intent.putExtra("stock", model.getStock());
//                        startActivity(intent);
//                    });

//                    AddToCart.setOnClickListener(v1 -> {
//                        cartReference.child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                boolean dataExist = false;
//                                if(snapshot.getChildrenCount() > 0) {
//                                    for(DataSnapshot data : snapshot.getChildren()) {
//                                        if(Objects.equals(data.child("id").getValue(String.class), model.getId())) {
//                                            dataExist = true;
//                                        }
//                                    }
//
//                                    if(dataExist) {
//                                        Toast.makeText(Plane.this, "This item is already in the cart", Toast.LENGTH_SHORT).show();
//                                    } else {
//                                        HashMap<String, String> hashMap = new HashMap<>();
//                                        hashMap.put("id", model.getId());
//                                        hashMap.put("quantity", String.valueOf(counter[0]));
//                                        cartReference.child(phoneNumber).child(model.getId()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                            @Override
//                                            public void onSuccess(@NonNull Void unused) {
//                                                dialog.dismiss();
//                                                Snackbar snackbar = Snackbar
//                                                        .make(v, "Item is added to your cart", Snackbar.LENGTH_LONG)
//                                                        .setAction("Go to Cart", view1 -> startActivity(new Intent(Plane.this, Cart.class)));
//                                                snackbar.getView().setBackgroundColor(Color.parseColor("#323232"));
//                                                snackbar.setActionTextColor(Color.parseColor("#FFFFFF"));
//                                                snackbar.setTextColor(Color.parseColor("#F4F9F9"));
//                                                snackbar.show();
//                                            }
//                                        });
//                                    }
//                                } else {
//                                    HashMap<String, String> hashMap = new HashMap<>();
//                                    hashMap.put("id", model.getId());
//                                    hashMap.put("quantity", String.valueOf(counter[0]));
//                                    cartReference.child(phoneNumber).child(model.getId()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(@NonNull Void unused) {
//                                            dialog.dismiss();
//                                            Snackbar snackbar = Snackbar
//                                                    .make(v, "Item is added to your cart", Snackbar.LENGTH_LONG)
//                                                    .setAction("Go to Cart", view1 -> startActivity(new Intent(Plane.this, Cart.class)));
//                                            snackbar.getView().setBackgroundColor(Color.parseColor("#323232"));
//                                            snackbar.setActionTextColor(Color.parseColor("#FFFFFF"));
//                                            snackbar.setTextColor(Color.parseColor("#F4F9F9"));
//                                            snackbar.show();
//                                        }
//                                    });
//                                }
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//
//                            }
//                        });
//                    });
//
//                    dialog.show();
                });
            }

            @NonNull
            @Override
            public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.plane_card, parent, false);
                return new CardViewHolder(view);
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    public void Back(View view) {
        finish();
    }

    public void Cart(View view) {
        startActivity(new Intent(Plane.this, Cart.class));
    }
}