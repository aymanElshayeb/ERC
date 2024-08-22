package com.app.smartpos.checkout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_checkout_order_details);

        device = DeviceFactory.getDevice();

        ImageView receiptIm = findViewById(R.id.receipt_im);
        TextView printReceipt = findViewById(R.id.print_receipt_tv);
        TextView noReceipt = findViewById(R.id.no_receipt_tv);

        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        String currency = databaseAccess.getCurrency();
        databaseAccess.open();
        HashMap<String, String> map = databaseAccess.getOrderDetailsList(getIntent().getStringExtra("id")).get(0);

        String invoice_id = map.get("invoice_id");
        String customer_name = map.get("customer_name");
        String order_date = map.get("order_date");
        String order_time = map.get("order_time");
        String tax = map.get("tax");
        String discount = map.get("discount");
        double price_before_tax = Double.parseDouble(map.get("ex_tax_total"));
        double price_after_tax = Double.parseDouble(map.get("in_tax_total"));

        OrderBitmap orderBitmap = new OrderBitmap();
        Bitmap bitmap = orderBitmap.orderBitmap(invoice_id, order_date, order_time, price_before_tax, price_after_tax, tax, discount, currency);
        receiptIm.setImageBitmap(bitmap);

        printReceipt.setOnClickListener(view -> {
            device.print(invoice_id, order_date, order_time, price_before_tax, price_after_tax, tax, discount, currency);
        });

        noReceipt.setOnClickListener(view -> {
            Intent intent = new Intent(this, NewHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

    }
}