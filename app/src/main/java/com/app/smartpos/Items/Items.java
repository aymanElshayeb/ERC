package com.app.smartpos.Items;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.smartpos.R;
import com.app.smartpos.adapter.CartAdapter;
import com.app.smartpos.adapter.PosProductAdapter;
import com.app.smartpos.cart.Cart;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.pos.PosActivity;
import com.app.smartpos.pos.ProductCart;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Items extends AppCompatActivity {

    PosProductAdapter productCartAdapter;
    List<HashMap<String, String>> productList;
    List<HashMap<String, String>> selectedProductList=new LinkedList<>();
    TextView cartCountTv;
    TextView cartTotalPriceTv;
    ConstraintLayout viewCartCl;
    ConstraintLayout openCartCl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        setContentView(R.layout.activity_items);

        ImageView backIm=findViewById(R.id.back_im);
        TextView clearTv=findViewById(R.id.clear_tv);
        RecyclerView recycler=findViewById(R.id.recycler);
        cartCountTv = findViewById(R.id.cart_count_tv);
        cartTotalPriceTv = findViewById(R.id.cart_total_price_tv);
        viewCartCl=findViewById(R.id.view_cart_cl);
        openCartCl=findViewById(R.id.open_cart_cl);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(),2,RecyclerView.VERTICAL,false);
        recycler.setLayoutManager(gridLayoutManager);

        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();


        //get data from local database

        productList = databaseAccess.getProducts(false);

        Log.i("datadata",productList.size()+"");
        productCartAdapter = new PosProductAdapter(this, productList);
        recycler.setAdapter(productCartAdapter);

        openCartCl.setOnClickListener(view -> {
            for (int i=0;i<selectedProductList.size();i++){
                databaseAccess.open();
                String product_id = selectedProductList.get(i).get("product_id");
                String product_weight = selectedProductList.get(i).get("product_weight");
                String product_stock = selectedProductList.get(i).get("product_stock");
                String product_price = selectedProductList.get(i).get("product_sell_price");
                String weight_unit_id = selectedProductList.get(i).get("product_weight_unit_id");
                int product_count = Integer.parseInt(selectedProductList.get(i).get("product_count"));
                databaseAccess.addToCart(product_id, product_weight, weight_unit_id, product_price, product_count, product_stock);
            }

            Intent intent = new Intent(Items.this, Cart.class);
            startActivity(intent);
        });

        clearTv.setOnClickListener(view -> {

        });

        backIm.setOnClickListener(view -> {
            finish();
        });
    }

    public void updateCart(HashMap<String, String> product){
        int index=-1;
        for(int i=0;i<selectedProductList.size();i++){
            if(selectedProductList.get(i).get("product_id").toString().equals(product.get("product_id").toString())){
                index=i;
            }
        }
        int count=0;
        int total=0;
        if(index!=-1){
            double productCount=Double.parseDouble(selectedProductList.get(index).get("product_count"));
            if(productCount==0){
                selectedProductList.remove(index);
            }else {
                selectedProductList.set(index, product);
            }
        }else{
            selectedProductList.add(product);
        }

        for(int i=0;i<selectedProductList.size();i++){
            double productPrice=Double.parseDouble(selectedProductList.get(i).get("product_sell_price"));
            double productCount=Double.parseDouble(selectedProductList.get(i).get("product_count"));
            total+=productPrice*productCount;
            count+=productCount;
        }

        cartCountTv.setText(""+count);
        cartTotalPriceTv.setText(total+ " SAR");

        if(count>0){
            animateViewCartHeight(103*getResources().getDisplayMetrics().density);
        }else{
            animateViewCartHeight(0);
        }


    }

    public void animateViewCartHeight(float height){
        ValueAnimator animator=ValueAnimator.ofFloat(viewCartCl.getHeight(),height);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(500);
        animator.addUpdateListener(valueAnimator -> {
            ViewGroup.LayoutParams params=viewCartCl.getLayoutParams();
            params.height = (int)(float)valueAnimator.getAnimatedValue();
            viewCartCl.setLayoutParams(params);
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if(height==0){
                    viewCartCl.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if(height>0){
                    viewCartCl.setVisibility(View.VISIBLE);
                }
            }
        });
        animator.start();
    }
}