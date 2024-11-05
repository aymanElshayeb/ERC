package com.app.smartpos.Registration;

import static com.app.smartpos.utils.SSLUtils.getUnsafeOkHttpClient;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.app.smartpos.R;
import com.app.smartpos.common.Utils;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.utils.SharedPrefUtils;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegistrationWorker extends Worker {
    public RegistrationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String email = getInputData().getString("email");
        String password = getInputData().getString("password");
        String tenantId = getInputData().getString("tenantId");
        String deviceId = getInputData().getString("deviceId");
        String urlString = getInputData().getString("url");
        if (email == null || password == null || tenantId == null || deviceId == null) {
            return Result.failure();
        }
        // Perform registration logic here
        String apiKey = SharedPrefUtils.getApiKey();
        Utils.addLog("datadata_key", apiKey);
        OkHttpClient client = getUnsafeOkHttpClient();
        Headers headers = new Headers.Builder().
                add("tenantId", tenantId).
                add("apikey", apiKey).
                build();
        //prepare the dto class
        JSONObject json = new JSONObject();
        JSONObject data = new JSONObject();

        try {
            data.put("email", email);
            data.put("password", password);
            data.put("deviceId", deviceId);
            json.put("data", data);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(urlString)
                .post(body)
                .headers(headers)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                JSONObject responseBody = new JSONObject(response.body().string());
                int code = responseBody.getInt("code");


                if (code == 400 && responseBody.getJSONObject("fault").getString("statusCode").equalsIgnoreCase("E0000004")) {
                    Data outputData = new Data.Builder().putString("errorMessage", getApplicationContext().getString(R.string.non_admin_register)).build();
                    return Result.failure(outputData);
                }
                if (code != 200) {
                    Data outputData = new Data.Builder().putString("errorMessage", getApplicationContext().getString(R.string.failed_to_register)).build();
                    return Result.failure(outputData);
                }

                JSONObject returnedObj = responseBody.getJSONObject("data").getJSONArray("returnedObj").getJSONObject(0);
                DatabaseAccess databaseAccess = DatabaseAccess.getInstance(getApplicationContext());
                databaseAccess.open();

                databaseAccess.addShop(returnedObj.getJSONObject("merchant").getString("companyPhone"), returnedObj.getJSONObject("merchant").getString("currency"), returnedObj.getJSONObject("merchant").getString("companyEmail"), returnedObj.getJSONObject("merchant").getString("name"), returnedObj.getJSONObject("tax").getDouble("percentage"), databaseAccess);
                String authorization = returnedObj.getString("token");
                Data outputData;
                String ecr = returnedObj.getString("ecrCode");
                String merchantId = returnedObj.getJSONObject("merchant").getString("merchantId");
                String logo = returnedObj.getJSONObject("merchant").has("logo") ? returnedObj.getJSONObject("merchant").getString("logo") : "";
                if (!returnedObj.getJSONObject("merchant").has("VATNumber")) {
                    outputData= new Data.Builder().
                            putString("Authorization", authorization).
                            putString("ecrCode", returnedObj.getString("ecrCode")).
                            putBoolean("vatNumberExist",false).
                            build();
                }else{
                    outputData= new Data.Builder().
                            putString("Authorization", authorization).
                            putString("ecrCode", returnedObj.getString("ecrCode")).
                            putBoolean("vatNumberExist",true).
                            build();
                }
                String vatNumber = returnedObj.getJSONObject("merchant").has("VATNumber") ? returnedObj.getJSONObject("merchant").getString("VATNumber") : "";
                databaseAccess.addConfiguration(ecr, merchantId, logo, vatNumber);
                databaseAccess.open();
                return Result.success(outputData);
            } else {
                Data outputData = new Data.Builder().putString("errorMessage", getApplicationContext().getString(R.string.failed_to_register)).build();
                return Result.failure(outputData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}
