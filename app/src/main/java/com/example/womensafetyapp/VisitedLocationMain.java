package com.example.womensafetyapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VisitedLocationMain extends Fragment {
    private Button checkInBtn;
    private String timeStamp, currentDateTime;

    LocationRequest mLocationRequest;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.display_visited_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        checkInBtn = view.findViewById(R.id.checkIn_btn);
        checkInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getActivity()
                        , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    LocationCallback mLocationCallback = new LocationCallback(){
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            if (locationResult == null){
                                Toast.makeText(getActivity(), "Location not found! Please try again!", Toast.LENGTH_LONG).show();
                                return;
                            }
                            for (Location location : locationResult.getLocations()){
                                if (location != null){
                                    try {
                                        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                                        List<Address> addresses = geocoder.getFromLocation(
                                                location.getLatitude(), location.getLongitude(), 1);
                                        storeData(addresses.get(0).getAddressLine(0));

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
                            , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                }
            }
        });

        FragmentManager fm = getChildFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.location_fragment_container);

        if (fragment == null){
            fragment = new VisitedLocationFragment();
            fm.beginTransaction()
                    .add(R.id.location_fragment_container, fragment)
                    .commit();
        }
    }

    private void storeData(String location) {
        timeStamp = Calendar.getInstance().getTime().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy  HH:mm:ss");
        currentDateTime = sdf.format(new Date());
        LocationsVisited details = new LocationsVisited(location, currentDateTime);

        FirebaseDatabase.getInstance("https://womensafetyapplication-a9d7b-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Visited Locations")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(timeStamp)
                .setValue(details).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(getActivity(), "Checked-in successfully", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getActivity(), "Failed to check-in!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
