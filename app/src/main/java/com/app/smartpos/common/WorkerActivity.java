package com.app.smartpos.common;

import static com.app.smartpos.Constant.LAST_SYNC_URL;
import static com.app.smartpos.Constant.LOGIN_URL;
import static com.app.smartpos.Constant.SYNC_URL;
import static com.app.smartpos.Constant.UPLOAD_FILE_NAME;

import android.os.Build;
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
import com.app.smartpos.settings.Synchronization.ExportFileWorker;
import com.app.smartpos.settings.Synchronization.LastSyncWorker;
import com.app.smartpos.settings.Synchronization.UploadWorker;
import com.app.smartpos.utils.AuthoruzationHolder;

import java.util.HashMap;

public class WorkerActivity extends AppCompatActivity {


    public void enqueueCreateAndUploadWorkers(AppCompatActivity activity) {
        //username Admin
        //password 01111Mm&
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(activity);
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
            continuation = WorkManager.getInstance(activity)
                    .beginWith(lastSyncRequest)
                    .then(exportRequest)
                    .then(compressRequest)
                    .then(uploadRequest);
        } else {
            continuation = WorkManager.getInstance(activity)
                    .beginWith(lastSyncRequest)
                    .then(exportRequest)
                    .then(compressRequest)
                    .then(uploadRequest);
        }

        continuation.enqueue();
        WorkManager.getInstance(activity).getWorkInfoByIdLiveData(uploadRequest.getId())
                .observeForever(workInfo -> {
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
