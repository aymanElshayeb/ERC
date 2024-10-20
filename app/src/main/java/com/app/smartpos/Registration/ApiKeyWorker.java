package com.app.smartpos.Registration;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.app.smartpos.R;
import com.app.smartpos.common.Keystore.EncryptionHelper;
import com.app.smartpos.common.Keystore.KeyStoreHelper;
import com.app.smartpos.common.Utils;
import com.app.smartpos.utils.SharedPrefUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiKeyWorker extends Worker {

    public ApiKeyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Perform login with server logic here
        String email = getInputData().getString("email");
        String password = getInputData().getString("password");
        String tenantId = getInputData().getString("tenantId");
        String urlString = getInputData().getString("url");

        if (email == null || password == null || tenantId == null) {
            return Result.failure();
        }

        OkHttpClient client = getUnsafeOkHttpClient();

        FormBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .build();
        Headers headers = new Headers.Builder().
                add("tenantId", tenantId).
                build();

        Request request = new Request.Builder()
                .url(urlString)
                .post(formBody)
                .headers(headers)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                Data.Builder outputData = new Data.Builder();
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    if(jsonObject.getInt("code")==200) {
                        JSONObject body = jsonObject.getJSONObject("data").getJSONArray("returnedObj").getJSONObject(0);

                        String apikey = body.getString("apikey");
                        String password_response = new String(Base64.decode(body.getString("password"), Base64.DEFAULT), StandardCharsets.UTF_8);

                        Pair<byte[],byte[]>encryption=new EncryptionHelper().encrypt(apikey,new KeyStoreHelper().getOrCreateSecretKey());
                        byte[] first = encryption.first;
                        byte[] second = encryption.second;

                        String encryptedApiKey = Base64.encodeToString(second, Base64.DEFAULT);
                        String encryptedVector = Base64.encodeToString(first, Base64.DEFAULT);

                        SharedPrefUtils.setApiVector(encryptedVector);
                        SharedPrefUtils.setApiKey(encryptedApiKey);

                        String encryptedPassword = new String(new EncryptionHelper().encrypt(password_response,new KeyStoreHelper().getOrCreateSecretKey()).second, StandardCharsets.UTF_8);
                        SharedPrefUtils.setDatabasePassword(encryptedPassword);
                        outputData.putString("apikey", apikey).putString("database_password", password_response);
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
                return Result.success(outputData.build());
            } else {
                Utils.addLog("datadata_error",response.body().toString());
                Data outputData = new Data.Builder().putString("errorMessage", getApplicationContext().getString(R.string.failed_to_login)).build();
                return Result.failure(outputData);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Result.failure();
        }
    }

    private OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final javax.net.ssl.SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
