package com.app.smartpos.settings.end_shift;

import static com.app.smartpos.common.Utils.trimLongDouble;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.app.smartpos.R;
import com.app.smartpos.auth.AuthActivity;
import com.app.smartpos.utils.SharedPrefUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class EndShiftReportDialog extends DialogFragment {

    View root;

    EndShiftModel endShiftModel;
    LinearLayout viewLl;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (root == null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            root = inflater.inflate(R.layout.dialog_end_shift_report, container, false);
            setCancelable(false);

            Button logoutBtn = root.findViewById(R.id.btn_logout);
            //ImageButton closeBtn = root.findViewById(R.id.btn_close);
            viewLl=root.findViewById(R.id.views_ll);

            logoutBtn.setOnClickListener(view -> {
                requireActivity().finish();
                requireActivity().startActivity(new Intent(requireActivity(), AuthActivity.class));
            });

            LinkedList<String> keys = new LinkedList<>(endShiftModel.getShiftDifferences().keySet());
            for (int i = 0; i < keys.size(); i++) {
                ShiftDifferences shiftDifferences=endShiftModel.getShiftDifferences().get(keys.get(i));
                addView(keys.get(i) + "-" + requireContext().getResources().getString(R.string.real), trimLongDouble(shiftDifferences.getReal()));
                addView(keys.get(i) + "-" + requireContext().getResources().getString(R.string.input), trimLongDouble(shiftDifferences.getInput()));
                addView(keys.get(i) + "-" + requireContext().getResources().getString(R.string.diff), trimLongDouble(shiftDifferences.getDiff()));
            }
            addView(requireContext().getResources().getString(R.string.total_sales_transactions), endShiftModel.getNum_successful_transaction() + "");
            //addView(requireContext().getResources().getString(R.string.total_amount), trimLongDouble(endShiftModel.getTotal_amount()));
            //addView(requireContext().getResources().getString(R.string.total_tax), trimLongDouble(endShiftModel.getTotal_tax()));


            SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy\nhh:mm aa");
            String start_date = formatter.format(new Date(endShiftModel.getStartDateTime()));
            addView(requireContext().getResources().getString(R.string.start_shift_date), start_date);
            String end_date = formatter.format(new Date(endShiftModel.getEndDateTime()));
            addView(requireContext().getResources().getString(R.string.end_shift_date), end_date);


//            addView(requireContext().getResources().getString(R.string.user_id), SharedPrefUtils.getUserId(requireContext()));
//            addView(requireContext().getResources().getString(R.string.user_mail), SharedPrefUtils.getUserName(requireContext()));
            addView(requireContext().getResources().getString(R.string.username), SharedPrefUtils.getUserName(requireContext()));
            addView(requireContext().getResources().getString(R.string.shift_sequence), endShiftModel.getSequence());


        }

        return root;
    }

    private void addView(String text, String value) {
        View root_view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_end_shift_report, null);

        TextView textTv = root_view.findViewById(R.id.text_tv);
        TextView valueTv = root_view.findViewById(R.id.value_tv);

        textTv.setText(text);
        valueTv.setText(value);
        viewLl.addView(root_view);
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.9);
        params.height = (int) (getContext().getResources().getDisplayMetrics().heightPixels * 0.8);
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }

    public void setEndShiftModel(EndShiftModel endShiftModel) {
        this.endShiftModel = endShiftModel;
    }
}
