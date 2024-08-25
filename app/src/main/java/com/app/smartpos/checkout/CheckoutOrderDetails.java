package com.app.smartpos.checkout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.app.smartpos.NewHomeActivity;
import com.app.smartpos.R;
import com.app.smartpos.common.DeviceFactory.Device;
import com.app.smartpos.common.DeviceFactory.DeviceFactory;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.orders.OrderBitmap;

import java.util.HashMap;

public class CheckoutOrderDetails extends AppCompatActivity {

    DatabaseAccess databaseAccess;
    Device device;
    private ImageView receiptIm;
    private TextView printReceipt;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_checkout_order_details);

        device = DeviceFactory.getDevice();

        scrollView = findViewById(R.id.scrollView);
        receiptIm = findViewById(R.id.receipt_im);
        printReceipt = findViewById(R.id.print_receipt_tv);
        TextView noReceipt = findViewById(R.id.no_receipt_tv);
        TextView closeTv = findViewById(R.id.close_tv);

        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                createBitmap();
            }
        });


        noReceipt.setOnClickListener(view -> {
            Intent intent = new Intent(this, NewHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        closeTv.setOnClickListener(view -> {
            Intent intent = new Intent(this, NewHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

    }

    private void createBitmap() {
        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        String currency = databaseAccess.getCurrency();
        databaseAccess.open();
        //HashMap<String, String> orderDetails = databaseAccess.getOrderDetailsList(getIntent().getStringExtra("id")).get(0);
        HashMap<String, String> orderLitItem = databaseAccess.getOrderListByOrderId(getIntent().getStringExtra("id"));
        //Log.i("datadata",map.toString());
        String invoice_id = orderLitItem.get("invoice_id");
        String customer_name = orderLitItem.get("customer_name");
        String order_date = orderLitItem.get("order_date");
        String order_time = orderLitItem.get("order_time");
        databaseAccess.open();
        double tax = databaseAccess.totalOrderTax(invoice_id);
        String discount = orderLitItem.get("discount");
        databaseAccess.open();
        double price_after_tax = databaseAccess.totalOrderPrice(invoice_id);
        double price_before_tax = price_after_tax-tax;

        OrderBitmap orderBitmap = new OrderBitmap();
        Bitmap bitmap = orderBitmap.orderBitmap(invoice_id, order_date, order_time, price_before_tax, price_after_tax, tax, discount, currency);
        Log.i("datadata", bitmap.getHeight() + " " + scrollView.getHeight());

        if (bitmap.getHeight() < scrollView.getHeight()) {
            double scale=(double) scrollView.getHeight()/ bitmap.getHeight();
            bitmap=Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth()*scale),scrollView.getHeight(),false);
        }else{
            //bitmap=Bitmap.createScaledBitmap(bitmap,(int)(scrollView.getWidth()*0.7),bitmap.getHeight(),false);
        }
        receiptIm.setImageBitmap(bitmap);

        printReceipt.setOnClickListener(view -> {
            device.print(invoice_id, order_date, order_time, price_before_tax, price_after_tax, tax+"", discount, currency);
        });
    }
}