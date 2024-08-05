package com.app.smartpos.settings.end_shift;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import com.app.smartpos.auth.LoginUser;
import com.app.smartpos.database.DatabaseAccess;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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

            for(int i=0;i<endShiftModel.getShiftDifferences().size();i++){
                addView(endShiftModel.getShiftDifferences().get(i).getType()+"-"+requireContext().getResources().getString(R.string.real),endShiftModel.getShiftDifferences().get(i).getReal()+"");
                addView(endShiftModel.getShiftDifferences().get(i).getType()+"-"+requireContext().getResources().getString(R.string.input),endShiftModel.getShiftDifferences().get(i).getInput()+"");
                addView(endShiftModel.getShiftDifferences().get(i).getType()+"-"+requireContext().getResources().getString(R.string.diff),endShiftModel.getShiftDifferences().get(i).getDiff()+"");
            }
            addView(requireContext().getResources().getString(R.string.total_transactions_number),endShiftModel.getTotal_transactions());
            addView(requireContext().getResources().getString(R.string.total_amount),endShiftModel.getTotal_amount());
            addView(requireContext().getResources().getString(R.string.total_tax),endShiftModel.getTotal_tax());

            addView(requireContext().getResources().getString(R.string.date_),endShiftModel.getDate());

            LoginUser loginUser=new LoginUser();
            addView(requireContext().getResources().getString(R.string.user_id), loginUser.getId()+"");
            addView(requireContext().getResources().getString(R.string.user_name),loginUser.getName());
            addView(requireContext().getResources().getString(R.string.shift_id),"123455");



        }

        return root;
    }

    private void addView(String text,String value){
        View root_view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_end_shift_report, null);

        TextView textTv = root_view.findViewById(R.id.text_tv);
        TextView valueTv = root_view.findViewById(R.id.value_tv);

        textTv.setText(text);
        valueTv.setText(value);
        ((LinearLayout)root).addView(root_view);
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = (int)(getContext().getResources().getDisplayMetrics().widthPixels*0.9);
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }

    public void setEndShiftModel(EndShiftModel endShiftModel) {
        this.endShiftModel = endShiftModel;
    }
}
