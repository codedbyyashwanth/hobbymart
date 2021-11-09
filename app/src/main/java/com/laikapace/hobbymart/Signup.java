package com.laikapace.hobbymart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.chaos.view.PinView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Signup extends AppCompatActivity {

    TextInputLayout nameLayout, emailLayout, passwordLayout, confirmPassword, phoneLayout;
    CheckBox checkBox;
    RequestQueue requestQueue;
    FirebaseAuth auth;
    AlertDialog.Builder builder;
    AlertDialog dialog;
    DatabaseReference dbReference;
    String otpSent;
    View view;
    boolean Clicked = false;
    BottomSheetDialog bottomSheetDialog;
    BroadcastReceiver Receiver;
    PinView otp;
    HashMap<String, String> data;
    String name, email, password, phoneNo, encryptedPassword;
    PhoneAuthProvider.ForceResendingToken token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        nameLayout = findViewById(R.id.name_layout);
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        phoneLayout = findViewById(R.id.phone_layout);
        confirmPassword = findViewById(R.id.confirm_layout);
        checkBox = findViewById(R.id.checkbox);

        DisableError(nameLayout);
        DisableError(emailLayout);
        DisableError(phoneLayout);
        DisableError(passwordLayout);
        DisableError(confirmPassword);

        // Making request using Volley Library
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        auth = FirebaseAuth.getInstance();
        dbReference = FirebaseDatabase.getInstance().getReference();

        bottomSheetDialog = new BottomSheetDialog(Signup.this, R.style.AppBottomSheetDialogTheme);
        view = getLayoutInflater().inflate(R.layout.bottomsheet, null);
        otp = view.findViewById(R.id.otp_code);

        data = new HashMap<>();

        requestsmspermission();
    }

    private void requestsmspermission() {
        String smspermission = Manifest.permission.RECEIVE_SMS;
        int grant = ContextCompat.checkSelfPermission(this,smspermission);

        // Checking for Permission for SMS
        if(grant!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, 1);
        }
    }

    public void DisableError(TextInputLayout inputLayout){
        Objects.requireNonNull(inputLayout.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Nothing to do here
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Nothing to do here
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Disable the Error while typing
                inputLayout.setError(null);
                inputLayout.setErrorEnabled(false);
            }
        });
    }

    public void Register(View view) {

        if(checkBox.isChecked()) {
            int val = ValidateInput();
            if(val > 0)     // Checking weather the input fields are valid
                Toast.makeText(this, "Input Field is not filled properly", Toast.LENGTH_SHORT).show();
            else {

                Loading();      // Show Loading on the screen
                phoneNo = "+91" + Objects.requireNonNull(phoneLayout.getEditText()).getText().toString();
                checkNumber();

                // Passing all the data to next activity for registering
            }
        }
        else
            Toast.makeText(this, "Agree to our term and conditions", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("SetTextI18n")
    private void Loading() {
        builder = new AlertDialog.Builder(Signup.this);
        View view1 = getLayoutInflater().inflate(R.layout.loading_view, null);
        TextView msg = view1.findViewById(R.id.msg);
        msg.setText("Registering Please Wait");
        builder.setView(view1);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void checkNumber() {
        // Query to check whether phone number exits or not
        Query checkPhoneNo = dbReference.child("Users").orderByChild("phone").equalTo(phoneNo);

        checkPhoneNo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // if number exits then send to login activity
                // if not then send the OTP and verify the number
                if (snapshot.exists()) {
                    startActivity(new Intent(Signup.this, Login.class));
                    Toast.makeText(Signup.this, "User Already Exits", Toast.LENGTH_SHORT).show();
                } else {
                    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                            .setPhoneNumber(phoneNo)     // Phone number to verify
                            .setTimeout(30L, TimeUnit.SECONDS)      // Timeout and unit
                            .setActivity(Signup.this)
                            .setCallbacks(mCallback)        // OnVerificationStateChangedCallbacks
                            .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            // Nothing to do here
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(Signup.this, "Failed to send otp", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            otpSent = s;
            token = forceResendingToken;        // Resend OTP Token
            Toast.makeText(Signup.this, "OTP has sent", Toast.LENGTH_SHORT).show();
            verifyOTP();
        }
    };

    private void verifyOTP() {
        Button verifyBtn = view.findViewById(R.id.verifyBtn);
        TextView number = view.findViewById(R.id.number);
        assert phoneNo != null;
        number.setText(phoneNo);

        verifyBtn.setOnClickListener(view1 -> {
            if(Objects.requireNonNull(otp.getText()).toString().length() > 0){
                verifyNumber(otp.getText().toString());
                dialog.show();
                bottomSheetDialog.dismiss();
            }
            else
                Toast.makeText(Signup.this, "Enter valid OTP", Toast.LENGTH_SHORT).show();
        });

        dialog.dismiss();
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();


        if(bottomSheetDialog.isShowing()) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);

            // Scanning OTP automatically using BroadCast receiver
            Receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);

                    // Getting the OTP from the Message sent
                    for(SmsMessage sms:messages){
                        String message = sms.getMessageBody();
                        String otpCode = message.substring(0, 6);
                        try {
                            if(!otpCode.isEmpty() && otpCode != null) {
                                Integer.parseInt(otpCode);
                                otp.setText(otpCode);
                                dialog.show();
                                verifyNumber(otpCode);
                                break;
                            }
                        } catch (NumberFormatException e){
                            // Nothing to do here
                        }
                    }
                }
            };

            registerReceiver(Receiver, filter);     // Registering the BroadCast Receiver
            ResendOTP();        // If failed to send OTP then enable resend OTP
        }
    }

    private void ResendOTP() {
        TextView resendBtn, timer;
        resendBtn = view.findViewById(R.id.resend_otp);
        timer = view.findViewById(R.id.timer);

        resendBtn.setEnabled(false);
        resendBtn.setAlpha(0.7f);
        resendBtn.setTextColor(Color.parseColor("#707278"));
        if(!Clicked) {
            startTimer(timer, resendBtn);
        }

        resendBtn.setOnClickListener(view -> {
            Resend();
            startTimer(timer, resendBtn);
            Clicked = true;
            resendBtn.setEnabled(false);
            resendBtn.setAlpha(0.7f);
            resendBtn.setTextColor(Color.parseColor("#707278"));
        });
    }

    private void Resend() {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setActivity(Signup.this)
                .setPhoneNumber(phoneNo)
                .setTimeout(30L,TimeUnit.SECONDS)
                .setCallbacks(mCallback)
                .setForceResendingToken(token)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void startTimer(TextView timer, TextView resendBtn) {
        final int[] count = {30};
        new CountDownTimer(31000, 1000){

            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long l) {
                if(count[0] < 10)
                    timer.setText("(0:0" + count[0] + ")");
                else
                    timer.setText("(0:" + count[0] + ")");
                count[0]--;
            }

            @Override
            public void onFinish() {
                resendBtn.setEnabled(true);
                resendBtn.setAlpha(1f);
                resendBtn.setTextColor(Color.parseColor("#2962ff"));
            }
        }.start();
    }

    private void verifyNumber(String otp) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(
                otpSent,
                otp
        );
        unregisterReceiver(Receiver);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                data.put("name", name);
                data.put("email", email);
                data.put("password", encryptedPassword);
                data.put("phone", phoneNo);
                dbReference.child("Users").child(phoneNo).setValue(data);
                Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Signup.this, Home.class);
                startActivity(intent);
                finish();
            }
            else
                Toast.makeText(Signup.this, "Failed to register", Toast.LENGTH_SHORT).show();
        });
    }

    public int ValidateInput(){
        final int[] count = {0};
        name = Objects.requireNonNull(nameLayout.getEditText()).getText().toString();
        email = Objects.requireNonNull(emailLayout.getEditText()).getText().toString();
        password = Objects.requireNonNull(passwordLayout.getEditText()).getText().toString();

        encryptedPassword = Crypto.encryptAndEncode(password);

        if(name.length() == 0) {
            ErrorMsg("Username should not be empty", nameLayout);
            count[0]++;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ErrorMsg("Email is not valid", emailLayout);
            count[0]++;
        }

        if(Objects.requireNonNull(phoneLayout.getEditText()).getText().toString().length() != 10) {
            ErrorMsg("Phone number should have 10 numbers", phoneLayout);
            count[0]++;
        }

        if(password.length() < 8) {
            ErrorMsg("Password must have 8 characters", passwordLayout);
            count[0]++;
        }

        if(Objects.requireNonNull(confirmPassword.getEditText()).getText().toString().length() < 8 ||
                !confirmPassword.getEditText().getText().toString().equals(Objects.requireNonNull(passwordLayout.getEditText()).getText().toString())
        ) {
            ErrorMsg("Confirm Password is wrong", confirmPassword);
            count[0]++;
        }

        String api = "https://emailvalidation.abstractapi.com/v1/?api_key=3a7a91eeda9c4477adfffb24ac658579&email=" + email;

        // JSON Request to verify the Email Address is valid or not
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, api, null, response -> {
            try {
                String value = response.getJSONObject("is_valid_format").getString("text");
                if(!value.equals("TRUE")){
                    ErrorMsg("Email is not valid", emailLayout);
                    count[0]++;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {

        });

        requestQueue.add(request);
        return count[0];
    }

    public void ErrorMsg(String msg, TextInputLayout inputLayout){
        inputLayout.setErrorEnabled(true);
        inputLayout.setError(msg);
    }

    public void Login(View view) {
        Intent intent = new Intent(Signup.this, Login.class);
        startActivity(intent);
        finish();
    }
}