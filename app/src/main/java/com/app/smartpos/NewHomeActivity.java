package com.app.smartpos;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.smartpos.Items.Items;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.profile.Profile;
import com.app.smartpos.refund.Refund;
import com.app.smartpos.refund.RefundOrOrderList;
import com.app.smartpos.settings.Synchronization.DataBaseBackupActivity;
import com.app.smartpos.settings.end_shift.EndShiftDialog;
import com.app.smartpos.settings.end_shift.EndShiftStep1;
import com.app.smartpos.utils.BaseActivity;
import com.app.smartpos.utils.SharedPrefUtils;

import java.util.HashMap;
import java.util.List;

public class NewHomeActivity extends BaseActivity {

    private DatabaseAccess databaseAccess;
    TextView currentShiftNumberTv;
    TextView currentShiftSarTv;
    String currency;
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
        LinearLayout allOrdersLL=findViewById(R.id.all_orders_ll);
        LinearLayout refundLL=findViewById(R.id.refund_ll);
        LinearLayout endOfShiftLl=findViewById(R.id.end_of_shift_ll);
        TextView syncTv=findViewById(R.id.sync_tv);

        currentShiftNumberTv=findViewById(R.id.current_shift_number_tv);
        currentShiftSarTv=findViewById(R.id.current_shift_sar_tv);

        circleNameLl.setOnClickListener(view -> {
            startActivity(new Intent(this, Profile.class));
        });


        itemsLL.setOnClickListener(view -> {
            startActivity(new Intent(this, Items.class));
        });

        allOrdersLL.setOnClickListener(view -> {
            startActivity(new Intent(this, RefundOrOrderList.class).putExtra("isRefund",false));
        });

        syncTv.setOnClickListener(view -> {
            startActivity(new Intent(this, DataBaseBackupActivity.class));
        });

        refundLL.setOnClickListener(v -> {
            startActivity(new Intent(this, Refund.class));
        });

        endOfShiftLl.setOnClickListener(v -> {
            startActivity(new Intent(this, EndShiftStep1.class));
        });

        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();

        currency = databaseAccess.getCurrency();
        currentShiftSarTv.setText(currency);
    }

    @Override
    protected void onResume() {
        super.onResume();
        databaseAccess.open();
        String endDateString = databaseAccess.getLastShift("end_date_time");
        long lastShiftDate = endDateString.equals("") ? SharedPrefUtils.getStartDateTime(this) : Long.parseLong(endDateString);
        databaseAccess.open();
        //get data from local database
        List<HashMap<String, String>> orderList;
        orderList = databaseAccess.getOrderListWithTime(lastShiftDate);
        double total_amount=0;
        for (int i = 0; i < orderList.size(); i++) {
            databaseAccess.open();
            total_amount += databaseAccess.totalOrderPrice(orderList.get(i).get("invoice_id"));
        }
        currentShiftNumberTv.setText(total_amount+"");
    }
}