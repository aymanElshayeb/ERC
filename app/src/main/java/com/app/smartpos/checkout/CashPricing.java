package com.app.smartpos.checkout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.app.smartpos.R;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.pos.ProductCart;

public class CashPricing extends AppCompatActivity {

    double totalAmount=0;
    String cash="";
    TextView totalAmountTv;
    TextView cashGivingTv;
    TextView changeTv;

    String currency;
    DatabaseAccess databaseAccess;
    private double change;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_cash_pricing);

        totalAmount=(double) getIntent().getLongExtra("total_amount",0);

        totalAmountTv=findViewById(R.id.total_amount_tv);
        cashGivingTv=findViewById(R.id.cash_giving_tv);
        changeTv=findViewById(R.id.change_tv);
        TextView payTv=findViewById(R.id.pay_tv);



        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        currency = databaseAccess.getCurrency();

        totalAmountTv.setText(totalAmount+" "+currency);

        TextView num0=findViewById(R.id.num_0);
        TextView num1=findViewById(R.id.num_1);
        TextView num2=findViewById(R.id.num_2);
        TextView num3=findViewById(R.id.num_3);
        TextView num4=findViewById(R.id.num_4);
        TextView num5=findViewById(R.id.num_5);
        TextView num6=findViewById(R.id.num_6);
        TextView num7=findViewById(R.id.num_7);
        TextView num8=findViewById(R.id.num_8);
        TextView num9=findViewById(R.id.num_9);
        TextView dot=findViewById(R.id.dot);
        TextView del=findViewById(R.id.del);

        num0.setOnClickListener(view -> setNumber("0"));
        num1.setOnClickListener(view -> setNumber("1"));
        num2.setOnClickListener(view -> setNumber("2"));
        num3.setOnClickListener(view -> setNumber("3"));
        num4.setOnClickListener(view -> setNumber("4"));
        num5.setOnClickListener(view -> setNumber("5"));
        num6.setOnClickListener(view -> setNumber("6"));
        num7.setOnClickListener(view -> setNumber("7"));
        num8.setOnClickListener(view -> setNumber("8"));
        num9.setOnClickListener(view -> setNumber("9"));
        dot.setOnClickListener(view -> setNumber("."));
        del.setOnClickListener(view -> del());

        payTv.setOnClickListener(view -> {
            Intent intent=new Intent();
            intent.putExtra("change",change);
            setResult(RESULT_OK,intent);
            finish();
        });

    }

    private void setNumber(String number){
        cash+=number;
        cashGivingTv.setText(cash+" "+currency);
        change=totalAmount-Double.parseDouble(cash);
        changeTv.setText(change+" "+currency);
    }

    private void del(){
        cash=cash.substring(0,cash.length()-1);
        cashGivingTv.setText(cash+" "+currency);
        change=totalAmount-Double.parseDouble(cash);
        changeTv.setText(change+" "+currency);
    }
}