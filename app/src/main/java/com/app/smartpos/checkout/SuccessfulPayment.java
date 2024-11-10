package com.app.smartpos.checkout;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import com.app.smartpos.NewHomeActivity;
import com.app.smartpos.R;
import com.app.smartpos.devices.DeviceFactory.Device;
import com.app.smartpos.devices.DeviceFactory.DeviceFactory;
import com.app.smartpos.common.Utils;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.utils.BaseActivity;
import com.app.smartpos.utils.printing.PrinterData;
import com.app.smartpos.utils.printing.PrintingHelper;

public class SuccessfulPayment extends BaseActivity {
    ImageView biggerCircleIm;
    ImageView smallerCircleIm;
    TextView amountTv;
    LinearLayout printLl;
    PrinterData printerData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_successful_payment);
        biggerCircleIm = findViewById(R.id.bigger_circle_im);
        smallerCircleIm = findViewById(R.id.smaller_circle_im);
        amountTv = findViewById(R.id.amount_tv);
        printLl = findViewById(R.id.print_ll);
        TextView titleTv = findViewById(R.id.title_tv);

        TextView printReceipt = findViewById(R.id.print_receipt_tv);
        TextView noReceipt = findViewById(R.id.no_receipt_tv);

        if (!getIntent().getExtras().containsKey("id")) {
            titleTv.setText(getString(R.string.items_successfully_refunded));
        }
        startTimer();
        if (getIntent().getExtras().containsKey("order_id")) {
            printerData = PrintingHelper.createBitmap(DatabaseAccess.getInstance(this), this, getIntent().getStringExtra("order_id"), getIntent().getStringExtra("printType"));
            printLl.setVisibility(View.VISIBLE);
        }
        String amount = getIntent().getStringExtra("amount").split(" ")[0];
        String currency = getIntent().getStringExtra("amount").split(" ")[1];
        amountTv.setText(Utils.trimLongDouble(amount) + " " + currency);

        noReceipt.setOnClickListener(view -> {
            finish();
        });
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        printReceipt.setOnClickListener(view -> {
            Device device = DeviceFactory.getDevice();
            try {
                Bitmap newBitmap = Bitmap.createBitmap(printerData.getBitmap());
                boolean success=device.printReceipt(newBitmap);
                if(success){
                    databaseAccess.open();
                    databaseAccess.updateOrderPrintFlag(true,getIntent().getStringExtra("id"));
                    Intent intent = new Intent(this, NewHomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.no_printer_found, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startTimer() {
        new CountDownTimer(4000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                if (!getIntent().getExtras().containsKey("order_id")) {
                    finish();
                }
                if (getIntent().getExtras().containsKey("id")) {
                    startActivity(new Intent(SuccessfulPayment.this, CheckoutOrderDetails.class).putExtra("id", getIntent().getStringExtra("id")).putExtra("printType", getIntent().getStringExtra("printType")));
                }
            }
        }.start();

        animateCircle(smallerCircleIm, 140 * getResources().getDisplayMetrics().density);
        animateCircle(biggerCircleIm, 200 * getResources().getDisplayMetrics().density);
    }

    private void animateCircle(View view, float size) {
        ValueAnimator animator = ValueAnimator.ofFloat(view.getWidth(), size);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(valueAnimator -> {
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.width = (int) (float) valueAnimator.getAnimatedValue();
            params.height = (int) (float) valueAnimator.getAnimatedValue();
            view.setLayoutParams(params);
        });
        animator.setDuration(3000);
        animator.start();
    }

    @Override
    public void onBackPressed() {

    }
}