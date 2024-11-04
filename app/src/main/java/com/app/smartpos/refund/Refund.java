package com.app.smartpos.refund;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;

import com.app.smartpos.R;
import com.app.smartpos.checkout.NoVatNumberDialog;
import com.app.smartpos.common.Utils;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.pos.ScannerActivity;
import com.app.smartpos.utils.BaseActivity;
import com.app.smartpos.utils.SharedPrefUtils;

public class Refund extends BaseActivity {

    DatabaseAccess databaseAccess;
    RefundDetailsViewModel model;
    public static EditText searchEt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_refund);

        model = new RefundDetailsViewModel();

        databaseAccess = DatabaseAccess.getInstance(this);
        ImageView scannerIm = findViewById(R.id.img_scanner);
        Button allRefundsBtn = findViewById(R.id.view_all_receipt_btn);
        searchEt = findViewById(R.id.search_et);
        Button view_receipt_btn = findViewById(R.id.view_receipt_btn);
        scannerIm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Refund.this, ScannerActivity.class);
                startActivity(intent);
            }
        });
        allRefundsBtn.setOnClickListener(view -> startActivity(new Intent(this, RefundOrOrderList.class).putExtra("isRefund", true)));
        view_receipt_btn.setOnClickListener(view -> {
            databaseAccess.open();
            if(databaseAccess.getConfiguration().get("merchant_tax_number").isEmpty()){
                NoVatNumberDialog dialog=new NoVatNumberDialog();
                dialog.show(getSupportFragmentManager(),"dialog");
            }
            else if (!searchEt.getText().toString().trim().isEmpty()) {
//                List<HashMap<String, String>> list = databaseAccess.searchOrderList(search_et.getText().toString());
//                if (list.size() > 0) {
//                    Intent i = new Intent(this, RefundOrOrderDetails.class).putExtra("isRefund",true);
//
//                    i.putExtra("order_id", list.get(0).get("invoice_id"));
//                    i.putExtra("order_payment_method", list.get(0).get("order_payment_method"));
//                    i.putExtra("operation_type", list.get(0).get("operation_type"));
//
//                    startActivity(i);
//                }
                databaseAccess.open();
                ConfirmSyncDialog confirmdialog = new ConfirmSyncDialog();
                confirmdialog.show(getSupportFragmentManager(), "confirmDialog");

                //callApi();
            }
        });

        findViewById(R.id.back_im).setOnClickListener(view -> finish());

        model.getLiveData().observe(this, refundModel -> {
            if (refundModel == null) {
                ItemNotFoundDialog dialog = new ItemNotFoundDialog();
                dialog.show(getSupportFragmentManager(), "dialog");
            } else {
                Intent i = new Intent(this, RefundOrOrderDetails.class).putExtra("isRefund", true);
                i.putExtra("refundModel", refundModel);
                startActivity(i);
            }
        });
    }

    public void callApi() {
        Utils.addLog("INSIDE CALL API", SharedPrefUtils.getAuthorization());

        model.start(searchEt.getText().toString().trim(), databaseAccess);
    }
}