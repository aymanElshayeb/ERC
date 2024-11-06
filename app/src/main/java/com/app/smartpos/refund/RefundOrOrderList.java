package com.app.smartpos.refund;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.smartpos.R;
import com.app.smartpos.adapter.RefundsOrOrdersAdapter;
import com.app.smartpos.common.Utils;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.utils.BaseActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RefundOrOrderList extends BaseActivity {

    DatabaseAccess databaseAccess;
    String currency;

    RefundsOrOrdersAdapter refundsOrOrdersAdapter;
    boolean isRefund;
    List<HashMap<String, String>> orderList = new ArrayList<>();
    int offset = 0;
    boolean hasMore = true;
    RefundDetailsViewModel model;
    String invoiceSeq = "";
    private RecyclerView recycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_refund_list);
        isRefund = getIntent().getBooleanExtra("isRefund", false);
        TextView title_tv = findViewById(R.id.title_tv);
        TextView title_text = findViewById(R.id.title_text);
        recycler = findViewById(R.id.recycler);

        model = new RefundDetailsViewModel();
        if (!isRefund) {
            title_tv.setText(getString(R.string.orders));
            title_text.setText(getString(R.string.all_orders));
        }

        databaseAccess = DatabaseAccess.getInstance(this);

        databaseAccess.open();
        currency = databaseAccess.getCurrency();

        databaseAccess.open();


        //get data from local database

        orderList.addAll(databaseAccess.getOrderListPaginated(offset, isRefund));
        for (int i = 0; i < orderList.size(); i++) {
            Utils.addLog("datadata_order", orderList.get(i).toString());
        }
        refundsOrOrdersAdapter = new RefundsOrOrdersAdapter(this, orderList);
        recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recycler.setAdapter(refundsOrOrdersAdapter);

        findViewById(R.id.back_im).setOnClickListener(view -> finish());

        model.getLiveData().observe(this, refundModel -> {
            if (refundModel == null) {
                ItemNotFoundDialog dialog = new ItemNotFoundDialog();
                dialog.show(getSupportFragmentManager(), "dialog");
            } else {
                finish();
                Intent i = new Intent(this, RefundOrOrderDetails.class).putExtra("isRefund", true);
                i.putExtra("refundModel", refundModel);
                startActivity(i);
            }
        });
    }

    private void test() {
        Locale locale = new Locale("ar");
        Configuration configuration = getResources().getConfiguration();
        configuration.setLocale(locale);
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
    }

    public boolean isRefund() {
        return isRefund;
    }

    public String getCurrency() {
        return currency;
    }

    public void loadMore() {
        if (hasMore) {
            offset += 10;
            int size = orderList.size();
            databaseAccess.open();
            orderList.addAll(databaseAccess.getOrderListPaginated(offset, isRefund));
            hasMore = orderList.size() > size;
            recycler.post(() -> refundsOrOrdersAdapter.notifyDataSetChanged());
        }
    }

    public void openDetails(int adapterPosition) {
        invoiceSeq = orderList.get(adapterPosition).get("invoice_id");
        Intent i = new Intent(this, RefundOrOrderDetails.class).putExtra("isRefund", isRefund);
        i.putExtra("order_id", orderList.get(adapterPosition).get("invoice_id"));
        i.putExtra("order_payment_method", orderList.get(adapterPosition).get("order_payment_method"));
        i.putExtra("operation_type", orderList.get(adapterPosition).get("operation_type"));
        i.putExtra("operation_sub_type", orderList.get(adapterPosition).get("operation_sub_type"));
        i.putExtra("printed", Boolean.parseBoolean(orderList.get(adapterPosition).get("printed")));
        if (isRefund()) {
//            DownloadDataDialog dialog=DownloadDataDialog.newInstance(DownloadDataDialog.OPERATION_REFUND);
//            dialog.show(getSupportFragmentManager(),"dialog");

            ConfirmSyncDialog confirmation = new ConfirmSyncDialog();
            confirmation.show(getSupportFragmentManager(), "confirmDialog");

        } else {
            startActivity(i);
        }
    }

    public void callApi() {
        model.start(invoiceSeq, databaseAccess);
    }
}