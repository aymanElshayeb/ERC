package com.app.smartpos.settings;


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


public class ChangeLanguageDialog extends DialogFragment {

    View root;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (root == null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            root = inflater.inflate(R.layout.change_language_dialog, container, false);
            setCancelable(false);
            TextView confirmTv = root.findViewById(R.id.confirm_change_language_tv);
            TextView cancelTv = root.findViewById(R.id.cancel_change_language_tv);
            String language = LocaleManager.getLanguage(root.getContext());

            confirmTv.setOnClickListener(view -> {
                LocaleManager.updateLocale(root.getContext(), language.equals("en") ? "ar" : "en");
                LocaleManager.resetApp(getActivity());
                dismiss();
            });
            cancelTv.setOnClickListener(view -> {
                dismiss();
            });

            return root;
        }


        return null;
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