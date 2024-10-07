package com.app.smartpos.settings.end_shift;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.smartpos.R;
import com.app.smartpos.auth.AuthActivity;
import com.app.smartpos.utils.BaseActivity;
import com.app.smartpos.utils.SharedPrefUtils;

public class ShiftEndedSuccessfully extends BaseActivity {

    ImageView biggerCircleIm;
    ImageView smallerCircleIm;
    TextView logoutTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_shift_ended_successfully);
        biggerCircleIm=findViewById(R.id.bigger_circle_im);
        smallerCircleIm=findViewById(R.id.smaller_circle_im);
        logoutTv=findViewById(R.id.logout_timer_tv);

        startTimer();
    }

    private void startTimer(){
        new CountDownTimer(6000,1000) {
            @Override
            public void onTick(long l) {
                int time=(int)(l/1000);
                logoutTv.setText(getString(R.string.you_will_be_logged_out_in)+" "+time+" "+getString(R.string.seconds));
            }

            @Override
            public void onFinish() {
                SharedPrefUtils.setIsLoggedIn(ShiftEndedSuccessfully.this, true);
                finish();
                startActivity(new Intent(ShiftEndedSuccessfully.this, AuthActivity.class));
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