package com.app.smartpos.settings.end_shift;

import static com.app.smartpos.common.Utils.trimLongDouble;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.smartpos.R;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.utils.SharedPrefUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class EndShiftStep1 extends AppCompatActivity {

    double total_amount = 0;
    double total_tax = 0;
    EndShiftModel endShiftModel;

    DatabaseAccess databaseAccess;
    String currency;
    int totalRefunds=0;

    double totalRefundsAmount=0;
    double totalCardsAmount=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Window w = getWindow();
       // w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_end_shift_step1);

        TextView totalAmountTv = findViewById(R.id.total_amount_tv);
        EditText leaveCashEt = findViewById(R.id.leave_cash_et);
        EditText noteEt = findViewById(R.id.note_et);
        LinearLayout shiftItemsLl = findViewById(R.id.shift_items_ll);
        TextView confirmTv = findViewById(R.id.confirm_tv);
        TextView confirmWithErrorTv = findViewById(R.id.confirm_with_error_tv);
        ImageView backIm = findViewById(R.id.back_im);

        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        currency=databaseAccess.getCurrency();

        backIm.setOnClickListener(view -> finish());
        databaseAccess.open();
        String endDateString = databaseAccess.getLastShift("end_date_time");
        long lastShiftDate = endDateString.equals("") ? SharedPrefUtils.getStartDateTime(this) : Long.parseLong(endDateString);
        databaseAccess.open();
        //get data from local database
        List<HashMap<String, String>> orderList;
        orderList = databaseAccess.getOrderListWithTime(lastShiftDate);
        Log.i("datadata", orderList.size() + " " + lastShiftDate);
        HashMap<String, String> paymentTypesCashMap = new HashMap<>();

        databaseAccess.open();
        String startCashString = databaseAccess.getLastShift("leave_cash");
        double startCash = startCashString.equals("") ? 0 : Double.parseDouble(startCashString);

        paymentTypesCashMap.put("CASH",startCash+"");

        databaseAccess.open();
        for (int i = 0; i < orderList.size(); i++) {
            Log.i("datadata_tax",orderList.get(i).toString());
            databaseAccess.open();
            double total_price = databaseAccess.totalOrderPrice(orderList.get(i).get("invoice_id"));
            databaseAccess.open();
            double tax = databaseAccess.totalOrderTax(orderList.get(i).get("invoice_id"));
            double discount = Double.parseDouble(orderList.get(i).get("discount"));
            if(orderList.get(i).get("operation_type").equals("refund")){
                totalRefunds++;
                totalRefundsAmount +=  total_price;
            }
            total_amount += total_price;
            total_tax += tax;
            Log.i("datadata_card",orderList.get(i).get("order_payment_method")+" "+orderList.get(i).get("card_type_code"));
            double calculated_total_price = total_price - discount;
            if (orderList.get(i).get("order_payment_method").equals("CASH")) {
                double cash = Double.parseDouble(paymentTypesCashMap.get("CASH"));
                double total = calculated_total_price + cash;
                Log.i("datadata_shift",calculated_total_price+" "+cash+" "+totalRefundsAmount+" "+total);
                paymentTypesCashMap.put("CASH", total + "");
            }else if (orderList.get(i).get("order_payment_method").equals("CARD") && paymentTypesCashMap.containsKey(orderList.get(i).get("card_type_code").toString())) {
                double cash = Double.parseDouble(paymentTypesCashMap.get(orderList.get(i).get("card_type_code")));
                double total = calculated_total_price + cash;
                paymentTypesCashMap.put(orderList.get(i).get("card_type_code"), total + "");
                totalCardsAmount += calculated_total_price;
            } else {
                paymentTypesCashMap.put(orderList.get(i).get("card_type_code"), calculated_total_price + "");
            }
        }
        Log.i("datadata_shift",total_amount+" "+totalRefundsAmount);
        totalAmountTv.setText((total_amount+totalRefundsAmount)+" "+currency);
        databaseAccess.open();
        List<HashMap<String, String>> cardTypes=new ArrayList<>();
        HashMap<String, String> cash_map = new HashMap<>();
        cash_map.put("active", "1");
        cash_map.put("CASH", paymentTypesCashMap.get("CASH").toString());
        cash_map.put("name", "CASH");
        cash_map.put("code", "CASH");
        cardTypes.add(cash_map);
        cardTypes.addAll(databaseAccess.getCardTypes(true));
        for(int i=0;i<cardTypes.size();i++){
            String code=cardTypes.get(i).get("code");
            String cash=paymentTypesCashMap.get(code);
            cardTypes.get(i).put("CASH",cash);
        }
        Log.i("datadata",paymentTypesCashMap.get("CASH").toString());
        LinkedList<EndShiftPaymentModels> models = new LinkedList<>();
        for (int i = 0; i < cardTypes.size(); i++) {

            // remove this condition if you want to show all cards
            if ("CASH".equals(cardTypes.get(i).get("name"))) {
                View root_view = LayoutInflater.from(this).inflate(R.layout.layout_new_end_shift_payment_method, null);
                Log.w("card types", cardTypes + "");

                TextView paymentTypeTv = root_view.findViewById(R.id.payment_type_tv);
                EditText paymentTypeAmountEt = root_view.findViewById(R.id.payment_type_amount_et);
                TextView paymentTypeAmountErrorTv = root_view.findViewById(R.id.payment_type_amount_error_tv);
                paymentTypeTv.setText(cardTypes.get(i).get("name"));

                String cash = cardTypes.get(i).get("CASH");
                if (cash == null) {
                    cash = "0";
                }
                models.addLast(new EndShiftPaymentModels(paymentTypeAmountEt, paymentTypeAmountErrorTv, cardTypes.get(i).get("name"), cardTypes.get(i).get("code"), Double.parseDouble(cash)));

                shiftItemsLl.addView(root_view);

                paymentTypeAmountEt.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        confirmWithErrorTv.setVisibility(View.GONE);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {}
                });
            }
        }

        HashMap<String, ShiftDifferences> map = new HashMap<>();

        confirmTv.setOnClickListener(view -> {
            if (leaveCashEt.getText().toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.leave_cash_empty), Toast.LENGTH_SHORT).show();
                return;
            }

            int total_transactions = orderList.size()-totalRefunds;

            for (int i = 0; i < models.size(); i++) {
                String real = models.get(i).inputPaymentCashEt.getText().toString();
                double employeeCash = Double.parseDouble(real.isEmpty() ? "0" : real);
                models.get(i).setError(employeeCash != models.get(i).real);
                ShiftDifferences shiftDifferences=new ShiftDifferences(models.get(i).real, employeeCash, (employeeCash - models.get(i).real),models.get(i).code);
                map.put(models.get(i).type, shiftDifferences);
                Log.i("datadata_type",models.get(i).type+" "+shiftDifferences.toString());
                if (employeeCash != models.get(i).real) {
                    String value = trimLongDouble((employeeCash - models.get(i).real));
                    if (employeeCash - models.get(i).real > 0) {
                        models.get(i).paymentCashErrorTv.setText("+" + value);
                        models.get(i).paymentCashErrorTv.setTextColor(getResources().getColor(R.color.successColor));
                    } else {
                        models.get(i).paymentCashErrorTv.setText(value);
                        models.get(i).paymentCashErrorTv.setTextColor(getResources().getColor(R.color.errorColor));
                    }
                }
                if(!models.get(i).type.equalsIgnoreCase("CASH"))
                    totalCardsAmount += shiftDifferences.getReal();
            }


            confirmWithErrorTv.setVisibility(View.GONE);
            boolean hasError = false;
            for (int i = 0; i < models.size(); i++) {
                if (models.get(i).isError()) {
                    hasError = true;
                    confirmWithErrorTv.setVisibility(View.VISIBLE);
                }
            }
//            String android_id = Settings.Secure.getString(this.getContentResolver(),
//                    Settings.Secure.ANDROID_ID);
            databaseAccess.open();
            HashMap<String, String> configuration = databaseAccess.getConfiguration();
            String ecr_code = configuration.isEmpty() ? "" : configuration.get("ecr_code").toString();
            databaseAccess.open();
            HashMap<String, String> sequenceMap = databaseAccess.getSequence(2, ecr_code);
            databaseAccess.open();
            databaseAccess.updateSequence(Integer.parseInt(sequenceMap.get("next_value")),Integer.parseInt(sequenceMap.get("sequence_id")));
            databaseAccess.open();
            String startDateString = databaseAccess.getLastShift("end_date_time");
            long startDate = startDateString.equals("") ? SharedPrefUtils.getStartDateTime(this) : Long.parseLong(startDateString);

//                ShiftDifferences shiftDifferencesForLeaveCash = map.get("CASH");
//                double realCash=0;
//                if(shiftDifferencesForLeaveCash!=null){
//                    realCash=shiftDifferencesForLeaveCash.real;
//                }

            endShiftModel = new EndShiftModel(map, sequenceMap.get("sequence"), SharedPrefUtils.getUsername(this), total_transactions, 0, totalRefunds, total_amount, total_tax, configuration.get("ecr_code"), startDate, new Date().getTime(), startCash, Double.parseDouble(leaveCashEt.getText().toString()),noteEt.getText().toString().trim() , totalRefundsAmount, totalCardsAmount);
            endShiftModel.setTotalRefunds(totalRefunds);
            if (hasError) {
                confirmWithErrorTv.setVisibility(View.VISIBLE);
            } else {
                EndShiftConfirmationDialog dialog=new EndShiftConfirmationDialog();
                dialog.setEndShiftStep2(this);
                dialog.show(getSupportFragmentManager(),"dialog");
            }

        });

        confirmWithErrorTv.setOnClickListener(view -> {
            EndShiftConfirmationDialog dialog=new EndShiftConfirmationDialog();
            dialog.setEndShiftStep2(this);
            dialog.show(getSupportFragmentManager(),"dialog");
        });

}

    public void addToShift() {
        databaseAccess.open();
        int id = databaseAccess.addShift(endShiftModel);
        if (id > -1) {
            LinkedList<String>keys=new LinkedList<>(endShiftModel.getShiftDifferences().keySet());
            for(int i=0;i<keys.size();i++) {
                if(!keys.get(i).equals("CASH")) {
                    Log.i("datadata",keys.get(i));
                    databaseAccess.open();
                    boolean added = databaseAccess.addShiftCreditCalculations(endShiftModel.getSequence(), endShiftModel.getShiftDifferences().get(keys.get(i)), keys.get(i));
                    Log.i("datadata", added + "");
                }
            }

        }
        Log.i("datadata", id + "");
        startActivity(new Intent(this, EndShiftStep2.class).putExtra("model",endShiftModel));

    }
}