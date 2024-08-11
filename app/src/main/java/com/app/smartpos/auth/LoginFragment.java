package com.app.smartpos.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.smartpos.HomeActivity;
import com.app.smartpos.R;
import com.app.smartpos.common.Consts;
import com.app.smartpos.common.ThirdTag;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.utils.SharedPrefUtils;

import java.util.HashMap;

public class LoginFragment extends Fragment {

    private View root;
    private Context context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_login, container, false);
            context = requireContext();
            EditText emailEt = root.findViewById(R.id.email_et);
            EditText passwordEt = root.findViewById(R.id.password_et);
            Button loginBtn = root.findViewById(R.id.login_btn);
            emailEt.setText("karimsaad687@gmail.com");
            passwordEt.setText("123456789");
            final DatabaseAccess databaseAccess = DatabaseAccess.getInstance(requireActivity());

            loginBtn.setOnClickListener(view -> {
                databaseAccess.open();
                HashMap<String,String> map = databaseAccess.getUserWithEmailPassword(emailEt.getText().toString(), passwordEt.getText().toString());
                if (map!=null) {
                    SharedPrefUtils.setUsername(requireActivity(),map.get("username"));
                    SharedPrefUtils.setUserId(requireActivity(),map.get("id"));
                    Intent intent = new Intent(context, HomeActivity.class);
                    startActivity(intent);
                    requireActivity().finish();
                } else {
                    Toast.makeText(context, getString(R.string.wrong_email_password), Toast.LENGTH_SHORT).show();
                }
            });
        }

        return root;
    }
}
