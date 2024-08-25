package com.app.smartpos.settings.end_shift;

import static com.app.smartpos.common.Utils.trimLongDouble;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.app.smartpos.R;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.utils.SharedPrefUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class EndShiftStep2 extends AppCompatActivity {

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

        viewsLl=findViewById(R.id.views_ll);
        TextView endMyShiftTv=findViewById(R.id.end_my_shift_tv);

        endShiftModel=(EndShiftModel)getIntent().getSerializableExtra("model");

        LinkedList<String> keys = new LinkedList<>(endShiftModel.getShiftDifferences().keySet());
        for (int i = 0; i < keys.size(); i++) {
            ShiftDifferences shiftDifferences=endShiftModel.getShiftDifferences().get(keys.get(i));
            addView(keys.get(i) + "-" + getResources().getString(R.string.real), trimLongDouble(shiftDifferences.getReal()));
            addView(keys.get(i) + "-" + getResources().getString(R.string.input), trimLongDouble(shiftDifferences.getInput()));
            addView(keys.get(i) + "-" + getResources().getString(R.string.diff), trimLongDouble(shiftDifferences.getDiff()));
        }
        addView(getResources().getString(R.string.total_refunds), endShiftModel.getTotalRefunds() + "");
        addView(getResources().getString(R.string.total_successful_transactions), endShiftModel.getNum_successful_transaction() + "");
        //addView(requireContext().getResources().getString(R.string.total_amount), trimLongDouble(endShiftModel.getTotal_amount()));
        //addView(requireContext().getResources().getString(R.string.total_tax), trimLongDouble(endShiftModel.getTotal_tax()));


        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy\nhh:mm aa");
        String start_date = formatter.format(new Date(endShiftModel.getStartDateTime()));
        addView(getResources().getString(R.string.start_shift_date), start_date);
        String end_date = formatter.format(new Date(endShiftModel.getEndDateTime()));
        addView(getResources().getString(R.string.end_shift_date), end_date);


        addView(getResources().getString(R.string.user_id), SharedPrefUtils.getUserId(this));
        addView(getResources().getString(R.string.user_name), SharedPrefUtils.getUsername(this));
        addView(getResources().getString(R.string.shift_sequence), endShiftModel.getSequence());

        endMyShiftTv.setOnClickListener(view -> {
            startActivity(new Intent(this, ShiftEndedSuccessfully.class));
        });

    }

    private void addView(String text, String value) {
        View root_view = LayoutInflater.from(this).inflate(R.layout.layout_end_shift_report, null);

        TextView textTv = root_view.findViewById(R.id.text_tv);
        TextView valueTv = root_view.findViewById(R.id.value_tv);

        textTv.setText(text);
        valueTv.setText(value);
        viewsLl.addView(root_view);
    }
}