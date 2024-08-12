package com.app.smartpos.auth;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;

import com.app.smartpos.R;
import com.app.smartpos.Registration.RegistrationDialog;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.utils.BaseActivity;
import com.app.smartpos.utils.SharedPrefUtils;

import java.util.HashMap;

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
        if (conf.isEmpty()) {
            RegistrationDialog dialog = new RegistrationDialog();
            dialog.show(getSupportFragmentManager(), "register dialog");
        }else {
            String merchantId=conf.get("merchant_id");
            String ecrCode=conf.get("ecr_code");
            SharedPrefUtils.setMerchantId(this,merchantId);
            SharedPrefUtils.setEcrCode(this,ecrCode);
        }

    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}