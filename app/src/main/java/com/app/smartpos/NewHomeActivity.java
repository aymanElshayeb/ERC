package com.app.smartpos;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;

import com.app.smartpos.Items.Items;
import com.app.smartpos.common.Utils;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.profile.Profile;
import com.app.smartpos.refund.Refund;
import com.app.smartpos.refund.RefundOrOrderList;
import com.app.smartpos.settings.Synchronization.DataBaseBackupActivity;
import com.app.smartpos.settings.end_shift.EndShiftStep1;
import com.app.smartpos.utils.BaseActivity;
import com.app.smartpos.utils.SharedPrefUtils;

import java.util.HashMap;
import java.util.List;

public class NewHomeActivity extends BaseActivity {

    TextView currentShiftNumberTv;
    TextView currentShiftSarTv;
    TextView startCashTv;
    TextView startCashSarTv;
    String currency;
    private DatabaseAccess databaseAccess;
    private HashMap<String, String> configuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_new_home);

        LinearLayout circleNameLl = findViewById(R.id.circle_name_ll);
        LinearLayout itemsLL = findViewById(R.id.items_ll);
        LinearLayout allOrdersLL = findViewById(R.id.all_orders_ll);
        LinearLayout billsLl = findViewById(R.id.bills_ll);
        LinearLayout refundLL = findViewById(R.id.refund_ll);
        LinearLayout endOfShiftLl = findViewById(R.id.end_of_shift_ll);
        TextView syncTv = findViewById(R.id.sync_tv);
        TextView nameTv = findViewById(R.id.name_tv);
        TextView locationTv = findViewById(R.id.location_tv);
        TextView ha_name_tv = findViewById(R.id.fl_home_name_tv);
        if (!SharedPrefUtils.getName(this).isEmpty()) {
            ha_name_tv.setText(SharedPrefUtils.getName(this).substring(0, 1));
            nameTv.setText(SharedPrefUtils.getName(this));
        } else {
            ha_name_tv.setText("G");
            nameTv.setText(R.string.guest);
        }
        databaseAccess = DatabaseAccess.getInstance(this);

        databaseAccess.open();
        HashMap<String, String> shop = databaseAccess.getShopInformation();
        String shopLocation = String.valueOf(shop.get("shop_address"));
        locationTv.setText(shopLocation);

        currentShiftNumberTv = findViewById(R.id.current_shift_number_tv);
        currentShiftSarTv = findViewById(R.id.current_shift_sar_tv);

        startCashTv = findViewById(R.id.start_cash_tv);
        startCashSarTv = findViewById(R.id.start_cash_sar_tv);

        circleNameLl.setOnClickListener(view -> {
            startActivity(new Intent(this, Profile.class));
        });


        itemsLL.setOnClickListener(view -> {
            startActivity(new Intent(this, Items.class));
        });

        allOrdersLL.setOnClickListener(view -> {
            startActivity(new Intent(this, RefundOrOrderList.class).putExtra("isRefund", false));
        });

        billsLl.setOnClickListener(view -> {
            startActivity(new Intent(this, QuickBill.class).putExtra("type", "quickBill"));
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

        databaseAccess.open();

        currency = databaseAccess.getCurrency();
        currentShiftSarTv.setText(currency);
        startCashSarTv.setText(currency);
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
        double total_amount = 0;
        for (int i = 0; i < orderList.size(); i++) {
            databaseAccess.open();
            total_amount += databaseAccess.totalOrderPrice(orderList.get(i).get("invoice_id"));
        }
        currentShiftNumberTv.setText(Utils.trimLongDouble(total_amount));

        databaseAccess.open();
        String startCashString = databaseAccess.getLastShift("leave_cash");
        double startCash = startCashString.equals("") ? 0 : Double.parseDouble(startCashString);
        startCashTv.setText(Utils.trimLongDouble(startCash));
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}