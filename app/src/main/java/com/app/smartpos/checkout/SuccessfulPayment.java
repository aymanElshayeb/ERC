package com.app.smartpos.checkout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.smartpos.R;
import com.app.smartpos.auth.AuthActivity;
import com.app.smartpos.orders.OrderDetailsActivity;
import com.app.smartpos.settings.end_shift.ShiftEndedSuccessfully;

public class SuccessfulPayment extends AppCompatActivity {
    ImageView biggerCircleIm;
    ImageView smallerCircleIm;
    TextView amountTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_successful_payment);
        biggerCircleIm=findViewById(R.id.bigger_circle_im);
        smallerCircleIm=findViewById(R.id.smaller_circle_im);
        amountTv=findViewById(R.id.amount_tv);
        TextView titleTv=findViewById(R.id.title_tv);

        if(!getIntent().getExtras().containsKey("id")) {
            titleTv.setText(getString(R.string.items_successfully_refunded));
        }
        amountTv.setText(getIntent().getStringExtra("amount"));
        startTimer();
    }

    private void startTimer(){
        new CountDownTimer(4000,1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                finish();
                if(getIntent().getExtras().containsKey("id")) {
                    startActivity(new Intent(SuccessfulPayment.this, CheckoutOrderDetails.class).putExtra("id", getIntent().getStringExtra("id")));
                }
            }
        }.start();

        animateCircle(smallerCircleIm,140*getResources().getDisplayMetrics().density);
        animateCircle(biggerCircleIm,200*getResources().getDisplayMetrics().density);
    }

    private void animateCircle(View view, float size){
        ValueAnimator animator=ValueAnimator.ofFloat(view.getWidth(),size);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(valueAnimator -> {
            ViewGroup.LayoutParams params=view.getLayoutParams();
            params.width=(int)(float)valueAnimator.getAnimatedValue();
            params.height=(int)(float)valueAnimator.getAnimatedValue();
            view.setLayoutParams(params);
        });
        animator.setDuration(3000);
        animator.start();
    }

    @Override
    public void onBackPressed() {

    }
}