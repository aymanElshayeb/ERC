package com.app.smartpos.settings.end_shift;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.app.smartpos.R;
import com.app.smartpos.database.DatabaseAccess;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class EndShiftDialog extends DialogFragment {

    View root;

    double total_amount=0;
    double total_tax=0;
    EndShiftModel endShiftModel;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (root == null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            root = inflater.inflate(R.layout.dialog_end_shift, container, false);
            setCancelable(false);

            Button submitBtn = root.findViewById(R.id.btn_submit);
            Button confirmBtn = root.findViewById(R.id.confirm_btn);
            ImageButton closeBtn = root.findViewById(R.id.btn_close);
            LinearLayout endCashTypesLl = root.findViewById(R.id.end_cash_types_ll);
            LinearLayout errorLl = root.findViewById(R.id.error_ll);


            final DatabaseAccess databaseAccess = DatabaseAccess.getInstance(requireActivity());
            databaseAccess.open();


            //get data from local database
            List<HashMap<String, String>> orderList;
            orderList = databaseAccess.getOrderList();
            HashMap<String, String> paymentTypesCashMap = new HashMap<>();
            databaseAccess.open();
            for (int i = 0; i < orderList.size(); i++) {
                double total_price = databaseAccess.totalOrderPrice(orderList.get(i).get("invoice_id"));
                double tax = Double.parseDouble(orderList.get(i).get("tax"));
                double discount = Double.parseDouble(orderList.get(i).get("discount"));

                total_amount+=total_price;
                total_tax+=tax;

                double calculated_total_price = total_price + tax - discount;
                if (paymentTypesCashMap.containsKey(orderList.get(i).get("order_payment_method").toString())) {
                    double cash = Double.parseDouble(paymentTypesCashMap.get(orderList.get(i).get("order_payment_method")));
                    double total = calculated_total_price + cash;
                    paymentTypesCashMap.put(orderList.get(i).get("order_payment_method"), total + "");
                } else {
                    paymentTypesCashMap.put(orderList.get(i).get("order_payment_method"), calculated_total_price + "");
                }
            }
            databaseAccess.open();
            List<HashMap<String, String>> paymentMethods;
            paymentMethods = databaseAccess.getPaymentMethod();
            LinkedList<EndShiftPaymentModels> models = new LinkedList<>();
            for (int i = 0; i < paymentMethods.size(); i++) {
                View root_view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_end_shift_payment_method, null);

                TextView paymentTypeTv = root_view.findViewById(R.id.payment_type_tv);
                EditText paymentTypeAmountEt = root_view.findViewById(R.id.payment_type_amount_et);
                TextView paymentTypeAmountErrorTv = root_view.findViewById(R.id.payment_type_amount_error_tv);

                paymentTypeTv.setText(paymentMethods.get(i).get("payment_method_name"));

                String cash = paymentTypesCashMap.get(paymentMethods.get(i).get("payment_method_name"));
                if (cash == null) {
                    cash = "0";
                }

                models.addLast(new EndShiftPaymentModels(paymentTypeAmountEt, paymentTypeAmountErrorTv, paymentMethods.get(i).get("payment_method_name"), Double.parseDouble(cash)));

                endCashTypesLl.addView(root_view);


                paymentTypeAmountEt.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        errorLl.setVisibility(View.GONE);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

            }



            submitBtn.setOnClickListener(view -> {
                String real_cash="";
                String employee_cash="";
                String differences="";
                double total_transactions = orderList.size();

                for (int i = 0; i < models.size(); i++) {
                    String cash = models.get(i).inputPaymentCashEt.getText().toString();
                    double employeeCash = Double.parseDouble(cash.isEmpty() ? "0" : cash);
                    models.get(i).setError(employeeCash != models.get(i).cash);
                    real_cash+=models.get(i).type + ": +" + models.get(i).cash;
                    employee_cash+=models.get(i).type + ": +" + employeeCash;
                    differences+=models.get(i).type + ": +" + (employeeCash - models.get(i).cash);
                    if (employeeCash != models.get(i).cash) {

                        if(employeeCash - models.get(i).cash > 0){
                            models.get(i).paymentCashErrorTv.setText(models.get(i).type + ": +" + (employeeCash - models.get(i).cash));
                            models.get(i).paymentCashErrorTv.setTextColor(requireContext().getResources().getColor(R.color.successColor));
                        }else{
                            models.get(i).paymentCashErrorTv.setText(models.get(i).type + ": " + (employeeCash - models.get(i).cash));
                            models.get(i).paymentCashErrorTv.setTextColor(requireContext().getResources().getColor(R.color.errorColor));
                        }
                    }
                }
                errorLl.setVisibility(View.GONE);
                boolean hasError=false;
                for (int i = 0; i < models.size(); i++) {
                    if (models.get(i).isError()) {
                        hasError=true;
                        errorLl.setVisibility(View.VISIBLE);
                    }
                }

                endShiftModel=new EndShiftModel(real_cash,employee_cash,differences,total_transactions+"", total_amount+"",total_tax+"");
                if (hasError) {
                    errorLl.setVisibility(View.VISIBLE);
                }else{
                    databaseAccess.open();
                    Log.i("datadata",""+databaseAccess.addShift(endShiftModel));
                    dismissAllowingStateLoss();
                }
            });

            confirmBtn.setOnClickListener(view -> {
                databaseAccess.open();
                Log.i("datadata",""+databaseAccess.addShift(endShiftModel));
                dismissAllowingStateLoss();
            });

            closeBtn.setOnClickListener(view -> dismissAllowingStateLoss());
        }

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = (int)(getContext().getResources().getDisplayMetrics().widthPixels*0.9);
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }

}
