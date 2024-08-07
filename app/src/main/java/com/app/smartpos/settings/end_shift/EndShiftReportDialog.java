package com.app.smartpos.settings.end_shift;

import static com.app.smartpos.common.Utils.trimLongDouble;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.app.smartpos.R;
import com.app.smartpos.auth.LoginUser;
import com.app.smartpos.utils.SharedPrefUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class EndShiftReportDialog extends DialogFragment {

    View root;

    EndShiftModel endShiftModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (root == null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            root = inflater.inflate(R.layout.dialog_end_shift_report, container, false);
            setCancelable(false);

            Button submitBtn = root.findViewById(R.id.btn_submit);
            ImageButton closeBtn = root.findViewById(R.id.btn_close);

            closeBtn.setOnClickListener(view -> dismissAllowingStateLoss());

            LinkedList<String> keys = new LinkedList<>(endShiftModel.getShiftDifferences().keySet());
            for (int i = 0; i < keys.size(); i++) {
                ShiftDifferences shiftDifferences=endShiftModel.getShiftDifferences().get(keys.get(i));
                addView(keys.get(i) + "-" + requireContext().getResources().getString(R.string.real), trimLongDouble(shiftDifferences.getReal()));
                addView(keys.get(i) + "-" + requireContext().getResources().getString(R.string.input), trimLongDouble(shiftDifferences.getInput()));
                addView(keys.get(i) + "-" + requireContext().getResources().getString(R.string.diff), trimLongDouble(shiftDifferences.getDiff()));
            }
            addView(requireContext().getResources().getString(R.string.total_transactions_number), endShiftModel.getNum_successful_transaction() + "");
            addView(requireContext().getResources().getString(R.string.total_amount), trimLongDouble(endShiftModel.getTotal_amount()));
            addView(requireContext().getResources().getString(R.string.total_tax), trimLongDouble(endShiftModel.getTotal_tax()));


            SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy'T'hh:mm a");
            String date = formatter.format(new Date(endShiftModel.getEndDateTime()));
            addView(requireContext().getResources().getString(R.string.date_), date);


            addView(requireContext().getResources().getString(R.string.user_id), SharedPrefUtils.getUserId(requireContext()));
            addView(requireContext().getResources().getString(R.string.user_name), SharedPrefUtils.getUsername(requireContext()));
            addView(requireContext().getResources().getString(R.string.shift_id), "123455");


        }

        return root;
    }

    private void addView(String text, String value) {
        View root_view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_end_shift_report, null);

        TextView textTv = root_view.findViewById(R.id.text_tv);
        TextView valueTv = root_view.findViewById(R.id.value_tv);

        textTv.setText(text);
        valueTv.setText(value);
        ((LinearLayout) root).addView(root_view);
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.9);
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }

    public void setEndShiftModel(EndShiftModel endShiftModel) {
        this.endShiftModel = endShiftModel;
    }
}
