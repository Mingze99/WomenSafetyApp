package com.example.womensafetyapp;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class locationService extends Service {

    private static final String TAG = "locationService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "HI", Toast.LENGTH_SHORT).show();
        CountDownTimer countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.e(TAG, "onTick: " + millisUntilFinished/1000 );
            }

            @Override
            public void onFinish() {
                Log.e(TAG, "onFinish: " );
            }
        }.start();
        return START_STICKY;
    }
}
