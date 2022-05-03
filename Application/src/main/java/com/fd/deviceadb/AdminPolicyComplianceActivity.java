package com.fd.deviceadb;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE;

public class AdminPolicyComplianceActivity extends AppCompatActivity {

    private final String TAG = "AdminPolicyCompliance";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FDLog.d(TAG + " => onCreate");
        setContentView(R.layout.activity_admin_policy_compliance);

        Intent intent = getIntent();

        PersistableBundle extras = intent.getParcelableExtra(
                EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE);

        FDLog.d(TAG + " => onCreate -> extras = " + extras);

        setResult(RESULT_OK, intent);

        FDLog.d(TAG + " => onCreate -> end");

        finish();
    }

    @Override
    protected void onDestroy() {
        FDLog.d(TAG + " => onDestroy");
        super.onDestroy();
    }
}