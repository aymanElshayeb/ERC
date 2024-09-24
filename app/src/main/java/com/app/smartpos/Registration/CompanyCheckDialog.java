package com.app.smartpos.Registration;

import static com.app.smartpos.Constant.CHECK_COMPANY_URL;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.app.smartpos.NewHomeActivity;
import com.app.smartpos.R;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.utils.Hasher;
import com.app.smartpos.utils.LocaleManager;
import com.app.smartpos.utils.SharedPrefUtils;

public class CompanyCheckDialog extends DialogFragment {

    private static final int STORAGE_PERMISSION_CODE = 23;
    View root;
    EditText usernameEt;
    EditText passwordEt;
    EditText merchantEt;
    ProgressBar progressBar;
    Button registerBtn;
    Button demoBtn;

    private String deviceId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (root == null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            root = inflater.inflate(R.layout.company_check_registration, container, false);
            setCancelable(false);

            deviceId = Settings.Secure.getString(getContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);

            usernameEt = root.findViewById(R.id.username_et);
            registerBtn = root.findViewById(R.id.register_btn);
            demoBtn = root.findViewById(R.id.demo_btn);
            progressBar = root.findViewById(R.id.progress);
            String lang = LocaleManager.getLanguage(requireActivity());

            usernameEt.setGravity((lang.equals("ar") ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);

            ConstraintLayout languageCl = root.findViewById(R.id.language_cl);
            registerBtn.setOnClickListener(view -> {
                if (usernameEt.getText().toString().isEmpty()) {
                    Toast.makeText(requireActivity(), requireContext().getResources().getString(R.string.user_name_empty), Toast.LENGTH_SHORT).show();
                } else {
                    registerBtn.setVisibility(View.GONE);
                    demoBtn.setEnabled(false);
                    enqueueDownloadAndReadWorkers();
                }
            });
            String language = LocaleManager.getLanguage(root.getContext());

            languageCl.setOnClickListener(view -> {
                LocaleManager.updateLocale(root.getContext(), language.equals("en") ? "ar" : "en");
                LocaleManager.resetApp(getActivity());
            });
            demoBtn.setOnClickListener(view -> {
                DatabaseAccess databaseAccess = DatabaseAccess.getInstance(requireContext());
                databaseAccess.open();
                databaseAccess.addDemoConfiguration();
                SharedPrefUtils.setStartDateTime(requireActivity());
                SharedPrefUtils.setDemoPressed(requireActivity(), true);
                requireActivity().finish();
                requireActivity().startActivity(new Intent(requireActivity(), NewHomeActivity.class));
            });

            requestForStoragePermissions();
        }

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.9);
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }

    private void requestForStoragePermissions() {
        //Android is 11 (R) or above
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) || Build.VERSION.SDK_INT >= 35) {
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        requireActivity(),
                        new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        },
                        STORAGE_PERMISSION_CODE
                );
            }

        }

    }


    private void enqueueDownloadAndReadWorkers() {
        //username Admin
        //password 01111Mm&
        Data register = new Data.Builder().
                putString("url", CHECK_COMPANY_URL).
                putString("email", usernameEt.getText().toString()).
                putString("deviceId", deviceId).
                build();


        OneTimeWorkRequest registerRequest = new OneTimeWorkRequest.Builder(CheckCompanyWorker.class)
                .setInputData(register)
                .build();


        WorkContinuation continuation = WorkManager.getInstance(requireActivity())
                .beginWith(registerRequest);

        continuation.enqueue();
        observeWorker(registerRequest);

    }

    private void handleWorkCompletion(WorkInfo workInfo) {
        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
            // Work succeeded, handle success
            showMessage("Registration Successful");

            SharedPrefUtils.setStartDateTime(requireContext());
            byte[] bytes = Hasher.encryptMsg(usernameEt.getText().toString().trim() + "-" + passwordEt.getText().toString().trim());
            SharedPrefUtils.setAuthData(requireContext(), bytes);
            closePendingScreen();
        } else if (workInfo.getState() == WorkInfo.State.FAILED) {
            // Work failed, handle failure
            showMessage("Error in Syncing data");
            registerBtn.setVisibility(View.VISIBLE);
            demoBtn.setEnabled(true);

        }
    }

    private void closePendingScreen() {
        dismissAllowingStateLoss();
    }

    private void observeWorker(OneTimeWorkRequest workRequest) {
        WorkManager.getInstance(requireActivity()).getWorkInfoByIdLiveData(workRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        if (workInfo.getState() == WorkInfo.State.FAILED) {
                            String errorMessage = workInfo.getOutputData().getString("errorMessage");
                            showMessage((errorMessage != null ? errorMessage : "Unknown error occurred"));
                        }
                    }
                });
    }

    private void showMessage(String message) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
    }

}
