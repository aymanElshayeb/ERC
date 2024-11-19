package com.app.smartpos.Items;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.smartpos.QuickBill;
import com.app.smartpos.R;
import com.app.smartpos.adapter.PosProductAdapter;
import com.app.smartpos.cart.Cart;
import com.app.smartpos.common.Utils;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.pos.ScannerActivity;
import com.app.smartpos.utils.BaseActivity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Items extends BaseActivity {

    public static EditText searchEt;
    PosProductAdapter productCartAdapter;
    List<HashMap<String, String>> productList;
    List<HashMap<String, String>> selectedProductList = new LinkedList<>();
    private RecyclerView recycler;
    TextView cartCountTv;
    TextView cartTotalPriceTv;
    ConstraintLayout viewCartCl;
    ConstraintLayout openCartCl;
    DatabaseAccess databaseAccess;
    boolean firstOpen = true;
    String currency;
    boolean checkConnectionOnce = true;

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

        searchEt = findViewById(R.id.search_et);
        LinearLayout noProductsLl = findViewById(R.id.no_products_ll);
        ImageView scannerIm = findViewById(R.id.img_scanner);
        ImageView backIm = findViewById(R.id.back_im);
        ImageView moreIm = findViewById(R.id.more_im);
        recycler = findViewById(R.id.recycler);
        cartCountTv = findViewById(R.id.cart_count_tv);
        cartTotalPriceTv = findViewById(R.id.cart_total_price_tv);
        viewCartCl = findViewById(R.id.view_cart_cl);
        openCartCl = findViewById(R.id.open_cart_cl);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2, RecyclerView.VERTICAL, false);
        recycler.setLayoutManager(gridLayoutManager);

        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();

        currency = databaseAccess.getCurrency();

        databaseAccess.open();
        productList = databaseAccess.getProducts(true);

        productCartAdapter = new PosProductAdapter(this, productList);


        openCartCl.setOnClickListener(view -> {
            Intent intent = new Intent(Items.this, Cart.class);
            startActivity(intent);
        });

        scannerIm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Items.this, ScannerActivity.class).putExtra("screenType","items");
                startActivity(intent);
            }
        });

        moreIm.setOnClickListener(view -> {
            ItemsOptionsDialog dialog = new ItemsOptionsDialog();
            dialog.setItems(this);
            dialog.show(getSupportFragmentManager(), "dialog");
        });

        backIm.setOnClickListener(view -> {
            finish();
        });

        searchEt.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                databaseAccess.open();
                productList = databaseAccess.getSearchProducts(s.toString(), true);
                productCartAdapter = new PosProductAdapter(Items.this, productList);
                recycler.setAdapter(productCartAdapter);

                noProductsLl.setVisibility(productList.size() > 0 ? View.GONE : View.VISIBLE);
                recycler.setVisibility(productList.size() == 0 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }


        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        databaseAccess.open();

        selectedProductList = databaseAccess.getCartProduct();
        updateCartUI();
        firstOpen = false;
        productCartAdapter.notifyDataSetChanged();
        if (!isConnected()) {
            setAdapter();
        }
    }

    @Override
    public void connectionChanged(boolean state) {
        super.connectionChanged(state);
        Utils.addLog("datadata_connection", String.valueOf(state));
        setAdapter();
    }

    private void setAdapter() {
        if (checkConnectionOnce) {
            checkConnectionOnce = false;
            runOnUiThread(() -> {
                recycler.setAdapter(productCartAdapter);
            });
        }
    }

    public void updateCart(HashMap<String, String> product, int position) {
        int index = -1;
        for (int i = 0; i < selectedProductList.size(); i++) {
            if (selectedProductList.get(i).get("product_id").equals(product.get("product_id"))) {
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
                databaseAccess.updateProductInCart(id, product.get("product_count"));
                selectedProductList.set(index, convertProductToCartItem(product));
            }
        } else {

            String product_id = product.get("product_id");
            String product_weight = product.get("product_weight");
            String product_stock = product.get("product_stock");
            String product_price = product.get("product_sell_price");
            String weight_unit_id = product.get("product_weight_unit_id");
            String product_uuid = product.get("product_uuid");
            databaseAccess.open();
            int check = databaseAccess.addToCart(product_id, product_weight, weight_unit_id, product_price, 1, product_stock, product_uuid, "");
            selectedProductList.add(convertProductToCartItem(product));
        }

        updateCartUI();
    }

    private void updateCartUI() {
        int count = 0;
        double total = 0;
        for (int i = 0; i < selectedProductList.size(); i++) {
            double productPrice = Double.parseDouble(selectedProductList.get(i).get("product_price"));
            double productCount = Double.parseDouble(selectedProductList.get(i).get("product_qty"));
            total += productPrice * productCount;
            count += productCount;
        }
        Utils.addLog("datadata_count", String.valueOf(count));
        cartCountTv.setText(String.valueOf(count));
        cartTotalPriceTv.setText(Utils.trimLongDouble(total) + " " + currency);


        if (count > 0) {
            animateViewCartHeight(103 * getResources().getDisplayMetrics().density);
        } else if (!firstOpen) {
            animateViewCartHeight(1f);
        }
    }

    public Boolean checkCartTotalPrice(int pos) {
        double total = 0;
        for (int i = 0; i < selectedProductList.size(); i++) {
            double productPrice = Double.parseDouble(selectedProductList.get(i).get("product_price"));
            double productCount = Double.parseDouble(selectedProductList.get(i).get("product_qty"));
            total += productPrice * productCount;
            Utils.addLog("datadata_total", (i == pos) + " " + (productPrice * productCount));
        }
        total += Double.parseDouble(productList.get(pos).get("product_sell_price"));
        Utils.addLog("datadata_total", String.valueOf(total));
        return total > 999999999.99;
    }

    public Boolean checkCartTotalPriceForCustomItem(double amount) {
        double total = 0;
        for (int i = 0; i < selectedProductList.size(); i++) {
            double productPrice = Double.parseDouble(selectedProductList.get(i).get("product_price"));
            double productCount = Double.parseDouble(selectedProductList.get(i).get("product_qty"));
            total += productPrice * productCount;
        }
        return (total + amount) > 999999999.99;

    }

    private HashMap<String, String> convertProductToCartItem(HashMap<String, String> product) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("product_id", product.get("product_id"));
        map.put("product_price", product.get("product_sell_price"));
        map.put("product_qty", product.get("product_count"));
        map.put("product_uuid", product.get("product_uuid"));
        map.put("product_description", product.get("product_description"));

        return map;
    }

    public void animateViewCartHeight(float height) {
        ValueAnimator animator = ValueAnimator.ofFloat(viewCartCl.getHeight(), height);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(500);
        animator.addUpdateListener(valueAnimator -> {
            ViewGroup.LayoutParams params = viewCartCl.getLayoutParams();
            Utils.addLog("datadata_height", String.valueOf((int) (float) valueAnimator.getAnimatedValue()));
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

    public String checkCount(int position) {
        String count = "0";
        for (int i = 0; i < selectedProductList.size(); i++) {
            if (selectedProductList.get(i).get("product_id").equals(productList.get(position).get("product_id"))) {
                count = selectedProductList.get(i).get("product_qty");
                Utils.addLog("datadata_count", selectedProductList.get(i).toString());
                Utils.addLog("datadata_count", count);
                productList.get(position).put("product_count", count);
                break;
            }
        }
        return count;
    }

    public void openCustomBill() {
        databaseAccess.open();
        boolean isItemExist = databaseAccess.checkCustomProductInCart();
        if (!isItemExist) {
            startActivityForResult(new Intent(this, QuickBill.class).putExtra("type", "customItem"), 12);
        } else {
            Toast.makeText(this, getString(R.string.cutom_item_already_added), Toast.LENGTH_SHORT).show();
        }
    }

    public void addCustomItem(String amount, String description) {
        databaseAccess.open();
        boolean isItemExist = databaseAccess.checkCustomProductInCart();
        if (!isItemExist) {
            if (checkCartTotalPriceForCustomItem(Double.parseDouble(amount))) {
                Toast.makeText(this, R.string.total_price_cannot_exceed, Toast.LENGTH_SHORT).show();
                return;
            }
            databaseAccess.open();
            HashMap<String, String> product = databaseAccess.getCustomProduct();
            String product_id = product.get("product_id");
            String product_weight = product.get("product_weight");
            String product_stock = product.get("product_stock");
            //String product_price = product.get("product_sell_price");
            String weight_unit_id = product.get("product_weight_unit_id");
            product.put("product_count", "1");
            product.put("product_sell_price", amount);
            product.put("product_description", description);
            String product_uuid = product.get("product_uuid");
            databaseAccess.open();
            databaseAccess.addToCart(product_id, product_weight, weight_unit_id, amount, 1, product_stock, product_uuid, description);
            selectedProductList.add(convertProductToCartItem(product));
            Utils.addLog("datadata", String.valueOf(selectedProductList.size()));
            updateCartUI();
        } else {
            Toast.makeText(this, getString(R.string.cutom_item_already_added), Toast.LENGTH_SHORT).show();
        }
    }

    public void clearAllItems() {
        for (int i = 0; i < selectedProductList.size(); i++) {
            String product_id = selectedProductList.get(i).get("product_id");
            databaseAccess.open();
            databaseAccess.removeProductFromCart(product_id);
        }
        selectedProductList.clear();
        productCartAdapter.notifyDataSetChanged();
        updateCartUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 12) {
            addCustomItem(data.getStringExtra("amount"), data.getStringExtra("description"));
        }
    }
}