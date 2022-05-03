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
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.widget.Toast;

import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE;

/**
 * Handles events related to device owner.
 */
public class DeviceOwnerReceiver extends DeviceAdminReceiver {

    private static DevicePolicyManager manager = null;
    private static ComponentName componentName = null;
    private final Handler ServiceInfo = new Handler(Looper.getMainLooper());
    private static final String TAG = "DeviceOwnerReceiver";


    private void StartInfoSrv(final Context context, final PersistableBundle extras) {
        FDLog.d("StartInfoSrv ++");
        ServiceInfo.postDelayed(() -> {
            Intent localIntent = new Intent(context, InfoService.class);
            if (extras != null) {
                localIntent.putExtra("tags", extras);
            }
            if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(localIntent);
                //return;
            } else
            	context.startService(localIntent);
        }
                , 100L);
        FDLog.d("StartInfoSrv --");
    }

    private void StartActivity(final Context context, final PersistableBundle extras) {
        ServiceInfo.postDelayed(() -> {

            Intent launch = new Intent(context, MainActivity.class);
            if (extras != null) {
                launch.putExtra("tags", extras);
            }
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launch);
        }
                , 100L);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        FDLog.d("receive: "+ intent.getAction());
        switch (intent.getAction()) {
            case Intent.ACTION_BOOT_COMPLETED:
                FDLog.d("ACTION_BOOT_COMPLETED");
                break;
            case DevicePolicyManager.ACTION_PROFILE_OWNER_CHANGED:
                FDLog.d("ACTION_PROFILE_OWNER_CHANGED");
                break;
            case DevicePolicyManager.ACTION_DEVICE_OWNER_CHANGED:
                FDLog.d("ACTION_DEVICE_OWNER_CHANGED");
                break;
            case DevicePolicyManager.ACTION_GET_PROVISIONING_MODE:
                FDLog.d("ACTION_GET_PROVISIONING_MODE");
                break;
            default:
            	super.onReceive(context, intent);
        }
    }


    /**
     * Called on the new profile when device owner provisioning has completed. Device owner
     * provisioning is the process of setting up the device so that its main profile is managed by
     * the mobile device management (MDM) application set up as the device owner.
     */
    @Override
    public void onProfileProvisioningComplete(Context context, Intent intent) {
        FDLog.d("onProfileProvisioningComplete ++");

        if (Build.VERSION.SDK_INT < 31) {
            if (manager == null) {
                manager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            }
            if (componentName == null) {
                componentName = getComponentName(context);
            }
            if (!manager.isDeviceOwnerApp(context.getPackageName())) {
                super.onProfileProvisioningComplete(context, intent);
                return;
            }

            EnablePermission(context);
            manager.setProfileName(componentName, context.getString(R.string.profile_name));


            Toast.makeText(context, "adb complete", Toast.LENGTH_LONG).show();
            // Open the main screen
            PersistableBundle extras = intent.getParcelableExtra(
                    EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE);

            StartInfoSrv(context, extras);
        }
        super.onProfileProvisioningComplete(context, intent);
    }

    /**
     * @return A newly instantiated {@link android.content.ComponentName} for this
     * DeviceAdminReceiver.
     */
    public static ComponentName getComponentName(Context context) {
        FDLog.d("getComponentName ++");
        return new ComponentName(context.getApplicationContext(), DeviceOwnerReceiver.class);
    }

    public static void EnablePermission(Context context) {
        FDLog.d("EnablePermission ++");
        if (manager == null) {
            manager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        }
        if (componentName == null) {
            componentName = getComponentName(context);
        }
        try {
            if (manager.isAdminActive(componentName)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    manager.setPermissionGrantState(componentName, context.getPackageName(),
                            Manifest.permission.READ_PHONE_STATE,
                            DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);
                    manager.setPermissionGrantState(componentName, context.getPackageName(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);

                    manager.setPermissionGrantState(componentName, context.getPackageName(),
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);

                    manager.setPermissionGrantState(componentName, context.getPackageName(),
                            Manifest.permission.RECORD_AUDIO,
                            DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);

                    manager.setPermissionGrantState(componentName, context.getPackageName(),
                            Manifest.permission.CAMERA,
                            DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);


                    manager.setPermissionGrantState(componentName, context.getPackageName(),
                            Manifest.permission.WRITE_SETTINGS,
                            DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);

                    manager.setPermissionGrantState(componentName, context.getPackageName(),
                            Manifest.permission.WRITE_SECURE_SETTINGS,
                            DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (manager.isAdminActive(componentName)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    manager.setPermissionGrantState(componentName, context.getPackageName(),
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);

                    manager.setPermissionGrantState(componentName, context.getPackageName(),
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        manager.setPermissionGrantState(componentName, context.getPackageName(),
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                                DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        FDLog.d("EnablePermission --");
    }

   @Override
   public void onDisabled (Context context,
                           Intent intent){
        FDLog.d("Admin disabled");
   }

    @Override
    public void onEnabled(Context context, Intent intent) {
        FDLog.d("Admin Enabled ++");
        if (manager == null) {
            manager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        }
        if (componentName == null) {
            componentName = getComponentName(context);
        }
        FDLog.d("call EnablePermission ++");
        EnablePermission(context);
        FDLog.d("call EnablePermission --");
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                manager.setGlobalSetting(DeviceOwnerReceiver.getComponentName(context), "stay_on_while_plugged_in", "7");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        //manager.setGlobalSetting(componentName, Settings.Global.ADB_ENABLED, "1");
        Toast.makeText(context, "adb opened task start", Toast.LENGTH_LONG).show();
        //PersistableBundle extras = intent.getParcelableExtra(EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE);
        //StartInfoSrv(context,extras);
        if (manager.isDeviceOwnerApp(context.getPackageName())) {
            try {
                manager.setSecureSetting(componentName, Settings.Secure.SKIP_FIRST_USE_HINTS, "1");
            }catch (Exception e){
                e.printStackTrace();
            }
            //manager.setGlobalSetting(componentName, Settings.Global.ADB_ENABLED, "1");
            //Toast.makeText(context, "adb opened1", Toast.LENGTH_LONG).show();
        }

        if (Build.VERSION.SDK_INT >= 31) {
            if (!manager.isDeviceOwnerApp(context.getPackageName())) {
                FDLog.d("Admin Enabled, isDeviceOwnerApp failed");
            }

            manager.setProfileName(componentName, context.getString(R.string.profile_name));
            // Open the main screen
            PersistableBundle extras = intent.getParcelableExtra(
                    EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE);

            FDLog.d("Admin Enabled:StartInfoSrv");
            StartInfoSrv(context, extras);
        }

        super.onEnabled(context, intent);
        FDLog.d("Admin Enabled --");
    }
}
