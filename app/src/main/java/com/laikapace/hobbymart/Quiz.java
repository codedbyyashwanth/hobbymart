package com.laikapace.hobbymart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

public class Quiz extends AppCompatActivity {

    TextView questionCountView, questionView;
    FirebaseUser user;
    DatabaseReference questionReference;
    String phoneNumber;
    Button nextBtn, checkAnswer;
    AppCompatButton option1, option2, option3, option4, selectedButton;
    String selectedAnswer;
    int totalQuestions, index = 1;
    String correctOption;
    ArrayList<AppCompatButton> buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        questionCountView = findViewById(R.id.question_count);
        questionView = findViewById(R.id.question);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        nextBtn = findViewById(R.id.next);
        checkAnswer = findViewById(R.id.checkAnswer);
        buttons = new ArrayList<>(Arrays.asList(option1, option2, option3, option4));

        user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        phoneNumber = user.getPhoneNumber();

        questionReference = FirebaseDatabase.getInstance().getReference("quiz");
        DisplayQuestions(phoneNumber, questionReference);

        getWindow().setStatusBarColor(getResources().getColor(R.color.blueish, this.getTheme()));
    }

    private void DisplayQuestions(String phoneNumber, DatabaseReference questionReference) {
        questionReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("completion").child(phoneNumber).exists()) {
                    startActivity(new Intent(Quiz.this, Home.class));
                    Toast.makeText(Quiz.this, "You have completed the Quiz", Toast.LENGTH_SHORT).show();
                } else {
                    DataSnapshot questionsSnapshot = snapshot.child("questions");
                    totalQuestions = (int) questionsSnapshot.getChildrenCount();
                    DataSnapshot options = questionsSnapshot.child(index + "");

                    if (options.child("answered").child(phoneNumber).exists()) {
                        index++;
                        DisplayQuestions(phoneNumber, questionReference);
                    } else  {
                        String question = questionsSnapshot.child(index + "").child("title").getValue(String.class);
                        String optionA, optionB, optionC, optionD;
                        optionA = options.child("option").child("A").getValue(String.class);
                        assert optionA != null;
                        optionA = Character.toUpperCase(optionA.charAt(0)) + optionA.substring(1);
                        optionB = options.child("option").child("B").getValue(String.class);
                        assert optionB != null;
                        optionB = Character.toUpperCase(optionB.charAt(0)) + optionB.substring(1);
                        optionC = options.child("option").child("C").getValue(String.class);
                        assert optionC != null;
                        optionC = Character.toUpperCase(optionC.charAt(0)) + optionC.substring(1);
                        optionD = options.child("option").child("D").getValue(String.class);
                        assert optionD != null;
                        optionD = Character.toUpperCase(optionD.charAt(0)) + optionD.substring(1);
                        questionView.setText(question);
                        option1.setText("A. " + optionA);
                        option2.setText("B. " + optionB);
                        option3.setText("C. " + optionC);
                        option4.setText("D. " + optionD);
                        questionCountView.setText("Question " + index + " out of " + totalQuestions);
                        correctOption = questionsSnapshot.child(index + "").child("answer").getValue(String.class);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void Back(View view) {
        finish();
    }

    public void CheckAnswer(View view) {
        HashMap<String, String> hashMap = new HashMap<>();

            try {
                if (correctOption.trim().equalsIgnoreCase(selectedAnswer.trim())) {
                    selectedButton.setTextColor(getColor(R.color.whitest));
                    selectedButton.setBackgroundResource(R.drawable.btn_border_correct);
                    hashMap.put("result", "correct");
                } else {
                    selectedButton.setTextColor(getColor(R.color.whitest));
                    selectedButton.setBackgroundResource(R.drawable.btn_border_incorrect);
                    hashMap.put("result", "wrong");
                }

                for (AppCompatButton button: buttons) {
                    button.setClickable(false);
                }

                hashMap.put("answer", selectedAnswer.trim());
                hashMap.put("id", phoneNumber);
                questionReference.child("questions").child(index + "").child("answered").child(phoneNumber).setValue(hashMap);
                nextBtn.setVisibility(View.VISIBLE);
                view.setEnabled(false);
            } catch (Exception e) {

            }
    }

    public void OptionSelected(View view) {
        for (AppCompatButton button : buttons) {
            button.setTextColor(getColor(R.color.whitest));
            button.setBackgroundResource(R.drawable.btn_border_transparent);
        }
        ((Button)view).setTextColor(getColor(R.color.blueish));
        view.setBackgroundResource(R.drawable.btn_border_white);
        selectedButton = (AppCompatButton) view;
        selectedAnswer = view.getTag().toString();
    }

    @SuppressLint("SetTextI18n")
    public void NextQuestion(View view) {
        if (index == totalQuestions) {
            questionReference.child("completion").child(phoneNumber).child("id").setValue(phoneNumber);
            Intent intent = new Intent(Quiz.this, Congratz.class);
            intent.putExtra("no", phoneNumber);
            intent.putExtra("totalQuestions", totalQuestions + "");
            startActivity(intent);
            finish();
        } else if ((index + 1) == totalQuestions) {
            index++;
            nextBtn.setText("Done");
            DisplayQuestions(phoneNumber, questionReference);
        } else {
            index++;
            DisplayQuestions(phoneNumber, questionReference);
        }

        for (AppCompatButton button : buttons) {
            button.setTextColor(getColor(R.color.whitest));
            button.setBackgroundResource(R.drawable.btn_border_transparent);
        }

        for (AppCompatButton button: buttons) {
            button.setClickable(true);
        }

        view.setVisibility(View.GONE);
        checkAnswer.setEnabled(true);
    }
}