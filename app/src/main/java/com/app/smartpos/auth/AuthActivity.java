package com.app.smartpos.auth;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.work.WorkInfo;

import com.app.smartpos.NewHomeActivity;
import com.app.smartpos.R;
import com.app.smartpos.Registration.Registration;
import com.app.smartpos.common.WorkerActivity;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.utils.SharedPrefUtils;

import java.util.HashMap;

public class AuthActivity extends WorkerActivity {

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
        if (conf.isEmpty() && !SharedPrefUtils.isRegistered(this)) {
            Intent intent = new Intent(AuthActivity.this, Registration.class);
            startActivity(intent);
//            RegistrationDialog dialog = new RegistrationDialog();
//            dialog.show(getSupportFragmentManager(), "register dialog");
//            CompanyCheckDialog dialog = new CompanyCheckDialog();
//            dialog.show(getSupportFragmentManager(), "register dialog");
        }else {
            String merchantId=conf.get("merchant_id");
            //String ecrCode=conf.get("ecr_code");
            SharedPrefUtils.setMerchantId(this,merchantId);
        }
        new KeyGenParameterSpec.Builder("MySecretKey", KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(false) // Adjust as needed
                .setKeySize(256)
                .build();
        final PackageManager pm = getPackageManager();
//get a list of installed apps.
//        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
//
//        for (ApplicationInfo packageInfo : packages) {
//            Utils.addLog("datadata", "Installed package :" + packageInfo.packageName);
//            Utils.addLog("datadata", "Source dir : " + packageInfo.sourceDir);
//            Utils.addLog("datadata", "Launch Activity :" + pm.getLaunchIntentForPackage(packageInfo.packageName));
//        }

    }

    @Override
    public void handleWorkCompletion(WorkInfo workInfo) {
        super.handleWorkCompletion(workInfo);
        if(workInfo.getState() == WorkInfo.State.SUCCEEDED){
            String email=workInfo.getOutputData().getString("email");

            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
            databaseAccess.open();
            HashMap<String, String> map = databaseAccess.getUserWithEmail(email);
            SharedPrefUtils.setName(this, map.get("name_ar"));
            SharedPrefUtils.setEmail(this, map.get("email"));
            SharedPrefUtils.setMobileNumber(this, map.get("mobile"));
            SharedPrefUtils.setUserId(this, map.get("id"));
            SharedPrefUtils.setUserName(this, map.get("username"));
            SharedPrefUtils.setIsLoggedIn(this, true);
            Intent intent = new Intent(this, NewHomeActivity.class);
            startActivity(intent);
            this.finish();
        }else{
            Toast.makeText(this, getString(R.string.wrong_email_password), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}