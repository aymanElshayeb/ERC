package com.app.smartpos.refund;

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
import com.app.smartpos.utils.LocaleManager;

public class RefundConfirmationDialog extends DialogFragment {


    View root;
    RefundOrOrderDetails refundDetails;
    String amount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (root == null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            root = inflater.inflate(R.layout.refund_confirmation_dialog, container, false);
            setCancelable(false);

            TextView title_tv=root.findViewById(R.id.title_tv);
            TextView confirmTv = root.findViewById(R.id.confirm_tv);
            TextView cancelTv = root.findViewById(R.id.cancel_tv);

            title_tv.setText(requireContext().getString(R.string.confirmation_refund)+" "+amount+(LocaleManager.getLanguage(requireActivity()).equals("ar")?" ؟":" ?"));
            confirmTv.setOnClickListener(view -> {
                refundDetails.refundConfirmation();
                dismiss();
            });
            cancelTv.setOnClickListener(view -> {
                dismiss();
            });
        }

        return root;
    }

    public void setData(RefundOrOrderDetails refundDetails, String amount) {
        this.refundDetails = refundDetails;
        this.amount=amount;
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
