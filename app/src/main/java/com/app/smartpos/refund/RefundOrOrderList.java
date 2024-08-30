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
import com.app.smartpos.adapter.RefundsOrOrdersAdapter;
import com.app.smartpos.database.DatabaseAccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RefundOrOrderList extends AppCompatActivity {

    DatabaseAccess databaseAccess;
    String currency;

    RefundsOrOrdersAdapter refundsOrOrdersAdapter;
    boolean isRefund;
    List<HashMap<String, String>> orderList=new ArrayList<>();
    int offset=0;
    boolean hasMore=true;
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

        if (!isRefund) {
            title_tv.setText(getString(R.string.orders));
            title_text.setText(getString(R.string.all_orders));
        }

        databaseAccess = DatabaseAccess.getInstance(this);

        databaseAccess.open();
        currency = databaseAccess.getCurrency();

        databaseAccess.open();


        //get data from local database

        orderList.addAll(databaseAccess.getOrderListPaginated(offset,isRefund));

        refundsOrOrdersAdapter = new RefundsOrOrdersAdapter(this, orderList);
        recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recycler.setAdapter(refundsOrOrdersAdapter);

        findViewById(R.id.back_im).setOnClickListener(view -> finish());

    }

    public boolean isRefund() {
        return isRefund;
    }

    public String getCurrency() {
        return currency;
    }

    public void loadMore(){
        if(hasMore) {
            offset+=10;
            int size=orderList.size();
            databaseAccess.open();
            orderList.addAll(databaseAccess.getOrderListPaginated(offset,isRefund));
            hasMore=orderList.size()>size;
            recycler.post(() -> refundsOrOrdersAdapter.notifyDataSetChanged());
        }
    }
}