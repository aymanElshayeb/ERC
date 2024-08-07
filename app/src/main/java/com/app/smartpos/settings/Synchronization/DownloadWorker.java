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

import com.app.smartpos.utils.SSLUtils;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

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
        SSLUtils.trustAllCertificates();
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (connection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) connection).setHostnameVerifier((hostname, session) -> true);
            }
            connection.setRequestMethod("GET");
            connection.setRequestProperty("tenantId", "test");
            connection.setRequestProperty("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEwMDAwMDAwLCJpYXQiOjE3MjMwMTQxOTN9.pdhCP153EH8tvRVlWS7LURmdVPhl4CVmcuxvTFZOTYk");
            connection.setRequestProperty("apikey", "eyJ4NXQiOiJPREUzWTJaaE1UQmpNRE00WlRCbU1qQXlZemxpWVRJMllqUmhZVFpsT0dJeVptVXhOV0UzWVE9PSIsImtpZCI6ImdhdGV3YXlfY2VydGlmaWNhdGVfYWxpYXMiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhZG1pbkBjYXJib24uc3VwZXIiLCJhcHBsaWNhdGlvbiI6eyJvd25lciI6ImFkbWluIiwidGllclF1b3RhVHlwZSI6bnVsbCwidGllciI6IlVubGltaXRlZCIsIm5hbWUiOiJFQ1JfQXBwbGljYXRpb24iLCJpZCI6MTE2LCJ1dWlkIjoiYTA2MGViNTgtN2Y5NC00YWRmLTk3YWMtZmMzZmRmOTUxNjIzIn0sImlzcyI6Imh0dHBzOlwvXC9hbS13c28yLW5vbnByb2QuYXBwcy5udC1ub24tb2NwLm5lb3Rlay5zYTo0NDNcL29hdXRoMlwvdG9rZW4iLCJ0aWVySW5mbyI6eyJVbmxpbWl0ZWQiOnsidGllclF1b3RhVHlwZSI6InJlcXVlc3RDb3VudCIsImdyYXBoUUxNYXhDb21wbGV4aXR5IjowLCJncmFwaFFMTWF4RGVwdGgiOjAsInN0b3BPblF1b3RhUmVhY2giOnRydWUsInNwaWtlQXJyZXN0TGltaXQiOjAsInNwaWtlQXJyZXN0VW5pdCI6bnVsbH19LCJrZXl0eXBlIjoiUFJPRFVDVElPTiIsInBlcm1pdHRlZFJlZmVyZXIiOiIiLCJzdWJzY3JpYmVkQVBJcyI6W3sic3Vic2NyaWJlclRlbmFudERvbWFpbiI6ImNhcmJvbi5zdXBlciIsIm5hbWUiOiJlY3IiLCJjb250ZXh0IjoiXC9lY3JcL3YxIiwicHVibGlzaGVyIjoiYWRtaW4iLCJ2ZXJzaW9uIjoidjEiLCJzdWJzY3JpcHRpb25UaWVyIjoiVW5saW1pdGVkIn1dLCJ0b2tlbl90eXBlIjoiYXBpS2V5IiwicGVybWl0dGVkSVAiOiIiLCJpYXQiOjE3MjI1MTg0MzIsImp0aSI6IjRhYWRiMGM0LWYwOWQtNGZjYS1iZDZmLWYwOGM0ZTY5N2ZjNyJ9.Wjtrfb5XmBkduIkKkcpZrfwrfIMTaX328Sv8rUcpmqdlv4qDAmCkFMfoNku5IkGjW_dukr9Q1-ueqedl1-r9PDjmZsEyoLinyxnCDo4dMDJftdms-rsf873WJLlQe3Umifrsfx07Je_-wGi2S6q72w3TcCaEjYDMjB005FcBcE2o2QCX0B9kjxmQFdEASKE-tuUGnKAZfKpouvqpoPzxk3Tfxa7qCpTrdIZTrLHBJbLNEKZPbBkzl8mIaEh3_HD5dliTGw9rdyL2XAa2lKUJjrhmOdrm6EmyS3_hnZ8tyEuWXNeHvcJ2-DWEso7wQsn8M7WQD8dXebyHjG-Tfyle3g==");

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
