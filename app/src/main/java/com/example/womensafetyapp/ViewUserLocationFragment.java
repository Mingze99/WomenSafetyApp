package com.example.womensafetyapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ViewUserLocationFragment extends Fragment {

    private DatabaseReference mDatabase;

    private String userID;
    private RecyclerView mRecyclerView;
    private UserLocationAdapter mAdapter;

    private List<LocationsVisited> locationsVisitedList = new ArrayList<>();

    public ViewUserLocationFragment(String id){
        userID = id;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_user_location_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = view.findViewById(R.id.user_location_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        getData();
    }

    private void getData() {
        mDatabase = FirebaseDatabase.getInstance("https://womensafetyapplication-a9d7b-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference().child("Visited Locations").child(userID);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                locationsVisitedList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    LocationsVisited details = snapshot.getValue(LocationsVisited.class);
                    locationsVisitedList.add(details);
                }
                if (locationsVisitedList.size() == 0){
                    Toast.makeText(getActivity(), "No locations found!", Toast.LENGTH_LONG).show();
                    return;
                }
                Collections.reverse(locationsVisitedList);
                updateUI(locationsVisitedList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.toException().toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void updateUI(List<LocationsVisited> userLocationList) {
        mAdapter = new UserLocationAdapter(userLocationList);
        mRecyclerView.setAdapter(mAdapter);
    }

    private class UserLocationHolder extends RecyclerView.ViewHolder{
        private TextView dateTime, location;
        private LocationsVisited lVisited;

        public UserLocationHolder(View itemView){
            super(itemView);
            dateTime = itemView.findViewById(R.id.dateTime);
            location = itemView.findViewById(R.id.location);
        }

        public void bindUserLocation(LocationsVisited userLocation){
            lVisited = userLocation;
            dateTime.setText(String.format("%s%s", dateTime.getText(), lVisited.getTimestamp()));
            location.setText(String.format("%s%s", location.getText(), lVisited.getLocation()));
        }
    }

    private class UserLocationAdapter extends RecyclerView.Adapter<UserLocationHolder>{
        public List<LocationsVisited> mUserLocation;

        public UserLocationAdapter(List<LocationsVisited> locationsVisited){
            mUserLocation = locationsVisited;
        }

        @NonNull
        @Override
        public UserLocationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.visited_location_item, parent, false);
            return new UserLocationHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserLocationHolder holder, int position) {
            LocationsVisited userLocation = mUserLocation.get(position);
            holder.bindUserLocation(userLocation);
        }

        @Override
        public int getItemCount() {
            return mUserLocation.size();
        }
    }
}
