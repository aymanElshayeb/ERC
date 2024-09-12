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

import com.app.smartpos.NewHomeActivity;
import com.app.smartpos.R;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.utils.Hasher;
import com.app.smartpos.utils.SharedPrefUtils;

import java.util.HashMap;
import java.util.List;

public class LoginFragment extends Fragment {

    private View root;
    private Context context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_login, container, false);
            context = requireContext();
            EditText EmailEt = root.findViewById(R.id.email_et);
            EditText passwordEt = root.findViewById(R.id.password_et);
            Button loginBtn = root.findViewById(R.id.login_btn);
//            EmailEt.setText("kohoru@polkaroad.net");
//            passwordEt.setText("01111Mm&");
            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(requireActivity());
            databaseAccess.open();
            List<HashMap<String,String>>list=databaseAccess.getAllUsers();
            for(int i=0;i<list.size();i++) {
                Log.i("datadata",list.get(i).toString());
            }

            loginBtn.setOnClickListener(view -> {

                databaseAccess.open();
                HashMap<String, String> map = databaseAccess.getUserWithEmail(EmailEt.getText().toString());
                Hasher hasher = new Hasher();
                boolean isMatch = hasher.hashPassword(passwordEt.getText().toString(), map.get("password"));
                //Log.i("datadata",map.toString());
                if (isMatch) {
                    SharedPrefUtils.setName(requireActivity(), map.get("name_ar"));
                    SharedPrefUtils.setUserEmail(requireActivity(), map.get("email"));
                    SharedPrefUtils.setMobileNumber(requireActivity(), map.get("mobile"));
                    SharedPrefUtils.setUserId(requireActivity(), map.get("id"));
                    Intent intent = new Intent(context, NewHomeActivity.class);
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
