package com.app.smartpos.auth;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.app.smartpos.R;
import com.app.smartpos.downloaddatadialog.DownloadDataDialog;
import com.app.smartpos.utils.BaseActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AuthActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
//        getWindow().setFlags(
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//        );

        setContentView(R.layout.activity_auth);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name),MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        boolean firstOpen=sharedPreferences.getBoolean("first_open",true);
        if(firstOpen) {
            Date date = new Date();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm a", Locale.US);
            myEdit.putString("last_order", df.format(date));
            myEdit.putBoolean("first_open", false);

            DownloadDataDialog dialog=new DownloadDataDialog();
            dialog.show(getSupportFragmentManager(),"dialog");
        }

    }
}