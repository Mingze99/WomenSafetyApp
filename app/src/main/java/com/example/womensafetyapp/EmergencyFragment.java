package com.example.womensafetyapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EmergencyFragment extends Fragment {

    private DatabaseReference mDatabase;
    private DatabaseReference mDatabase2;

    private final int REQUESTCODE = 1;
    private String address;
    private Button btn_sendLocation;
    private Button btn_fakeCall;
    private Button btn_sos;
    private List<GuardianDetails> guardianDetailsList = new ArrayList<>();
    private User user;

    LocationRequest mLocationRequest;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_emergency, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        btn_sendLocation = view.findViewById(R.id.btn_sendLocation);
        btn_fakeCall = view.findViewById(R.id.btn_fake_call);
        btn_sos = view.findViewById(R.id.btn_sos);

        //request 3 permissions from the users
        ActivityCompat.requestPermissions(getActivity()
                , new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS}, 44);


        //start SOS activity
        btn_sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SOSActivity.class));
            }
        });

        //get current location and send to the guardians
        btn_sendLocation.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                 if (ActivityCompat.checkSelfPermission(getActivity()
                            , Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getActivity()
                            , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    LocationCallback mLocationCallback = new LocationCallback(){
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            if (locationResult == null){
                                Toast.makeText(getActivity(), "Location not found! Please try again!", Toast.LENGTH_LONG).show();
                                return;
                            }
                            else{
                                ActivityCompat.requestPermissions(getActivity()
                                        , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                            }
                            for (Location location : locationResult.getLocations()){
                                if (location != null){
                                    try {
                                        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                                        List<Address> addresses = geocoder.getFromLocation(
                                                location.getLatitude(), location.getLongitude(), 1);
                                        address = addresses.get(0).getAddressLine(0);
                                        sendSMS(v);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        Toast.makeText(getActivity(), "Error! Please try again!", Toast.LENGTH_SHORT).show();
                                    }
                                }

                            }
                        }
                    };
                    LocationServices.getFusedLocationProviderClient(getActivity()).requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                }
                else {
                    ActivityCompat.requestPermissions(getActivity()
                            , new String[]{Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                    LocationCallback mLocationCallback = new LocationCallback(){
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            if (locationResult == null){
                                Toast.makeText(getActivity(), "Location not found! Please try again!", Toast.LENGTH_LONG).show();
                                return;
                            }
                            else{
                                ActivityCompat.requestPermissions(getActivity()
                                        , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                            }
                            for (Location location : locationResult.getLocations()){
                                if (location != null){
                                    try {
                                        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                                        List<Address> addresses = geocoder.getFromLocation(
                                                location.getLatitude(), location.getLongitude(), 1);
                                        address = addresses.get(0).getAddressLine(0);
                                        sendSMS(v);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        Toast.makeText(getActivity(), "Error! Please try again!", Toast.LENGTH_SHORT).show();
                                    }
                                }

                            }
                        }
                    };
                    LocationServices.getFusedLocationProviderClient(getActivity()).requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                }
            }
        });

        //run dial function
        btn_fakeCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dial();
            }
        });
    }

    public void sendSMS(View view) {
        //get contact number
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

                //get user's name
                mDatabase2 = FirebaseDatabase.getInstance("https://womensafetyapplication-a9d7b-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .getReference().child("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                mDatabase2.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user_temp = dataSnapshot.getValue(User.class);

                        user = new User(user_temp.getName(),user_temp.getEmail());
                        if(guardianDetailsList.size() == 0){
                            Toast.makeText(getActivity(), "No guardian found!", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            //send sms to the guardians
                            for(GuardianDetails snapshot : guardianDetailsList){
                                SmsManager mySmsManager = SmsManager.getDefault();
                                mySmsManager.sendTextMessage(snapshot.getContact(), null, user.getName() + " is sharing location with you: \n" + address, null, null);
                                Toast.makeText(getActivity(), "SMS is successfully sent", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getActivity(), error.toException().toString(), Toast.LENGTH_SHORT).show();
                    }
                });



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.toException().toString(), Toast.LENGTH_SHORT).show();
            }

        });



    }

    public void dial(){
        //call 999
        if (ActivityCompat.checkSelfPermission(getActivity()
                , Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity()
                    , new String[]{Manifest.permission.CALL_PHONE}, PackageManager.PERMISSION_GRANTED);
        }
        else {
            String phoneNumber = "999";
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:"+ phoneNumber));
            startActivity(intent);
        }
    }
}