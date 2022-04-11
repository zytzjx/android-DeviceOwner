/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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