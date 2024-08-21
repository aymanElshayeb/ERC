package com.app.smartpos.Items;

import static com.app.smartpos.common.Utils.trimLongDouble;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
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
import com.app.smartpos.settings.end_shift.EndShiftModel;
import com.app.smartpos.settings.end_shift.ShiftDifferences;
import com.app.smartpos.utils.SharedPrefUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class ItemsOptionsDialog extends DialogFragment {

    View root;
    Items items;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (root == null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            root = inflater.inflate(R.layout.dialog_items_options, container, false);

            LinearLayout customItemLl = root.findViewById(R.id.custom_item_ll);
            LinearLayout clearAllItemsLl = root.findViewById(R.id.clear_items_ll);
            //ImageButton closeBtn = root.findViewById(R.id.btn_close);

            customItemLl.setOnClickListener(view -> {
               items.addCustomItem();
               dismiss();
            });

            clearAllItemsLl.setOnClickListener(view -> {
                items.clearAllItems();
                dismiss();
            });



        }

        return root;
    }

    public void setItems(Items items) {
        this.items = items;
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.9);
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        getDialog().getWindow().setGravity(Gravity.BOTTOM);
    }

}
