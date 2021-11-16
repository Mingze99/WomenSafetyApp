package com.example.womensafetyapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class UserFragmentAdapter extends FragmentStateAdapter {
    public UserFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 1:
                return new VisitedLocationMain();
            case 2:
                return new GuardianDetailMain();
            case 3:
                return new ProfileFragment();
        }
        return new EmergencyFragment();
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
