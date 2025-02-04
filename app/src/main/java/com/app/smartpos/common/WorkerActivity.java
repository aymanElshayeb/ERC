package com.app.smartpos.common;

import static com.app.smartpos.Constant.CRASH_REPORT_SYNC_URL;
import static com.app.smartpos.Constant.DOWNLOAD_FILE_NAME;
import static com.app.smartpos.Constant.DOWNLOAD_FILE_NAME_GZIP;
import static com.app.smartpos.Constant.LAST_SYNC_URL;
import static com.app.smartpos.Constant.LOGIN_URL;
import static com.app.smartpos.Constant.PRODUCT_IMAGES;
import static com.app.smartpos.Constant.PRODUCT_IMAGES_FILE_NAME;
import static com.app.smartpos.Constant.PRODUCT_IMAGES_FILE_NAME_GZIP;
import static com.app.smartpos.Constant.PRODUCT_IMAGES_SIZE;
import static com.app.smartpos.Constant.REQUEST_TRACKING_SYNC_URL;
import static com.app.smartpos.Constant.SYNC_URL;
import static com.app.smartpos.Constant.UPLOAD_ERROR_TRACKING_FILE_NAME;
import static com.app.smartpos.Constant.UPLOAD_FILE_NAME;
import static com.app.smartpos.Constant.UPLOAD_REQUEST_TRACKING_FILE_NAME;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.app.smartpos.R;
import com.app.smartpos.auth.LoginWithServerWorker;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.settings.Synchronization.workers.CompressWorker;
import com.app.smartpos.settings.Synchronization.workers.DecompressWorker;
import com.app.smartpos.settings.Synchronization.workers.DownloadWorker;
import com.app.smartpos.settings.Synchronization.workers.ExportCrashReportFileWorker;
import com.app.smartpos.settings.Synchronization.workers.ExportFileWorker;
import com.app.smartpos.settings.Synchronization.workers.ExportRequestTrackingFileWorker;
import com.app.smartpos.settings.Synchronization.workers.LastSyncWorker;
import com.app.smartpos.settings.Synchronization.workers.ProductImagesSizeWorker;
import com.app.smartpos.settings.Synchronization.workers.ProductImagesWorker;
import com.app.smartpos.settings.Synchronization.workers.ReadFileWorker;
import com.app.smartpos.settings.Synchronization.workers.ReadProductImagesFileWorker;
import com.app.smartpos.settings.Synchronization.workers.ReportsUploadWorker;
import com.app.smartpos.settings.Synchronization.workers.UploadWorker;
import com.app.smartpos.utils.BaseActivity;
import com.app.smartpos.utils.SharedPrefUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class WorkerActivity extends BaseActivity {

    public long imagesSize;
    public Boolean needToUpdate;
    String lastUpdated;

    DatabaseAccess databaseAccess;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseAccess = DatabaseAccess.getInstance(this);
    }

    public void enqueueCreateAndUploadWorkers() {
        //username Admin
        //password 01111Mm&
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        HashMap<String, String> conf = databaseAccess.getConfiguration();

        Data lastSync = new Data.Builder().
                putString("url", LAST_SYNC_URL).
                putString("tenantId", conf.get("merchant_id")).
                putString("ecrCode", conf.get("ecr_code")).
                putString("Authorization", SharedPrefUtils.getAuthorization()).
                build();
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
        WorkContinuation continuation = WorkManager.getInstance(this)
                .beginWith(lastSyncRequest)
                .then(exportRequest)
                .then(compressRequest)
                .then(uploadRequest);

        continuation.enqueue();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(uploadRequest.getId())
                .observeForever(workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        // Work is finished, close pending screen or perform any action
                        SharedPrefUtils.resetAuthorization();
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
        String ecr = conf.get("ecr_code");
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

    public void syncDownloadAndUploadWorker() {
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        HashMap<String, String> conf = databaseAccess.getConfiguration();
        String ecr = conf.get("ecr_code");
        String deviceId = Utils.getDeviceId(this);
        Data loginInputData = new Data.Builder().
                putString("url", LOGIN_URL).
                putString("tenantId", conf.get("merchant_id")).
                putString("email", ecr).
                putString("password", deviceId).
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


        WorkContinuation continuation = WorkManager.getInstance(this)
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

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(loginRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        // Work is finished, close pending screen or perform any action
                        if (workInfo.getOutputData().getKeyValueMap().containsKey("Authorization")) {
                            Utils.addLog("WORK INFO", workInfo.getOutputData().getString("Authorization"));
//                        authorization= workInfo.getOutputData().getString("Authorization");
                            SharedPrefUtils.setAuthorization(workInfo.getOutputData().getString("Authorization"));
                            Utils.addLog("WORK AUTH", SharedPrefUtils.getAuthorization());
                        }
                    }
                });
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(readRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        // Work is finished, close pending screen or perform any action
                        Utils.addLog("log_auth", SharedPrefUtils.getAuthorization());
                        handleWorkCompletion(workInfo);

                    }
                });
    }
    public void enqueueUploadWorkers(boolean hasObserver) {
        //username Admin
        //password 01111Mm&
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        HashMap<String, String> conf = databaseAccess.getConfiguration();

        String ecr = conf.get("ecr_code");
        String deviceId = Utils.getDeviceId(this);
        Data loginInputData = new Data.Builder().
                putString("url", LOGIN_URL).
                putString("tenantId", conf.get("merchant_id")).
                putString("email", ecr).
                putString("password", deviceId).
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
        WorkContinuation continuation = WorkManager.getInstance(this)
                .beginWith(loginRequest)
                .then(lastSyncRequest)
                .then(exportRequest)
                .then(compressRequest)
                .then(uploadRequest);

        continuation.enqueue();
        observeWorker(loginRequest);
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(uploadRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished() && hasObserver) {
                        // Work is finished, close pending screen or perform any action
                        handleWorkCompletion(workInfo);
                    }
                });

    }

    public void enqueueUploadCrashReportWorkers() {
        //username Admin
        //password 01111Mm&
        databaseAccess.open();
        HashMap<String, String> conf = databaseAccess.getConfiguration();

        String ecr = conf.get("ecr_code");
        String deviceId = Utils.getDeviceId(this);
        Data loginInputData = new Data.Builder().
                putString("url", LOGIN_URL).
                putString("tenantId", conf.get("merchant_id")).
                putString("email", ecr).
                putString("password", deviceId).
                build();

        Data exportData = new Data.Builder()
                .putString("fileName", UPLOAD_ERROR_TRACKING_FILE_NAME)
                .build();
        Data uploadInputData = new Data.Builder().
                putString("url", CRASH_REPORT_SYNC_URL).
                putString("tenantId", conf.get("merchant_id")).
                putString("ecrCode", conf.get("ecr_code")).
                build();

        OneTimeWorkRequest loginRequest = new OneTimeWorkRequest.Builder(LoginWithServerWorker.class).
                setInputData(loginInputData).
                build();
        OneTimeWorkRequest exportRequest = new OneTimeWorkRequest.Builder(ExportCrashReportFileWorker.class).
                setInputData(exportData).
                build();
        OneTimeWorkRequest compressRequest = new OneTimeWorkRequest.Builder(CompressWorker.class).build();
        OneTimeWorkRequest uploadRequest = new OneTimeWorkRequest.Builder(ReportsUploadWorker.class).
                setInputData(uploadInputData).
                build();
        WorkContinuation continuation = WorkManager.getInstance(this)
                .beginWith(loginRequest)
                .then(exportRequest)
                .then(compressRequest)
                .then(uploadRequest);

        continuation.enqueue();
        observeWorker(loginRequest);
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(uploadRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        if(workInfo.getState() == WorkInfo.State.SUCCEEDED){
                            databaseAccess.open();
                            databaseAccess.deleteReportRows();
                        }
                    }
                });

    }

    public void enqueueUploadRequestTrackingWorkers() {
        //username Admin
        //password 01111Mm&
        databaseAccess.open();
        HashMap<String, String> conf = databaseAccess.getConfiguration();

        String ecr = conf.get("ecr_code");
        String deviceId = Utils.getDeviceId(this);
        Data loginInputData = new Data.Builder().
                putString("url", LOGIN_URL).
                putString("tenantId", conf.get("merchant_id")).
                putString("email", ecr).
                putString("password", deviceId).
                build();

        Data exportData = new Data.Builder()
                .putString("fileName", UPLOAD_REQUEST_TRACKING_FILE_NAME)
                .build();
        Data uploadInputData = new Data.Builder().
                putString("url", REQUEST_TRACKING_SYNC_URL).
                putString("tenantId", conf.get("merchant_id")).
                putString("ecrCode", conf.get("ecr_code")).
                build();

        OneTimeWorkRequest loginRequest = new OneTimeWorkRequest.Builder(LoginWithServerWorker.class).
                setInputData(loginInputData).
                build();
        OneTimeWorkRequest exportRequest = new OneTimeWorkRequest.Builder(ExportRequestTrackingFileWorker.class).
                setInputData(exportData).
                build();
        OneTimeWorkRequest compressRequest = new OneTimeWorkRequest.Builder(CompressWorker.class).build();
        OneTimeWorkRequest uploadRequest = new OneTimeWorkRequest.Builder(ReportsUploadWorker.class).
                setInputData(uploadInputData).
                build();
        WorkContinuation continuation = WorkManager.getInstance(this)
                .beginWith(loginRequest)
                .then(exportRequest)
                .then(compressRequest)
                .then(uploadRequest);

        continuation.enqueue();
        observeWorker(loginRequest);
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(uploadRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        if(workInfo.getState() == WorkInfo.State.SUCCEEDED){
                            databaseAccess.open();
                            databaseAccess.deleteRequestTrackingRows();
                        }
                    }
                });

    }

    public void enqueueDownloadProductsImagesSizeWorkers() {
        //username Admin
        //password 01111Mm&
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        HashMap<String, String> conf = databaseAccess.getConfiguration();

        Data lastSync = null;
        String ecr = conf.get("ecr_code");
        String deviceId = Utils.getDeviceId(this);
        Data loginInputData = new Data.Builder().
                putString("url", LOGIN_URL).
                putString("tenantId", conf.get("merchant_id")).
                putString("email", ecr).
                putString("password", deviceId).
                build();
        String lastUpdateTimeStamp = SharedPrefUtils.getProductLastUpdatedTimeStamp();

        Data SizeData = new Data.Builder()
                .putString("url", PRODUCT_IMAGES_SIZE + lastUpdateTimeStamp)
                .putString("apikey", SharedPrefUtils.getApiKey())
                .putString("tenantId", conf.get("merchant_id"))
                .build();


        OneTimeWorkRequest loginRequest = new OneTimeWorkRequest.Builder(LoginWithServerWorker.class).
                setInputData(loginInputData).
                build();

        OneTimeWorkRequest productImagesSizeRequest = new OneTimeWorkRequest.Builder(ProductImagesSizeWorker.class).
                setInputData(SizeData).
                build();

        WorkContinuation continuation = WorkManager.getInstance(this)
                .beginWith(loginRequest)
                .then(productImagesSizeRequest);

        continuation.enqueue();

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(productImagesSizeRequest.getId())
                .observeForever(workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        Utils.addLog("datadata", workInfo.getOutputData().toString());
                        // Work is finished, close pending screen or perform any action
                        imagesSize = workInfo.getOutputData().getLong("imagesSize", 0);
                        needToUpdate = workInfo.getOutputData().getBoolean("needToUpdate", false);
                        lastUpdated = workInfo.getOutputData().getString("newUpdateTimestamp");
                        handleWorkCompletion(workInfo);
                    }
                    if (workInfo.getState() == WorkInfo.State.FAILED) {
                        Utils.addLog("datadata_worker", workInfo.getOutputData().toString());
                        String errorMessage = workInfo.getOutputData().getString("errorMessage");
                        showMessage((errorMessage != null ? errorMessage : getString(R.string.unknown_error_occurred)));
                    }
                });

    }

    public void enqueueDownloadProductsImagesWorkers() {
        //username Admin
        //password 01111Mm&
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        HashMap<String, String> conf = databaseAccess.getConfiguration();

        Data lastSync = null;
        String ecr = conf.get("ecr_code");
        String deviceId = Utils.getDeviceId(this);
        Data loginInputData = new Data.Builder().
                putString("url", LOGIN_URL).
                putString("tenantId", conf.get("merchant_id")).
                putString("email", ecr).
                putString("password", deviceId).
                build();
        String lastUpdateTimeStamp = SharedPrefUtils.getProductLastUpdatedTimeStamp();

        Data downloadInputData = new Data.Builder()
                .putString("url", PRODUCT_IMAGES + lastUpdateTimeStamp)
                .putString("apikey", SharedPrefUtils.getApiKey())
                .putString("ecrCode", ecr)
                .putString("tenantId", conf.get("merchant_id"))
                .putString("fileName", PRODUCT_IMAGES_FILE_NAME_GZIP)
                .build();

        Data decompressInputData = new Data.Builder()
                .putString("fileName", PRODUCT_IMAGES_FILE_NAME_GZIP)
                .build();

        Data readInputData = new Data.Builder()
                .putString("fileName", PRODUCT_IMAGES_FILE_NAME)
                .build();


        OneTimeWorkRequest loginRequest = new OneTimeWorkRequest.Builder(LoginWithServerWorker.class).
                setInputData(loginInputData).
                build();

        OneTimeWorkRequest downloadRequest = new OneTimeWorkRequest.Builder(ProductImagesWorker.class)
                .setInputData(downloadInputData)
                .build();

        OneTimeWorkRequest decompressRequest = new OneTimeWorkRequest.Builder(DecompressWorker.class)
                .setInputData(decompressInputData)
                .build();

        OneTimeWorkRequest readRequest = new OneTimeWorkRequest.Builder(ReadProductImagesFileWorker.class)
                .setInputData(readInputData)
                .build();

        WorkContinuation continuation = WorkManager.getInstance(this)
                .beginWith(loginRequest)
                .then(downloadRequest)
                .then(decompressRequest)
                .then(readRequest);

        continuation.enqueue();

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(readRequest.getId())
                .observeForever(workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        Utils.addLog("datadata", workInfo.getOutputData().toString());
                        // Work is finished, close pending screen or perform any action
                        handleWorkCompletion(workInfo);
                    }
                    if (workInfo.getState() == WorkInfo.State.FAILED) {
                        Utils.addLog("datadata_worker", workInfo.getOutputData().toString());
                        String errorMessage = workInfo.getOutputData().getString("errorMessage");
                        showMessage((errorMessage != null ? errorMessage : getString(R.string.unknown_error_occurred)));
                    }
                    if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                        SharedPrefUtils.setProductLastUpdatedTimeStamp(lastUpdated);
                    }
                });

    }

    public void loginWorkers(String email, String password) {
        //username Admin
        //password 01111Mm&
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        HashMap<String, String> conf = databaseAccess.getConfiguration();

        Data loginInputData = new Data.Builder().
                putString("url", LOGIN_URL).
                putString("tenantId", conf.get("merchant_id")).
                putString("email", email).
                putString("password", password).
                build();


        OneTimeWorkRequest loginRequest = new OneTimeWorkRequest.Builder(LoginWithServerWorker.class).
                setInputData(loginInputData).
                build();

        WorkContinuation continuation = WorkManager.getInstance(this)
                .beginWith(loginRequest);

        continuation.enqueue();

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(loginRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        // Work is finished, close pending screen or perform any action
                        handleWorkCompletion(workInfo);
                    }
                });

    }

    public void handleWorkCompletion(WorkInfo workInfo) {
        if(workInfo.getState() == WorkInfo.State.FAILED){
            sendReport();
        }
    }

    private void observeWorker(OneTimeWorkRequest workRequest) {
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workRequest.getId())
                .observeForever(workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        if (workInfo.getState() == WorkInfo.State.FAILED) {
                            String errorMessage = workInfo.getOutputData().getString("errorMessage");
                            showMessage((errorMessage != null ? errorMessage : getString(R.string.unknown_error_occurred)));
                        }
                    }
                });
    }

    public void showMessage(String message) {
        if(!message.isEmpty())
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        databaseAccess.open();
        HashMap<String, String> configuration = databaseAccess.getConfiguration();
        if(!configuration.isEmpty() && isConnected()) {
            enqueueUploadWorkers(false);
            sendReport();
        }
    }

    public void sendReport() {
        HashMap<String, String> configuration = databaseAccess.getConfiguration();
        if(!configuration.isEmpty() && isConnected()) {
            ArrayList<HashMap<String, String>> requestTrackingReports = databaseAccess.getRequestTracking();
            if (!requestTrackingReports.isEmpty()) {
                enqueueUploadRequestTrackingWorkers();
            }
            ArrayList<HashMap<String, String>> reports = databaseAccess.getReports();
            if (!reports.isEmpty()) {
                enqueueUploadCrashReportWorkers();
            }
        }
    }
}
