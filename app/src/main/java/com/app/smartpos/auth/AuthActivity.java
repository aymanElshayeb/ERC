package com.app.smartpos.auth;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.WorkInfo;

import com.app.smartpos.NewHomeActivity;
import com.app.smartpos.R;
import com.app.smartpos.Registration.Registration;
import com.app.smartpos.common.WorkerActivity;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.utils.SharedPrefUtils;

import java.util.HashMap;

public class AuthActivity extends WorkerActivity {

    int workerType = 1;
    private ConstraintLayout loadingCl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        loadingCl = findViewById(R.id.loading_cl);
//        getWindow().setFlags(
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//        );

        setContentView(R.layout.activity_auth);

        if (!SharedPrefUtils.isRegistered(this)) {
            Intent intent = new Intent(AuthActivity.this, Registration.class);
            startActivity(intent);
//            RegistrationDialog dialog = new RegistrationDialog();
//            dialog.show(getSupportFragmentManager(), "register dialog");
//            CompanyCheckDialog dialog = new CompanyCheckDialog();
//            dialog.show(getSupportFragmentManager(), "register dialog");
        }

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

    public void showHideLoading(boolean flag) {
        loadingCl.setVisibility(flag ? View.VISIBLE : View.GONE);
    }

    @Override
    public void handleWorkCompletion(WorkInfo workInfo) {
        super.handleWorkCompletion(workInfo);
        if (workerType == 1) {
            if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                String email = workInfo.getOutputData().getString("email");

                DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
                databaseAccess.open();
                HashMap<String, String> map = databaseAccess.getUserWithEmail(email);
                SharedPrefUtils.setName(this, map.get("name_ar"));
                SharedPrefUtils.setEmail(this, map.get("email"));
                SharedPrefUtils.setMobileNumber(this, map.get("mobile"));
                SharedPrefUtils.setUserId(this, map.get("id"));
                SharedPrefUtils.setUserName(this, map.get("username"));
                if (isConnected()) {
                    workerType = 2;
                    enqueueDownloadAndReadWorkers();
                }
            } else {

                Toast.makeText(this, getString(R.string.wrong_email_password), Toast.LENGTH_SHORT).show();
            }
        } else if (workerType == 2) {
            SharedPrefUtils.setIsLoggedIn(this, true);
            Intent intent = new Intent(this, NewHomeActivity.class);
            startActivity(intent);
            this.finish();
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}