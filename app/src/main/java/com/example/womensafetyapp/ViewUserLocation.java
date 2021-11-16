package com.example.womensafetyapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewUserLocation extends Fragment {
    private DatabaseReference mDatabase;

    private Spinner user_sp;

    private List<String> userID = new ArrayList<>();
    private List<String> userName = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.display_user_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        user_sp = view.findViewById(R.id.user_sp);

        mDatabase = FirebaseDatabase.getInstance("https://womensafetyapplication-a9d7b-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference().child("User Guardian Connected");

        //Get all userID connected to the guardian
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userID.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    GuardianUserConnected connected = snapshot.getValue(GuardianUserConnected.class);
                    if (connected.getGuardianID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        userID.add(connected.getUserID());
                    }
                }
                if (userID.size() == 0){
                    Toast.makeText(getActivity(), "No connected users found!", Toast.LENGTH_SHORT).show();
                    return;
                }
                getUserName(userID);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.toException().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        user_sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (user_sp.getItemAtPosition(position).toString().equals("Select User"))
                    return;
                getUserID(user_sp.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void getUserID(String userName) {
        DatabaseReference ref = FirebaseDatabase.getInstance("https://womensafetyapplication-a9d7b-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();
        Query query = ref.child("User").orderByChild("name").equalTo(userName);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){

                    Fragment fragment = new ViewUserLocationFragment(dataSnapshot.getKey());
                    FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

                    transaction.replace(R.id.user_location_fragment_container, fragment);
                    transaction.commit();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.toException().toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void occupySpinner(List<String> userName) {
        List<String> userArray = new ArrayList<>();
        userArray.add("Select User");

        for (String name : userName){
            userArray.add(name);
        }


        ArrayAdapter adapter = new ArrayAdapter<>(
                getActivity(), R.layout.spinner_item, userArray
        );

        user_sp.setAdapter(adapter);
    }

    public void getUserName(List<String> userID){
        mDatabase = FirebaseDatabase.getInstance("https://womensafetyapplication-a9d7b-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference().child("User");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userName.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    for (String id : userID){
                        if (snapshot.getKey().equals(id)){
                            userName.add(user.getName());
                        }
                    }
                    occupySpinner(userName);
                }
                if (userName.size() == 0){
                    Toast.makeText(getActivity(), "No connected users found!", Toast.LENGTH_SHORT).show();
                    return;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.toException().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}