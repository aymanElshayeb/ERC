package com.app.smartpos.downloaddatadialog;


import static com.app.smartpos.Constant.DOWNLOAD_FILE_NAME;
import static com.app.smartpos.Constant.DOWNLOAD_FILE_NAME_GZIP;
import static com.app.smartpos.Constant.LAST_SYNC_URL;
import static com.app.smartpos.Constant.LOGIN_URL;
import static com.app.smartpos.Constant.SYNC_URL;
import static com.app.smartpos.Constant.UPLOAD_FILE_NAME;

import android.Manifest;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.app.smartpos.R;
import com.app.smartpos.auth.LoginWithServerWorker;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.database.DatabaseOpenHelper;
import com.app.smartpos.refund.Refund;
import com.app.smartpos.refund.RefundOrOrderDetails;
import com.app.smartpos.refund.RefundOrOrderList;
import com.app.smartpos.settings.Synchronization.CompressWorker;
import com.app.smartpos.settings.Synchronization.DataBaseBackupActivity;
import com.app.smartpos.settings.Synchronization.DecompressWorker;
import com.app.smartpos.settings.Synchronization.DownloadWorker;
import com.app.smartpos.settings.Synchronization.ExportFileWorker;
import com.app.smartpos.settings.Synchronization.LastSyncWorker;
import com.app.smartpos.settings.Synchronization.ReadFileWorker;
import com.app.smartpos.settings.Synchronization.UploadWorker;
import com.app.smartpos.utils.AuthoruzationHolder;
import com.app.smartpos.utils.SharedPrefUtils;

import java.util.HashMap;

public class DownloadDataDialog extends DialogFragment {

    private static final int STORAGE_PERMISSION_CODE = 23;
    View root;
    EditText usernameEt;
    EditText passwordEt;
    ProgressBar progressBar;
    Button downloadBtn;

    private static final String ARG_OPERATION_TYPE = "operation_type";
    public static final String OPERATION_UPLOAD = "upload";
    public static final String OPERATION_DOWNLOAD = "download";
    public static final String OPERATION_REFUND = "refund";

    public static DownloadDataDialog newInstance(String operationType) {
        DownloadDataDialog dialog = new DownloadDataDialog();
        Bundle args = new Bundle();
        args.putString(ARG_OPERATION_TYPE, operationType);
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (root == null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            root = inflater.inflate(R.layout.dialog_download_data, container, false);
            //setCancelable(false);
            String operationType;
            usernameEt = root.findViewById(R.id.username_et);
            passwordEt = root.findViewById(R.id.password_et);
            downloadBtn = root.findViewById(R.id.download_btn);
            progressBar = root.findViewById(R.id.progress);
            usernameEt.setText("admin@admin.com");
            passwordEt.setText("01111Mm&");
            if (getArguments() != null)
                operationType = getArguments().getString(ARG_OPERATION_TYPE);
            else {
                operationType = "";
            }
            downloadBtn.setOnClickListener(view -> {
                if (usernameEt.getText().toString().isEmpty()) {
                    Toast.makeText(requireActivity(), requireContext().getResources().getString(R.string.user_name_empty), Toast.LENGTH_SHORT).show();
                } else if (passwordEt.getText().toString().isEmpty()) {
                    Toast.makeText(requireActivity(), requireContext().getResources().getString(R.string.password_empty), Toast.LENGTH_SHORT).show();
                } else {
                    downloadBtn.setVisibility(View.GONE);
                    if (OPERATION_UPLOAD.equals(operationType)) {
                        enqueueCreateAndUploadWorkers();
                    } else if (OPERATION_DOWNLOAD.equals(operationType)) {
                        enqueueDownloadAndReadWorkers();
                    } else if (OPERATION_REFUND.equals(operationType)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            enqueueRefundWorkers();
                        }
                    }
                }
            });

            requestForStoragePermissions();
        }
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.9);
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((WindowManager.LayoutParams) params);
    }

    private void requestForStoragePermissions() {
        //Android is 11 (R) or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
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
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(requireContext());
        databaseAccess.open();
        HashMap<String, String> conf = databaseAccess.getConfiguration();
        Data login = new Data.Builder().
                putString("url", LOGIN_URL).
                putString("tenantId", conf.get("merchant_id")).
                putString("email", usernameEt.getText().toString()).
                putString("password", passwordEt.getText().toString()).
                build();
        Data downloadInputData = new Data.Builder()
                .putString("url", SYNC_URL)
                .putString("tenantId", conf.get("merchant_id"))
                .putString("fileName", DOWNLOAD_FILE_NAME_GZIP)
                .putString("ecrCode", conf.get("ecr_code"))
                .build();

        Data decompressInputData = new Data.Builder()
                .putString("fileName", DOWNLOAD_FILE_NAME_GZIP)
                .build();

        Data readInputData = new Data.Builder()
                .putString("fileName", DOWNLOAD_FILE_NAME)
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
        observeWorker(loginRequest);
        WorkManager.getInstance(requireActivity()).getWorkInfoByIdLiveData(readRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        // Work is finished, close pending screen or perform any action
                        handleWorkCompletion(workInfo);
                    }
                });
    }

    public void enqueueCreateAndUploadWorkers() {
        //username Admin
        //password 01111Mm&
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(requireContext());
        databaseAccess.open();
        HashMap<String, String> conf = databaseAccess.getConfiguration();
        Data loginInputData = new Data.Builder().
                putString("url", LOGIN_URL).
                putString("tenantId", conf.get("merchant_id")).
                putString("email", usernameEt.getText().toString()).
                putString("password", passwordEt.getText().toString()).
                build();

        Data lastSync = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if(AuthoruzationHolder.getAuthorization().isEmpty()){
                lastSync = new Data.Builder().
                        putString("url", LAST_SYNC_URL).
                        putString("tenantId", conf.get("merchant_id")).
                        putString("ecrCode", conf.get("ecr_code")).
                        build();
            } else{
                lastSync = new Data.Builder().
                        putString("url", LAST_SYNC_URL).
                        putString("tenantId", conf.get("merchant_id")).
                        putString("ecrCode", conf.get("ecr_code")).
                        putString("Authorization",AuthoruzationHolder.getAuthorization()).
                        build();
            }
        }
        Data exportData = new Data.Builder()
                .putString("fileName", UPLOAD_FILE_NAME)
                .build();
        Data uploadInputData = new Data.Builder().
                putString("url", SYNC_URL).
                putString("tenantId", conf.get("merchant_id")).
                putString("ecrCode", conf.get("ecr_code")).
                build();

        OneTimeWorkRequest loginRequest = new OneTimeWorkRequest.Builder(LoginWithServerWorker.class).
                setInputData(loginInputData).
                build();
        OneTimeWorkRequest lastSyncRequest = new OneTimeWorkRequest.Builder(LastSyncWorker.class).
                setInputData(lastSync).
                build();
        OneTimeWorkRequest exportRequest = new OneTimeWorkRequest.Builder(ExportFileWorker.class).
                setInputData(exportData).
                build();
        OneTimeWorkRequest compressRequest = new OneTimeWorkRequest.Builder(CompressWorker.class).build();
        OneTimeWorkRequest uploadRequest = new OneTimeWorkRequest.Builder(UploadWorker.class).
                setInputData(uploadInputData).
                build();
        WorkContinuation continuation ;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && AuthoruzationHolder.getAuthorization().isEmpty()) {
            continuation = WorkManager.getInstance(requireActivity())
                    .beginWith(loginRequest)
                    .then(lastSyncRequest)
                    .then(exportRequest)
                    .then(compressRequest)
                    .then(uploadRequest);
        } else {
            continuation = WorkManager.getInstance(requireActivity())
                    .beginWith(lastSyncRequest)
                    .then(exportRequest)
                    .then(compressRequest)
                    .then(uploadRequest);
        }

        continuation.enqueue();
        observeWorker(loginRequest);
        WorkManager.getInstance(requireActivity()).getWorkInfoByIdLiveData(uploadRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        // Work is finished, close pending screen or perform any action
                        handleWorkCompletion(workInfo);
                    }
                });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void enqueueRefundWorkers() {
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(requireContext());
        databaseAccess.open();
        HashMap<String, String> conf = databaseAccess.getConfiguration();
        Data loginInputData = new Data.Builder().
                putString("url", LOGIN_URL).
                putString("tenantId", conf.get("merchant_id")).
                putString("email", usernameEt.getText().toString()).
                putString("password", passwordEt.getText().toString()).
                build();
        Data lastSync = new Data.Builder().
                putString("url", LAST_SYNC_URL).
                putString("tenantId", conf.get("merchant_id")).
                putString("ecrCode", conf.get("ecr_code")).
                build();
        Data exportData = new Data.Builder()
                .putString("fileName", UPLOAD_FILE_NAME)
                .build();
        Data uploadInputData = new Data.Builder().
                putString("url", SYNC_URL).
                putString("tenantId", conf.get("merchant_id")).
                putString("ecrCode", conf.get("ecr_code")).
                build();

        Data downloadInputData = new Data.Builder()
                .putString("url", SYNC_URL)
                .putString("tenantId", conf.get("merchant_id"))
                .putString("fileName", DOWNLOAD_FILE_NAME_GZIP)
                .putString("ecrCode", conf.get("ecr_code"))
                .build();

        Data decompressInputData = new Data.Builder()
                .putString("fileName", DOWNLOAD_FILE_NAME_GZIP)
                .build();

        Data readInputData = new Data.Builder()
                .putString("fileName", DOWNLOAD_FILE_NAME)
                .build();
        OneTimeWorkRequest loginRequest = new OneTimeWorkRequest.Builder(LoginWithServerWorker.class).
                setInputData(loginInputData).
                build();

        OneTimeWorkRequest lastSyncRequest = new OneTimeWorkRequest.Builder(LastSyncWorker.class).
                setInputData(lastSync).
                build();
        OneTimeWorkRequest exportRequest = new OneTimeWorkRequest.Builder(ExportFileWorker.class).
                setInputData(exportData).
                build();
        OneTimeWorkRequest compressRequest = new OneTimeWorkRequest.Builder(CompressWorker.class).build();
        OneTimeWorkRequest uploadRequest = new OneTimeWorkRequest.Builder(UploadWorker.class).
                setInputData(uploadInputData).
                build();
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
                .then(lastSyncRequest)
                .then(exportRequest)
                .then(compressRequest)
                .then(uploadRequest)
                .then(downloadRequest)
                .then(decompressRequest)
                .then(readRequest);

        continuation.enqueue();
        observeWorker(loginRequest);

        WorkManager.getInstance(requireActivity()).getWorkInfoByIdLiveData(loginRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        // Work is finished, close pending screen or perform any action
                        Log.i("WORK INFO", workInfo.getOutputData().getString("Authorization"));
//                        authorization= workInfo.getOutputData().getString("Authorization");
                        AuthoruzationHolder.setAuthorization(workInfo.getOutputData().getString("Authorization"));
                        Log.i("WORK AUTH", AuthoruzationHolder.getAuthorization());
                    }
                });
        WorkManager.getInstance(requireActivity()).getWorkInfoByIdLiveData(readRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        // Work is finished, close pending screen or perform any action
                        Log.i("LOG AUTH",AuthoruzationHolder.getAuthorization());
                        handleWorkCompletion(workInfo);
                    }
                });
    }

    private void handleWorkCompletion(WorkInfo workInfo) {
        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
            // Work succeeded, handle success
            showMessage("Data Synced Successfully");
            closePendingScreen();
        } else if (workInfo.getState() == WorkInfo.State.FAILED) {
            // Work failed, handle failure
            downloadBtn.setVisibility(View.VISIBLE);
            showMessage("Error in Syncing data");
        }
    }

    private void closePendingScreen() {
        dismissAllowingStateLoss();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i("LOG AUTHINSIDE Close ",AuthoruzationHolder.getAuthorization());
        }

        if (getActivity() instanceof Refund) {
            ((Refund) getActivity()).callApi();
        } else if (getActivity() instanceof RefundOrOrderList) {
            ((RefundOrOrderList) getActivity()).callApi();
        } else if (getActivity() instanceof RefundOrOrderDetails) {
            ((RefundOrOrderDetails) getActivity()).redirectToSuccess();
        }
    }

    private void observeWorker(OneTimeWorkRequest workRequest) {
        WorkManager.getInstance(requireActivity()).getWorkInfoByIdLiveData(workRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        if (workInfo.getState() == WorkInfo.State.FAILED) {
                            String errorMessage = workInfo.getOutputData().getString("errorMessage");
                            showMessage((errorMessage != null ? errorMessage : "Unknown error occurred"));
                        }
                    }
                });
    }

    private void showMessage(String message) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
    }
}
