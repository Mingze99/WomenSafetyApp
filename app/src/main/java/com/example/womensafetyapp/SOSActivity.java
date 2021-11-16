package com.example.womensafetyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

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

public class SOSActivity extends AppCompatActivity {

    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;

    private String address;
    private List<GuardianDetails> guardianDetailsList = new ArrayList<>();
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabase2;
    User user;

    private TextView location_tv;
    private Chronometer timer;
    private Button stop_btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);
        location_tv = findViewById(R.id.displayLocation_tv);

        timer = findViewById(R.id.timer);
        timer.start();
        stop_btn = findViewById(R.id.stop_btn);

        //update location every 5 seconds
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10*1000);
        mLocationRequest.setFastestInterval(5*1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(SOSActivity.this
                , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mLocationCallback = new LocationCallback(){
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null){
                        Toast.makeText(SOSActivity.this, "Location not found! Please try again!", Toast.LENGTH_LONG).show();
                        return;
                    }
                    for (Location location : locationResult.getLocations()){
                        if (location != null){
                            try {
                                Geocoder geocoder = new Geocoder(SOSActivity.this, Locale.getDefault());
                                List<Address> addresses = geocoder.getFromLocation(
                                        location.getLatitude(), location.getLongitude(), 1);
                                location_tv.setText(addresses.get(0).getAddressLine(0));
                                //store address line in address variable
                                address = addresses.get(0).getAddressLine(0);

                                //get guardians contact number
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
                                            Toast.makeText(SOSActivity.this, "No guardian details found!", Toast.LENGTH_SHORT).show();
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
                                                for(GuardianDetails snapshot : guardianDetailsList){
                                                    SmsManager mySmsManager = SmsManager.getDefault();
                                                    mySmsManager.sendTextMessage(snapshot.getContact(), null, user.getName() + " is in danger \nTheir current location is: \n" + address, null, null);

                                                }
                                                Toast.makeText(SOSActivity.this, "SMS is successfully sent", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Toast.makeText(SOSActivity.this, error.toException().toString(), Toast.LENGTH_SHORT).show();
                                            }
                                        });



                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(SOSActivity.this, error.toException().toString(), Toast.LENGTH_SHORT).show();
                                    }

                                });
                                SmsManager mySmsManager = SmsManager.getDefault();

                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(SOSActivity.this, "Error! Please try again!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            };
            LocationServices.getFusedLocationProviderClient(SOSActivity.this).requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
        else {
            ActivityCompat.requestPermissions(SOSActivity.this
                    , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }

        stop_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.stop();
                LocationServices.getFusedLocationProviderClient(SOSActivity.this).removeLocationUpdates(mLocationCallback);
                finish();
            }
        });

    }
}