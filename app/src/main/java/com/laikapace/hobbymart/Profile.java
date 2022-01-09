package com.laikapace.hobbymart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile extends AppCompatActivity {

    FirebaseUser user;
    String phoneNumber;
    DatabaseReference databaseReference, quizReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        phoneNumber = user.getPhoneNumber();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        quizReference = FirebaseDatabase.getInstance().getReference("quiz");

        GetData();
    }

    private void GetData() {
        TextView usernameView, phoneView;
        usernameView = findViewById(R.id.username);
        phoneView = findViewById(R.id.phone_number);

        databaseReference.child(phoneNumber).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.child("name").getValue(String.class);
                usernameView.setText(username);
                phoneView.setText(phoneNumber);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void Logout(View view) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        startActivity(new Intent(Profile.this, Login.class));
        finish();
    }

    public void Back(View view) {
        finish();
    }

    public void ViewCart(View view) {
        startActivity(new Intent(Profile.this, Cart.class));
    }

    public void EditAddress(View view) {
        startActivity(new Intent(Profile.this, AddAddress.class));
    }

    public void Orders(View view) {
        startActivity(new Intent(Profile.this, Orders.class));
    }

    public void DeleteQuiz(View view) {
        quizReference.child("completion").child(phoneNumber).removeValue();
        quizReference.child("discount").child(phoneNumber).removeValue();
        for (int i = 1; i<=12; i++) {
            quizReference.child("questions").child(i + "").child("answered").child(phoneNumber).removeValue();
        }
    }
}