package com.app.smartpos.settings.end_shift;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.app.smartpos.R;

public class EndShiftConfirmationDialog extends DialogFragment {


    View root;
    EndShiftStep2 endShiftStep2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (root == null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            root = inflater.inflate(R.layout.end_shift_confirmation_dialog, container, false);
            setCancelable(false);


            TextView confirmTv = root.findViewById(R.id.confirm_tv);
            TextView cancelTv = root.findViewById(R.id.cancel_tv);

            confirmTv.setOnClickListener(view -> {
                endShiftStep2.addToShift();
                dismiss();
            });
            cancelTv.setOnClickListener(view -> {
                dismiss();
            });
        }

        return root;
    }

    public void setEndShiftStep2(EndShiftStep2 endShiftStep2) {
        this.endShiftStep2 = endShiftStep2;
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.9);
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

    }
}
