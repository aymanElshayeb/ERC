package com.app.smartpos.Registration;

import static com.app.smartpos.Constant.CHECK_COMPANY_URL;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.app.smartpos.NewHomeActivity;
import com.app.smartpos.R;
import com.app.smartpos.Registration.Model.CompanyModel;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.utils.Hasher;
import com.app.smartpos.utils.LocaleManager;
import com.app.smartpos.utils.SharedPrefUtils;

import java.util.ArrayList;
import java.util.LinkedList;

public class CompanyCheckDialog extends DialogFragment {

    private static final int STORAGE_PERMISSION_CODE = 23;
    View root;
    EditText usernameEt;
    EditText passwordEt;
    EditText merchantEt;
    ProgressBar progressBar;
    Button registerBtn;
    Button demoBtn;
    Spinner company_spinner;
    private String deviceId;
    CheckCompaniesViewModel companiesViewModel;
    String tenantId;
    LinkedList<CompanyModel> companyList = new LinkedList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (root == null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            root = inflater.inflate(R.layout.company_check_registration, container, false);
            setCancelable(false);
            companiesViewModel = new CheckCompaniesViewModel();
            deviceId = Settings.Secure.getString(getContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);

            usernameEt = root.findViewById(R.id.username_et);
            registerBtn = root.findViewById(R.id.register_btn);
            company_spinner = root.findViewById(R.id.company_spinner);
            demoBtn = root.findViewById(R.id.demo_btn);
            progressBar = root.findViewById(R.id.progress);
            String lang = LocaleManager.getLanguage(requireActivity());

            usernameEt.setGravity((lang.equals("ar") ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);

            ConstraintLayout languageCl = root.findViewById(R.id.language_cl);
            registerBtn.setOnClickListener(view -> {
                if (usernameEt.getText().toString().isEmpty()) {
                    Toast.makeText(requireActivity(), requireContext().getResources().getString(R.string.user_name_empty), Toast.LENGTH_SHORT).show();
                } else {
                    registerBtn.setVisibility(View.GONE);
                    demoBtn.setEnabled(false);
                    companiesViewModel.start(usernameEt.getText().toString().trim());

                }

            });

            String language = LocaleManager.getLanguage(root.getContext());

            languageCl.setOnClickListener(view -> {
                LocaleManager.updateLocale(root.getContext(), language.equals("en") ? "ar" : "en");
                LocaleManager.resetApp(getActivity());
            });
            demoBtn.setOnClickListener(view -> {
                DatabaseAccess databaseAccess = DatabaseAccess.getInstance(requireContext());
                databaseAccess.open();
                databaseAccess.addDemoConfiguration();
                SharedPrefUtils.setStartDateTime(requireActivity());
                SharedPrefUtils.setDemoPressed(requireActivity(), true);
                requireActivity().finish();
                requireActivity().startActivity(new Intent(requireActivity(), NewHomeActivity.class));
            });


            companiesViewModel.getLiveData().observe(this, companyModels -> {
                registerBtn.setVisibility(View.VISIBLE);
                companyList = companyModels;
                if (companyModels.size() > 0) {
                    tenantId = companyModels.get(0).getCompanyCode();
                }

                if (companyModels.isEmpty()) {
                    Toast.makeText(requireActivity(), requireContext().getResources().getString(R.string.no_data_found), Toast.LENGTH_SHORT).show();
                } else {
                    company_spinner.setVisibility(View.VISIBLE);

                    ArrayList<String> arrayList = new ArrayList<>();

                    for (int i = 0; i < companyModels.size(); i++) {
                        arrayList.add(companyModels.get(i).getCompanyName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item, arrayList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    company_spinner.setAdapter(adapter);
                }
            });
            company_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    tenantId = companyList.get(i).getCompanyCode();

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
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
