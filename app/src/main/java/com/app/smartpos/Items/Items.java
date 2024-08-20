package com.app.smartpos.Items;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.smartpos.R;
import com.app.smartpos.adapter.PosProductAdapter;
import com.app.smartpos.cart.Cart;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.pos.PosActivity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class Items extends AppCompatActivity {

    PosProductAdapter productCartAdapter;
    List<HashMap<String, String>> productList;
    List<HashMap<String, String>> selectedProductList = new LinkedList<>();
    TextView cartCountTv;
    TextView cartTotalPriceTv;
    ConstraintLayout viewCartCl;
    ConstraintLayout openCartCl;
    DatabaseAccess databaseAccess;

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

        ImageView backIm = findViewById(R.id.back_im);
        TextView clearTv = findViewById(R.id.clear_tv);
        RecyclerView recycler = findViewById(R.id.recycler);
        cartCountTv = findViewById(R.id.cart_count_tv);
        cartTotalPriceTv = findViewById(R.id.cart_total_price_tv);
        viewCartCl = findViewById(R.id.view_cart_cl);
        openCartCl = findViewById(R.id.open_cart_cl);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2, RecyclerView.VERTICAL, false);
        recycler.setLayoutManager(gridLayoutManager);

        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();

        productList = databaseAccess.getProducts(false);

        Log.i("datadata", productList.size() + "");
        productCartAdapter = new PosProductAdapter(this, productList);
        recycler.setAdapter(productCartAdapter);

        openCartCl.setOnClickListener(view -> {
            Intent intent = new Intent(Items.this, Cart.class);
            startActivity(intent);
        });

        clearTv.setOnClickListener(view -> {

        });

        backIm.setOnClickListener(view -> {
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        databaseAccess.open();

        selectedProductList = databaseAccess.getCartProduct();
        if(selectedProductList.size()>0) {
            updateCartUI();
        }
        productCartAdapter.notifyDataSetChanged();
    }

    public void updateCart(HashMap<String, String> product, int position) {
        int index = -1;
        for (int i = 0; i < selectedProductList.size(); i++) {
            if (selectedProductList.get(i).get("product_id").toString().equals(product.get("product_id").toString())) {
                index = i;
            }
        }

        if (index != -1) {
            double productCount = Double.parseDouble(product.get("product_count"));


            databaseAccess.open();
            String id = databaseAccess.getCartProductById(product.get("product_id")).get("product_id");

            if (productCount == 0) {
                databaseAccess.open();
                databaseAccess.removeProductFromCart(id);
                selectedProductList.remove(index);
            } else {

                databaseAccess.open();
                databaseAccess.updateProductInCart(id,product.get("product_count"));
                selectedProductList.set(index, convertProductToCartItem(product));
            }
        } else {

            final String product_id = product.get("product_id");
            final String product_weight = product.get("product_weight");
            final String product_stock = product.get("product_stock");
            final String product_price = product.get("product_sell_price");
            final String weight_unit_id = product.get("product_weight_unit_id");
            databaseAccess.open();
            int check = databaseAccess.addToCart(product_id, product_weight, weight_unit_id, product_price, 1, product_stock);
            selectedProductList.add(convertProductToCartItem(product));
        }

        updateCartUI();
    }

    private void updateCartUI(){
        int count = 0;
        int total = 0;
        for (int i = 0; i < selectedProductList.size(); i++) {
            double productPrice = Double.parseDouble(selectedProductList.get(i).get("product_price"));
            double productCount = Double.parseDouble(selectedProductList.get(i).get("product_qty"));
            total += productPrice * productCount;
            count += productCount;
        }

        cartCountTv.setText("" + count);
        cartTotalPriceTv.setText(total + " SAR");


        if (count > 0) {
            animateViewCartHeight(103 * getResources().getDisplayMetrics().density);
        } else {
            animateViewCartHeight(1);
        }
    }

    private HashMap<String ,String> convertProductToCartItem(HashMap<String,String>product){
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("product_id", product.get("product_id"));
        map.put("product_price", product.get("product_sell_price"));
        map.put("product_qty", product.get("product_count"));

        return map;
    }
    public void animateViewCartHeight(float height) {
        ValueAnimator animator = ValueAnimator.ofFloat(viewCartCl.getHeight(), height);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(500);
        animator.addUpdateListener(valueAnimator -> {
            ViewGroup.LayoutParams params = viewCartCl.getLayoutParams();
            params.height = (int) (float) valueAnimator.getAnimatedValue();
            viewCartCl.setLayoutParams(params);
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                viewCartCl.setVisibility(View.VISIBLE);
            }
        });

        animator.start();
    }

    public String checkCount(int position){
        String count="0";
        for (int i = 0; i < selectedProductList.size(); i++) {
            if (selectedProductList.get(i).get("product_id").toString().equals(productList.get(position).get("product_id").toString())) {
                count=selectedProductList.get(i).get("product_qty");
                Log.i("datadata_count",selectedProductList.get(i).toString());
                Log.i("datadata_count",count);
                productList.get(position).put("product_count",count);
                break;
            }
        }
        return count;
    }
}