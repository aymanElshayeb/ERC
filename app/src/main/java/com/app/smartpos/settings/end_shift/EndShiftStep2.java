package com.app.smartpos.settings.end_shift;

import static com.app.smartpos.common.Utils.trimLongDouble;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.app.smartpos.R;
import com.app.smartpos.common.DeviceFactory.Device;
import com.app.smartpos.common.DeviceFactory.DeviceFactory;
import com.app.smartpos.common.Utils;
import com.app.smartpos.common.WorkerActivity;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.orders.OrderBitmap;
import com.app.smartpos.utils.SharedPrefUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class EndShiftStep2 extends WorkerActivity {

    DatabaseAccess databaseAccess;
    String currency;
    EndShiftModel endShiftModel;
    LinearLayout viewsLl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_end_shift_step2);

        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        currency=databaseAccess.getCurrency();

        viewsLl=findViewById(R.id.views_ll);
        TextView totalAmountTv=findViewById(R.id.total_amount_tv);
        TextView endMyShiftTv=findViewById(R.id.end_my_shift_tv);
        TextView printZReport=findViewById(R.id.print_z_report);

        endShiftModel=(EndShiftModel)getIntent().getSerializableExtra("model");

        LinkedList<String> keys = new LinkedList<>(endShiftModel.getShiftDifferences().keySet());
        double totalCard=0;
        for (int i = 0; i < keys.size(); i++) {
            ShiftDifferences shiftDifferences=endShiftModel.getShiftDifferences().get(keys.get(i));
            if( keys.get(i).equals("CASH")){
                addView(getString(R.string.total_cash_sales_amount), trimLongDouble(shiftDifferences.getReal()));
                addView(getString(R.string.input_total_cash), trimLongDouble(shiftDifferences.getInput()));
                addView(getString(R.string.cash_amount_discrepancy), trimLongDouble(shiftDifferences.getDiff()));
            }else{
                totalCard+=shiftDifferences.getReal();
            }
        }
        addView(getResources().getString(R.string.total_card_amount), Utils.trimLongDouble(totalCard) + "");
        addView(getResources().getString(R.string.total_refunds), String.valueOf(endShiftModel.getTotalRefunds()));
        addView(getResources().getString(R.string.total_sales_transactions), endShiftModel.getNum_successful_transaction() + "");
        //addView(requireContext().getResources().getString(R.string.total_tax), trimLongDouble(endShiftModel.getTotal_tax()));
        addView(getResources().getString(R.string.total_cash_amount), trimLongDouble(endShiftModel.getTotal_amount() - endShiftModel.getTotalRefundsAmount()));
        addView(getResources().getString(R.string.total_refunds_amount), trimLongDouble(endShiftModel.getTotalRefundsAmount() * -1));
        addView(getResources().getString(R.string.start_cash), trimLongDouble(endShiftModel.getStartCash()));
        addView(getResources().getString(R.string.leave_cash), trimLongDouble(endShiftModel.getLeaveCash()));

        totalAmountTv.setText(Utils.trimLongDouble(endShiftModel.total_amount)+" "+currency);
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy   hh:mm aa");
        String start_date = formatter.format(new Date(endShiftModel.getStartDateTime()));
        addView(getResources().getString(R.string.start_shift_date), start_date);
        String end_date = formatter.format(new Date(endShiftModel.getEndDateTime()));
        addView(getResources().getString(R.string.end_shift_date), end_date);


//        addView(getResources().getString(R.string.user_id), SharedPrefUtils.getUserId(this));
        addView(getResources().getString(R.string.user_mail), SharedPrefUtils.getUserName(this));
        addView(getResources().getString(R.string.shift_sequence), endShiftModel.getSequence());
        endMyShiftTv.setOnClickListener(view -> {
            startActivity(new Intent(this, ShiftEndedSuccessfully.class));
            enqueueUploadWorkers();
        });
        printZReport.setOnClickListener(view -> {
            onPrintZReport();
        });

    }

    private void addView(String text, String value) {
        View root_view = LayoutInflater.from(this).inflate(R.layout.layout_end_shift_report, null);

        TextView textTv = root_view.findViewById(R.id.text_tv);
        TextView valueTv = root_view.findViewById(R.id.value_tv);

        textTv.setText(text);
        valueTv.setText(value);

        // Find the root ConstraintLayout
        ConstraintLayout rootLayout = root_view.findViewById(R.id.rootLayout);

        // Adjust layout dynamically based on the length of the value
        if (value.length() > 15) { // Adjust the condition as needed
            // If the value is too long, stack text and value vertically
            valueTv.setMaxLines(1);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(rootLayout);
            constraintSet.clear(R.id.text_tv, ConstraintSet.END); // Remove horizontal constraint
            constraintSet.connect(R.id.value_tv, ConstraintSet.TOP, R.id.text_tv, ConstraintSet.BOTTOM); // Stack value below text
            constraintSet.connect(R.id.value_tv, ConstraintSet.START, R.id.text_tv, ConstraintSet.START); // Align start of value with text
            constraintSet.applyTo(rootLayout);
        } else {
            // For shorter values, keep them in the same row
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(rootLayout);
            constraintSet.connect(R.id.text_tv, ConstraintSet.END, R.id.value_tv, ConstraintSet.START); // Keep text and value on the same line
            constraintSet.connect(R.id.value_tv, ConstraintSet.START, R.id.text_tv, ConstraintSet.END);
            constraintSet.applyTo(rootLayout);
        }

        viewsLl.addView(root_view);
    }

    private void onPrintZReport () {
        Device device = DeviceFactory.getDevice();
        try {
            Bitmap bitmap=new OrderBitmap(this).shiftZReport(endShiftModel);
            device.printZReport(bitmap);
        }catch (Exception e){
            Toast.makeText(this, R.string.no_printer_found, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {

    }
}