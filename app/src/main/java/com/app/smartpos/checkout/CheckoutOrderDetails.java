package com.app.smartpos.checkout;

import static com.app.smartpos.common.CrashReport.CustomExceptionHandler.addToDatabase;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.app.smartpos.NewHomeActivity;
import com.app.smartpos.R;
import com.app.smartpos.devices.DeviceFactory.Device;
import com.app.smartpos.devices.DeviceFactory.DeviceFactory;
import com.app.smartpos.common.Utils;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.utils.BaseActivity;
import com.app.smartpos.utils.FilesUtils;
import com.app.smartpos.utils.printing.PrinterData;
import com.app.smartpos.utils.printing.PrintingHelper;

public class CheckoutOrderDetails extends BaseActivity {

    DatabaseAccess databaseAccess;
    Device device;
    private ImageView receiptIm;
    private TextView printReceipt;
    private ScrollView scrollView;
    private ConstraintLayout loadingCl;

    PrinterData printerData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_checkout_order_details);

        databaseAccess = DatabaseAccess.getInstance(this);
        device = DeviceFactory.getDevice();

        scrollView = findViewById(R.id.scrollView);
        loadingCl = findViewById(R.id.loading_cl);
        receiptIm = findViewById(R.id.receipt_im);
        printReceipt = findViewById(R.id.print_receipt_tv);
        TextView noReceipt = findViewById(R.id.no_receipt_tv);
        TextView closeTv = findViewById(R.id.close_tv);

        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                AsyncTask.execute(() -> {
                    runOnUiThread(() -> {
                        printerData = PrintingHelper.createBitmap(databaseAccess, CheckoutOrderDetails.this, getIntent().getStringExtra("id"), getIntent().getStringExtra("printType"));
                        Utils.addLog("datadata", printerData.toString());
                        final Bitmap[] bitmap = {printerData.getBitmap()};
                        if (bitmap[0].getHeight() < scrollView.getHeight()) {
                            double scale = (double) scrollView.getHeight() / bitmap[0].getHeight();
                            bitmap[0] = Bitmap.createScaledBitmap(bitmap[0], (int) (bitmap[0].getWidth() * scale), scrollView.getHeight(), false);
                        } else {
                            //bitmap=Bitmap.createScaledBitmap(bitmap,(int)(scrollView.getWidth()*0.7),bitmap.getHeight(),false);
                        }
                        loadingCl.setVisibility(View.GONE);
                        receiptIm.setImageBitmap(bitmap[0]);
                    });

                });

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
            try {
                Bitmap newBitmap = Bitmap.createBitmap(printerData.getBitmap());
                boolean success= device.printReceipt(newBitmap);
                if(success){
                    databaseAccess.open();
                    databaseAccess.updateOrderPrintFlag(true,getIntent().getStringExtra("id"));
                    Intent intent = new Intent(this, NewHomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            } catch (Exception e) {
                addToDatabase(e,getString(R.string.no_printer_found)+"-checkoutOrderDetails");
                e.printStackTrace();
                FilesUtils.generateNoteOnSD("no-printer",e.getStackTrace(),databaseAccess);
                Toast.makeText(this, R.string.no_printer_found, Toast.LENGTH_SHORT).show();
            }
        });

    }
}