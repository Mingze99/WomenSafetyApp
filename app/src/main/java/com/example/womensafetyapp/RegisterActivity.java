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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;

    private TextView tv_loginLink;
    private EditText et_name, et_email, et_password, et_repassword;
    private Button btn_reg;
    private ProgressBar mProgressBar;
    private RadioButton user_rb, guardian_rb;
    private String accountType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        btn_reg = findViewById(R.id.btn_register);
        btn_reg.setOnClickListener(this);
        tv_loginLink = findViewById(R.id.tv_loginLink);
        tv_loginLink.setOnClickListener(this);

        et_name = findViewById(R.id.et_register_name);
        et_email = findViewById(R.id.et_register_email);
        et_password = findViewById(R.id.et_register_password);
        et_repassword = findViewById(R.id.et_register_repassword);
        user_rb = findViewById(R.id.registerUser_rb);
        guardian_rb = findViewById(R.id.registerGuardian_rb);

        mProgressBar = findViewById(R.id.progress_bar);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_loginLink:
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                break;
            case R.id.btn_register:
                registerUser();
        }
    }

    private void registerUser() {
        String name = et_name.getText().toString().trim();
        String email = et_email.getText().toString().trim();
        String password = et_password.getText().toString().trim();
        String repassword = et_repassword.getText().toString().trim();

        if (name.isEmpty()){
            et_name.setError("Name is required");
            et_name.requestFocus();
            return;
        }

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

        if (repassword.isEmpty()){
            et_repassword.setError("Re-type password is required");
            et_repassword.requestFocus();
            return;
        }

        if (!repassword.equals(password)){
            et_repassword.setError("Password does not match");
            et_repassword.requestFocus();
            return;
        }

        if (user_rb.isChecked())
            accountType = "User";
        else
            accountType = "Guardian";

        mProgressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password) //create user using email and password
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){   //if user account created, then run
                            User user = new User(name, email);

                            //store user data into firebase
                            FirebaseDatabase.getInstance("https://womensafetyapplication-a9d7b-default-rtdb.asia-southeast1.firebasedatabase.app").getReference(accountType)
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){   //sign out user to prevent instant log in
                                        Toast.makeText(RegisterActivity.this, accountType + " Registered Successfully!", Toast.LENGTH_LONG).show();
                                        mAuth.signOut();
                                        mProgressBar.setVisibility(View.GONE);
                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    }
                                    else{
                                        Toast.makeText(RegisterActivity.this, "Failed To Register " + accountType + "! Try Again!", Toast.LENGTH_LONG).show();
                                        mProgressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                        else{
                            Toast.makeText(RegisterActivity.this, "Failed To Register User! Try Again!", Toast.LENGTH_LONG).show();
                            mProgressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }
}