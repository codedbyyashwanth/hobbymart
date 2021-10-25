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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Login extends AppCompatActivity {

    TextInputLayout phoneLayout, passwordLayout;
    FirebaseAuth auth;
    DatabaseReference reference;
    String phoneNo, password, otpSent;
    ImageView logo;
    TextView appname, desc;
    Button signin;
    AlertDialog.Builder builder;
    AlertDialog dialog;
    PinView otp;
    View view;
    boolean Clicked = false;
    BottomSheetDialog bottomSheetDialog;
    BroadcastReceiver Receiver;
    PhoneAuthProvider.ForceResendingToken token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        phoneLayout = findViewById(R.id.phone_layout);
        passwordLayout = findViewById(R.id.password_layout);
        logo = findViewById(R.id.logo);
        appname = findViewById(R.id.textView);
        signin = findViewById(R.id.signin);
        desc = findViewById(R.id.text);

        // Disable error while typing
        DisableError(phoneLayout);
        DisableError(passwordLayout);

        // Permission for Reading the OTP from message
        requestsmspermission();

        // Layout initialization for OTP Verification (used in verifyOTP() function)
        bottomSheetDialog = new BottomSheetDialog(Login.this, R.style.AppBottomSheetDialogTheme);
        view = getLayoutInflater().inflate(R.layout.bottomsheet, null);
        otp = view.findViewById(R.id.otp_code);

        // Firebase Database for storing user details
        reference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Checking weather the user is Registered or Not
        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null){
            // if user exist then redirecting to home activity
            Intent intent = new Intent(Login.this, Home.class);
            startActivity(intent);
            finish();
        }
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
                // Disable Error while Typing
                inputLayout.setError(null);
                inputLayout.setErrorEnabled(false);
            }
        });
    }

    public void Signup(View view) {
        // If user is not registered then redirecting to register activity
        Intent intent = new Intent(Login.this, Signup.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @SuppressLint("SetTextI18n")
    public void Signin(View view) {

        if(Validation() > 0)        // Checking weather the input fields are valid
            Toast.makeText(this, "Enter correct details", Toast.LENGTH_SHORT).show();
        else {
            // Storing the details
            phoneNo = "+91" + Objects.requireNonNull(phoneLayout.getEditText()).getText().toString();
            password = Objects.requireNonNull(passwordLayout.getEditText()).getText().toString();

            // Query to check whether phone number exits or not
            Query checkPhoneNo = reference.child("Users").orderByChild("phone").equalTo(phoneNo);

            checkPhoneNo.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        // the password in checkPassword is encrypted
                        String checkPassword = Objects.requireNonNull(snapshot.child(phoneNo).child("password").getValue()).toString();
                        try {
                            String decryptedPassword = Crypto.decodeAndDecrypt(checkPassword);      // Decrypting the password
                            if (decryptedPassword.equals(password)) {
                                Loading();      // Show Loading on the screen until OTP is sent

                                // Sending the OTP to given phone number
                                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                                        .setPhoneNumber(phoneNo)
                                        .setActivity(Login.this)
                                        .setTimeout(30L, TimeUnit.SECONDS)
                                        .setCallbacks(mCallbacks)
                                        .build();
                                PhoneAuthProvider.verifyPhoneNumber(options);

                            } else {
                                // Showing Error if the password is incorrect
                                passwordLayout.setErrorEnabled(true);
                                passwordLayout.setError("Password is incorrect");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        // Showing Error if the phone number is incorrect
                        phoneLayout.setErrorEnabled(true);
                        phoneLayout.setError("Phone Number doesn't exits");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @SuppressLint("SetTextI18n")
    private void Loading() {
        builder = new AlertDialog.Builder(Login.this);
        View view1 = getLayoutInflater().inflate(R.layout.loading_view, null);
        TextView msg = view1.findViewById(R.id.msg);
        msg.setText("Wait for the OTP");
        builder.setView(view1);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }


    // Callback function which executes after sending the OTP
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            //  Storing the OTP and verifying it
            otpSent = s;
            token = forceResendingToken;        // Resend OTP Token
            verifyOTP();
            Toast.makeText(Login.this, "OTP has sent", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            // Nothing to do here
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            dialog.dismiss();
            Toast.makeText(Login.this, "Unable to send OTP", Toast.LENGTH_SHORT).show();
        }

    };

    private void verifyOTP() {
        /*
         * This function will show the bottom sheet dialog
         * where user can enter the otp and verify it
         * And if OTP fails then the user can click on Resend OTP
         */

        Button verifyBtn = view.findViewById(R.id.verifyBtn);
        TextView number = view.findViewById(R.id.number);
        assert phoneNo != null;
        number.setText(phoneNo);

        verifyBtn.setOnClickListener(view1 -> {
            if(Objects.requireNonNull(otp.getText()).toString().length() > 0){
                verifyNumber(otp.getText().toString());
            }
            else
                Toast.makeText(Login.this, "Enter valid OTP", Toast.LENGTH_SHORT).show();
        });

        dialog.dismiss();
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();

        if(bottomSheetDialog.isShowing()) {
            AutoReceiveOTP();       // Scanning OTP automatically using BroadCast receiver
            ResendOTP();        // If failed to send OTP then enable resend OTP
        }
    }

    private void AutoReceiveOTP() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);

        Receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);

                // Getting the OTP from the Message sent
                for(SmsMessage sms:messages){
                    String message = sms.getMessageBody();
                    String otpCode = message.substring(0, 6);
                    try {
                        Integer.parseInt(otpCode);
                        otp.setText(otpCode);
                        dialog.show();
                        verifyNumber(otpCode);
                        break;
                    } catch (NumberFormatException e){
                        // Nothing to do here
                    }
                }
            }
        };

        registerReceiver(Receiver, filter);     // Registering the BroadCast Receiver
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
                .setActivity(Login.this)
                .setPhoneNumber(phoneNo)
                .setTimeout(30L,TimeUnit.SECONDS)
                .setCallbacks(mCallbacks)
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
        // Passing both entered and sent OTP to credential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(
                otpSent,
                otp
        );
        unregisterReceiver(Receiver);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        // Logging in if the credential is correct
        auth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                Toast.makeText(this, "Login Successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Login.this, Home.class);
                startActivity(intent);
                finish();
            }
            else
                Toast.makeText(Login.this, "Failed to register", Toast.LENGTH_SHORT).show();
        });
    }

    private int Validation(){
        int count = 0;

        if(Objects.requireNonNull(phoneLayout.getEditText()).getText().toString().length() != 10) {
            phoneLayout.setErrorEnabled(true);
            phoneLayout.setError("Email is not valid");
            count++;
        }
        if(Objects.requireNonNull(passwordLayout.getEditText()).getText().toString().length() < 8) {
            passwordLayout.setErrorEnabled(true);
            passwordLayout.setError("Password must have 8 characters");
            count++;
        }

        return count;
    }

    public void ForgotPassword(View view) {
        startActivity(new Intent(Login.this, VerifyPassword.class));
    }
}