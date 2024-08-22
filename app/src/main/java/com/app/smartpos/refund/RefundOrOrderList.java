package com.app.smartpos.refund;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.app.smartpos.R;
import com.app.smartpos.adapter.RefundsOrOrdersAdapter;
import com.app.smartpos.database.DatabaseAccess;

import java.util.HashMap;
import java.util.List;

public class RefundOrOrderList extends AppCompatActivity {

    DatabaseAccess databaseAccess;
    String currency;

    RefundsOrOrdersAdapter refundsOrOrdersAdapter;
    boolean isRefund;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_refund_list);
        isRefund=getIntent().getBooleanExtra("isRefund",false);
        TextView title_tv=findViewById(R.id.title_tv);
        TextView title_text=findViewById(R.id.title_text);
        RecyclerView recycler=findViewById(R.id.recycler);

        if(!isRefund){
            title_tv.setText(getString(R.string.orders));
            title_text.setText(getString(R.string.all_orders));
        }

        databaseAccess=DatabaseAccess.getInstance(this);

        databaseAccess.open();
        currency=databaseAccess.getCurrency();

        databaseAccess.open();


        //get data from local database
        List<HashMap<String, String>> orderList;
        orderList = databaseAccess.getOrderList();

        refundsOrOrdersAdapter =new RefundsOrOrdersAdapter(this,orderList);
        recycler.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        recycler.setAdapter(refundsOrOrdersAdapter);

        findViewById(R.id.back_im).setOnClickListener(view -> finish());

    }

    public boolean isRefund() {
        return isRefund;
    }

    public String getCurrency() {
        return currency;
    }
}