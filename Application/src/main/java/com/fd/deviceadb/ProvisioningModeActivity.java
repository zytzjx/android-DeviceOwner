package com.fd.deviceadb;

import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE;

public class ProvisioningModeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provisioning_mode);

//        FDLog.d("PolicyComplianceActivity => onCreate");
        Intent intent = getIntent();
        int provisioningMode = 1;
        List<Integer> allowedProvisioningModes = intent.getIntegerArrayListExtra(DevicePolicyManager.EXTRA_PROVISIONING_ALLOWED_PROVISIONING_MODES);

        if (allowedProvisioningModes.contains(DevicePolicyManager.PROVISIONING_MODE_FULLY_MANAGED_DEVICE)) {
            provisioningMode = DevicePolicyManager.PROVISIONING_MODE_FULLY_MANAGED_DEVICE;
//            FDLog.d("PolicyComplianceActivity => PROVISIONING_MODE_FULLY_MANAGED_DEVICE");

        } else if (allowedProvisioningModes.contains(DevicePolicyManager.PROVISIONING_MODE_MANAGED_PROFILE)) {
//            FDLog.d("PolicyComplianceActivity => PROVISIONING_MODE_MANAGED_PROFILE");
            provisioningMode = DevicePolicyManager.PROVISIONING_MODE_MANAGED_PROFILE;
        }
        PersistableBundle extras = intent.getParcelableExtra(
                EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE);

        Intent resultIntent = getIntent();

//        FDLog.d("PolicyComplianceActivity => onCreate - extras = " + extras);
        if (extras != null)
            resultIntent.putExtra(EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE, extras);
        resultIntent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_MODE, provisioningMode);

        setResult(RESULT_OK, resultIntent);
//        FDLog.d("PolicyComplianceActivity => onCreate - end");
        finish();
    }

    @Override
    protected void onDestroy() {
//        FDLog.d("PolicyComplianceActivity" + " => onDestroy");
        super.onDestroy();
    }
}
