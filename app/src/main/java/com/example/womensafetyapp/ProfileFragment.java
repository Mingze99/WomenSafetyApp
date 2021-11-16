package com.example.womensafetyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    private TextView userID_tv, userName_tv, userEmail_tv;
    private Button logout_btn;
    private CardView cv;
    private String id, name, email;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        cv = view.findViewById(R.id.profile_cv);
        logout_btn = view.findViewById(R.id.logout_btn);
        userID_tv = view.findViewById(R.id.userID_tv);
        userName_tv = view.findViewById(R.id.userName_tv);
        userEmail_tv = view.findViewById(R.id.userEmail_tv);

        updateData();

        cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id = userID_tv.getText().toString().replace("ID: ", "");
                name = userName_tv.getText().toString().replace("Name: ", "");
                email = userEmail_tv.getText().toString().replace("Email: ", "");

                Intent intent = UpdateProfile.newIntent(getActivity(), id, name, email);
                startActivity(intent);
            }
        });

        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                if (FirebaseAuth.getInstance().getCurrentUser() == null){
                    Toast.makeText(getActivity(), "Logged out successfully!", Toast.LENGTH_LONG).show();
                    getActivity().finish();
                }
                else{
                    Toast.makeText(getActivity(), "Failed to logout! Please try again!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void getData(String key){
        mDatabase = FirebaseDatabase.getInstance("https://womensafetyapplication-a9d7b-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference().child(key).child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                userID_tv.setText(String.format("ID: %s", dataSnapshot.getKey()));
                userName_tv.setText(String.format("Name: %s", user.getName()));
                userEmail_tv.setText(String.format("Email: %s", user.getEmail()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.toException().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateData(){
        DatabaseReference ref = FirebaseDatabase.getInstance("https://womensafetyapplication-a9d7b-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Guardian");
        ref.orderByKey().equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    getData("Guardian");
                }
                else{
                    getData("User");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
