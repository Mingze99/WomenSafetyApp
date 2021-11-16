package com.example.womensafetyapp;

import android.location.Location;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class VisitedLocationFragment extends Fragment {
    private DatabaseReference mDatabase;

    private RecyclerView mVisitedLocationRV;
    private VisitedLocationAdapter mAdapter;

    private List<LocationsVisited> locationsVisitedList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visited_location_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mVisitedLocationRV = view.findViewById(R.id.visited_location_recycler_view);
        mVisitedLocationRV.setLayoutManager(new LinearLayoutManager(getActivity()));

        getData();
    }

    public void getData() {
        //get check in details from firebase
        mDatabase = FirebaseDatabase.getInstance("https://womensafetyapplication-a9d7b-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference().child("Visited Locations").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                locationsVisitedList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    LocationsVisited details = snapshot.getValue(LocationsVisited.class);
                    locationsVisitedList.add(details);
                }
                if (locationsVisitedList.size() == 0){
                    Toast.makeText(getActivity(), "No visited locations found!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Collections.reverse(locationsVisitedList);
                updateUI(locationsVisitedList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.toException().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateUI(List<LocationsVisited> locationsVisitedList) {
        mAdapter = new VisitedLocationAdapter(locationsVisitedList);
        mVisitedLocationRV.setAdapter(mAdapter);

        //allow card view to swipe left or right
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                DatabaseReference ref = FirebaseDatabase.getInstance("https://womensafetyapplication-a9d7b-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();
                Query query = ref.child("Visited Locations").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .orderByChild("timestamp").equalTo(locationsVisitedList.get(viewHolder.getAdapterPosition()).getTimestamp());

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                            //delete check in details when swiping left or right
                            dataSnapshot.getRef().removeValue();
                            Toast.makeText(getActivity(), "Check-in details successfully deleted!", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getActivity(), error.toException().toString(), Toast.LENGTH_LONG).show();
                    }
                });
                locationsVisitedList.remove(viewHolder.getAdapterPosition());
                mAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            }
        };

        //attach itemTouchHelper to recyclerview
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mVisitedLocationRV);
    }

    //display each check in details with viewholder
    private class VisitedLocationHolder extends RecyclerView.ViewHolder{
        private TextView dateTime, location;
        private LocationsVisited lVisited;


        public VisitedLocationHolder(View itemView){
            super(itemView);
            dateTime = itemView.findViewById(R.id.dateTime);
            location = itemView.findViewById(R.id.location);
        }

        public void bindVisitedLocation(LocationsVisited locationsVisited){
            lVisited = locationsVisited;
            dateTime.setText(String.format("%s%s", dateTime.getText(), lVisited.getTimestamp()));
            location.setText(String.format("%s%s", location.getText(), lVisited.getLocation()));
        }
    }

    //responsible to inflate all check in details into viewholder
    private class VisitedLocationAdapter extends RecyclerView.Adapter<VisitedLocationHolder>{
        public List<LocationsVisited> mLocationsVisited;

        public VisitedLocationAdapter(List<LocationsVisited> locationsVisited){
            mLocationsVisited = locationsVisited;
        }

        @NonNull
        @Override
        public VisitedLocationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.visited_location_item,parent,false);
            return new VisitedLocationHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VisitedLocationHolder holder, int position) {
            LocationsVisited locationsVisited = mLocationsVisited.get(position);
            holder.bindVisitedLocation(locationsVisited);
        }

        @Override
        public int getItemCount() {
            return mLocationsVisited.size();
        }
    }
}
