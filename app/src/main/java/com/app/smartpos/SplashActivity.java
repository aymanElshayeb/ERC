package com.app.smartpos;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.app.smartpos.auth.AuthActivity;
import com.app.smartpos.database.DatabaseAccess;

import org.apache.http.auth.AUTH;

public class SplashActivity extends AppCompatActivity {


    public static int splashTimeOut = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        String endDateString = databaseAccess.getLastShift("end_date_time");
        Log.i("end_date", endDateString);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, AuthActivity.class);
                startActivity(intent);
                finish();
            }
        }, splashTimeOut);
    }
}

