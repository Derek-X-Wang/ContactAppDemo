package com.derekxw.contactlist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import com.badoo.mobile.util.WeakHandler;
import com.derekxw.contactlist.utils.Fader;

/**
 *
 * Created by Derek on 5/17/2017.
 */

public class SplashActivity extends Activity {
    private final static String TAG = "SplashActivity";
    private static int SPLASH_TIME_OUT = 2000;

    private WeakHandler handler = new WeakHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Fader.runAlphaAnimation(this, R.id.splash_icon);
        handler.postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
