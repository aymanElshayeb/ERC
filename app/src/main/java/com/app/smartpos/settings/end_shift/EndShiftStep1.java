package com.app.smartpos.settings.end_shift;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.app.smartpos.R;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.utils.SharedPrefUtils;

import java.util.HashMap;
import java.util.List;

public class EndShiftStep1 extends AppCompatActivity {

    DatabaseAccess databaseAccess;
    String currency;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_end_shift_step1);

        TextView totalAmountTv = findViewById(R.id.total_amount_tv);
        TextView totalCashTv = findViewById(R.id.total_cash_tv);
        TextView totalCardTv = findViewById(R.id.total_card_tv);
        TextView numberOfSalesTv = findViewById(R.id.number_of_sales_tv);
        TextView endMyShiftTv = findViewById(R.id.end_my_shift_tv);
        ImageView backIm = findViewById(R.id.back_im);

        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        currency=databaseAccess.getCurrency();
        databaseAccess.open();
        String endDateString = databaseAccess.getLastShift("end_date_time");
        long lastShiftDate = endDateString.equals("") ? SharedPrefUtils.getStartDateTime(this) : Long.parseLong(endDateString);
        databaseAccess.open();
        //get data from local database
        List<HashMap<String, String>> orderList = databaseAccess.getOrderListWithTime(lastShiftDate);
        double totalCash=0;
        double totalCard=0;
        double total=0;
        for (int i=0;i<orderList.size();i++){
            databaseAccess.open();
            total += databaseAccess.totalOrderPrice(orderList.get(i).get("invoice_id"));
            if (orderList.get(i).get("order_payment_method").equals("CASH")) {
                totalCash+=Double.parseDouble(orderList.get(i).get("in_tax_total"));
            }else if (orderList.get(i).get("order_payment_method").equals("CARD")) {
                totalCard+=Double.parseDouble(orderList.get(i).get("in_tax_total"));
            }
        }
        totalAmountTv.setText(total+" "+currency);
        totalCashTv.setText(totalCash+" "+currency);
        totalCardTv.setText(totalCard+" "+currency);
        numberOfSalesTv.setText(orderList.size()+"");

        backIm.setOnClickListener(view -> finish());

    }
}