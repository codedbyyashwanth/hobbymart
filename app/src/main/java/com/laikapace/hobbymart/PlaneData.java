package com.laikapace.hobbymart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
import java.util.List;
import java.util.Objects;

public class PlaneData extends AppCompatActivity {

    String id, pUrl, tag, phoneNumber, ProductName, ProductPrice, Stock;
    DatabaseReference reference, cartReference;
    ArrayList<MultipleImageData> sliderDataArrayList;
    Button AddToCart;
    SliderView sliderView;
    MultipleImageAdapter adapter;
    CardView Add, Remove;
    int counter = 1;
    Spinner spinner;
    RelativeLayout priceLayout;
    String[] priceList, descList;
    ArrayList<CharSequence> prices;
    TextView ProductNameView, ProductPriceView, ProductDescView, ProductQuantity, PriceDesc;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plane_data);

        sliderView = findViewById(R.id.image_slider);
        sliderDataArrayList = new ArrayList<>();
        id = getIntent().getStringExtra("id");
        pUrl = getIntent().getStringExtra("url");
        tag = getIntent().getStringExtra("tag");
        phoneNumber = getIntent().getStringExtra("no");
        Stock = getIntent().getStringExtra("stock");
        prices = getIntent().getCharSequenceArrayListExtra("prices");


        reference = FirebaseDatabase.getInstance().getReference(tag);
        cartReference = FirebaseDatabase.getInstance().getReference("Cart");

        ProductNameView = findViewById(R.id.product_title);
        ProductPriceView = findViewById(R.id.product_price);
        ProductDescView = findViewById(R.id.product_desc);
        AddToCart = findViewById(R.id.add_to_cart);
        spinner = findViewById(R.id.price_option);
        priceLayout = findViewById(R.id.price_layout);
        priceLayout.setVisibility(View.GONE);
        PriceDesc = findViewById(R.id.desc);



        Add = findViewById(R.id.more);
        Remove = findViewById(R.id.less);
        ProductQuantity = findViewById(R.id.product_quantity);

        priceList = new String[prices.size() + 1];
        descList = new String[prices.size() + 1];
        priceList[0] = "Select Price";
        for (int i=0; i<prices.size()  ; i++) {
            HashMap<String, String> datas = (HashMap<String, String>) prices.get(i);
            priceList[i + 1] = "₹ " + datas.get("price");
            descList[i] = datas.get("desc");
        }

//        if (Stock.equalsIgnoreCase("yes")) {
//            AddToCart.setText("Add to Cart");
//            AddToCart.setEnabled(true);
//        } else {
//            AddToCart.setText("Out of Stock");
//            AddToCart.setEnabled(false);
//        }

        GetData(id);

        Add.setOnClickListener(v2 -> {
            counter++;
            ProductQuantity.setText(counter + "");
            int newPrice = counter * Integer.parseInt(ProductPrice);
            ProductPriceView.setText("₹ " + newPrice);
        });

        Remove.setOnClickListener(v3 -> {
            if(counter > 1) {
                counter--;
                ProductQuantity.setText(counter + "");
                int newPrice = counter * Integer.parseInt(ProductPrice);
                ProductPriceView.setText("₹ " + newPrice);
            } else {
                Toast.makeText(PlaneData.this, "Minimum Quantity is 1", Toast.LENGTH_SHORT).show();
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    PriceDesc.setVisibility(View.GONE);
                    priceLayout.setVisibility(View.GONE);
                }
                else {
                    PriceDesc.setVisibility(View.VISIBLE);
                    PriceDesc.setText(descList[position - 1]);
                    ProductPriceView.setText(priceList[position]);
                    priceLayout.setVisibility(View.VISIBLE);
                    ProductPrice = priceList[position].replace("₹ ", "");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void GetData(String id) {
        reference.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adapter = new MultipleImageAdapter(PlaneData.this, sliderDataArrayList);
                sliderDataArrayList.add(new MultipleImageData(pUrl));
                ProductName = snapshot.child("title").getValue(String.class);
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
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(PlaneData.this, android.R.layout.simple_spinner_item, priceList);
                adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void AddToCart(View view) {
        if (spinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Select the price", Toast.LENGTH_SHORT).show();
        } else {
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
                            Toast.makeText(PlaneData.this, "This item is already in the cart", Toast.LENGTH_SHORT).show();
                        } else {
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id", id);
                            hashMap.put("quantity", String.valueOf(counter));
                            hashMap.put("price", ProductPrice);

                            cartReference.child(phoneNumber).child(id).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(@NonNull Void unused) {
                                    Snackbar snackbar = Snackbar
                                            .make(view, "Item is added to your cart", Snackbar.LENGTH_LONG)
                                            .setAction("Go to Cart", view1 -> startActivity(new Intent(PlaneData.this, Cart.class)));
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
                        hashMap.put("price", ProductPrice);
                        cartReference.child(phoneNumber).child(id).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void unused) {
                                Snackbar snackbar = Snackbar
                                        .make(view, "Item is added to your cart", Snackbar.LENGTH_LONG)
                                        .setAction("Go to Cart", view1 -> startActivity(new Intent(PlaneData.this, Cart.class)));
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

    public void Back(View view) {
        finish();
    }
}