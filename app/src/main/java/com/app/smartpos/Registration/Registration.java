package com.app.smartpos.Registration;

import static com.app.smartpos.Constant.DOWNLOAD_FILE_NAME;
import static com.app.smartpos.Constant.DOWNLOAD_FILE_NAME_GZIP;
import static com.app.smartpos.Constant.REGISTER_DEVICE_URL;
import static com.app.smartpos.Constant.SYNC_URL;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.app.smartpos.R;
import com.app.smartpos.Registration.Model.CompanyModel;
import com.app.smartpos.common.Utils;
import com.app.smartpos.settings.ChangeLanguageDialog;
import com.app.smartpos.settings.Synchronization.workers.DecompressWorker;
import com.app.smartpos.settings.Synchronization.workers.DownloadWorker;
import com.app.smartpos.settings.Synchronization.workers.ReadFileWorker;
import com.app.smartpos.utils.BaseActivity;
import com.app.smartpos.utils.Hasher;
import com.app.smartpos.utils.LocaleManager;
import com.app.smartpos.utils.MultiLanguageApp;
import com.app.smartpos.utils.SharedPrefUtils;

import java.util.ArrayList;
import java.util.LinkedList;

public class Registration extends BaseActivity {
    EditText email;
    Spinner spinner;
    EditText password;
    Button actionBtn;
    ProgressBar loadingPb;
    private String deviceId;
    CheckCompaniesViewModel companiesViewModel;
    LinkedList<CompanyModel> companyList = new LinkedList<>();
    String tenantId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registartion);

        deviceId = Utils.getDeviceId(this);
        ConstraintLayout languageCl = findViewById(R.id.language_cl);
        companiesViewModel = new CheckCompaniesViewModel();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        email = findViewById(R.id.email_et);
        spinner = findViewById(R.id.company_spinner);
        password = findViewById(R.id.password_et);
        actionBtn = findViewById(R.id.action_btn);
        loadingPb = findViewById(R.id.loading_pb);
        String lang = LocaleManager.getLanguage(this);

        email.setGravity((lang.equals("ar") ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        password.setGravity((lang.equals("ar") ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        email.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (email.getText().toString().trim().matches(emailPattern)) {
                    actionBtn.setEnabled(true);
                    actionBtn.setAlpha(1);

                } else {
                    actionBtn.setEnabled(false);
                    actionBtn.setAlpha(0.5f);
                }
                tenantId = "";
                spinner.setVisibility(View.GONE);
                password.setVisibility(View.GONE);
                actionBtn.setText(getResources().getString(R.string.check_email));
            }
        });
        password.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!password.getText().toString().trim().isEmpty()) {
                    actionBtn.setEnabled(true);
                    actionBtn.setAlpha(1);

                } else {
                    actionBtn.setEnabled(false);
                    actionBtn.setAlpha(0.5f);
                }
            }
        });

        actionBtn.setOnClickListener(view -> {
            if (!email.getText().toString().isEmpty() && !tenantId.isEmpty() && !password.getText().toString().isEmpty()) {
                actionBtn.setVisibility(View.GONE);
                loadingPb.setVisibility(View.VISIBLE);
                enqueueDownloadAndReadWorkers();
            } else {
                if (email.getText().toString().isEmpty()) {
                    Toast.makeText(this, getResources().getString(R.string.user_name_empty), Toast.LENGTH_SHORT).show();
                } else {
                    actionBtn.setVisibility(View.GONE);
                    loadingPb.setVisibility(View.VISIBLE);
                    companiesViewModel.start(email.getText().toString().trim());
                }
            }
        });

        languageCl.setOnClickListener(view -> {
            ChangeLanguageDialog dialog = new ChangeLanguageDialog();
            dialog.show(getSupportFragmentManager(), "change language dialog");
        });

        companiesViewModel.getLiveData().observe(this, companyModels -> {
            actionBtn.setVisibility(View.VISIBLE);
            loadingPb.setVisibility(View.GONE);
            companyList = companyModels;
            if (companyModels.size() > 0) {
                tenantId = companyModels.get(0).getCompanyCode();
            }

            if (companyModels.isEmpty()) {
                Toast.makeText(this, getResources().getString(R.string.no_data_found), Toast.LENGTH_SHORT).show();
            } else {
                spinner.setVisibility(View.VISIBLE);
                password.setVisibility(View.VISIBLE);
                password.setText("");
                actionBtn.setText(getString(R.string.register));
                ArrayList<String> arrayList = new ArrayList<>();

                for (int i = 0; i < companyModels.size(); i++) {
                    arrayList.add(companyModels.get(i).getCompanyName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arrayList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tenantId = companyList.get(i).getCompanyCode();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void enqueueDownloadAndReadWorkers() {
        //username Admin
        //password 01111Mm&
        Data register = new Data.Builder().
                putString("url", REGISTER_DEVICE_URL).
                putString("tenantId", tenantId).
                putString("email", email.getText().toString()).
                putString("password", password.getText().toString()).
                putString("deviceId", deviceId).
                build();
        Data downloadInputData = new Data.Builder()
                .putString("url", SYNC_URL)
                .putString("tenantId", tenantId)
                .putString("fileName", DOWNLOAD_FILE_NAME_GZIP)
                .build();

        Data decompressInputData = new Data.Builder()
                .putString("fileName", DOWNLOAD_FILE_NAME_GZIP)
                .build();

        Data readInputData = new Data.Builder()
                .putString("fileName", DOWNLOAD_FILE_NAME)
                .build();

        OneTimeWorkRequest registerRequest = new OneTimeWorkRequest.Builder(RegistrationWorker.class)
                .setInputData(register)
                .build();
        OneTimeWorkRequest downloadRequest = new OneTimeWorkRequest.Builder(DownloadWorker.class)
                .setInputData(downloadInputData)
                .build();

        OneTimeWorkRequest decompressRequest = new OneTimeWorkRequest.Builder(DecompressWorker.class)
                .setInputData(decompressInputData)
                .build();

        OneTimeWorkRequest readRequest = new OneTimeWorkRequest.Builder(ReadFileWorker.class)
                .setInputData(readInputData)
                .build();

        WorkContinuation continuation = WorkManager.getInstance(this)
                .beginWith(registerRequest)
                .then(downloadRequest)
                .then(decompressRequest)
                .then(readRequest);
        continuation.enqueue();
        observeWorker(registerRequest);
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(readRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        // Work is finished, close pending screen or perform any action
                        handleWorkCompletion(workInfo);
                    }
                });
    }

    private void handleWorkCompletion(WorkInfo workInfo) {
        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
            // Work succeeded, handle success
            showMessage(getString(R.string.registration_successful));
            SharedPrefUtils.setIsRegistered(this, true);
            SharedPrefUtils.setStartDateTime(this);
            byte[] bytes = Hasher.encryptMsg(email.getText().toString().trim() + "-" + password.getText().toString().trim());
            SharedPrefUtils.setAuthData(this, bytes);
            finish();
//            closePendingScreen();
        } else if (workInfo.getState() == WorkInfo.State.FAILED) {
            // Work failed, handle failure
            if(workInfo.getOutputData().getKeyValueMap().isEmpty())
                showMessage(MultiLanguageApp.app.getString(R.string.error_in_syncing_data));
            actionBtn.setVisibility(View.VISIBLE);
            loadingPb.setVisibility(View.GONE);
//            demoBtn.setEnabled(true);

        }
    }

    //    private void closePendingScreen() {
//        dismissAllowingStateLoss();
//    }
    private void observeWorker(OneTimeWorkRequest workRequest) {
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        if (workInfo.getState() == WorkInfo.State.FAILED) {
                            String errorMessage = workInfo.getOutputData().getString("errorMessage");
                            showMessage((errorMessage != null ? errorMessage : getString(R.string.unknown_error_occurred)));
                        }
                    }
                });
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
    }


}