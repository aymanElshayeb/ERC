package com.app.smartpos.settings.Synchronization;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadWorker extends Worker {

    public DownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String urlString = getInputData().getString("url");
        String fileName = getInputData().getString("fileName");
        if (urlString == null || fileName == null) {
            return Result.failure();
        }

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("tenantId", "test");
            connection.setRequestProperty("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEwMDAwMDAwLCJpYXQiOjE3MjI4NTU5NzF9.ALx4el9D3FcwoWrhimZTBcaaXEc3O7sAULtBFjrPDds");


            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                Uri uri = saveFileToDownloads(getApplicationContext(), fileName);

                if (uri != null) {
                    try (FileOutputStream outputStream = (FileOutputStream) getApplicationContext().getContentResolver().openOutputStream(uri)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                }

                inputStream.close();
                connection.disconnect();
                return Result.success();
            } else {
                // Handle error response
                connection.disconnect();
                return Result.failure();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }

    private Uri saveFileToDownloads(Context context, String fileName) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri downloadsUri = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            downloadsUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
        }

        // Check if the file already exists
        String selection = MediaStore.Downloads.DISPLAY_NAME + "=?";
        String[] selectionArgs = new String[]{fileName};
        Cursor cursor = contentResolver.query(downloadsUri, null, selection, selectionArgs, null);

        if (cursor != null && cursor.moveToFirst()) {
            // File exists, delete it
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID));
            Uri existingFileUri = Uri.withAppendedPath(downloadsUri, String.valueOf(id));
            contentResolver.delete(existingFileUri, null, null);
            cursor.close();
        }

        // Create a new file entry
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        return contentResolver.insert(downloadsUri, values);
    }
}
