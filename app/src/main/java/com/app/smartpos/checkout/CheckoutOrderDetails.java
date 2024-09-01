package com.app.smartpos.checkout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
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
import com.app.smartpos.common.Utils;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.orders.OrderBitmap;
import com.app.smartpos.utils.printing.PrinterData;
import com.app.smartpos.utils.printing.PrintingHelper;

import java.util.HashMap;

public class CheckoutOrderDetails extends AppCompatActivity {

    DatabaseAccess databaseAccess;
    Device device;
    private ImageView receiptIm;
    private TextView printReceipt;
    private ScrollView scrollView;

    PrinterData printerData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_checkout_order_details);

        databaseAccess=DatabaseAccess.getInstance(this);
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
                printerData=PrintingHelper.createBitmap(databaseAccess,getIntent().getStringExtra("id"));
                Bitmap bitmap=printerData.getBitmap();
                if (bitmap.getHeight() < scrollView.getHeight()) {
                    double scale=(double) scrollView.getHeight()/ bitmap.getHeight();
                    bitmap=Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth()*scale),scrollView.getHeight(),false);
                }else{
                    //bitmap=Bitmap.createScaledBitmap(bitmap,(int)(scrollView.getWidth()*0.7),bitmap.getHeight(),false);
                }
                receiptIm.setImageBitmap(bitmap);


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

        printReceipt.setOnClickListener(view -> {
            device.printReciept(printerData.getInvoice_id(), printerData.getOrder_date(), printerData.getOrder_time(), printerData.getPrice_before_tax(), printerData.getPrice_after_tax(), printerData.getTax()+"", printerData.getDiscount(), printerData.getCurrency());
        });

    }
}