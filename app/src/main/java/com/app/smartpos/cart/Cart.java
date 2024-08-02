package com.app.smartpos.cart;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.app.smartpos.R;
import com.app.smartpos.adapter.CartAdapter;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.pos.ProductCart;

import java.util.HashMap;
import java.util.List;

public class Cart extends AppCompatActivity {

    CartAdapter productCartAdapter;

    TextView totalAmountWithoutVatTv;
    TextView totalAmountTv;
    TextView totalVatTv;
    List<HashMap<String, String>> cartProductList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_cart);

        RecyclerView recyclerView = findViewById(R.id.recycler);
        totalAmountWithoutVatTv = findViewById(R.id.total_amount_without_vat_tv);
        totalAmountTv = findViewById(R.id.total_amount_tv);
        totalVatTv = findViewById(R.id.total_vat_tv);
        TextView addItemsTv = findViewById(R.id.add_items_tv);
        TextView confirmTv = findViewById(R.id.confirm_tv);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager); // set LayoutManager to RecyclerView

        final DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();


        //get data from local database
        cartProductList = databaseAccess.getCartProduct();



        productCartAdapter = new CartAdapter(this, cartProductList);
        recyclerView.setAdapter(productCartAdapter);

        calculatePrices();

        addItemsTv.setOnClickListener(view -> finish());
        confirmTv.setOnClickListener(view -> {});
    }

    public void calculatePrices(){
        double total=0;
        for(int i=0;i<cartProductList.size();i++){
            double productPrice=Double.parseDouble(cartProductList.get(i).get("product_price"));
            double productCount=Double.parseDouble(cartProductList.get(i).get("product_qty"));
            total+=productPrice*productCount;
        }
        totalAmountWithoutVatTv.setText(total+" SAR");
    }
}