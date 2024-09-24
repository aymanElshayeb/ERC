package com.app.smartpos.common;

import static com.app.smartpos.Constant.DOWNLOAD_FILE_NAME;
import static com.app.smartpos.Constant.DOWNLOAD_FILE_NAME_GZIP;
import static com.app.smartpos.Constant.LAST_SYNC_URL;
import static com.app.smartpos.Constant.LOGIN_URL;
import static com.app.smartpos.Constant.SYNC_URL;
import static com.app.smartpos.Constant.UPLOAD_FILE_NAME;

import android.app.Activity;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.app.smartpos.auth.LoginWithServerWorker;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.settings.Synchronization.CompressWorker;
import com.app.smartpos.settings.Synchronization.DecompressWorker;
import com.app.smartpos.settings.Synchronization.DownloadWorker;
import com.app.smartpos.settings.Synchronization.ExportFileWorker;
import com.app.smartpos.settings.Synchronization.LastSyncWorker;
import com.app.smartpos.settings.Synchronization.ReadFileWorker;
import com.app.smartpos.settings.Synchronization.UploadWorker;
import com.app.smartpos.utils.AuthoruzationHolder;
import com.app.smartpos.utils.Hasher;
import com.app.smartpos.utils.SharedPrefUtils;

import java.util.HashMap;

public class WorkerActivity extends AppCompatActivity {


    public void enqueueCreateAndUploadWorkers() {
        //username Admin
        //password 01111Mm&
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        HashMap<String, String> conf = databaseAccess.getConfiguration();

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
            continuation = WorkManager.getInstance(this)
                    .beginWith(lastSyncRequest)
                    .then(exportRequest)
                    .then(compressRequest)
                    .then(uploadRequest);
        } else {
            continuation = WorkManager.getInstance(this)
                    .beginWith(lastSyncRequest)
                    .then(exportRequest)
                    .then(compressRequest)
                    .then(uploadRequest);
        }

        continuation.enqueue();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(uploadRequest.getId())
                .observeForever(workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        // Work is finished, close pending screen or perform any action
                        handleWorkCompletion(workInfo);
                    }
                });

    }

    public void enqueueDownloadAndReadWorkers() {
        //username Admin
        //password 01111Mm&
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        HashMap<String, String> conf = databaseAccess.getConfiguration();
        String ecr= conf.get("ecr_code");
        String deviceId = Utils.getDeviceId(this);
        Data login = new Data.Builder().
                putString("url", LOGIN_URL).
                putString("tenantId", conf.get("merchant_id")).
                putString("email", ecr).
                putString("password", deviceId).
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

        WorkContinuation continuation = WorkManager.getInstance(this)
                .beginWith(loginRequest)
                .then(downloadRequest)
                .then(decompressRequest)
                .then(readRequest);
        continuation.enqueue();
        observeWorker(loginRequest);
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(readRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        // Work is finished, close pending screen or perform any action
                        handleWorkCompletion(workInfo);
                    }
                });
    }

    public void enqueueUploadWorkers() {
        //username Admin
        //password 01111Mm&
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        HashMap<String, String> conf = databaseAccess.getConfiguration();

        String ecr= conf.get("ecr_code");
        String deviceId = Utils.getDeviceId(this);
        Data loginInputData = new Data.Builder().
                putString("url", LOGIN_URL).
                putString("tenantId", conf.get("merchant_id")).
                putString("email", ecr).
                putString("password", deviceId).
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
            continuation = WorkManager.getInstance(this)
                    .beginWith(loginRequest)
                    .then(lastSyncRequest)
                    .then(exportRequest)
                    .then(compressRequest)
                    .then(uploadRequest);
        } else {
            continuation = WorkManager.getInstance(this)
                    .beginWith(lastSyncRequest)
                    .then(exportRequest)
                    .then(compressRequest)
                    .then(uploadRequest);
        }

        continuation.enqueue();
        observeWorker(loginRequest);
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(uploadRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        // Work is finished, close pending screen or perform any action
                        handleWorkCompletion(workInfo);
                    }
                });

    }

    public void handleWorkCompletion(WorkInfo workInfo) {

    }

    private void observeWorker(OneTimeWorkRequest workRequest) {
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workRequest.getId())
                .observeForever(workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        if (workInfo.getState() == WorkInfo.State.FAILED) {
                            String errorMessage = workInfo.getOutputData().getString("errorMessage");
                            showMessage((errorMessage != null ? errorMessage : "Unknown error occurred"));
                        }
                    }
                });
    }

    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
