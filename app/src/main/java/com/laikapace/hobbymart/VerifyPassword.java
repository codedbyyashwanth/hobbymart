package com.laikapace.hobbymart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
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

public class VerifyPassword extends AppCompatActivity {

    TextInputLayout phoneLayout;
    FirebaseAuth auth;
    DatabaseReference database;
    String otpSent, phone;
    PhoneAuthProvider.ForceResendingToken token;
    BottomSheetDialog bottomSheetDialog;
    View view;
    PinView otp;
    AlertDialog.Builder builder;
    AlertDialog dialog;
    BroadcastReceiver Receiver;
    boolean Clicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_password);

        phoneLayout = findViewById(R.id.phone_layout);

        // Layout initialization for OTP Verification
        bottomSheetDialog = new BottomSheetDialog(VerifyPassword.this, R.style.AppBottomSheetDialogTheme);
        view = getLayoutInflater().inflate(R.layout.bottomsheet, null);
        otp = view.findViewById(R.id.otp_code);

        // Initializing the Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference().child("Users");

        // Disable Error while Typing
        DisableError(phoneLayout);
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

    public void Verify(View view) {
        if (Validation() == 0) {        // Checking weather the input fields are valid
            phone = "+91" + Objects.requireNonNull(phoneLayout.getEditText()).getText().toString();

            // Query to check whether phone number exits or not
            Query query = database.orderByChild("phone").equalTo(phone);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                                .setPhoneNumber(phone)
                                .setActivity(VerifyPassword.this)
                                .setTimeout(30L, TimeUnit.SECONDS)
                                .setCallbacks(mCallbacks)
                                .build();

                        PhoneAuthProvider.verifyPhoneNumber(options);
                        Loading();
                    } else {
                        Toast.makeText(VerifyPassword.this, "Phone Number Not Registered", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private int Validation() {
        if (Objects.requireNonNull(phoneLayout.getEditText()).getText().toString().length() != 10) {
            phoneLayout.setErrorEnabled(true);
            phoneLayout.setError("Number is Invalid");
            return 1;
        }
        return 0;
    }

    @SuppressLint("SetTextI18n")
    private void Loading() {
        builder = new AlertDialog.Builder(VerifyPassword.this);
        View view1 = getLayoutInflater().inflate(R.layout.loading_view, null);
        TextView msg = view1.findViewById(R.id.msg);
        msg.setText("Wait for the OTP");
        builder.setView(view1);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            // Nothing to do here
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            // Nothing to do here
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            otpSent = s;
            token = forceResendingToken;
            Toast.makeText(VerifyPassword.this, "OTP has sent", Toast.LENGTH_SHORT).show();
            verifyOTP();
        }
    };

    private void verifyOTP() {
        Button verifyBtn = view.findViewById(R.id.verifyBtn);
        TextView number = view.findViewById(R.id.number);
        assert phone != null;
        number.setText(phone);
        verifyBtn.setOnClickListener(view1 -> {
            if(Objects.requireNonNull(otp.getText()).toString().length() > 0){
                verifyNumber(otp.getText().toString());
            }
            else
                Toast.makeText(VerifyPassword.this, "Enter valid OTP", Toast.LENGTH_SHORT).show();
        });
        dialog.dismiss();
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
        if(bottomSheetDialog.isShowing()) {
            AutoReceiveOTP();
            ResendOTP();
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
                .setActivity(VerifyPassword.this)
                .setPhoneNumber(phone)
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

    private void AutoReceiveOTP() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        Receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                for(SmsMessage sms:messages){
                    String message = sms.getMessageBody();
                    String otpCode = message.substring(0, 6);
                    try {
                        Integer.parseInt(otpCode);
                        otp.setText(otpCode);
                        dialog.show();
                        verifyNumber(otpCode);
                        Toast.makeText(context, "" + otp, Toast.LENGTH_SHORT).show();
                        break;
                    } catch (NumberFormatException e){
                        e.printStackTrace();
                    }
                }
            }
        };

        registerReceiver(Receiver, filter);
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
                Toast.makeText(this, "Verification Successfully", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
                dialog.dismiss();
                NewPassword();
            }
            else
                Toast.makeText(VerifyPassword.this, "Failed to register", Toast.LENGTH_SHORT).show();
        });
    }

    private void NewPassword() {
        BottomSheetDialog passwordDialog = new BottomSheetDialog(VerifyPassword.this, R.style.AppBottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.create_password, null);

        TextInputLayout newPasswordLayout = view.findViewById(R.id.password_layout);
        TextInputLayout  newConfirmLayout = view.findViewById(R.id.confirm_layout);
        Button createBtn = view.findViewById(R.id.createBtn);

        DisableError(newPasswordLayout);
        DisableError(newConfirmLayout);

        createBtn.setOnClickListener(view1 -> {
            if (ValidPassword(newPasswordLayout, newConfirmLayout) == 0){
                String password = Objects.requireNonNull(newPasswordLayout.getEditText()).getText().toString();
                String encryptedPassword = Crypto.encryptAndEncode(password);
                database.child(phone).child("password").setValue(encryptedPassword);
                Toast.makeText(VerifyPassword.this, "Password Changed", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(VerifyPassword.this, Home.class));
                finish();
            }
        });

        passwordDialog.setContentView(view);
        passwordDialog.show();
    }

    private int ValidPassword(TextInputLayout newPasswordLayout, TextInputLayout newConfirmLayout) {
        int[] count = new int[2];
        String password = Objects.requireNonNull(newPasswordLayout.getEditText()).getText().toString();

        if(password.length() < 8) {
            ErrorMsg("Password must have 8 characters", newPasswordLayout);
            count[0]++;
        }

        if(Objects.requireNonNull(newConfirmLayout.getEditText()).getText().toString().length() < 8 ||
                !newConfirmLayout.getEditText().getText().toString().equals(Objects.requireNonNull(newPasswordLayout.getEditText()).getText().toString())
        ) {
            ErrorMsg("Confirm Password is wrong", newConfirmLayout);
            count[0]++;
        }

        return count[0];
    }

    public void ErrorMsg(String msg, TextInputLayout inputLayout){
        inputLayout.setErrorEnabled(true);
        inputLayout.setError(msg);
    }
}