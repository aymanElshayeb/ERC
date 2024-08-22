package com.app.smartpos.refund;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.smartpos.R;
import com.app.smartpos.adapter.RefundOrOrderDetailsAdapter;
import com.app.smartpos.checkout.SuccessfulPayment;
import com.app.smartpos.database.DatabaseAccess;

import java.util.HashMap;
import java.util.List;

public class RefundOrOrderDetails extends AppCompatActivity {

    DatabaseAccess databaseAccess;
    RefundOrOrderDetailsAdapter refundDetailsAdapter;
    String currency;
    TextView receipt_number_tv;
    TextView card_tv, cash_tv, refunded_tv, total_amount_tv;

    List<HashMap<String, String>> orderDetailsList;
    String orderId;
    TextView refund_tv;
    boolean isRefund;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_refund_details);

        isRefund=getIntent().getBooleanExtra("isRefund",false);

        TextView title_tv = findViewById(R.id.title_tv);
        TextView question_tv = findViewById(R.id.question_tv);
        TextView amount_tv = findViewById(R.id.amount_tv);
        LinearLayout btn_ll = findViewById(R.id.btn_ll);
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

        refundDetailsAdapter = new RefundOrOrderDetailsAdapter(this, orderDetailsList);
        recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recycler.setAdapter(refundDetailsAdapter);
        if(!isRefund){
            title_tv.setText(getString(R.string.order_details));
            question_tv.setVisibility(View.GONE);
            amount_tv.setText(getString(R.string.total_amount));
            btn_ll.setVisibility(View.GONE);
        }
        updateTotalAmount();

        refund_tv.setOnClickListener(view -> refundPressed());
        findViewById(R.id.back_im).setOnClickListener(view -> finish());
    }

    public boolean isRefund() {
        return isRefund;
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
            double product_qty = Double.parseDouble(orderDetailsList.get(i).get("product_qty"));
            String item_checked = orderDetailsList.get(i).get("item_checked");

            if(!isRefund){
                total += product_qty * product_price;
            }else {
                if (item_checked.equals("1") && refund_qty > 0) {
                    total += refund_qty * product_price;
                    canRefund = true;
                }
            }
        }
        refund_tv.setEnabled(canRefund);
        refund_tv.setAlpha(canRefund ? 1.0f : 0.5f);
        total_amount_tv.setText(total + " " + currency);
    }

    private void refundPressed() {
        RefundConfirmationDialog dialog=new RefundConfirmationDialog();
        dialog.setData(this,total_amount_tv.getText().toString());
        dialog.show(getSupportFragmentManager(),"dialog");
    }

    public void refundConfirmation(){
        boolean canRefund = false;
        for (int i = 0; i < orderDetailsList.size(); i++) {
            double refund_qty = Double.parseDouble(orderDetailsList.get(i).get("refund_qty"));
            double product_qty = Double.parseDouble(orderDetailsList.get(i).get("product_qty"));
            String item_checked = orderDetailsList.get(i).get("item_checked");
            String order_details_id = orderDetailsList.get(i).get("order_details_id");

            if (item_checked.equals("1") && refund_qty > 0) {
                canRefund = true;
                databaseAccess.open();
                databaseAccess.updateOrderDetailsItem("product_qty", "" + (int) (product_qty - refund_qty), order_details_id);
            }
        }

        if (canRefund) {
            databaseAccess.open();
            databaseAccess.updateOrderListItem("operation_type", "refunded", orderId);
            Intent intent = new Intent(this, SuccessfulPayment.class).putExtra("amount", total_amount_tv.getText().toString());
            startActivity(intent);
            finish();
        }
    }
}