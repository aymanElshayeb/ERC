package com.app.smartpos.refund;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.app.smartpos.R;
import com.app.smartpos.downloaddatadialog.DownloadDataDialog;


public class ConfirmSyncDialog extends DialogFragment {

    View root;

    Button registerBtn;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (root == null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            root = inflater.inflate(R.layout.dialog_confirm_sync, container, false);
            setCancelable(false);
            registerBtn = root.findViewById(R.id.confirm_btn);
            registerBtn.setOnClickListener(view -> {

                ConfirmSyncDialog.this.dismiss();
                DownloadDataDialog dialog = DownloadDataDialog.newInstance(DownloadDataDialog.OPERATION_REFUND);
                dialog.show(getParentFragmentManager(), "dialog");
            });

        }

        return root;
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
