package com.laikapace.hobbymart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Congratz extends AppCompatActivity {

    DatabaseReference resultReference, completionRef, discountRef;
    int index = 1, answerCount = 0, totalQuestions;
    String phoneNumber = "";
    TextView result, coupon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congratz);

        result = findViewById(R.id.result_details);
        coupon = findViewById(R.id.coupon);

        phoneNumber = getIntent().getStringExtra("no");
        totalQuestions = Integer.parseInt(getIntent().getStringExtra("totalQuestions"));

        discountRef = FirebaseDatabase.getInstance().getReference("quiz").child("discount");
        completionRef = FirebaseDatabase.getInstance().getReference("quiz").child("completion");
        resultReference = FirebaseDatabase.getInstance().getReference("quiz").child("questions");
        GetResult();

        getWindow().setStatusBarColor(getResources().getColor(R.color.blueish, this.getTheme()));
    }

    private void GetResult() {
        resultReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (index = 1; index<=totalQuestions; index++) {
                    String answer = snapshot.child(index + "").child("answered").child(phoneNumber).child("result").getValue(String.class);
                    assert answer != null;
                    if (answer.equals("correct"))
                        answerCount++;

                    if (index == totalQuestions) {
                        result.setText("You have scored " + answerCount +" out of " + totalQuestions);
                        if (answerCount == totalQuestions) {
                            discountRef.child(phoneNumber).child("coupon").setValue("applied");
                            coupon.setText("You have won 10% OFF discount on purchasing order above ₹1200");
                        } else {
                            coupon.setText("");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void BackHome(View view) {
        startActivity(new Intent(Congratz.this, Home.class));
        finish();
    }

    public void BackProducts(View view) {
        Intent intent = new Intent(Congratz.this, Kit.class);
        intent.putExtra("tag", "drone");
        startActivity(intent);
        finish();
    }
}