package com.app.smartpos.settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.app.smartpos.R;
import com.app.smartpos.settings.Synchronization.DataBaseBackupActivity;
import com.app.smartpos.settings.backup.BackupActivity;
import com.app.smartpos.settings.categories.CategoriesActivity;
import com.app.smartpos.settings.end_shift.EndShiftDialog;
import com.app.smartpos.settings.end_shift.EndShiftModel;
import com.app.smartpos.settings.end_shift.EndShiftReportDialog;
import com.app.smartpos.settings.order_type.OrderTypeActivity;
import com.app.smartpos.settings.payment_method.PaymentMethodActivity;
import com.app.smartpos.settings.shop.ShopInformationActivity;
import com.app.smartpos.settings.unit.UnitActivity;
import com.app.smartpos.utils.BaseActivity;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class SettingsActivity extends BaseActivity {


    CardView cardShopInfo, cardBackup,cardCategory,cardPaymentMethod,cardOrderType,cardUnit,cardLogout,DBBackup;

    private InterstitialAd mInterstitialAd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setHomeButtonEnabled(true); //for back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//for back button
        getSupportActionBar().setTitle(R.string.action_settings);


        cardShopInfo = findViewById(R.id.card_shop_info);
        cardBackup = findViewById(R.id.card_backup);
        DBBackup = findViewById(R.id.db_backup);

        cardCategory=findViewById(R.id.card_category);
        cardPaymentMethod=findViewById(R.id.card_payment_method);
        cardOrderType=findViewById(R.id.card_order_type);
        cardUnit=findViewById(R.id.card_unit);
        cardLogout=findViewById(R.id.card_logout);


        MobileAds.initialize(this, initializationStatus -> {
        });


        AdRequest adRequest = new AdRequest.Builder().build();
        //ca-app-pub-3940256099942544/1033173712
        InterstitialAd.load(this, getString(R.string.admob_interstitial_ads_id), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        //Utils.addLog(TAG, "onAdLoaded");
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                Log.d("TAG", "The ad was dismissed.");
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when fullscreen content failed to show.
                                Log.d("TAG", "The ad failed to show.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                mInterstitialAd = null;
                                Log.d("TAG", "The ad was shown.");
                            }
                        });

                        if (mInterstitialAd != null) {
                            mInterstitialAd.show(SettingsActivity.this);
                        } else {
                            Log.d("TAG", "The interstitial ad wasn't ready yet.");
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        //Utils.addLog(TAG, loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });




        cardShopInfo.setOnClickListener(v -> {

            Intent intent = new Intent(SettingsActivity.this, ShopInformationActivity.class);
            startActivity(intent);
        });



        cardCategory.setOnClickListener(v -> {
            Intent intent=new Intent(SettingsActivity.this, CategoriesActivity.class);
            startActivity(intent);
        });


        cardOrderType.setOnClickListener(v -> {
            Intent intent=new Intent(SettingsActivity.this, OrderTypeActivity.class);
            startActivity(intent);
        });

        cardUnit.setOnClickListener(v -> {
            Intent intent=new Intent(SettingsActivity.this, UnitActivity.class);
            startActivity(intent);
        });


        cardPaymentMethod.setOnClickListener(v -> {

            Intent intent = new Intent(SettingsActivity.this, PaymentMethodActivity.class);
            startActivity(intent);
        });


        cardBackup.setOnClickListener(v -> {

            Intent intent = new Intent(SettingsActivity.this, BackupActivity.class);
            startActivity(intent);
        });

        cardLogout.setOnClickListener(v -> {
            EndShiftDialog dialog=new EndShiftDialog();
            dialog.show(getSupportFragmentManager(),"end shift dialog");
        });

        DBBackup.setOnClickListener(v -> {

            Intent intent = new Intent(SettingsActivity.this, DataBaseBackupActivity.class);
            startActivity(intent);
        });

    }

    public void openReport(EndShiftModel model){
        EndShiftReportDialog reportDialog=new EndShiftReportDialog();
        reportDialog.setEndShiftModel(model);
        reportDialog.show(getSupportFragmentManager(),"dialog");
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
}
