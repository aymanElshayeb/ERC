package com.app.smartpos.Registration;

import static com.app.smartpos.Constant.DOWNLOAD_FILE_NAME;
import static com.app.smartpos.Constant.DOWNLOAD_FILE_NAME_GZIP;
import static com.app.smartpos.Constant.KEY_URL;
import static com.app.smartpos.Constant.REGISTER_DEVICE_URL;
import static com.app.smartpos.Constant.SYNC_URL;

import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
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
    EditText tenantIdEt;
    ImageView eyeIm;
    Spinner spinner;
    EditText password;
    Button actionBtn;
    TextView changeEmailTv;
    ProgressBar loadingPb;
    CheckCompaniesViewModel companiesViewModel;
    LinkedList<CompanyModel> companyList = new LinkedList<>();
    String tenantId = "";
    private String deviceId;
    private boolean isPasswordShown = false;
    private OneTimeWorkRequest readRequest;

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
        tenantIdEt = findViewById(R.id.tenant_et);
        spinner = findViewById(R.id.company_spinner);
        password = findViewById(R.id.password_et);
        changeEmailTv = findViewById(R.id.change_email_tv);
        eyeIm = findViewById(R.id.eye_im);
        actionBtn = findViewById(R.id.action_btn);
        loadingPb = findViewById(R.id.loading_pb);
        String lang = LocaleManager.getLanguage(this);

        email.setGravity((lang.equals("ar") ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        password.setGravity((lang.equals("ar") ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);

        changeEmailTv.setOnClickListener(view -> {
            tenantId = "";
            spinner.setVisibility(View.GONE);
            password.setVisibility(View.GONE);
            eyeIm.setVisibility(View.GONE);
            changeEmailTv.setVisibility(View.GONE);
            email.setEnabled(true);
            email.setAlpha(1.0f);
            actionBtn.setText(getResources().getString(R.string.check_email));
        });

        actionBtn.setOnClickListener(view -> {
            if (email.getText().toString().isEmpty()) {
                Toast.makeText(this, getResources().getString(R.string.user_name_empty), Toast.LENGTH_SHORT).show();
            }else if (tenantIdEt.getText().toString().isEmpty()) {
                Toast.makeText(this, getResources().getString(R.string.company_empty), Toast.LENGTH_SHORT).show();
            }else if (password.getText().toString().isEmpty()) {
                Toast.makeText(this, getResources().getString(R.string.password_empty), Toast.LENGTH_SHORT).show();
            } else {
                actionBtn.setVisibility(View.GONE);
                loadingPb.setVisibility(View.VISIBLE);
                enqueueDownloadAndReadWorkers();
            }
        });

        languageCl.setOnClickListener(view -> {
            ChangeLanguageDialog dialog = new ChangeLanguageDialog();
            dialog.show(getSupportFragmentManager(), "change language dialog");
        });

        eyeIm.setOnClickListener(view -> {
            isPasswordShown = !isPasswordShown;
            if (isPasswordShown) {
                eyeIm.setAlpha(1.0f);
                password.setTransformationMethod(null);
            } else {
                eyeIm.setAlpha(0.5f);
                password.setTransformationMethod(new PasswordTransformationMethod());
            }
            password.setSelection(password.getText().length());
        });

        companiesViewModel.getLiveData().observe(this, companyModels -> {
            if (companyModels == null) {
                actionBtn.setVisibility(View.VISIBLE);
                loadingPb.setVisibility(View.GONE);
            } else {
                actionBtn.setVisibility(View.VISIBLE);
                loadingPb.setVisibility(View.GONE);
                companyList = companyModels;
                if (companyModels.size() > 0) {
                    tenantId = companyModels.get(0).getCompanyCode();
                }

                if (companyModels.isEmpty()) {
                    Toast.makeText(this, getResources().getString(R.string.please_enter_a_valid_email), Toast.LENGTH_SHORT).show();
                } else {
                    spinner.setVisibility(View.VISIBLE);
                    password.setVisibility(View.VISIBLE);
                    eyeIm.setVisibility(View.VISIBLE);
                    changeEmailTv.setVisibility(View.VISIBLE);
                    email.setEnabled(false);
                    email.setAlpha(0.5f);

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
        Utils.addLog("datadata",KEY_URL);
        Utils.addLog("datadata",REGISTER_DEVICE_URL);
        Data apiKey = new Data.Builder().
                putString("url", KEY_URL).
                putString("tenantId", tenantIdEt.getText().toString()).
                putString("email", email.getText().toString().trim()).
                putString("password", password.getText().toString()).
                build();
        Data register = new Data.Builder().
                putString("url", REGISTER_DEVICE_URL).
                putString("tenantId", tenantIdEt.getText().toString()).
                putString("email", email.getText().toString().trim()).
                putString("password", password.getText().toString()).
                putString("deviceId", deviceId).
                build();

        Data downloadInputData = new Data.Builder()
                .putString("url", SYNC_URL)
                .putString("tenantId", tenantIdEt.getText().toString())
                .putString("fileName", DOWNLOAD_FILE_NAME_GZIP)
                .build();

        Data decompressInputData = new Data.Builder()
                .putString("fileName", DOWNLOAD_FILE_NAME_GZIP)
                .build();

        Data readInputData = new Data.Builder()
                .putString("fileName", DOWNLOAD_FILE_NAME)
                .build();

        OneTimeWorkRequest apiKeyRequest = new OneTimeWorkRequest.Builder(ApiKeyWorker.class)
                .setInputData(apiKey)
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

        readRequest = new OneTimeWorkRequest.Builder(ReadFileWorker.class)
                .setInputData(readInputData)
                .build();

        WorkContinuation continuation = WorkManager.getInstance(this)
                .beginWith(apiKeyRequest)
                .then(registerRequest)
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
        if (workInfo.getState() == WorkInfo.State.SUCCEEDED && workInfo.getId().equals(readRequest.getId())) {
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
            if (workInfo.getOutputData().getKeyValueMap().isEmpty())
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