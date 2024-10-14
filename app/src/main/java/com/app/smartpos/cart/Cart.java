package com.app.smartpos.cart;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.smartpos.R;
import com.app.smartpos.adapter.CartAdapter;
import com.app.smartpos.checkout.NewCheckout;
import com.app.smartpos.common.Utils;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.pos.ProductCart;
import com.app.smartpos.utils.BaseActivity;

import java.util.HashMap;
import java.util.List;

public class Cart extends BaseActivity {

    CartAdapter productCartAdapter;

    TextView totalAmountWithoutVatTv;
    TextView totalAmountTv;
    TextView totalVatTv;
    List<HashMap<String, String>> cartProductList;

    String currency;
    DatabaseAccess databaseAccess;
    boolean checkConnectionOnce=true;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.recycler);
        ImageView backIm = findViewById(R.id.back_im);
        totalAmountWithoutVatTv = findViewById(R.id.total_amount_without_vat_tv);
        totalAmountTv = findViewById(R.id.total_amount_tv);
        totalVatTv = findViewById(R.id.total_vat_tv);
        TextView addItemsTv = findViewById(R.id.add_items_tv);
        TextView confirmTv = findViewById(R.id.confirm_tv);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager); // set LayoutManager to RecyclerView

        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();

        currency = databaseAccess.getCurrency();

        databaseAccess.open();
        //get data from local database
        cartProductList = databaseAccess.getCartProduct();

        productCartAdapter = new CartAdapter(this, cartProductList);
        recyclerView.setAdapter(productCartAdapter);

        updateTotalPrice(cartProductList);

        addItemsTv.setOnClickListener(view -> finish());
        backIm.setOnClickListener(view -> finish());
        confirmTv.setOnClickListener(view -> {
            if (productCartAdapter.getItemCount() == 0) {
                Toast.makeText(Cart.this, R.string.your_cart_is_empty, Toast.LENGTH_SHORT).show();
            } else {
                finish();
                startActivity(new Intent(this, NewCheckout.class));
            }
        });
    }

    public void updateTotalPrice(List<HashMap<String, String>> list){
        double totalWithoutTax=0;
        double total=0;
        for(int i=0;i<list.size();i++){
            String productId=list.get(i).get("product_id");
            double productPrice=Double.parseDouble(list.get(i).get("product_price"));
            double productCount=Double.parseDouble(list.get(i).get("product_qty"));
            databaseAccess.open();
            double productTax = 1+databaseAccess.getProductTax(productId)/100;
            totalWithoutTax+= (productPrice/productTax)*productCount;
            total+=productPrice*productCount;
            Utils.addLog("datadata",(productPrice*productCount)+" "+productTax+" "+totalWithoutTax+" "+total);
        }
        Utils.addLog("datadata_total",totalWithoutTax+" "+total);
        Utils.addLog("datadata_total",Utils.trimLongDouble(total));

        totalAmountWithoutVatTv.setText(Utils.trimLongDouble(totalWithoutTax)+" "+currency);
        totalVatTv.setText(Utils.trimLongDouble(total-totalWithoutTax)+" "+currency);
        totalAmountTv.setText(Utils.trimLongDouble(total)+" "+currency);

        if(total==0){
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isConnected()){
            setAdapter();
        }
    }

    @Override
    public void connectionChanged(boolean state) {
        super.connectionChanged(state);
        setAdapter();
    }

    private void setAdapter(){
        if(checkConnectionOnce){
            checkConnectionOnce=false;
            runOnUiThread(() -> {
                recyclerView.setAdapter(productCartAdapter);
            });
        }
    }

    public Boolean checkCartTotalPrice(int pos) {
        double total = 0;
        for (int i = 0; i < cartProductList.size(); i++) {
            double productPrice = Double.parseDouble(cartProductList.get(i).get("product_price"));
            double productCount = Double.parseDouble(cartProductList.get(i).get("product_qty"));
            total += productPrice * (productCount+(i==pos ? 1:0));
        }
        return total>999999999.99;
    }
}