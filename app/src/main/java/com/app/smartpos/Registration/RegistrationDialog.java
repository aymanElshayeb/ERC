package com.app.smartpos.Registration;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.app.smartpos.HomeActivity;
import com.app.smartpos.R;
import com.app.smartpos.auth.LoginWithServerWorker;
import com.app.smartpos.settings.Synchronization.DecompressWorker;
import com.app.smartpos.settings.Synchronization.DownloadWorker;
import com.app.smartpos.settings.Synchronization.ReadFileWorker;
import com.app.smartpos.utils.SharedPrefUtils;

public class RegistrationDialog extends DialogFragment {

    private static final int STORAGE_PERMISSION_CODE = 23;
    View root;
    EditText usernameEt;
    EditText passwordEt;
    EditText merchantEt;
    ProgressBar progressBar;
    Button registerBtn;
    Button demoBtn;

    private String deviceId;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(root==null){
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            root=inflater.inflate(R.layout.dialog_registration,container,false);
            setCancelable(false);

            deviceId = Settings.Secure.getString(getContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);

            usernameEt=root.findViewById(R.id.username_et);
            passwordEt=root.findViewById(R.id.password_et);
            merchantEt=root.findViewById(R.id.merchant_id_et);
            registerBtn =root.findViewById(R.id.register_btn);
            demoBtn =root.findViewById(R.id.demo_btn);
            progressBar=root.findViewById(R.id.progress);
            registerBtn.setOnClickListener(view -> {
                if(usernameEt.getText().toString().isEmpty()){
                    Toast.makeText(requireActivity(), requireContext().getResources().getString(R.string.user_name_empty), Toast.LENGTH_SHORT).show();
                }else if(passwordEt.getText().toString().isEmpty()){
                    Toast.makeText(requireActivity(), requireContext().getResources().getString(R.string.password_empty), Toast.LENGTH_SHORT).show();
                }else if(merchantEt.getText().toString().isEmpty()){
                    Toast.makeText(requireActivity(), requireContext().getResources().getString(R.string.merchant_empty), Toast.LENGTH_SHORT).show();
                }else {
                    //registerBtn.setVisibility(View.GONE);
                    //enqueueDownloadAndReadWorkers();
                }
            });

            demoBtn.setOnClickListener(view -> {
                SharedPrefUtils.setDemoPressed(requireActivity(),true);
                requireActivity().finish();
                requireActivity().startActivity(new Intent(requireActivity(), HomeActivity.class));
            });

            requestForStoragePermissions();
        }

        //SharedPrefUtils.setIsRegistered(this,true);
        //SharedPrefUtils.setStartDateTime(this);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = (int)(getContext().getResources().getDisplayMetrics().widthPixels*0.9);
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }

    private void requestForStoragePermissions() {
        //Android is 11 (R) or above
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(
                        requireActivity(),
                        new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        },
                        STORAGE_PERMISSION_CODE
                );
            }

        }

    }



    private void enqueueDownloadAndReadWorkers() {
        //username Admin
        //password 01111Mm&
        Data login = new Data.Builder().
                putString("url", "https://gateway-am-wso2-nonprod.apps.nt-non-ocp.neotek.sa/ecr/v1/auth/user").
                putString("tenantId", "test").
                putString("username", usernameEt.getText().toString()).
                putString("password", passwordEt.getText().toString()).
                build();
        Data downloadInputData = new Data.Builder()
                .putString("url", "https://gateway-am-wso2-nonprod.apps.nt-non-ocp.neotek.sa/ecr/v1/sync")
                .putString("tenantId", "test")
                .putString("fileName", "download.db.gz")
                .build();

        Data decompressInputData = new Data.Builder()
                .putString("fileName", "download.db.gz")
                .build();

        Data readInputData = new Data.Builder()
                .putString("fileName", "download.db")
                .build();

        OneTimeWorkRequest loginRequest = new OneTimeWorkRequest.Builder(LoginWithServerWorker.class)
                .setInputData(login)
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

        WorkContinuation continuation = WorkManager.getInstance(requireActivity())
                .beginWith(loginRequest)
                .then(downloadRequest)
                .then(decompressRequest)
                .then(readRequest);
        continuation.enqueue();

        WorkManager.getInstance(requireActivity()).getWorkInfoByIdLiveData(readRequest.getId())
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
            closePendingScreen();
        } else if (workInfo.getState() == WorkInfo.State.FAILED) {
            // Work failed, handle failure
            registerBtn.setVisibility(View.VISIBLE);
            showError();
        }
    }

    private void closePendingScreen() {
        dismissAllowingStateLoss();
    }

    private void showError() {
        Toast.makeText(requireActivity(), "Error in download data", Toast.LENGTH_SHORT).show();
    }

}
