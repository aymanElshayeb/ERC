package com.app.smartpos.profile;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.smartpos.R;
import com.app.smartpos.auth.AuthActivity;
import com.app.smartpos.utils.SharedPrefUtils;

public class Profile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_profile);

        TextView usernameTv=findViewById(R.id.username_tv);
        TextView flNameTv=findViewById(R.id.fl_name_tv);
        TextView emailTv=findViewById(R.id.email_tv);
        LinearLayout logoutLl=findViewById(R.id.logout_ll);
        ImageView closeIm=findViewById(R.id.close_im);
        usernameTv.setText(SharedPrefUtils.getName(this));
        TextView mobileTv=findViewById(R.id.mobile_tv);
        mobileTv.setText(SharedPrefUtils.getMobileNumber(this));
        emailTv.setText(SharedPrefUtils.getEmail(this));
        flNameTv.setText(SharedPrefUtils.getName(this).substring(0, 1));
        closeIm.setOnClickListener(view -> {
            finish();
        });

        logoutLl.setOnClickListener(view -> {
            Intent intent = new Intent(this, AuthActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

    }
}