package com.laikapace.hobbymart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class Kit extends AppCompatActivity {

    RecyclerView recyclerView;
    GridLayoutManager layoutManager;
    FirebaseRecyclerOptions<ProductCardInfo> options;
    FirebaseRecyclerAdapter<ProductCardInfo, CardViewHolder> adapter;
    DatabaseReference productsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kit);

        String tag = getIntent().getStringExtra("tag");
        recyclerView = findViewById(R.id.recyclerview);
        layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        LoadData(tag);
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
                holder.ProductPrice.setText("₹" + model.getPrice());
                Picasso.get().load(model.getUrl()).into(holder.ProductImage);

                holder.AddToCart.setOnClickListener(v -> {
                    BottomSheetDialog dialog = new BottomSheetDialog(Kit.this, R.style.AppBottomSheetDialogTheme);
                    View view = getLayoutInflater().inflate(R.layout.quantity_selector, null);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setContentView(view);

                    final int[] counter = {1};

                    ImageView ProductImage;
                    CardView Add, Remove, Close;
                    TextView ProductPrice, ProductQuantity, ProductName, Description;
                    Button AddToCart = view.findViewById(R.id.add_to_cart);

                    Add = view.findViewById(R.id.more);
                    Remove = view.findViewById(R.id.less);
                    Close = view.findViewById(R.id.close);
                    Description = view.findViewById(R.id.description);
                    ProductImage  = view.findViewById(R.id.product_image);
                    ProductName = view.findViewById(R.id.product_name);
                    ProductPrice = view.findViewById(R.id.product_price);
                    ProductQuantity = view.findViewById(R.id.product_quantity);

                    Add.setOnClickListener(v2 -> {
                        counter[0]++;
                        ProductQuantity.setText(counter[0] + "");
                        int newPrice = counter[0] * Integer.parseInt(model.getPrice());
                        ProductPrice.setText("₹" + newPrice);
                    });

                    Remove.setOnClickListener(v3 -> {
                        if(counter[0] > 1) {
                            counter[0]--;
                            ProductQuantity.setText(counter[0] + "");
                            int newPrice = counter[0] * Integer.parseInt(model.getPrice());
                            ProductPrice.setText("₹" + newPrice);
                        } else {
                            Toast.makeText(Kit.this, "Minimum Quantity is 1", Toast.LENGTH_SHORT).show();
                        }
                    });

                    Close.setOnClickListener(v4 -> dialog.dismiss() );
                    Description.setText(model.getDescription());

                    Picasso.get().load(model.getUrl()).into(ProductImage);
                    ProductPrice.setText("₹" + model.getPrice());
                    ProductQuantity.setText("1");
                    ProductName.setText(model.getTitle());

                    AddToCart.setOnClickListener(v1 -> {
                        if(true) {

                        }
                    });

                    dialog.show();
                });
            }

            @NonNull
            @Override
            public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_card, parent, false);
                return new CardViewHolder(view);
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    public void Back(View view) {
        finish();
    }

    public void Cart(View view) {
        startActivity(new Intent(this, Cart.class));
    }
}