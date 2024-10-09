package com.app.smartpos.auth;

import static com.app.smartpos.Constant.API_KEY;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.app.smartpos.R;
import com.app.smartpos.utils.SSLUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginWithServerWorker extends Worker {

    public LoginWithServerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Perform login with server logic here
        String email = getInputData().getString("email");
        String password = getInputData().getString("password");
        String tenantId= getInputData().getString("tenantId");
        String urlString=getInputData().getString("url");

        if (email == null || password == null || tenantId==null) {
            return Result.failure();
        }

        OkHttpClient client = getUnsafeOkHttpClient();

        FormBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("password",password)
                .build();
        Headers headers=new Headers.Builder().
                add("tenantId", tenantId).
                add("apikey", API_KEY).

                build();
        Request request = new Request.Builder()
                .url(urlString)
                .post(formBody)
                .headers(headers)
                .build();
        try(Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String authorization=response.header("Authorization");
                Data outputData = new Data.Builder().putString("Authorization", authorization).build();
                return Result.success(outputData);
            } else {
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
