package com.laikapace.hobbymart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ProductData extends AppCompatActivity {

    String id, pUrl, tag, phoneNumber, ProductName, ProductPrice;
    DatabaseReference reference, cartReference;
    ArrayList<MultipleImageData> sliderDataArrayList;
    SliderView sliderView;
    MultipleImageAdapter adapter;
    CardView Add, Remove;
    int counter = 1;
    TextView ProductNameView, ProductPriceView, ProductDescView, ProductQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_data);

        sliderView = findViewById(R.id.image_slider);
        sliderDataArrayList = new ArrayList<>();
        id = getIntent().getStringExtra("id");
        pUrl = getIntent().getStringExtra("url");
        tag = getIntent().getStringExtra("tag");
        phoneNumber = getIntent().getStringExtra("no");

        reference = FirebaseDatabase.getInstance().getReference(tag);
        cartReference = FirebaseDatabase.getInstance().getReference("Cart");

        ProductNameView = findViewById(R.id.product_title);
        ProductPriceView = findViewById(R.id.product_price);
        ProductDescView = findViewById(R.id.product_desc);

        Add = findViewById(R.id.more);
        Remove = findViewById(R.id.less);
        ProductQuantity = findViewById(R.id.product_quantity);

        GetData(id);
        
        Add.setOnClickListener(v2 -> {
            counter++;
            ProductQuantity.setText(counter + "");
            int newPrice = counter * Integer.parseInt(ProductPrice);
            ProductPriceView.setText("₹" + newPrice);
        });

        Remove.setOnClickListener(v3 -> {
            if(counter > 1) {
                counter--;
                ProductQuantity.setText(counter + "");
                int newPrice = counter * Integer.parseInt(ProductPrice);
                ProductPriceView.setText("₹" + newPrice);
            } else {
                Toast.makeText(ProductData.this, "Minimum Quantity is 1", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void Back(View view) {
        finish();
    }

    private void GetData(String id) {
        reference.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adapter = new MultipleImageAdapter(ProductData.this, sliderDataArrayList);
                sliderDataArrayList.add(new MultipleImageData(pUrl));
                ProductName = snapshot.child("title").getValue(String.class);
                ProductPrice = snapshot.child("price").getValue(String.class);
                ProductNameView.setText(ProductName);
                ProductPriceView.setText("₹" + ProductPrice);
                ProductDescView.setText(snapshot.child("description").getValue(String.class));
                for(DataSnapshot url: snapshot.child("gallery").getChildren()) {
                    String images = url.getValue(String.class);
                    sliderDataArrayList.add(new MultipleImageData(images));
                    adapter.notifyDataSetChanged();
                }
                sliderView.setAutoCycleDirection(SliderView.LAYOUT_DIRECTION_LTR);
                sliderView.setSliderAdapter(adapter);
                sliderView.setScrollTimeInSec(3);
                sliderView.setAutoCycle(true);
                sliderView.startAutoCycle();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void AddToCart(View view) {
        cartReference.child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean dataExist = false;
                if(snapshot.getChildrenCount() > 0) {
                    for(DataSnapshot data : snapshot.getChildren()) {
                        if(Objects.equals(data.child("id").getValue(String.class), id)) {
                            dataExist = true;
                        }
                    }

                    if(dataExist) {
                        Toast.makeText(ProductData.this, "This item is already in the cart", Toast.LENGTH_SHORT).show();
                    } else {
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("id", id);
                        hashMap.put("quantity", String.valueOf(counter));
                        cartReference.child(phoneNumber).child(id).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void unused) {
                                Snackbar snackbar = Snackbar
                                        .make(view, "Item is added to your cart", Snackbar.LENGTH_LONG)
                                        .setAction("Go to Cart", view1 -> startActivity(new Intent(ProductData.this, Cart.class)));
                                snackbar.getView().setBackgroundColor(Color.parseColor("#323232"));
                                snackbar.setActionTextColor(Color.parseColor("#FFFFFF"));
                                snackbar.setTextColor(Color.parseColor("#F4F9F9"));
                                snackbar.show();
                            }
                        });
                    }
                } else {
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", id);
                    hashMap.put("quantity", String.valueOf(counter));
                    cartReference.child(phoneNumber).child(id).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(@NonNull Void unused) {
                            Snackbar snackbar = Snackbar
                                    .make(view, "Item is added to your cart", Snackbar.LENGTH_LONG)
                                    .setAction("Go to Cart", view1 -> startActivity(new Intent(ProductData.this, Cart.class)));
                            snackbar.getView().setBackgroundColor(Color.parseColor("#323232"));
                            snackbar.setActionTextColor(Color.parseColor("#FFFFFF"));
                            snackbar.setTextColor(Color.parseColor("#F4F9F9"));
                            snackbar.show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}