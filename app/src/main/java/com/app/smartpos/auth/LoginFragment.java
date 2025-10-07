package com.app.smartpos.auth;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.app.smartpos.NewHomeActivity;
import com.app.smartpos.R;
import com.app.smartpos.common.Utils;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.settings.ChangeLanguageDialog;
import com.app.smartpos.utils.Hasher;
import com.app.smartpos.utils.LocaleManager;
import com.app.smartpos.utils.SharedPrefUtils;

import java.util.HashMap;
import java.util.List;

public class LoginFragment extends Fragment {

    private View root;
    private Context context;
    private boolean isPasswordShown = false;
    private Button loginBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_login, container, false);
            context = requireContext();
            EditText emailEt = root.findViewById(R.id.email_et);
            EditText passwordEt = root.findViewById(R.id.password_et);
            ImageView eyeIm = root.findViewById(R.id.eye_im);
            loginBtn = root.findViewById(R.id.login_btn);
            ImageView languageIm = root.findViewById(R.id.language_im);
            ImageView closeIm = root.findViewById(R.id.close_im);

            languageIm.setOnClickListener(view -> {
                ChangeLanguageDialog dialog = new ChangeLanguageDialog();
                dialog.show(getParentFragmentManager(), "change language dialog");
            });

            closeIm.setOnClickListener(view -> {
                requireActivity().finish();
            });
            eyeIm.setOnClickListener(view -> {
                isPasswordShown = !isPasswordShown;
                if (isPasswordShown) {
                    eyeIm.setAlpha(1.0f);
                    passwordEt.setTransformationMethod(null);
                } else {
                    eyeIm.setAlpha(0.5f);
                    passwordEt.setTransformationMethod(new PasswordTransformationMethod());
                }
                passwordEt.setSelection(passwordEt.getText().length());
            });


            String lang = LocaleManager.getLanguage(requireActivity());
            emailEt.setGravity((lang.equals("ar") ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
            passwordEt.setGravity((lang.equals("ar") ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);

            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(requireActivity());
            databaseAccess.open();
            List<HashMap<String, String>> list = databaseAccess.getAllUsers();
            for (int i = 0; i < list.size(); i++) {
                Utils.addLog("datadata", list.get(i).toString());
            }

            loginBtn.setOnClickListener(view -> {
                ((AuthActivity)getActivity()).login(emailEt.getText().toString().trim(),passwordEt.getText().toString(),loginBtn);
            });
        }

        return root;
    }


}
