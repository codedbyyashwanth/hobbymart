package com.laikapace.hobbymart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Home extends AppCompatActivity {

    ImageView cartImage;
    Animation cartImgAnime;
    FirebaseUser user;
    String phoneNumber;
    DatabaseReference droneReference, planeReference, cartReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        cartImage = findViewById(R.id.cart);
        cartImgAnime = AnimationUtils.loadAnimation(this, R.anim.icon_anim);

        user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        phoneNumber = user.getPhoneNumber();

        droneReference = FirebaseDatabase.getInstance().getReference("drone");
        cartReference = FirebaseDatabase.getInstance().getReference("Cart").child(phoneNumber);


//        Explore list of best DIY drone kits best DIY drone kits. From Quadcopter DIY Combo Kit, Drone frame Kit,
//        LiPo Battery, GPS module, Flight controller and BLDC Motors, Remote control we have it all!
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

    public void ViewKit(View view) {
        String tag = view.getTag().toString();
        Intent intent = new Intent(this, Kit.class);
        intent.putExtra("tag",  tag);
        startActivity(intent);
    }

   public void BuyDrone(View view) {
        long id = 21012021001L;
        for (int i=1; i<=15; i++) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("id", String.valueOf(id));
            hashMap.put("quantity", "1");
            cartReference.child(String.valueOf(id)).setValue(hashMap);
            id++;
        }
    }

    public void BuyPlane(View view) {
    }
}