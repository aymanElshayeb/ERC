package com.app.smartpos;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.smartpos.checkout.NewCheckout;
import com.app.smartpos.common.Utils;
import com.app.smartpos.database.DatabaseAccess;

public class QuickBill extends Activity {
    //ConstraintLayout rootCl;
    double totalAmount=0;
    String cash="";
    TextView amountTv;
    TextView currencyTv;

    String currency;
    DatabaseAccess databaseAccess;

    String type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.hide();
//        Window w = getWindow();
  //      w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_quick_bill);

        totalAmount=(double) getIntent().getLongExtra("total_amount",0);

        type=getIntent().getStringExtra("type");


        //rootCl=findViewById(R.id.root_cl);
        amountTv=findViewById(R.id.amount_tv);
        currencyTv=findViewById(R.id.currency_tv);
        TextView titleTv=findViewById(R.id.title_tv);
        TextView payTv=findViewById(R.id.option_tv);
        ImageView backIm=findViewById(R.id.back_im);
        EditText descriptionEt=findViewById(R.id.description_et);

        descriptionEt.setHint(getString(R.string.description)+" "+getString(R.string.optional));
        if(type.equals("customItem")){
            titleTv.setText(getString(R.string.custom_item));
            payTv.setText(getString(R.string.add_to_cart));
        }else if(type.equals("quickBill")){
            titleTv.setText(getString(R.string.quick_bills));
            payTv.setText(getString(R.string.pay));
        }

        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        currency = databaseAccess.getCurrency();

        currencyTv.setText(currency);

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
            double cashResult=Double.parseDouble(amountTv.getText().toString());

            if(cashResult>0) {
                Intent intent = new Intent();
                intent.putExtra("amount", Utils.trimLongDouble(amountTv.getText().toString()));
                intent.putExtra("description", descriptionEt.getText().toString().trim());
                if(getIntent().getStringExtra("type").equals("customItem")) {
                    setResult(RESULT_OK, intent);
                    finish();
                }else{
                    startActivity(new Intent(this, NewCheckout.class).putExtra("fromQuickBill",true).putExtra("amount",Utils.trimLongDouble(amountTv.getText().toString())).putExtra("description", descriptionEt.getText().toString().trim()));
                }
            }else{
                Toast.makeText(this, getString(R.string.amount_must_be_more_than_zero), Toast.LENGTH_SHORT).show();
            }
        });

        amountTv.setText("0");

        backIm.setOnClickListener(view -> finish());


    }

    private void setNumber(String number){
        if(amountTv.getText().toString().length()==6){
            return;
        }
        if(cash.equals("0")){
            cash="";
        }
        if(cash.isEmpty() && number.equals(".")){
            number="0.";
        }
        if(cash.contains(".") && number.equals(".")){
            number="";
        }
        cash += number;
        amountTv.setText(cash);

    }

    private void del(){
        if(cash.length()<=1){
            cash="";
            amountTv.setText("0");
        }else {
            cash = cash.substring(0, cash.length() - 1);
            amountTv.setText(cash);
        }
    }
}