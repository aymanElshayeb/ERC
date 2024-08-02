package com.app.smartpos;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.app.smartpos.Items.Items;
import com.app.smartpos.profile.Profile;
import com.app.smartpos.settings.end_shift.EndShiftDialog;
import com.app.smartpos.utils.BaseActivity;

public class NewHomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_new_home);

        LinearLayout circleNameLl=findViewById(R.id.circle_name_ll);
        LinearLayout itemsLL=findViewById(R.id.items_ll);
        LinearLayout endOfShiftLl=findViewById(R.id.end_of_shift_ll);

        circleNameLl.setOnClickListener(view -> {
            startActivity(new Intent(this, Profile.class));
        });

        itemsLL.setOnClickListener(view -> {
            startActivity(new Intent(this, Items.class));
        });

        endOfShiftLl.setOnClickListener(v -> {
            EndShiftDialog dialog=new EndShiftDialog();
            dialog.show(getSupportFragmentManager(),"end shift dialog");
        });
    }
}