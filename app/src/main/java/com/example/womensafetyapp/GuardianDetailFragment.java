package com.example.womensafetyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GuardianDetailFragment extends Fragment{
    private DatabaseReference mDatabase;

    private RecyclerView mGuardianDetailRV;
    private GuardianDetailAdapter mAdapter;

    private List<GuardianDetails> guardianDetailsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_guardian_detail_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mGuardianDetailRV = view.findViewById(R.id.guardian_detail_recycler_view);
        mGuardianDetailRV.setLayoutManager(new LinearLayoutManager(getActivity()));

        getData();
    }

    @Override
    public void onResume() {
        super.onResume();
        getData();
    }

    public void getData(){
        mDatabase = FirebaseDatabase.getInstance("https://womensafetyapplication-a9d7b-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference().child("GuardianDetails").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                guardianDetailsList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    GuardianDetails details = snapshot.getValue(GuardianDetails.class);
                    guardianDetailsList.add(details);
                }
                if (guardianDetailsList.size() == 0){
                    Toast.makeText(getActivity(), "No guardian details found!", Toast.LENGTH_SHORT).show();
                    return;
                }
                updateUI(guardianDetailsList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.toException().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(List<GuardianDetails> list){
        mAdapter = new GuardianDetailAdapter(list);
        mGuardianDetailRV.setAdapter(mAdapter);
    }

    private class GuardianDetailHolder extends RecyclerView.ViewHolder{
        private TextView mGuardianName,mGuardianID, mRelationship, mGuardianContact;
        private GuardianDetails gDetails;

        public GuardianDetailHolder(View itemView){
            super(itemView);
            mGuardianName = itemView.findViewById(R.id.item_guardian_name);
            mGuardianID = itemView.findViewById(R.id.item_ID);
            mRelationship = itemView.findViewById(R.id.item_relationship);
            mGuardianContact = itemView.findViewById(R.id.item_guardian_contact);

            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Intent intent = UpdateGuardianDetails.newIntent(getActivity(),gDetails.getGuardianID(),
                            gDetails.getInitial(), gDetails.getName(), gDetails.getRelationship()
                            , gDetails.getEmail(), gDetails.getContact());
                    startActivity(intent);
                }
            });
        }

        public void bindGuardianDetail(GuardianDetails guardianDetails){
            gDetails = guardianDetails;
            mGuardianID.setText(String.format("ID: %s", gDetails.getGuardianID()));
            mGuardianName.setText(String.format("%s %s", gDetails.getInitial(), gDetails.getName()));
            mRelationship.setText(String.format("Relationship: %s", gDetails.getRelationship()));
            mGuardianContact.setText(gDetails.getContact());
        }
    }

    private class GuardianDetailAdapter extends RecyclerView.Adapter<GuardianDetailHolder>{
        public List<GuardianDetails> mGuardianDetails;

        public GuardianDetailAdapter(List<GuardianDetails> guardianDetails) {
            mGuardianDetails = guardianDetails;
        }

        @NonNull
        @Override
        public GuardianDetailHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.guadian_detail_item,parent,false);
            return new GuardianDetailHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GuardianDetailHolder holder, int position) {
            GuardianDetails guardianDetails = mGuardianDetails.get(position);
            holder.bindGuardianDetail(guardianDetails);
        }

        @Override
        public int getItemCount() {
            return mGuardianDetails.size();
        }
    }


}
