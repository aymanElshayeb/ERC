package com.app.smartpos.settings.end_shift;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.app.smartpos.settings.SettingsActivity;
import com.app.smartpos.utils.SharedPrefUtils;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class EndShiftDialog extends DialogFragment {

    View root;

    double total_amount=0;
    double total_tax=0;
    EndShiftModel endShiftModel;

    DatabaseAccess databaseAccess;

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

            databaseAccess = DatabaseAccess.getInstance(requireActivity());
            databaseAccess.open();
            String endDateString=databaseAccess.getLastShift("end_date_time");
            long lastShiftDate=endDateString.equals("") ? SharedPrefUtils.getStartDateTime(requireContext()):Long.parseLong(endDateString);
            databaseAccess.open();
            //get data from local database
            List<HashMap<String, String>> orderList;
            orderList = databaseAccess.getOrderListWithTime(lastShiftDate);
            HashMap<String, String> paymentTypesCashMap = new HashMap<>();
            databaseAccess.open();
            for (int i = 0; i < orderList.size(); i++) {
                databaseAccess.open();
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

            HashMap<String,ShiftDifferences>map=new HashMap<>();

            submitBtn.setOnClickListener(view -> {

                int total_transactions = orderList.size();

                for (int i = 0; i < models.size(); i++) {
                    String cash = models.get(i).inputPaymentCashEt.getText().toString();
                    double employeeCash = Double.parseDouble(cash.isEmpty() ? "0" : cash);
                    models.get(i).setError(employeeCash != models.get(i).cash);
                    map.put(models.get(i).type,new ShiftDifferences(models.get(i).cash,employeeCash,(employeeCash - models.get(i).cash)));

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
                String android_id = Settings.Secure.getString(getContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                databaseAccess.open();
                String ecr_code=databaseAccess.getConfiguration().get("ecr_code").toString();
                databaseAccess.open();
                HashMap<String,String> sequenceMap = databaseAccess.getSequence(2,ecr_code);
                databaseAccess.open();
                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.app_name),MODE_PRIVATE);
                String startDateString=databaseAccess.getLastShift("start_date_time");
                long startDate=startDateString.equals("") ? SharedPrefUtils.getStartDateTime(requireContext()):Long.parseLong(startDateString);
                databaseAccess.open();
                String startCashString=databaseAccess.getLastShift("start_cash");
                double startCash=startCashString.equals("") ? 0 : Double.parseDouble(startCashString);
                endShiftModel=new EndShiftModel(map,sequenceMap.get("next_value"), SharedPrefUtils.getUsername(requireContext()),total_transactions,0,0,total_amount,total_tax,android_id,startDate,new Date().getTime(),startCash,total_amount+startCash);
                if (hasError) {
                    errorLl.setVisibility(View.VISIBLE);
                }else{
                    addToShift();
                }

            });

            confirmBtn.setOnClickListener(view -> {
                addToShift();
            });

            closeBtn.setOnClickListener(view -> dismissAllowingStateLoss());
        }

        return root;
    }

    private void addToShift(){
        databaseAccess.open();
        int id=databaseAccess.addShift(endShiftModel);
        if(id>-1){
            databaseAccess.open();
            databaseAccess.addShiftCreditCalculations(id,endShiftModel.getShiftDifferences().get("CARD"));
        }
        ((SettingsActivity)requireActivity()).openReport(endShiftModel);
        dismissAllowingStateLoss();
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
