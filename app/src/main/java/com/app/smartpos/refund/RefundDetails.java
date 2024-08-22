package com.app.smartpos.refund;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.smartpos.R;
import com.app.smartpos.adapter.RefundDetailsAdapter;
import com.app.smartpos.database.DatabaseAccess;

import java.util.HashMap;
import java.util.List;

public class RefundDetails extends AppCompatActivity {

    DatabaseAccess databaseAccess;
    RefundDetailsAdapter refundDetailsAdapter;
    String currency;
    TextView receipt_number_tv;
    TextView card_tv, cash_tv, refunded_tv, total_amount_tv;

    List<HashMap<String, String>> orderDetailsList;
    String orderId;
    TextView refund_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_refund_details);

        receipt_number_tv = findViewById(R.id.receipt_number_tv);
        RecyclerView recycler = findViewById(R.id.recycler);
        card_tv = findViewById(R.id.card_tv);
        cash_tv = findViewById(R.id.cash_tv);
        refunded_tv = findViewById(R.id.refunded_tv);
        total_amount_tv = findViewById(R.id.total_amount_tv);

        refund_tv = findViewById(R.id.refund_tv);

        orderId = getIntent().getStringExtra("order_id");
        String order_payment_method = getIntent().getStringExtra("order_payment_method");
        String operation_type = getIntent().getStringExtra("operation_type");
        receipt_number_tv.setText(orderId);

        card_tv.setVisibility(order_payment_method.equals("CARD") ? View.VISIBLE : View.GONE);
        cash_tv.setVisibility(order_payment_method.equals("CASH") ? View.VISIBLE : View.GONE);
        refunded_tv.setVisibility(operation_type.equals("refunded") ? View.VISIBLE : View.GONE);

        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        currency = databaseAccess.getCurrency();
        databaseAccess.open();
        //get data from local database
        orderDetailsList = databaseAccess.getOrderDetailsList(orderId);
        for (int i = 0; i < orderDetailsList.size(); i++) {
            orderDetailsList.get(i).put("refund_qty", "0");
            orderDetailsList.get(i).put("item_checked", "0");
        }

        refundDetailsAdapter = new RefundDetailsAdapter(this, orderDetailsList);
        recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recycler.setAdapter(refundDetailsAdapter);

        updateTotalAmount();

        refund_tv.setOnClickListener(view -> refundPressed());
    }

    public String getCurrency() {
        return currency;
    }

    public void updateTotalAmount() {
        boolean canRefund = false;
        double total = 0;
        for (int i = 0; i < orderDetailsList.size(); i++) {
            double product_price = Double.parseDouble(orderDetailsList.get(i).get("product_price"));
            double refund_qty = Double.parseDouble(orderDetailsList.get(i).get("refund_qty"));
            String item_checked = orderDetailsList.get(i).get("item_checked");

            if (item_checked.equals("1") && refund_qty > 0) {
                total += refund_qty * product_price;
                canRefund = true;
            }
        }
        refund_tv.setEnabled(canRefund);
        refund_tv.setAlpha(canRefund ? 1.0f : 0.5f);
        total_amount_tv.setText(total + " " + currency);
    }

    private void refundPressed() {
        boolean canRefund = false;
        for (int i = 0; i < orderDetailsList.size(); i++) {
            double refund_qty = Double.parseDouble(orderDetailsList.get(i).get("refund_qty"));
            double product_qty = Double.parseDouble(orderDetailsList.get(i).get("product_qty"));
            String item_checked = orderDetailsList.get(i).get("item_checked");
            String order_details_id = orderDetailsList.get(i).get("order_details_id");

            if (item_checked.equals("1") && refund_qty > 0) {
                Log.i("datadata",product_qty+" "+refund_qty);
                canRefund = true;
                databaseAccess.open();
                databaseAccess.updateOrderDetailsItem("product_qty", "" + (int) (product_qty - refund_qty), order_details_id);
            }
        }

        if (canRefund) {
            databaseAccess.open();
            databaseAccess.updateOrderListItem("operation_type", "refunded", orderId);
            finish();
        }
    }
}