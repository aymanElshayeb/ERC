package com.app.smartpos.settings.Synchronization.workers;

import static com.app.smartpos.common.CrashReport.CustomExceptionHandler.addToDatabase;
import static com.app.smartpos.utils.SSLUtils.getUnsafeOkHttpClient;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.app.smartpos.common.Utils;
import com.app.smartpos.utils.SharedPrefUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProductImagesSizeWorker extends Worker {

    int code=-5;
    public ProductImagesSizeWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String urL = getInputData().getString("url");
        String tenantId = getInputData().getString("tenantId");
        String authorization = getInputData().getString("Authorization");

        if (urL == null || tenantId == null || authorization == null) {
            Utils.addLog("datadata_worker", "fail");
            return Result.failure();
        }
        FormBody formBody = new FormBody.Builder()
                .add("email", "email")
                .add("password", "")
                .build();
        OkHttpClient client = getUnsafeOkHttpClient();
        Headers headers = new Headers.Builder().
                add("tenantId", tenantId).
                add("Authorization", authorization).
                add("apikey", SharedPrefUtils.getApiKey()).
                build();
        Utils.addLog("datadata_worker", headers.toString());
        Utils.addLog("datadata_worker", authorization);
        Request request = new Request.Builder()
                .url(urL) // Replace with your server's upload endpoint
                .post(formBody)
                .headers(headers)
                .build();
        JSONObject headersJson = new JSONObject();
        try {
            headersJson.put("Authorization", Collections.singletonList("......"));
            headersJson.put("apikey",Collections.singletonList("......"));
            headersJson.put("tenantId", tenantId);
        } catch (JSONException e) {
            addToDatabase(e,"productImageSize");
        }

        Utils.addLog("datadata_worker", request.toString());
        try (Response response = client.newCall(request).execute()) {
            code= response.code();
            JSONObject responseBody = new JSONObject();
            if (response.isSuccessful()) {
                Utils.addLog("datadata_worker", "success");


                //ProductImagesResponseDto productImagesResponseDto=result.getData().getReturnedObj().get(0);
                assert response.body() != null;
                responseBody = new JSONObject(response.body().string());
                code = responseBody.getInt("code");
                Utils.addLog("datadata_worker", responseBody.toString());
                JSONObject returnedObj = responseBody.getJSONObject("data").getJSONArray("returnedObj").getJSONObject(0);
                long imagesSize = returnedObj.getLong("imagesSize");
                String newUpdateTimestamp = returnedObj.getString("newUpdateTimestamp");
                boolean needToUpdate = returnedObj.getBoolean("needToUpdate");

                Data data = new Data.Builder().
                        putLong("imagesSize", imagesSize).
                        putString("newUpdateTimestamp", newUpdateTimestamp).
                        putBoolean("needToUpdate",needToUpdate).
                        build();

                Utils.addRequestTracking(urL,"ProductImagesSizeWorker",headersJson.toString(),"",responseBody.toString());


                return Result.success(data); // Return success if the response is successful
            } else {
                Utils.addRequestTracking(urL,"ProductImagesSizeWorker",headersJson.toString(),"",code+"\n"+ responseBody);
                return Result.failure(); // Retry the work if the server returns an error
            }
        } catch (Exception e) {
            Utils.addRequestTracking(urL,"ProductImagesSizeWorker",headersJson.toString(),"",code+"\n"+e.getMessage());
            addToDatabase(e,"productImageSizeApi-cannot-call-request");
            e.printStackTrace();
            return Result.failure(); // Return failure if there is an exception
        }
    }
}
