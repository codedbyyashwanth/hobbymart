package com.laikapace.hobbymart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView logo = findViewById(R.id.logo);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.logo_anim);
        logo.startAnimation(animation);

        new CountDownTimer(4000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                // Nothing Todo
            }

            @Override
            public void onFinish() {
                startActivity(new Intent(MainActivity.this, Login.class));
                finish();
            }
        }.start();
    }
}