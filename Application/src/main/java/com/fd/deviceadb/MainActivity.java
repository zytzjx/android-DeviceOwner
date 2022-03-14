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

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.telephony.TelephonyManager;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FDIAL";
    private static String mSite = "";
    private static String mCompany = "";
    private static String mNFCID = "";

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_real);
//        UserManager mUserManager;

        if (savedInstanceState == null) {
            DevicePolicyManager manager =
                    (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
//            mUserManager = (UserManager) getSystemService(Context.USER_SERVICE);
            if (manager.isDeviceOwnerApp(getApplicationContext().getPackageName())) {
                // This app is set up as the device owner. Show the main features.
                FDLog.d(TAG, "The app is the device owner.");
                showFragment(DeviceOwnerFragment.newInstance());


                manager.setGlobalSetting(
                        // The ComponentName of the device owner
                        DeviceOwnerReceiver.getComponentName(this),
                        // The settings to be set
                        Settings.Global.ADB_ENABLED,
                        // The value we write here is a string representation for SQLite
                        "1");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    manager.setGlobalSetting(
                            // The ComponentName of the device owner
                            DeviceOwnerReceiver.getComponentName(this),
                            // The settings to be set
                            Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                            // The value we write here is a string representation for SQLite
                            "7");
                }


            } else {
                // This app is not set up as the device owner. Show instructions.
                FDLog.d(TAG, "The app is not the device owner.");
                showFragment(InstructionFragment.newInstance());
            }
        }
        Intent intent = getIntent();
        PersistableBundle extras = intent.getParcelableExtra(
                "tags");
        if (extras != null) {
            mCompany = extras.getString("cid", "");
            mSite = extras.getString("sid", "");
            mNFCID = extras.getString("nfid", "");
        }

        //sendPost();
    }


    public void SetAdbEnabled() {
        DevicePolicyManager manager =
                (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        try {
            Settings.Secure.putInt(getApplicationContext().getContentResolver(), Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED, 1);
            Settings.Secure.putInt(getApplicationContext().getContentResolver(), Settings.Global.ADB_ENABLED, 1);
        } catch (Exception e) {
            FDLog.e(TAG, e.toString());
        }
        try {
            try {
                Settings.Secure.putInt(getApplicationContext().getContentResolver(), "user_setup_complete", 1);
                Settings.Global.putInt(getApplicationContext().getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 1);
            } catch (Exception e) {
                FDLog.e(TAG, e.toString());
            }
            if (manager != null) {
                manager.setGlobalSetting(
                        // The ComponentName of the device owner
                        DeviceOwnerReceiver.getComponentName(this),
                        // The settings to be set
                        Settings.Global.ADB_ENABLED,
                        // The value we write here is a string representation for SQLite
                        "1");
                if (Build.VERSION.SDK_INT >= 23)
                    manager.setGlobalSetting(DeviceOwnerReceiver.getComponentName(this), Settings.Global.STAY_ON_WHILE_PLUGGED_IN, "7");
            }
        } catch (Exception e) {
            FDLog.e(TAG, e.toString());
        }
    }

    private void releaseOwnership() {
        try {
            DevicePolicyManager manager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            String PackageName = getPackageName();
            ActivityManager localActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            if (Build.VERSION.SDK_INT >= 23) {
                if (localActivityManager.getLockTaskModeState() != 0)
                    stopLockTask();
            } else if ((Build.VERSION.SDK_INT < 23) && (localActivityManager.isInLockTaskMode()))
                stopLockTask();
            if (manager.isDeviceOwnerApp(PackageName)) {
                ComponentName componentName = DeviceOwnerReceiver.getComponentName(getApplicationContext());
                if (Build.VERSION.SDK_INT >= 24)
                    FDLog.d("OwnerRemover", "Clearing profile owner");
                FDLog.d("OwnerRemover", "Clearing device owner");
                manager.removeActiveAdmin(componentName);
                manager.clearDeviceOwnerApp(PackageName);
                FDLog.d("OwnerRemover", "Device owner cleared succesfully");
            } else {
                FDLog.d("OwnerRemover", "App is not device owner");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void sendPost() {
        FDLog.d("FDAIL", "sendPost++");
        final Context context = this;
        @SuppressLint("SimpleDateFormat") Thread thread = new Thread(() -> {
            //downloadapk();
            try {
                HttpsTrustManager.allowAllSSL();
                URL url = new URL("http://cmc.futuredial.com/ws/insert/");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("uuid", UUID.randomUUID().toString().replace("-", ""));
                jsonParam.put("timeCreated", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSS'Z'").format(new Date()));
                jsonParam.put("StartTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                jsonParam.put("site", mSite);
                jsonParam.put("company", mCompany);
                jsonParam.put("operator", "");
                jsonParam.put("productid", "34");
                jsonParam.put("errorCode", "1");
                jsonParam.put("nfcid", mNFCID);
                TelephonyManager mTelephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String sn = null;
//                try {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
//                            sn = mTelephony.getImei();
//                        }else{
//                            sn=mTelephony.getDeviceId();
//                        }
//
//                    } else {
//                        sn = mTelephony.getDeviceId();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                jsonParam.put("esnNumber", sn == null ? "" : sn);

                String address = WifiAddress.getMacAddress(getApplicationContext());
                jsonParam.put("MacAddress", address);
                jsonParam.put("sourceMake", Build.MANUFACTURER);
                jsonParam.put("sourceModel", Build.MODEL);
                jsonParam.put("serialnumber", Build.SERIAL);
                jsonParam.put("AndroidVersion", Build.VERSION.RELEASE);
                jsonParam.put("buildnumber", Build.DISPLAY);

                FDLog.i("JSON", jsonParam.toString());
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(jsonParam.toString());

                os.flush();
                os.close();

                FDLog.d("FDAIL", String.valueOf(conn.getResponseCode()));
                FDLog.d("FDAIL", conn.getResponseMessage());
                SetAdbEnabled();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            WifiAddress.RemoveWifi(context);
            releaseOwnership();

            //WifiManager wifi = (WifiManager)getApplicationContext()
            // .getSystemService(Context.WIFI_SERVICE);
            //wifi.setWifiEnabled(false);
            finish();
        });

        thread.start();
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

}
