package com.app.smartpos.auth;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.ActionBar;

import com.app.smartpos.R;
import com.app.smartpos.Registration.RegistrationDialog;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.utils.BaseActivity;
import com.app.smartpos.utils.SharedPrefUtils;

<<<<<<< HEAD
import java.util.HashMap;
=======
import java.util.List;
>>>>>>> dev

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


        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        HashMap<String, String> conf=databaseAccess.getConfiguration();
        if (conf.isEmpty() || !SharedPrefUtils.isDemoPressed(this)) { 
            RegistrationDialog dialog = new RegistrationDialog();
            dialog.show(getSupportFragmentManager(), "register dialog");
        }else {
            String merchantId=conf.get("merchant_id");
            String ecrCode=conf.get("ecr_code");
            SharedPrefUtils.setMerchantId(this,merchantId);
            SharedPrefUtils.setEcrCode(this,ecrCode);
        }

        final PackageManager pm = getPackageManager();
//get a list of installed apps.
//        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
//
//        for (ApplicationInfo packageInfo : packages) {
//            Log.i("datadata", "Installed package :" + packageInfo.packageName);
//            Log.i("datadata", "Source dir : " + packageInfo.sourceDir);
//            Log.i("datadata", "Launch Activity :" + pm.getLaunchIntentForPackage(packageInfo.packageName));
//        }

    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}