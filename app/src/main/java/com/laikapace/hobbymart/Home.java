package com.laikapace.hobbymart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;

public class Home extends AppCompatActivity {

    ImageView cartImage;
    Animation cartImgAnime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        cartImage = findViewById(R.id.cart);
        cartImgAnime = AnimationUtils.loadAnimation(this, R.anim.icon_anim);
    }

    public void Logout(View view) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        startActivity(new Intent(Home.this, Login.class));
        finish();
    }

    public void AddToCart(View view) {
        cartImage.startAnimation(cartImgAnime);

        Intent intent = new Intent(Home.this, Cart.class);
        startActivity(intent);
    }

    public void Profile(View view) {
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
    }
}