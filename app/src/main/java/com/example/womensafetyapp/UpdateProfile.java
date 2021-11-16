package com.example.womensafetyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UpdateProfile extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser user;

    public static final String EXTRA_USER_ID = "userID";
    public static final String EXTRA_USER_NAME = "userName";
    public static final String EXTRA_USER_EMAIL = "userEmail";

    private TextView userID_tv;
    private EditText userName_et, userEmail_et;
    private String currentUserID, currentUserName, currentUserEmail;
    public String newUserName, newUserEmail;
    private Button saveChanges_btn, cancel_btn;

    public static Intent newIntent(Context packageContext, String uid, String name, String email){
        Intent intent = new Intent(packageContext, UpdateProfile.class);
        intent.putExtra(EXTRA_USER_ID, uid);
        intent.putExtra(EXTRA_USER_NAME, name);
        intent.putExtra(EXTRA_USER_EMAIL, email);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        user = FirebaseAuth.getInstance().getCurrentUser();

        userID_tv = findViewById(R.id.update_userID);
        userName_et = findViewById(R.id.update_userName);
        userEmail_et = findViewById(R.id.update_userEmail);
        saveChanges_btn = findViewById(R.id.save_changes_btn);
        cancel_btn = findViewById(R.id.cancel_btn);

        currentUserID = (String) getIntent().getSerializableExtra(EXTRA_USER_ID);
        currentUserName = (String) getIntent().getSerializableExtra(EXTRA_USER_NAME);;
        currentUserEmail = (String) getIntent().getSerializableExtra(EXTRA_USER_EMAIL);;

        userID_tv.setText(String.format("ID: %s", currentUserID));
        userName_et.setText(currentUserName);
        userEmail_et.setText(currentUserEmail);

        saveChanges_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfileDetails();
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void reLogin(){
        if (!currentUserEmail.equals(newUserEmail)){
            mAuth = FirebaseAuth.getInstance();
            mAuth.signOut();
            Toast.makeText(UpdateProfile.this, "Please login with new email!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(UpdateProfile.this, LoginActivity.class));
        }
    }

    public void updateData(String accountType, String name, String email){
        User user = new User(name, email);

        FirebaseDatabase.getInstance("https://womensafetyapplication-a9d7b-default-rtdb.asia-southeast1.firebasedatabase.app").getReference(accountType)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    reLogin();
                }
                else{
                    Toast.makeText(UpdateProfile.this, "Failed to update profile details", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void updateDatabase(String name, String email){
        DatabaseReference ref = FirebaseDatabase.getInstance("https://womensafetyapplication-a9d7b-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Guardian");
        ref.orderByKey().equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    updateData("Guardian", name, email);
                }
                else{
                    updateData("User", name, email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateProfile.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateProfileDetails(){
        newUserName = userName_et.getText().toString();
        newUserEmail = userEmail_et.getText().toString();

        if (newUserName.isEmpty()){
            userName_et.setError("Name is required");
            userName_et.requestFocus();
            return;
        }

        if (!newUserName.replace(" ", "").matches("[a-zA-Z]+")){
            userName_et.setError("Name should only contain alphabets");
            userName_et.requestFocus();
            return;
        }

        if (newUserEmail.isEmpty()){
            userEmail_et.setError("Email is required");
            userEmail_et.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(newUserEmail).matches()){
            userEmail_et.setError("Please provide a valid email");
            userEmail_et.requestFocus();
            return;
        }

        if ((currentUserName.equals(newUserName) && currentUserEmail.equals(newUserEmail))){
            finish();
        }
        else{
            if (currentUserEmail.equals(newUserEmail)) {
                updateDatabase(newUserName, newUserEmail);
                Toast.makeText(UpdateProfile.this, "User details successfully updated!", Toast.LENGTH_SHORT).show();
                finish();
            }
            else{
                user.updateEmail(newUserEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            updateDatabase(newUserName, newUserEmail);
                        }
                        else{
                            Toast.makeText(UpdateProfile.this, "Please logout and login again to change email", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                });
            }
        }
    }
}