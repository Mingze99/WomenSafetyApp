package com.example.womensafetyapp;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddGuardianDetails extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private TextView title_tv;
    private Spinner mSpinner;
    private Spinner relationshipSpinner;
    private Button functionBtn;
    private Button deleteBtn;
    private Button cancelBtn;
    private EditText guardianID_et;
    private EditText name_et;
    private EditText email_et;
    private EditText contact_et;

    private Date currentDateTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guardian_details);

        mAuth = FirebaseAuth.getInstance();

        title_tv = findViewById(R.id.guardianDetailAction_tv);
        mSpinner = findViewById(R.id.spinner);
        relationshipSpinner = findViewById(R.id.relationship_sp);
        functionBtn = findViewById(R.id.function_btn);
        deleteBtn = findViewById(R.id.delete_btn);
        cancelBtn = findViewById(R.id.cancel_btn);
        guardianID_et = findViewById(R.id.guardianID_et);
        name_et = findViewById(R.id.guardianName_et);
        email_et = findViewById(R.id.guardianEmail_et);
        contact_et = findViewById(R.id.guardianContact_et);

        title_tv.setText(R.string.add_guardian_detail);
        currentDateTime = Calendar.getInstance().getTime();

        functionBtn.setText(R.string.add_guardian_detail);
        deleteBtn.setVisibility(View.GONE);

        List<String> spinnerArray = new ArrayList<String>();
        spinnerArray.add("None");
        spinnerArray.add("Mr.");
        spinnerArray.add("Ms.");

        List<String> relationshipArray = new ArrayList<String>();
        relationshipArray.add("None");
        relationshipArray.add("Parent");
        relationshipArray.add("Cousin");
        relationshipArray.add("Sibling");
        relationshipArray.add("Other");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, R.layout.spinner_item, spinnerArray
        );

        ArrayAdapter<String> relationshipAdapter = new ArrayAdapter<String>(
                this, R.layout.spinner_item, relationshipArray
        );

        mSpinner.setAdapter(adapter);
        relationshipSpinner.setAdapter(relationshipAdapter);

        functionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String guardianID = guardianID_et.getText().toString();
                String initial = mSpinner.getSelectedItem().toString();
                String name = name_et.getText().toString().trim();
                String relationship = relationshipSpinner.getSelectedItem().toString();
                String email = email_et.getText().toString().trim();
                String contact = contact_et.getText().toString().trim();

                if (guardianID.isEmpty()){
                    guardianID_et.setError("Guardian ID is required");
                    guardianID_et.requestFocus();
                    return;
                }

                if (initial.equals("None")){
                    TextView errorText = (TextView) mSpinner.getSelectedView();
                    errorText.setError("");
                    errorText.setTextColor(Color.RED);
                    errorText.setText("");
                    Toast.makeText(AddGuardianDetails.this, "Please select an initial!", Toast.LENGTH_LONG).show();
                    return;
                }

                if (name.isEmpty()){
                    name_et.setError("Name is required!");
                    name_et.requestFocus();
                    return;
                }

                if (!name.replace(" ", "").matches("^[A-Za-z]+$")){
                    name_et.setError("Name should only contain alphabets!");
                    name_et.requestFocus();
                    return;
                }

                if (relationship.equals("None")){
                    TextView errorText = (TextView) relationshipSpinner.getSelectedView();
                    errorText.setError("");
                    errorText.setTextColor(Color.RED);
                    errorText.setText(relationship);
                    Toast.makeText(AddGuardianDetails.this, "Please select a relationship!", Toast.LENGTH_LONG).show();
                    return;
                }

                if (email.isEmpty()){
                    email_et.setError("Email is required");
                    email_et.requestFocus();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    email_et.setError("Please provide a valid email");
                    email_et.requestFocus();
                    return;
                }

                if (contact.isEmpty()){
                    contact_et.setError("Contact No. is required!");
                    contact_et.requestFocus();
                    return;
                }

                if (!contact.matches("^[0-9]{3}-[0-9]{7,8}$")){
                    contact_et.setError("Invalid contact no. entered! Number format should be: 012-3456789");
                    contact_et.requestFocus();
                    return;
                }

                DatabaseReference ref = FirebaseDatabase.getInstance("https://womensafetyapplication-a9d7b-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Guardian");
                ref.orderByKey().equalTo(guardianID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            GuardianDetails gDetails = new GuardianDetails(guardianID, initial, name, relationship, email, contact);
                            addConnection(FirebaseAuth.getInstance().getCurrentUser().getUid(), guardianID, gDetails);
                        }
                        else{
                            Toast.makeText(AddGuardianDetails.this, "Guardian not found!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AddGuardianDetails.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void addDetails(GuardianDetails gDetails){
        FirebaseDatabase.getInstance("https://womensafetyapplication-a9d7b-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("GuardianDetails")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(currentDateTime.toString())
                .setValue(gDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(AddGuardianDetails.this, "Guardian details added!", Toast.LENGTH_LONG).show();
                    finish();
                }
                else {
                    Toast.makeText(AddGuardianDetails.this, "Failed to add guardian details!", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }

    public void addConnection(String userID, String guardianID, GuardianDetails gDetails){
        GuardianUserConnected connected = new GuardianUserConnected(userID, guardianID);

        FirebaseDatabase.getInstance("https://womensafetyapplication-a9d7b-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("User Guardian Connected")
                .child(currentDateTime.toString())
                .setValue(connected).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    addDetails(gDetails);
                }
                else {
                    Toast.makeText(AddGuardianDetails.this, "Failed to add guardian details!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
