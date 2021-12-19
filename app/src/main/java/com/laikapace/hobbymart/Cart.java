package com.laikapace.hobbymart;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.razorpay.Checkout;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class Cart extends AppCompatActivity implements PaymentResultWithDataListener {

    RecyclerView recyclerView;
    GridLayoutManager manager;
    FirebaseUser user;
    String phoneNumber;
    DatabaseReference cartReference, productReference, profileReference, orderReference, discountRef;
    FirebaseRecyclerOptions<CartInfo> options;
    FirebaseRecyclerAdapter<CartInfo, CartViewHolder> adapter;
    int PreviewCost = 0, TotalCost = 0, DeliveryCost = 49, Discount = 0;
    TextView PriceView, TotalPriceView, DeliveryView, DiscountView, OrderPrice, Empty;
    ProgressBar progressBar;
    RelativeLayout layout, layout1, addressLayout;
    TextView applyCoupon;
    Button placeOrder, Goto;
    EditText CouponCode;
    private String address1, address2, city, state, pincode, email;
    ArrayList<HashMap<String, String>> arrayList;
    boolean couponApplied = false, discount = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        progressBar = findViewById(R.id.progressBar);
        PriceView = findViewById(R.id.price);
        TotalPriceView = findViewById(R.id.total_price);
        DeliveryView = findViewById(R.id.delivery_charges);
        DiscountView = findViewById(R.id.discount);
        OrderPrice = findViewById(R.id.total_order_price);
        Empty = findViewById(R.id.empty);
        layout = findViewById(R.id.pricing_layout);
        layout1 = findViewById(R.id.coupon_layout);
        applyCoupon = findViewById(R.id.apply_coupon);
        placeOrder = findViewById(R.id.place_order);
        Goto = findViewById(R.id.goto_btn);
        CouponCode = findViewById(R.id.coupon_code);
        addressLayout = findViewById(R.id.address_layout);

        layout.setVisibility(View.GONE);
        layout1.setVisibility(View.GONE);
        addressLayout.setVisibility(View.GONE);
        applyCoupon.setVisibility(View.GONE);
        placeOrder.setEnabled(false);

        recyclerView = findViewById(R.id.recyclerview);
        manager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(manager);

        arrayList = new ArrayList<>();

        user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        phoneNumber = user.getPhoneNumber();

        discountRef = FirebaseDatabase.getInstance().getReference("quiz").child("discount").child(phoneNumber);
        cartReference = FirebaseDatabase.getInstance().getReference("Cart").child(phoneNumber);
        profileReference = FirebaseDatabase.getInstance().getReference("Users").child(phoneNumber);
        orderReference = FirebaseDatabase.getInstance().getReference("Orders").child(phoneNumber);

        checkData();
        Goto.setOnClickListener(v -> {
            Intent intent = new Intent(Cart.this, Kit.class);
            intent.putExtra("tag", "drone");
            startActivity(intent);
            finish();
        });

        profileReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                email = snapshot.child("email").getValue(String.class);
                String address = snapshot.child("address").child("location").getValue(String.class) + "\n" + snapshot.child("address").child("city").getValue(String.class)
                        + "\n" + snapshot.child("address").child("state").getValue(String.class);
                TextView addressView = findViewById(R.id.address);
                addressView.setText(address);
             }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Checkout.preload(getApplicationContext());
    }

    private void CheckDiscount() {
        discountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("coupon").exists()) {
                    Discount = (TotalCost * 10) / 100;
                    if(TotalCost > 1200) {
                        DiscountView.setText("- ₹ " + Discount);
                        TotalCost -= Discount;
                        Toast.makeText(Cart.this, "Total: " + TotalCost + "\nDiscount: " + Discount, Toast.LENGTH_SHORT).show();
                        TotalPriceView.setText("₹ " + TotalCost);
                        OrderPrice.setText("₹ " + TotalCost);
                        CouponCode.setText("WINNER");
                        CouponCode.setEnabled(false);
                        TextView msg = findViewById(R.id.applied_msg);
                        msg.setVisibility(View.VISIBLE);
                        msg.setText("Coupon Code is Applied");
                        TextView apply = findViewById(R.id.apply_code);
                        apply.setTextColor(Color.parseColor("#F4F9F9"));
                        apply.setEnabled(false);
                        discount = true;
                    } else {
                        Discount = 0;
                        Toast.makeText(Cart.this, "Order should be above ₹1299", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkData() {
        cartReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() > 0) {
                    GetData();
                    Empty.setVisibility(View.GONE);
                    Goto.setVisibility(View.GONE);
                } else {
                    Empty.setVisibility(View.VISIBLE);
                    Goto.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    OrderPrice.setText("₹ 0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void GetData() {
        productReference = FirebaseDatabase.getInstance().getReference("Products");
        options = new FirebaseRecyclerOptions.Builder<CartInfo>().setQuery(cartReference, CartInfo.class).build();
        adapter = new FirebaseRecyclerAdapter<CartInfo, CartViewHolder>(options) {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull CartViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull CartInfo model) {
                String id = model.getId();

                productReference.child(id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name, price, url;
                        name = snapshot.child("title").getValue(String.class);
                        price = snapshot.child("price").getValue(String.class);
                        url = snapshot.child("url").getValue(String.class);

                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("id", id);
                        hashMap.put("quantity", model.getQuantity());
                        arrayList.add(hashMap);

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

                        PriceView.setText("₹ " + PreviewCost);
                        TotalCost = (PreviewCost + DeliveryCost) - Discount;
                        TotalPriceView.setText("₹ " + TotalCost);
                        DeliveryView.setText("₹ " + DeliveryCost);
                        DiscountView.setText("- ₹ " + Discount);
                        OrderPrice.setText("₹ " + TotalCost);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                holder.Cartclose.setOnClickListener(v -> {

                    cartReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            snapshot.child(id).getRef().removeValue();
                            PreviewCost = 0;
                            TotalCost = 0;
                            adapter.stopListening();
                            adapter.startListening();
                            recyclerView.setAdapter(adapter);

                            TextView msg;

                            if (snapshot.getChildrenCount() <= 1) {
                                placeOrder.setEnabled(false);
                                applyCoupon.setVisibility(View.GONE);
                                layout1.setVisibility(View.GONE);
                                addressLayout.setVisibility(View.GONE);
                                layout.setVisibility(View.GONE);
                                Empty.setVisibility(View.VISIBLE);
                                Goto.setVisibility(View.VISIBLE);
                                msg = findViewById(R.id.applied_msg);
                                msg.setVisibility(View.GONE);
                                OrderPrice.setText("₹ 0");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    TextView msg;

                    Discount = 0;
                    DiscountView.setText("- ₹ " + Discount);
                    TotalCost -= Discount;
                    TotalPriceView.setText("₹ " + TotalCost);
                    OrderPrice.setText("₹ " + TotalCost);
                    CouponCode.setEnabled(true);
                    TextView Apply = findViewById(R.id.apply_code);
                    Apply.setEnabled(true);
                    Apply.setTextColor(Color.parseColor("#323232"));
                    msg = findViewById(R.id.applied_msg);
                    msg.setVisibility(View.GONE);
                    Toast.makeText(Cart.this, "Removed from Cart", Toast.LENGTH_SHORT).show();
                });
            }

            @NonNull
            @Override
            public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item_card, parent, false);
                return new CartViewHolder(view);
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
        progressBar.setVisibility(View.GONE);
        layout.setVisibility(View.VISIBLE);
        layout1.setVisibility(View.VISIBLE);
        addressLayout.setVisibility(View.VISIBLE);
        applyCoupon.setVisibility(View.VISIBLE);
        placeOrder.setEnabled(true);
    }

    public void Back(View view) {
        finish();
    }

    public void ApplyCode(View view) {
        String code = CouponCode.getText().toString();
        if(!code.isEmpty()) {
            DatabaseReference discountRef = FirebaseDatabase.getInstance().getReference("Discount");
            discountRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean valid = true;
                    for (DataSnapshot data: snapshot.getChildren()) {
                        if(Objects.equals(data.child("code").getValue(String.class), code)) {
                            Discount = Integer.parseInt(Objects.requireNonNull(data.child("discount").getValue(String.class)));
                            if(TotalCost > 1200) {
                                DiscountView.setText("- ₹ " + Discount);
                                TotalCost -= Discount;
                                TotalPriceView.setText("₹ " + TotalCost);
                                OrderPrice.setText("₹ " + TotalCost);
                                CouponCode.setEnabled(false);
                                view.setEnabled(false);
                                TextView msg = findViewById(R.id.applied_msg);
                                TextView apply = findViewById(R.id.apply_code);
                                apply.setTextColor(Color.parseColor("#F4F9F9"));
                                msg.setVisibility(View.VISIBLE);
                                couponApplied = true;
                            } else {
                                Discount = 0;
                                CouponCode.setText("");
                                couponApplied = false;
                                Toast.makeText(Cart.this, "Order should be above ₹1299", Toast.LENGTH_SHORT).show();
                            }
                            valid = true;
                            break;
                        } else {
                            valid = false;
                        }
                    }

                    if(!valid) {
                        CouponCode.setText("");
                        Toast.makeText(Cart.this, "Enter Valid Code", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            Toast.makeText(this, "Enter Valid Code", Toast.LENGTH_SHORT).show();
        }
    }

    public void PlaceOrder(View view) {
        profileReference.child("address").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    startPayment();
                } else {
                    BottomSheetDialog dialog = new BottomSheetDialog(Cart.this, R.style.AppBottomSheetDialogTheme);
                    View view = getLayoutInflater().inflate(R.layout.address_view, null);

                    TextInputLayout AddressLayout1, AddressLayout2,  CityLayout, StateLayout, PincodeLayout;
                    Button saveAddress = view.findViewById(R.id.save_address);

                    AddressLayout1 = view.findViewById(R.id.address_1);
                    AddressLayout2 = view.findViewById(R.id.address_2);
                    CityLayout = view.findViewById(R.id.city);
                    StateLayout = view.findViewById(R.id.state);
                    PincodeLayout = view.findViewById(R.id.pincode);

                    saveAddress.setOnClickListener(v1 -> {
                        if(Validation(AddressLayout1, AddressLayout2, CityLayout, StateLayout, PincodeLayout) > 0)
                            Toast.makeText(Cart.this, "Fill up details", Toast.LENGTH_SHORT).show();
                        else {
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("location",  address1 + ", " + address2);
                            hashMap.put("city", city + " - " + pincode);
                            hashMap.put("state", state);
                            profileReference.child("address").setValue(hashMap).addOnCompleteListener(task -> {
                                dialog.dismiss();
                                if (task.isSuccessful()) {
                                    Toast.makeText(Cart.this, "Saved", Toast.LENGTH_SHORT).show();
                                    startPayment();
                                } else {
                                    Toast.makeText(Cart.this, "Unable to save address\nTry Again Later", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });

                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setContentView(view);
                    dialog.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private int Validation(TextInputLayout addressLayout1, TextInputLayout addressLayout2, TextInputLayout cityLayout,
                           TextInputLayout stateLayout, TextInputLayout pincodeLayout) {
        final int[] count = {0};
        address1 = Objects.requireNonNull(addressLayout1.getEditText()).getText().toString();
        address2 = Objects.requireNonNull(addressLayout2.getEditText()).getText().toString();
        city = Objects.requireNonNull(cityLayout.getEditText()).getText().toString();
        state = Objects.requireNonNull(stateLayout.getEditText()).getText().toString();
        pincode = Objects.requireNonNull(pincodeLayout.getEditText()).getText().toString();

        if(address1.length() == 0) {
            ErrorMsg("House no or Building name cannot be empty", addressLayout1);
            count[0]++;
        }

        if(address2.length() == 0) {
            ErrorMsg("Road, Colony cannot be empty", addressLayout2);
            count[0]++;
        }

        if(city.length() == 0) {
            ErrorMsg("City cannot be empty", cityLayout);
            count[0]++;
        }

        if(state.length() == 0) {
            ErrorMsg("State cannot be empty", stateLayout);
            count[0]++;
        }

        if(pincode.length() == 0) {
            ErrorMsg("Pincode cannot be empty", pincodeLayout);
            count[0]++;
        }

        return count[0];
    }

    public void ErrorMsg(String msg, TextInputLayout inputLayout){
        inputLayout.setErrorEnabled(true);
        inputLayout.setError(msg);
    }


    public void startPayment() {
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_lwrLe85hfjCIWm");
        checkout.setImage(R.drawable.logo);
        final Activity activity = this;
        try {
            String totalAmount = String.valueOf((TotalCost * 100));
            JSONObject options = new JSONObject();

            options.put("name", "HobbyMart");
            options.put("description", "By Laikapace Pvt. Ltd");
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
//            options.put("order_id", "order_DBJOWzybf0sJbb");//from response of step 3.
            options.put("theme.color", "#0a68ff");
            options.put("currency", "INR");
            options.put("amount", totalAmount);//pass amount in currency subunits
            options.put("prefill.email", email);
            options.put("prefill.contact",phoneNumber);
            JSONObject retryObj = new JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 4);
            options.put("retry", retryObj);

            checkout.open(activity, options);

        } catch(Exception e) {
            Log.e(TAG, "Error in starting Razorpay Checkout", e);
        }
    }

    @Override
    public void onPaymentSuccess(String s, PaymentData paymentData) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy 'at' hh:mm aaa");
        String date = format.format(new Date());
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("no", phoneNumber);
        hashMap.put("payment", "success");
        hashMap.put("totalCost", String.valueOf(TotalCost));
        hashMap.put("paymentID", s);
        hashMap.put("date", date);
        hashMap.put("coupon", String.valueOf(couponApplied));
        orderReference.child(s).setValue(hashMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                orderReference.child(s).child("productData").setValue(arrayList);
                cartReference.removeValue();
                discountRef.child("coupon").removeValue();
                Intent intent = new Intent(Cart.this, Orders.class);
                startActivity(intent);
                Toast.makeText(Cart.this, "Order Placed", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        Checkout.clearUserData(this);
    }

    @Override
    public void onPaymentError(int i, String s, PaymentData paymentData) {
        Toast.makeText(Cart.this, "Payment was Unsuccessful", Toast.LENGTH_SHORT).show();
        Checkout.clearUserData(this);
    }
}