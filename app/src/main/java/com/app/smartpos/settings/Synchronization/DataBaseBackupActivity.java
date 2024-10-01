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

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.app.smartpos.R;
import com.app.smartpos.common.WorkerActivity;
import com.app.smartpos.database.DatabaseOpenHelper;
import com.app.smartpos.downloaddatadialog.DownloadDataDialog;
import com.app.smartpos.settings.backup.LocalBackup;
import com.app.smartpos.utils.BaseActivity;

public class DataBaseBackupActivity extends WorkerActivity {

    LinearLayout loadingLl;
    ProgressDialog loading;
    private LocalBackup localBackup;
    CardView cardLocalBackUp, cardLocalImport, downloadProductsImages, cardBackupToDrive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_backup);

        loadingLl=findViewById(R.id.loading_ll);
        getSupportActionBar().setHomeButtonEnabled(true); //for back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//for back button
        getSupportActionBar().setTitle(R.string.data_backup);

        final DatabaseOpenHelper db = new DatabaseOpenHelper(getApplicationContext());
        cardLocalBackUp = findViewById(R.id.download_backup);
        cardLocalImport = findViewById(R.id.upload_backup);
        downloadProductsImages = findViewById(R.id.download_products_images);




        localBackup = new LocalBackup(this);



        cardLocalImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingLl.setVisibility(View.VISIBLE);
                enqueueUploadWorkers();
            }
        });


        cardLocalBackUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingLl.setVisibility(View.VISIBLE);
                enqueueDownloadAndReadWorkers();

            }
        });

        downloadProductsImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingLl.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    enqueueDownloadProductsImagesWorkers();
                }

            }
        });

    }

    @Override
    public void handleWorkCompletion(WorkInfo workInfo) {
        super.handleWorkCompletion(workInfo);
        loadingLl.setVisibility(View.GONE);
        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
            // Work succeeded, handle success
            showMessage(getString(R.string.data_synced_successfully));
        } else if (workInfo.getState() == WorkInfo.State.FAILED) {
            // Work failed, handle failure
            showMessage(getString(R.string.error_in_syncing_data));
        }
    }


    //for back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean checkStoragePermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Android is 11 (R) or above
            return Environment.isExternalStorageManager();
        }else {
            //Below android 11
            int write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

            return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void enqueue() {
        //username Admin
        //password 01111Mm&
        Data writeFile  = new Data.Builder().
                build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ExportFileWorker.class)
                .setInputData(writeFile)
                .build();

        WorkContinuation continuation = WorkManager.getInstance(this)
                .beginWith(workRequest);
        continuation.enqueue();
    }


}
