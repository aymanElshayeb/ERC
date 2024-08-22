package com.app.smartpos.refund;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.app.smartpos.R;
import com.app.smartpos.adapter.RefundAdapter;
import com.app.smartpos.database.DatabaseAccess;

import java.util.HashMap;
import java.util.List;

public class RefundList extends AppCompatActivity {

    DatabaseAccess databaseAccess;
    String currency;

    RefundAdapter refundAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_refund_list);

        RecyclerView recycler=findViewById(R.id.recycler);

        databaseAccess=DatabaseAccess.getInstance(this);

        databaseAccess.open();
        currency=databaseAccess.getCurrency();

        databaseAccess.open();


        //get data from local database
        List<HashMap<String, String>> orderList;
        orderList = databaseAccess.getOrderList();

        refundAdapter=new RefundAdapter(this,orderList);
        recycler.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        recycler.setAdapter(refundAdapter);


    }

    public String getCurrency() {
        return currency;
    }
}