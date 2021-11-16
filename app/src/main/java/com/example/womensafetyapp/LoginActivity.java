package com.example.womensafetyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView register, resetPassword;
    private EditText et_email, et_password;
    private Button signIn;
    private String accountType;
    private RadioButton user_rb, guardian_rb;
    private ProgressBar mProgressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        register = findViewById(R.id.tv_registerLink);
        register.setOnClickListener(this);

        resetPassword = findViewById(R.id.tv_forgotPasswordLink);
        resetPassword.setOnClickListener(this);

        signIn = findViewById(R.id.btn_login);
        signIn.setOnClickListener(this);

        et_email = findViewById(R.id.et_login_email);
        et_password = findViewById(R.id.et_login_password);
        user_rb = findViewById(R.id.loginUser_rb);
        guardian_rb = findViewById(R.id.loginGuardian_rb);
        mProgressBar = findViewById(R.id.login_progress_bar);

        mAuth = FirebaseAuth.getInstance();

        //auto log in users if did not log out
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            mProgressBar.setVisibility(View.VISIBLE);
            DatabaseReference ref = FirebaseDatabase.getInstance("https://womensafetyapplication-a9d7b-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Guardian");
            ref.orderByKey().equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        mProgressBar.setVisibility(View.GONE);
                        startActivity(new Intent(LoginActivity.this, GuardianLaunchPage.class));
                    }
                    else{
                        mProgressBar.setVisibility(View.GONE);
                        startActivity(new Intent(LoginActivity.this, UserLaunchPage.class));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    mProgressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_registerLink:
                startActivity(new Intent(this, RegisterActivity.class));
                break;

            case R.id.btn_login:
                userLogin();
                break;

            case R.id.tv_forgotPasswordLink:
                startActivity(new Intent(this, ResetPassword.class));
        }
    }

    private void userLogin(){
        String email = et_email.getText().toString().trim();
        String password = et_password.getText().toString().trim();

        if (email.isEmpty()){
            et_email.setError("Email is required");
            et_email.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            et_email.setError("Please provide a valid email");
            et_email.requestFocus();
            return;
        }

        if (password.isEmpty()){
            et_password.setError("Password is required");
            et_password.requestFocus();
            return;
        }

        if (password.length() < 6){
            et_password.setError("Password should contain at least 6 characters");
            et_password.requestFocus();
            return;
        }

        if (user_rb.isChecked())
            accountType = "User";
        else
            accountType = "Guardian";

        //check if account exist
        DatabaseReference ref = FirebaseDatabase.getInstance("https://womensafetyapplication-a9d7b-default-rtdb.asia-southeast1.firebasedatabase.app").getReference(accountType);
        ref.orderByChild("email").equalTo(email).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    login(email, password);
                }
                else{
                    Toast.makeText(LoginActivity.this, accountType + " not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void login(String email, String password){
        //log in using email and password
        mProgressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //start activity based on account type
                        if (task.isSuccessful()){
                            if (user_rb.isChecked()) {
                                mProgressBar.setVisibility(View.GONE);
                                startActivity(new Intent(LoginActivity.this, UserLaunchPage.class));
                            }
                            else{
                                mProgressBar.setVisibility(View.GONE);
                                startActivity(new Intent(LoginActivity.this, GuardianLaunchPage.class));
                            }
                        }
                        else{
                            //incorrect password
                            mProgressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this
                                    , "Failed to login! Please check your credentials"
                                    , Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}