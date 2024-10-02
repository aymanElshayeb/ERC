package com.app.smartpos.common;

import static com.app.smartpos.Constant.API_KEY;
import static com.app.smartpos.Constant.DOWNLOAD_FILE_NAME;
import static com.app.smartpos.Constant.DOWNLOAD_FILE_NAME_GZIP;
import static com.app.smartpos.Constant.LAST_SYNC_URL;
import static com.app.smartpos.Constant.LOGIN_URL;
import static com.app.smartpos.Constant.PRODUCT_IMAGES_SIZE;
import static com.app.smartpos.Constant.SYNC_URL;
import static com.app.smartpos.Constant.UPLOAD_FILE_NAME;

import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.app.smartpos.auth.LoginWithServerWorker;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.settings.Synchronization.workers.CompressWorker;
import com.app.smartpos.settings.Synchronization.workers.DecompressWorker;
import com.app.smartpos.settings.Synchronization.workers.DownloadWorker;
import com.app.smartpos.settings.Synchronization.workers.ExportFileWorker;
import com.app.smartpos.settings.Synchronization.workers.LastSyncWorker;
import com.app.smartpos.settings.Synchronization.workers.ProductImagesSizeWorker;
import com.app.smartpos.settings.Synchronization.workers.ReadFileWorker;
import com.app.smartpos.settings.Synchronization.workers.UploadWorker;
import com.app.smartpos.utils.SharedPrefUtils;
import com.app.smartpos.utils.BaseActivity;

import java.util.HashMap;

public class WorkerActivity extends BaseActivity {


    public void enqueueCreateAndUploadWorkers() {
        //username Admin
        //password 01111Mm&
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        HashMap<String, String> conf = databaseAccess.getConfiguration();

        Data lastSync = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if(SharedPrefUtils.getAuthorization().isEmpty()){
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
                        putString("Authorization",SharedPrefUtils.getAuthorization()).
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && SharedPrefUtils.getAuthorization().isEmpty()) {
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
            if(SharedPrefUtils.getAuthorization().isEmpty()){
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
                        putString("Authorization",SharedPrefUtils.getAuthorization()).
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && SharedPrefUtils.getAuthorization().isEmpty()) {
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void enqueueDownloadProductsImagesWorkers() {
        //username Admin
        //password 01111Mm&
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        HashMap<String, String> conf = databaseAccess.getConfiguration();

        Data lastSync = null;
        String ecr= conf.get("ecr_code");
        String deviceId = Utils.getDeviceId(this);
        Data loginInputData = new Data.Builder().
                putString("url", LOGIN_URL).
                putString("tenantId", conf.get("merchant_id")).
                putString("email", ecr).
                putString("password", deviceId).
                build();
        Data SizeData = new Data.Builder()
                .putString("url",PRODUCT_IMAGES_SIZE)
                .putString("apikey", API_KEY)
                .putString("tenantId", conf.get("merchant_id"))
                .build();


        OneTimeWorkRequest loginRequest = new OneTimeWorkRequest.Builder(LoginWithServerWorker.class).
                setInputData(loginInputData).
                build();

        OneTimeWorkRequest productImagesSizeRequest = new OneTimeWorkRequest.Builder(ProductImagesSizeWorker.class).
                setInputData(SizeData).
                build();

        WorkContinuation  continuation = WorkManager.getInstance(this)
                .beginWith(loginRequest)
                .then(productImagesSizeRequest);

        continuation.enqueue();

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(productImagesSizeRequest.getId())
                .observeForever(workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        Log.i("datadata",workInfo.getOutputData().toString());
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
