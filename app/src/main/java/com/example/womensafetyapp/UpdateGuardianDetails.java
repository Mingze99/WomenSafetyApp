
package com.example.womensafetyapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class UpdateGuardianDetails extends AppCompatActivity {

    public static final String EXTRA_GUARDIAN_ID = "gdGuardianID";
    public static final String EXTRA_GUARDIAN_INITIAL = "gdInitial";
    public static final String EXTRA_GUARDIAN_NAME = "gdName";
    public static final String EXTRA_GUARDIAN_RELATIONSHIP = "gdRelationship";
    public static final String EXTRA_GUARDIAN_EMAIL = "gdEmail";
    public static final String EXTRA_GUARDIAN_CONTACT = "gdContact";

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
    private String currentID, currentInitial, currentName, currentRelationship, currentEmail, currentContact;

    private Date currentDateTime;

    public static Intent newIntent(Context packageContext, String gdGuardianID, String gdInitial, String gdName
            , String gdRelationship, String egdEmail, String gdContact){
        Intent intent = new Intent(packageContext, UpdateGuardianDetails.class);
        intent.putExtra(EXTRA_GUARDIAN_ID, gdGuardianID);
        intent.putExtra(EXTRA_GUARDIAN_INITIAL, gdInitial);
        intent.putExtra(EXTRA_GUARDIAN_NAME, gdName);
        intent.putExtra(EXTRA_GUARDIAN_RELATIONSHIP, gdRelationship);
        intent.putExtra(EXTRA_GUARDIAN_EMAIL, egdEmail);
        intent.putExtra(EXTRA_GUARDIAN_CONTACT, gdContact);
        return intent;
    }

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

        title_tv.setText(R.string.update_details);

        currentID = (String) getIntent().getSerializableExtra(EXTRA_GUARDIAN_ID);
        currentInitial = (String) getIntent().getSerializableExtra(EXTRA_GUARDIAN_INITIAL);
        currentName = (String) getIntent().getSerializableExtra(EXTRA_GUARDIAN_NAME);
        currentRelationship = (String) getIntent().getSerializableExtra(EXTRA_GUARDIAN_RELATIONSHIP);
        currentEmail = (String) getIntent().getSerializableExtra(EXTRA_GUARDIAN_EMAIL);
        currentContact = (String) getIntent().getSerializableExtra(EXTRA_GUARDIAN_CONTACT);

        functionBtn.setText(R.string.update_details);

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

        for (int x=0; x<mSpinner.getCount(); x++){
            if (mSpinner.getItemAtPosition(x).toString().equals(currentInitial)){
                mSpinner.setSelection(x);
            }
        }

        for (int x=0; x<relationshipSpinner.getCount(); x++){
            if (relationshipSpinner.getItemAtPosition(x).toString().equals(currentRelationship)){
                relationshipSpinner.setSelection(x);
            }
        }

        guardianID_et.setText(currentID);
        name_et.setText(currentName);
        email_et.setText(currentEmail);
        contact_et.setText(currentContact);

        functionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String guardianID = guardianID_et.getText().toString().trim();
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
                    Toast.makeText(UpdateGuardianDetails.this, "Please select an initial!", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(UpdateGuardianDetails.this, "Please select a relationship!", Toast.LENGTH_LONG).show();
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
                            updateDetails(gDetails);
                        }
                        else{
                            Toast.makeText(UpdateGuardianDetails.this, "Guardian not found!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UpdateGuardianDetails.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                confirmation();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void updateDetails(GuardianDetails gDetails){
        DatabaseReference ref = FirebaseDatabase.getInstance("https://womensafetyapplication-a9d7b-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();
        Query query = ref.child("GuardianDetails").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByChild("email").equalTo(currentEmail);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    dataSnapshot.getRef().setValue(gDetails);
                    Toast.makeText(UpdateGuardianDetails.this, "Guardian details successfully updated!", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateGuardianDetails.this, error.toException().toString(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    public void confirmation(){
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_question_mark_icon)
                .setTitle("Delete guardian's details")
                .setMessage("Are you sure you want to details the details?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference ref = FirebaseDatabase.getInstance("https://womensafetyapplication-a9d7b-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();
                        Query query = ref.child("GuardianDetails").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .orderByChild("email").equalTo(currentEmail);

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                                    dataSnapshot.getRef().removeValue();
                                    Toast.makeText(UpdateGuardianDetails.this, "Guardian details successfully deleted!", Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(UpdateGuardianDetails.this, error.toException().toString(), Toast.LENGTH_LONG).show();
                                finish();
                            }
                        });
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
