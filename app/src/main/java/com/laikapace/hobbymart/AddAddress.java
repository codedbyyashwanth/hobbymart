package com.laikapace.hobbymart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class AddAddress extends AppCompatActivity {

        ProgressBar progressBar;
        RelativeLayout CreateLayout, EditLayout;
        DatabaseReference profileReference;
        String phoneNumber;
        TextView addressView;
        TextInputLayout AddressLayout1, AddressLayout2,  CityLayout, StateLayout, PincodeLayout;
        Button saveAddress, editAddress;
        private String address1, address2, city, state, pincode;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_add_address);

                progressBar = findViewById(R.id.progress_bar);
                CreateLayout = findViewById(R.id.create_layout);
                EditLayout = findViewById(R.id.edit_layout);
                addressView = findViewById(R.id.address);
                editAddress = findViewById(R.id.edit_address);

                AddressLayout1 = findViewById(R.id.address_1);
                AddressLayout2 = findViewById(R.id.address_2);
                CityLayout = findViewById(R.id.city);
                StateLayout = findViewById(R.id.state);
                PincodeLayout = findViewById(R.id.pincode);
                saveAddress = findViewById(R.id.save_address);

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                assert user != null;
                phoneNumber = user.getPhoneNumber();

                profileReference = FirebaseDatabase.getInstance().getReference("Users").child(phoneNumber);

                profileReference.child("address").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                progressBar.setVisibility(View.GONE);

                                if (snapshot.exists()) {
                                        EditLayout.setVisibility(View.VISIBLE);
                                        CreateLayout.setVisibility(View.GONE);
                                        String address = snapshot.child("location").getValue(String.class) + "\n"  + snapshot.child("city").getValue(String.class) + "\n" +
                                                snapshot.child("state").getValue(String.class);
                                        addressView.setText(address);
                                } else {
                                        EditLayout.setVisibility(View.GONE);
                                        CreateLayout.setVisibility(View.VISIBLE);
                                }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                });

                editAddress.setOnClickListener(v -> {
                        EditLayout.setVisibility(View.GONE);
                        CreateLayout.setVisibility(View.VISIBLE);
                });

                saveAddress.setOnClickListener(v1 -> {
                        if(Validation(AddressLayout1, AddressLayout2, CityLayout, StateLayout, PincodeLayout) > 0)
                                Toast.makeText(AddAddress.this, "Fill up details", Toast.LENGTH_SHORT).show();
                        else {
                                HashMap<String, String> hashMap = new HashMap<>();
                                hashMap.put("location",  address1 + ", " + address2);
                                hashMap.put("city", city + " - " + pincode);
                                hashMap.put("state", state);
                                profileReference.child("address").setValue(hashMap).addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                                Toast.makeText(AddAddress.this, "Address Saved", Toast.LENGTH_SHORT).show();
                                        } else {
                                                Toast.makeText(AddAddress.this, "Unable to save address\nTry Again Later", Toast.LENGTH_SHORT).show();
                                        }
                                });
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

        public void Back(View view) {
                finish();
        }
}