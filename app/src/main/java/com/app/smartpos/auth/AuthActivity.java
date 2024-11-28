package com.app.smartpos.auth;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.WorkInfo;

import com.app.smartpos.NewHomeActivity;
import com.app.smartpos.R;
import com.app.smartpos.Registration.Registration;
import com.app.smartpos.common.WorkerActivity;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.utils.Hasher;
import com.app.smartpos.utils.SharedPrefUtils;

import java.util.HashMap;

public class AuthActivity extends WorkerActivity {

    int workerType = 1;
    private ConstraintLayout loadingCl;
    private String email;
    private String password;
    private Button loginBtn;
    DatabaseAccess databaseAccess;
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
        loadingCl = findViewById(R.id.loading_cl);
        databaseAccess = DatabaseAccess.getInstance(this);

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

    public void setEmailAndPassword(String email,String password) {
        this.email = email;
        this.password = password;
    }

    public void showHideLoading(boolean flag) {
        loadingCl.setVisibility(flag ? View.VISIBLE : View.GONE);
    }

    @Override
    public void handleWorkCompletion(WorkInfo workInfo) {
        super.handleWorkCompletion(workInfo);
        if (workerType == 1) {
            if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
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
//                showHideLoading(false);
//                Toast.makeText(this, getString(R.string.wrong_email_password), Toast.LENGTH_SHORT).show();
                offlineLogin();
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

    public void login(String email, String password,Button loginBtn) {
        if(email.trim().isEmpty()){
            Toast.makeText(this, getString(R.string.email_empty), Toast.LENGTH_SHORT).show();
        }else if(password.trim().isEmpty()){
            Toast.makeText(this, getString(R.string.password_empty), Toast.LENGTH_SHORT).show();
        }else {
            this.loginBtn = loginBtn;
            setEmailAndPassword(email, password);
            databaseAccess.open();
            HashMap<String, String> map = databaseAccess.getUserWithEmail(email);
            if (map != null) {
                showHideLoading(true);
                if (isConnected()) {
                    loginWorkers(email, password);

                } else {
                    loginBtn.setEnabled(false);
                    offlineLogin();

                }
            } else {
                Toast.makeText(AuthActivity.this, getString(R.string.wrong_email_password), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void offlineLogin(){
        HashMap<String, String> map = databaseAccess.getUserWithEmail(email);

        AsyncTask.execute(() -> {
            Hasher hasher = new Hasher();
            boolean isMatch = hasher.hashPassword(password, map.get("password"));
            //Utils.addLog("datadata",map.toString());
            if (isMatch) {

                runOnUiThread(() -> {
                    SharedPrefUtils.setName(AuthActivity.this, map.get("name_ar"));
                    SharedPrefUtils.setEmail(AuthActivity.this, map.get("email"));
                    SharedPrefUtils.setMobileNumber(AuthActivity.this, map.get("mobile"));
                    SharedPrefUtils.setUserId(AuthActivity.this, map.get("id"));
                    SharedPrefUtils.setUserName(AuthActivity.this, map.get("username"));
                    SharedPrefUtils.setIsLoggedIn(AuthActivity.this, true);
                    loginBtn.setEnabled(true);
                    showHideLoading(false);
                });
                Intent intent = new Intent(AuthActivity.this, NewHomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                runOnUiThread(() -> {
                    loginBtn.setEnabled(true);
                    showHideLoading(false);
                    Toast.makeText(AuthActivity.this, getString(R.string.wrong_email_password), Toast.LENGTH_SHORT).show();
                });

            }
        });
    }
}