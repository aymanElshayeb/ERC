package com.app.smartpos.settings.end_shift;

import static com.app.smartpos.common.Utils.trimLongDouble;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.app.smartpos.R;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.settings.SettingsActivity;
import com.app.smartpos.utils.SharedPrefUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class EndShiftDialog extends DialogFragment {

    View root;

    double total_amount = 0;
    double total_tax = 0;
    EndShiftModel endShiftModel;
    double totalRefundsAmount;
    double totalCardsAmount;
    DatabaseAccess databaseAccess;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (root == null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            root = inflater.inflate(R.layout.dialog_end_shift, container, false);
            setCancelable(false);

            EditText leaveCashEt = root.findViewById(R.id.leave_cash_et);
            Button submitBtn = root.findViewById(R.id.btn_submit);
            Button confirmBtn = root.findViewById(R.id.confirm_btn);
            ImageButton closeBtn = root.findViewById(R.id.btn_close);
            LinearLayout endCashTypesLl = root.findViewById(R.id.end_cash_types_ll);
            LinearLayout errorLl = root.findViewById(R.id.error_ll);

            databaseAccess = DatabaseAccess.getInstance(requireActivity());
            databaseAccess.open();
            String endDateString = databaseAccess.getLastShift("end_date_time");
            long lastShiftDate = endDateString.equals("") ? SharedPrefUtils.getStartDateTime(requireContext()) : Long.parseLong(endDateString);
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
                databaseAccess.open();
                double total_price = databaseAccess.totalOrderPrice(orderList.get(i).get("invoice_id"));
                double tax = Double.parseDouble(orderList.get(i).get("tax"));
                double discount = Double.parseDouble(orderList.get(i).get("discount"));

                total_amount += total_price;
                total_tax += tax;
                Log.i("datadata_card",orderList.get(i).get("order_payment_method")+" "+orderList.get(i).get("card_type_code"));
                double calculated_total_price = total_price - discount;
                if (orderList.get(i).get("order_payment_method").equals("CASH")) {
                    double cash = Double.parseDouble(paymentTypesCashMap.get("CASH"));
                    double total = calculated_total_price + cash;
                    paymentTypesCashMap.put("CASH", total + "");
                }else if (orderList.get(i).get("order_payment_method").equals("CARD") && paymentTypesCashMap.containsKey(orderList.get(i).get("card_type_code").toString())) {
                    double cash = Double.parseDouble(paymentTypesCashMap.get(orderList.get(i).get("card_type_code")));
                    double total = calculated_total_price + cash;
                    paymentTypesCashMap.put(orderList.get(i).get("card_type_code"), total + "");
                    Log.i("datadata_card",cash+" "+total+" "+paymentTypesCashMap.get(orderList.get(i).get("card_type_code")));
                } else {
                    paymentTypesCashMap.put(orderList.get(i).get("card_type_code"), calculated_total_price + "");
                }
            }
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
                View root_view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_end_shift_payment_method, null);

                TextView paymentTypeTv = root_view.findViewById(R.id.payment_type_tv);
                EditText paymentTypeAmountEt = root_view.findViewById(R.id.payment_type_amount_et);
                TextView paymentTypeAmountErrorTv = root_view.findViewById(R.id.payment_type_amount_error_tv);
                paymentTypeTv.setText(cardTypes.get(i).get("name"));

                String cash = cardTypes.get(i).get("CASH");
                if (cash == null) {
                    cash = "0";
                }
                Log.i("datadata",cardTypes.get(i).get("name")+" "+cash);
                models.addLast(new EndShiftPaymentModels(paymentTypeAmountEt, paymentTypeAmountErrorTv, cardTypes.get(i).get("name"),cardTypes.get(i).get("code"), Double.parseDouble(cash)));

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

            HashMap<String, ShiftDifferences> map = new HashMap<>();

            submitBtn.setOnClickListener(view -> {
                if (leaveCashEt.getText().toString().isEmpty()) {
                    Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.leave_cash_empty), Toast.LENGTH_SHORT).show();
                    return;
                }

                int total_transactions = orderList.size();

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
                            models.get(i).paymentCashErrorTv.setText(models.get(i).type + ": +" + value);
                            models.get(i).paymentCashErrorTv.setTextColor(requireContext().getResources().getColor(R.color.successColor));
                        } else {
                            models.get(i).paymentCashErrorTv.setText(models.get(i).type + ": " + value);
                            models.get(i).paymentCashErrorTv.setTextColor(requireContext().getResources().getColor(R.color.errorColor));
                        }
                    }
                }


                errorLl.setVisibility(View.GONE);
                boolean hasError = false;
                for (int i = 0; i < models.size(); i++) {
                    if (models.get(i).isError()) {
                        hasError = true;
                        errorLl.setVisibility(View.VISIBLE);
                    }
                }
                String android_id = Settings.Secure.getString(getContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                databaseAccess.open();
                HashMap<String, String> configuration = databaseAccess.getConfiguration();
                String ecr_code = configuration.isEmpty() ? "" : configuration.get("ecr_code").toString();
                databaseAccess.open();
                HashMap<String, String> sequenceMap = databaseAccess.getSequence(2, ecr_code);
                databaseAccess.open();
                databaseAccess.updateSequence(Integer.parseInt(sequenceMap.get("next_value")),Integer.parseInt(sequenceMap.get("sequence_id")));
                databaseAccess.open();
                String startDateString = databaseAccess.getLastShift("end_date_time");
                long startDate = startDateString.equals("") ? SharedPrefUtils.getStartDateTime(requireContext()) : Long.parseLong(startDateString);

//                ShiftDifferences shiftDifferencesForLeaveCash = map.get("CASH");
//                double realCash=0;
//                if(shiftDifferencesForLeaveCash!=null){
//                    realCash=shiftDifferencesForLeaveCash.real;
//                }

                endShiftModel = new EndShiftModel(map, sequenceMap.get("sequence"), SharedPrefUtils.getUsername(requireContext()), total_transactions, 0, 0, total_amount, total_tax, configuration.get("ecr_code"), startDate, new Date().getTime(), startCash, Double.parseDouble(leaveCashEt.getText().toString()),"" , totalRefundsAmount, totalCardsAmount);
                if (hasError) {
                    errorLl.setVisibility(View.VISIBLE);
                } else {
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

    private void addToShift() {
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
        ((SettingsActivity) requireActivity()).openReport(endShiftModel);
        dismissAllowingStateLoss();
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.9);
        params.height = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.8);
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }
}
