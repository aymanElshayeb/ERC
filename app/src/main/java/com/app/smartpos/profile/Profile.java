package com.app.smartpos.profile;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.smartpos.R;
import com.app.smartpos.auth.AuthActivity;
import com.app.smartpos.settings.ChangeLanguageDialog;
import com.app.smartpos.utils.BaseActivity;
import com.app.smartpos.utils.LocaleManager;
import com.app.smartpos.utils.SharedPrefUtils;

public class Profile extends BaseActivity {

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
        ConstraintLayout languageCl=findViewById(R.id.language_cl);
        TextView languageTv=findViewById(R.id.language_tv);
        LinearLayout logoutLl=findViewById(R.id.logout_ll);
        ImageView closeIm=findViewById(R.id.close_im);
        usernameTv.setText(SharedPrefUtils.getName(this));
        TextView mobileTv=findViewById(R.id.mobile_tv);
        mobileTv.setText(SharedPrefUtils.getMobileNumber(this));
        emailTv.setText(SharedPrefUtils.getEmail(this));
        if (SharedPrefUtils.getName(this).isEmpty()) {
            flNameTv.setText("G");
            usernameTv.setText(R.string.guest);
        }else{
            flNameTv.setText(SharedPrefUtils.getName(this).substring(0, 1));
        }
        String language = LocaleManager.getLanguage(this);
        languageTv.setText(language.equals("en") ? "English" : "عربى");
        ChangeLanguageDialog dialog = new ChangeLanguageDialog();

        languageCl.setOnClickListener(view -> {
            dialog.show(getSupportFragmentManager(), "change language dialog");
        });
        closeIm.setOnClickListener(view -> {
            finish();
        });

        logoutLl.setOnClickListener(view -> {
            Intent intent = new Intent(this, AuthActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            SharedPrefUtils.setIsLoggedIn(this, false);
            startActivity(intent);
            finish();
        });

    }
}