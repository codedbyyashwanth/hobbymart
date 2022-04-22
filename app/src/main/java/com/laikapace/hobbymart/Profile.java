package com.laikapace.hobbymart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
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

    public void AboutUs(View view) {
        startActivity(new Intent(Profile.this, AboutUs.class));
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void Privacy(View view) {
        Intent intent = new Intent(this, PDFView.class);
        intent.putExtra("url", "https://firebasestorage.googleapis.com/v0/b/laikapace.appspot.com/o/Privacy%20Policy-converted.pdf?alt=media&token=ca298856-5c4e-48ea-a675-87185f1842f1");
        startActivity(intent);
    }

    public void FacebookView(View view) {
        Intent intent = new Intent(this, SocialPage.class);
        intent.putExtra("url", "https://www.facebook.com/laikapace/");
        startActivity(intent);
    }

    public void LinkedinView(View view) {
        Intent intent = new Intent(this, SocialPage.class);
        intent.putExtra("url", "https://www.linkedin.com/company/laikapace/");
        startActivity(intent);
    }

    public void InstagramView(View view) {
        Intent intent = new Intent(this, SocialPage.class);
        intent.putExtra("url", "https://www.instagram.com/laikapace/");
        startActivity(intent);
    }

    public void WebsiteView(View view) {
        Intent intent = new Intent(this, SocialPage.class);
        intent.putExtra("url", "https://laikapace.tech/");
        startActivity(intent);
    }

    public void Terms(View view) {
        Intent intent = new Intent(this, PDFView.class);
        intent.putExtra("url", "https://firebasestorage.googleapis.com/v0/b/laikapace.appspot.com/o/Terms%20and%20Conditions-converted.pdf?alt=media&token=d1dd7857-6695-4e16-8959-2b7ec0915744");
        startActivity(intent);
    }

    public void Shipping(View view) {
        Intent intent = new Intent(this, PDFView.class);
        intent.putExtra("url", "https://firebasestorage.googleapis.com/v0/b/laikapace.appspot.com/o/Shipping%20and%20Refund%20Policy-converted.pdf?alt=media&token=c658d18b-c5f4-424c-a7f4-508833e9a7ff");
        startActivity(intent);
    }
}