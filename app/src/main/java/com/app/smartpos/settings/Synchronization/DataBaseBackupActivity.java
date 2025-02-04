package com.app.smartpos.settings.Synchronization;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.app.smartpos.R;
import com.app.smartpos.common.WorkerActivity;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.database.DatabaseOpenHelper;
import com.app.smartpos.settings.Synchronization.workers.ExportFileWorker;
import com.app.smartpos.settings.backup.LocalBackup;

public class DataBaseBackupActivity extends WorkerActivity {

    LinearLayout loadingLl;
    ProgressDialog loading;
    int workerType;
    CardView cardLocalBackUp, cardLocalImport, downloadProductsImages, cardBackupToDrive;
    private LocalBackup localBackup;
    DatabaseAccess databaseAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_backup);
        databaseAccess = DatabaseAccess.getInstance(this);
        loadingLl = findViewById(R.id.loading_ll);
        getSupportActionBar().setHomeButtonEnabled(true); //for back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//for back button
        getSupportActionBar().setTitle(R.string.data_backup);

        final DatabaseOpenHelper db = new DatabaseOpenHelper(getApplicationContext());
        cardLocalBackUp = findViewById(R.id.download_backup);
        cardLocalImport = findViewById(R.id.upload_backup);
        downloadProductsImages = findViewById(R.id.download_products_images);

        localBackup = new LocalBackup(this);

        cardLocalBackUp.setOnClickListener(v -> {
            if (isConnected()) {
                workerType = 1;
                loadingLl.setVisibility(View.VISIBLE);
                //enqueueUploadWorkers();
                enqueueDownloadAndReadWorkers();
            }else {
                Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
            }

        });

        cardLocalImport.setOnClickListener(v -> {
            if (isConnected()) {
                loadingLl.setVisibility(View.VISIBLE);
                workerType = 2;
                enqueueUploadWorkers(true);
            } else {
                Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
            }
        });


        downloadProductsImages.setOnClickListener(v -> {
            if (isConnected()) {
                loadingLl.setVisibility(View.VISIBLE);
                workerType = 3;
                enqueueDownloadProductsImagesSizeWorkers();
            } else {
                Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void showLoading() {
        loadingLl.setVisibility(View.VISIBLE);
    }

    @Override
    public void handleWorkCompletion(WorkInfo workInfo) {
        super.handleWorkCompletion(workInfo);
        loadingLl.setVisibility(View.GONE);
        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
            // Work succeeded, handle success

            if (workerType == 3) {
                if (imagesSize == 0 && needToUpdate) {
                    loadingLl.setVisibility(View.VISIBLE);
                    workerType = 4;
                    enqueueDownloadProductsImagesWorkers();
                } else if (imagesSize == 0) {
                    Toast.makeText(this, getString(R.string.no_product_images), Toast.LENGTH_SHORT).show();
                } else {
                    showMessage(getString(R.string.data_synced_successfully));
                    DownloadProductImagesConfirmationDialog dialog = new DownloadProductImagesConfirmationDialog();
                    dialog.setData(this, formatSize(imagesSize));
                    dialog.show(getSupportFragmentManager(), "dialog");
                }
            } else {
                showMessage(getString(R.string.data_synced_successfully));
            }
        } else if (workInfo.getState() == WorkInfo.State.FAILED) {
            // Work failed, handle failure
            showMessage(getString(R.string.error_in_syncing_data));
        }
    }

    private String formatSize(long size) {
        String result;
        if (size >= 1000000) {
            result = size / 1000000 + "MB";
        } else if (size >= 1000) {
            result = size / 1000 + "KB";
        } else {
            result = size + "B";
        }

        return result;
    }

    //for back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {// app icon in action bar clicked; goto parent activity.
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11 (R) or above
            return Environment.isExternalStorageManager();
        } else {
            //Below android 11
            int write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

            return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void enqueue() {
        //username Admin
        //password 01111Mm&
        Data writeFile = new Data.Builder().
                build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ExportFileWorker.class)
                .setInputData(writeFile)
                .build();

        WorkContinuation continuation = WorkManager.getInstance(this)
                .beginWith(workRequest);
        continuation.enqueue();
    }


}
