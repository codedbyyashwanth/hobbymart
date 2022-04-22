package com.laikapace.hobbymart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class Home extends AppCompatActivity {

    ImageView cartImage;
    Animation cartImgAnime;
    FirebaseUser user;
    String phoneNumber;
    DatabaseReference droneReference, cartReference, questionReference, quizRef;
    BottomSheetDialog sheetDialog;
    View sheetView;
    CardView quizView;
    RadioButton brushButton;
    RadioGroup brushGroup, controllerGroup, batteryGroup, transmitterGroup, frameGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        cartImage = findViewById(R.id.cart);
        quizView = findViewById(R.id.quiz_card);
        cartImgAnime = AnimationUtils.loadAnimation(this, R.anim.icon_anim);

        user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        phoneNumber = user.getPhoneNumber();

        droneReference = FirebaseDatabase.getInstance().getReference("drone");
        quizRef = FirebaseDatabase.getInstance().getReference("quiz");
        cartReference = FirebaseDatabase.getInstance().getReference("Cart").child(phoneNumber);
        questionReference = FirebaseDatabase.getInstance().getReference("quiz").child("completion").child(phoneNumber);

        sheetDialog = new BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme);
        sheetDialog.setCanceledOnTouchOutside(false);
        sheetDialog.setCancelable(false);
        checkQuiz();
    }

    private void checkQuiz() {
        quizRef.child("started").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (Objects.requireNonNull(snapshot.getValue(String.class)).equalsIgnoreCase("yes")) {
                    quizView.setVisibility(View.VISIBLE);
                } else {
                    quizView.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

   @SuppressLint("InflateParams")
   public void BuyDrone(View view) {
       String[] c = new String[] {"21012021003", "21012021007", "21012021014", "21012021015"};
       ArrayList<String> ids = new ArrayList<>(Arrays.asList(c));

       sheetView = getLayoutInflater().inflate(R.layout.drone_category_selection, null);
       brushGroup = sheetView.findViewById(R.id.brushless_motor);
       controllerGroup = sheetView.findViewById(R.id.flight_controller);
       batteryGroup = sheetView.findViewById(R.id.battery);
       transmitterGroup = sheetView.findViewById(R.id.transmitter);
       frameGroup = sheetView.findViewById(R.id.quad_frame);
       Button done = sheetView.findViewById(R.id.done);
       CardView close = sheetView.findViewById(R.id.close);

       close.setOnClickListener(v -> sheetDialog.dismiss());

       done.setOnClickListener(doneView -> {
           if (brushGroup.getCheckedRadioButtonId() != -1 && controllerGroup.getCheckedRadioButtonId() != -1 && batteryGroup.getCheckedRadioButtonId() != -1 && transmitterGroup.getCheckedRadioButtonId() != -1 && frameGroup.getCheckedRadioButtonId() != -1) {
               cartReference.removeValue().addOnCompleteListener(task -> {
                   if (task.isSuccessful()) {
                       brushButton = sheetView.findViewById(brushGroup.getCheckedRadioButtonId());
                       ids.add(brushButton.getTag().toString());

                       RadioButton controllerButton = sheetView.findViewById(controllerGroup.getCheckedRadioButtonId());
                       ids.add(controllerButton.getTag().toString());

                       RadioButton batteryButton = sheetView.findViewById(batteryGroup.getCheckedRadioButtonId());
                       ids.add(batteryButton.getTag().toString());

                       RadioButton transmitterButton = sheetView.findViewById(transmitterGroup.getCheckedRadioButtonId());
                       ids.add(transmitterButton.getTag().toString());

                       RadioButton frameButton = sheetView.findViewById(frameGroup.getCheckedRadioButtonId());
                       ids.add(frameButton.getTag().toString());

                       for (String id: ids) {
                           HashMap<String, String> hashMap = new HashMap<>();
                           hashMap.put("id", id);
                           hashMap.put("quantity", "1");
                           cartReference.child(id).setValue(hashMap);
                       }

                       sheetDialog.dismiss();
                       startActivity(new Intent(Home.this, Cart.class));
                   }
               });
           } else {
               Toast.makeText(Home.this, "Please select the category", Toast.LENGTH_SHORT).show();
           }

       });

       sheetDialog.setContentView(sheetView);
       sheetDialog.show();
    }

    @SuppressLint("InflateParams")
    public void BuyPlane(View view) {
        String[] c= new String[]  { "21012021003", "21012021007", "21012021016" };
        ArrayList<String> ids = new ArrayList<>(Arrays.asList(c));

        sheetView = getLayoutInflater().inflate(R.layout.plane_category_selection, null);
        brushGroup = sheetView.findViewById(R.id.brushless_motor);
        batteryGroup = sheetView.findViewById(R.id.battery);
        transmitterGroup = sheetView.findViewById(R.id.transmitter);
        Button done = sheetView.findViewById(R.id.done);
        CardView close = sheetView.findViewById(R.id.close);

        close.setOnClickListener(v -> sheetDialog.dismiss());

        done.setOnClickListener(doneView -> {
            if (brushGroup.getCheckedRadioButtonId() != -1 && batteryGroup.getCheckedRadioButtonId() != -1 && transmitterGroup.getCheckedRadioButtonId() != -1) {
                cartReference.removeValue();

                RadioButton brushButton = sheetView.findViewById(brushGroup.getCheckedRadioButtonId());
                ids.add(brushButton.getTag().toString());

                RadioButton batteryButton = sheetView.findViewById(batteryGroup.getCheckedRadioButtonId());
                ids.add(batteryButton.getTag().toString());

                RadioButton transmitterButton = sheetView.findViewById(transmitterGroup.getCheckedRadioButtonId());
                ids.add(transmitterButton.getTag().toString());

                for (String id: ids) {
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", id);
                    hashMap.put("quantity", "1");
                    cartReference.child(id).setValue(hashMap);
                }

                sheetDialog.dismiss();
                startActivity(new Intent(Home.this, Cart.class));
            } else {
                Toast.makeText(Home.this, "Please select the category", Toast.LENGTH_SHORT).show();
            }
        });

        sheetDialog.setContentView(sheetView);
        sheetDialog.show();
    }

    public void AttendQuiz(View view) {
        questionReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(Home.this, "You have completed the Quiz", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(Home.this, Quiz.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void ViewAll(View view) {
        Intent intent = new Intent(Home.this, Plane.class);
        startActivity(intent);
    }
}