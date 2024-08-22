package com.app.smartpos.refund;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.app.smartpos.R;
import com.app.smartpos.database.DatabaseAccess;

import java.util.HashMap;
import java.util.List;

public class Refund extends AppCompatActivity {

    DatabaseAccess databaseAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_refund);

        databaseAccess = DatabaseAccess.getInstance(this);

        Button allRefundsBtn = findViewById(R.id.view_all_receipt_btn);
        EditText search_et = findViewById(R.id.search_et);
        Button view_receipt_btn = findViewById(R.id.view_receipt_btn);

        allRefundsBtn.setOnClickListener(view -> startActivity(new Intent(this, RefundOrOrderList.class).putExtra("isRefund",true)));
        view_receipt_btn.setOnClickListener(view -> {
            databaseAccess.open();
            if (!search_et.getText().toString().trim().isEmpty()) {
                List<HashMap<String, String>> list = databaseAccess.searchOrderList(search_et.getText().toString());
                if (list.size() > 0) {
                    Intent i = new Intent(this, RefundOrOrderDetails.class).putExtra("isRefund",true);

                    i.putExtra("order_id", list.get(0).get("invoice_id"));
                    i.putExtra("order_payment_method", list.get(0).get("order_payment_method"));
                    i.putExtra("operation_type", list.get(0).get("operation_type"));

                    startActivity(i);
                }
            }
        });
    }
}