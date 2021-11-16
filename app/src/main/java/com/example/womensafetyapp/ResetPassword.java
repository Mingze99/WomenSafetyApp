package com.example.womensafetyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPassword extends AppCompatActivity {

    private EditText email_et;
    private Button resetPassword_btn;
    private ProgressBar mProgressBar;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        email_et = findViewById(R.id.resetPassword_email);
        resetPassword_btn = findViewById(R.id.resetPassword_btn);
        mProgressBar = findViewById(R.id.resetPassword_progress_bar);

        auth = FirebaseAuth.getInstance();

        resetPassword_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }

    private void resetPassword(){
        String email = email_et.getText().toString().trim();

        if (email.isEmpty()){
            email_et.setError("Email is required!");
            email_et.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            email_et.setError("Please provide valid email!");
            email_et.requestFocus();
            return;
        }


        mProgressBar.setVisibility(View.VISIBLE);
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    mProgressBar.setVisibility(View.GONE);
                    Toast.makeText(ResetPassword.this, "Check your email to reset your password!", Toast.LENGTH_LONG).show();
                    finish();
                }
                else{
                    mProgressBar.setVisibility(View.GONE);
                    Toast.makeText(ResetPassword.this, "Error! Please try again!", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }
}