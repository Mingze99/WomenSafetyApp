package com.example.womensafetyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.tabs.TabLayout;


public class UserLaunchPage extends AppCompatActivity {

    TabLayout mTabLayout;
    ViewPager2 pager2;
    UserFragmentAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTabLayout = findViewById(R.id.tab_layout);
        pager2 = findViewById(R.id.view_pager2);

        pager2.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                if (position < -1){    // [-Infinity,-1)
                    // This page is way off-screen to the left.
                    page.setAlpha(0);

                }
                else if (position <= 0){    // [-1,0]
                    page.setAlpha(1);
                    page.setTranslationX(0);
                    page.setScaleX(1);
                    page.setScaleY(1);
                }
                else if (position <= 1){    // (0,1]
                    page.setTranslationX(-position*page.getWidth());
                    page.setAlpha(1-Math.abs(position));
                    page.setScaleX(1-Math.abs(position));
                    page.setScaleY(1-Math.abs(position));
                }
                else {    // (1,+Infinity]
                    // This page is way off-screen to the right.
                    page.setAlpha(0);
                }
            }
        });

        FragmentManager fm = getSupportFragmentManager();
        mAdapter = new UserFragmentAdapter(fm, getLifecycle());
        pager2.setAdapter(mAdapter);

        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.sos));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.check_in));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.guardian_details));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.profile));

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        pager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                mTabLayout.selectTab(mTabLayout.getTabAt(position));
            }
        });
    }
}