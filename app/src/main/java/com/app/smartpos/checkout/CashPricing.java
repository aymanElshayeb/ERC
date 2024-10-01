package com.app.smartpos.checkout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.smartpos.R;
import com.app.smartpos.common.Utils;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.pos.ProductCart;
import com.app.smartpos.utils.BaseActivity;

import java.text.DecimalFormat;

public class CashPricing extends BaseActivity {

    double totalAmount = 0;
    String cash = "";
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

        totalAmount = (double) getIntent().getDoubleExtra("total_amount", 0);

        totalAmountTv = findViewById(R.id.total_amount_tv);
        cashGivingTv = findViewById(R.id.cash_giving_tv);
        changeTv = findViewById(R.id.change_tv);
        TextView totalAmountSarTv = findViewById(R.id.total_amount_sar_tv);
        TextView cashGivingSarTv = findViewById(R.id.cash_giving_sar_tv);
        TextView changeSarTv = findViewById(R.id.change_sar_tv);
        TextView payTv = findViewById(R.id.pay_tv);
        ImageView backIm = findViewById(R.id.back_im);


        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        currency = databaseAccess.getCurrency();

        totalAmountSarTv.setText(currency);
        changeSarTv.setText(currency);
        cashGivingSarTv.setText(currency);

        changeTv.setMaxEms(7+currency.length());

        totalAmountTv.setText(Utils.trimLongDouble(totalAmount));

        TextView num0 = findViewById(R.id.num_0);
        TextView num1 = findViewById(R.id.num_1);
        TextView num2 = findViewById(R.id.num_2);
        TextView num3 = findViewById(R.id.num_3);
        TextView num4 = findViewById(R.id.num_4);
        TextView num5 = findViewById(R.id.num_5);
        TextView num6 = findViewById(R.id.num_6);
        TextView num7 = findViewById(R.id.num_7);
        TextView num8 = findViewById(R.id.num_8);
        TextView num9 = findViewById(R.id.num_9);
        TextView dot = findViewById(R.id.dot);
        TextView del = findViewById(R.id.del);

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
            //double changeResult = Double.parseDouble(Utils.trimLongDouble(changeTv.getText().toString().split(" ")[0]));
            double cashResult = Double.parseDouble(Utils.trimLongDouble(cashGivingTv.getText().toString().split(" ")[0]));
            if (cashResult >= Double.parseDouble(Utils.trimLongDouble(totalAmount))) {
                Intent intent = new Intent();
                intent.putExtra("change", change);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(this, getString(R.string.cash_given_must_be_equal_or_more_than_total_amount), Toast.LENGTH_SHORT).show();
            }
        });

        cashGivingTv.setText("0");
        change = 0;
        changeTv.setText(change+"");

        backIm.setOnClickListener(view -> finish());

    }

    private void setNumber(String number) {
        if (cashGivingTv.getText().toString().length() == 7) {
            return;
        }
        if (cash.equals("0")) {
            cash = "";
        }
        if(cashGivingTv.getText().toString().contains(".") && cashGivingTv.getText().toString().split("\\.").length==2 && cashGivingTv.getText().toString().split("\\.")[1].length()==2){
            return;
        }
        if (cash.isEmpty() && number.equals(".")) {
            number = "0.";
        }
        if (cash.contains(".") && number.equals(".")) {
            number = "";
        }
        cash += number;
        cashGivingTv.setText(cash);
        change = totalAmount - Double.parseDouble(cash);
        changeTv.setText(Utils.trimLongDouble(change));
    }

    private void del() {
        if (cash.length() <= 1) {
            cash = "";
            cashGivingTv.setText("0");
            change = 0;
        } else {
            cash = cash.substring(0, cash.length()- 1);
            cashGivingTv.setText(cash);
            change = totalAmount - Double.parseDouble(cash);
        }

        changeTv.setText(Utils.trimLongDouble(change));
    }
}