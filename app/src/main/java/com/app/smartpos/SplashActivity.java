package com.app.smartpos;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.util.Pair;

import com.androidnetworking.AndroidNetworking;
import com.app.smartpos.auth.AuthActivity;
import com.app.smartpos.checkout.SuccessfulPayment;
import com.app.smartpos.common.Keystore.DecryptionHelper;
import com.app.smartpos.common.Keystore.EncryptionHelper;
import com.app.smartpos.common.Keystore.KeyStoreHelper;
import com.app.smartpos.common.RootUtil;
import com.app.smartpos.common.Utils;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.database.DatabaseOpenHelper;
import com.app.smartpos.utils.BaseActivity;
import com.app.smartpos.utils.Hasher;
import com.app.smartpos.utils.SharedPrefUtils;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.apache.http.auth.AUTH;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

import kotlin.text.Charsets;
import okhttp3.OkHttpClient;


public class SplashActivity extends BaseActivity {


    public static int splashTimeOut = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SQLiteDatabase.loadLibs(this);
        try {
            File f = new File("/data/data/com.app.smartpos/databases/smart_pos");
            SQLiteDatabase db = SQLiteDatabase.openDatabase(f.getPath(), DatabaseOpenHelper.DATABASE_PASSWORD, null,SQLiteDatabase.OPEN_READWRITE);

        } catch (Exception e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_splash);


        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        String endDateString = databaseAccess.getLastShift("end_date_time");
        Utils.addLog("end_date", endDateString);

        disableSSLCertificateChecking();
        AndroidNetworking.initialize(this, getUnsafeOkHttpClient());

        boolean access=(BuildConfig.BUILD_TYPE.equals("debug") || (Settings.Global.getInt(getContentResolver(), Settings.Global.ADB_ENABLED, 0)==0 && !RootUtil.isDeviceRooted()));
        Utils.addLog("datadata_adb",access+" "+ RootUtil.isDeviceRooted());

        if(!access){
            finishAffinity();
        }else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Intent intent = new Intent(SplashActivity.this, SuccessfulPayment.class).putExtra("amount", "100 SAR").putExtra("id", "e123-001-I0000000001");
                    Intent intent = new Intent(SplashActivity.this, AuthActivity.class);
                    Intent intentHome = new Intent(SplashActivity.this, NewHomeActivity.class);
                    Utils.addLog("datadata_login", "" + SharedPrefUtils.getIsLoggedIn(SplashActivity.this));
                    if (SharedPrefUtils.getIsLoggedIn(SplashActivity.this)) {
                        startActivity(intentHome);
                    } else {
                        startActivity(intent);
                    }
                    finish();
                }
            }, splashTimeOut);
        }
        encryptAndStoreApiKey(Constant.API_KEY);
    }

    private OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Disables the SSL certificate checking for new instances of {@link HttpsURLConnection} This has been created to
     * aid testing on a local box, not for use on production.
     */
    public static void disableSSLCertificateChecking() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }
        }};

        try {
            SSLContext sc = SSLContext.getInstance("TLS");

            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


    }

    public void encryptAndStoreApiKey(String apiKey) {
        try {
            // Initialize helpers
            KeyStoreHelper keyStoreHelper = new KeyStoreHelper();
            EncryptionHelper encryptionHelper = new EncryptionHelper();
            DecryptionHelper decryptionHelper=new DecryptionHelper();


            // Get or create the secret key
            SecretKey secretKey = keyStoreHelper.getOrCreateSecretKey();

            // Encrypt the API key
            Pair<byte[], byte[]> encryptedData = encryptionHelper.encrypt("database_password", secretKey);
            //decryptionHelper.decrypt(encryptedData)

            // Save the encrypted API key and IV to SharedPreferences

            // Confirmation

            Utils.addLog("datadata_key",new String(encryptedData.first,"UTF-8").toString());
            Utils.addLog("datadata_api_key",new String(encryptedData.second,"UTF-8"));
            Utils.addLog("datadata_key","API Key encrypted and stored successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            // Handle exceptions
            Utils.addLog("datadata_key","Error encrypting API Key: " + e.getMessage());
        }
    }
}

