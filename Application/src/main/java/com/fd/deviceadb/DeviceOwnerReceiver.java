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

import android.annotation.TargetApi;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.widget.Toast;

import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE;

/**
 * Handles events related to device owner.
 */
public class DeviceOwnerReceiver extends DeviceAdminReceiver {

    private static DevicePolicyManager manager;
    private static ComponentName componentName;
    final Handler ServiceInfo = new Handler();


    private void StartInfoSrv(final Context context, final  PersistableBundle extras)
    {
        ServiceInfo.postDelayed(new Runnable()
           {
               public void run()
               {
                   Intent localIntent = new Intent(context, InfoService.class);
                   if(extras!=null){
                       localIntent.putExtra("tags", extras);
                   }
                   if (Build.VERSION.SDK_INT >= 26)
                   {
                       context.startForegroundService(localIntent);
                       return;
                   }
                   context.startService(localIntent);
               }
           }
, 100L);
    }

    private void StartActivity(final Context context, final  PersistableBundle extras)
    {
        ServiceInfo.postDelayed(new Runnable()
            {
                public void run()
                {

                    Intent launch = new Intent(context, MainActivity.class);
                    if(extras!=null){
                        launch.putExtra("tags", extras);
                    }
                    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(launch);
                }
            }
, 100L);
    }


    /**
     * Called on the new profile when device owner provisioning has completed. Device owner
     * provisioning is the process of setting up the device so that its main profile is managed by
     * the mobile device management (MDM) application set up as the device owner.
     */
    @Override
    public void onProfileProvisioningComplete(Context context, Intent intent) {
        // Enable the profile
        if(manager==null) {
            manager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        }
        if(componentName==null) {
            componentName = getComponentName(context);
        }
        if(!manager.isDeviceOwnerApp(context.getPackageName())){
           super.onProfileProvisioningComplete(context, intent);
           return;
        }

            // EnablePermission(context);
        manager.setProfileName(componentName, context.getString(R.string.profile_name));


        Toast.makeText(context, "adb complete", Toast.LENGTH_LONG).show();
        // Open the main screen
        PersistableBundle extras = intent.getParcelableExtra(
                EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE);

        StartInfoSrv(context, extras);
//        Intent launch = new Intent(context, MainActivity.class);
//        if(extras!=null){
//            launch.putExtra("tags", extras);
//        }
//        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(launch);
    }

    /**
     * @return A newly instantiated {@link android.content.ComponentName} for this
     * DeviceAdminReceiver.
     */
    public static ComponentName getComponentName(Context context) {
        return new ComponentName(context.getApplicationContext(), DeviceOwnerReceiver.class);
    }

    @TargetApi(23)
    public static void EnablePermission(Context context)
    {
        if(manager==null) {
            manager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        }
        if(componentName==null) {
            componentName = getComponentName(context);
        }
        try {
            if(manager.isAdminActive(componentName)) {
                manager.setPermissionGrantState(componentName, context.getPackageName(), "android.permission.READ_PHONE_STATE", 1);
                manager.setPermissionGrantState(componentName, context.getPackageName(), "android.permission.WRITE_EXTERNAL_STORAGE", 1);
                manager.setPermissionGrantState(componentName, context.getPackageName(), "android.permission.READ_EXTERNAL_STORAGE", 1);
                manager.setPermissionGrantState(componentName, context.getPackageName(), "android.permission.RECORD_AUDIO", 1);
                manager.setPermissionGrantState(componentName, context.getPackageName(), "android.permission.CAMERA", 1);
                manager.setPermissionGrantState(componentName, context.getPackageName(), "android.permission.ACCESS_FINE_LOCATION", 1);
                manager.setPermissionGrantState(componentName, context.getPackageName(), "android.permission.WRITE_SETTINGS", 1);
                manager.setPermissionGrantState(componentName, context.getPackageName(), "android.permission.WRITE_SECURE_SETTINGS", 1);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        if(manager==null) {
            manager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        }
        if(componentName==null) {
            componentName = getComponentName(context);
        }
        //EnablePermission(context);
        //PersistableBundle extras = intent.getParcelableExtra(EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE);
        //StartInfoSrv(context,extras);
        if(manager.isDeviceOwnerApp(context.getPackageName())) {
            manager.setSecureSetting(componentName, Settings.Secure.SKIP_FIRST_USE_HINTS, "1");
            manager.setGlobalSetting(componentName, Settings.Global.ADB_ENABLED, "1");
            Toast.makeText(context, "adb opened1", Toast.LENGTH_LONG).show();
        }
        //StartActivity(context, extras);
        super.onEnabled(context, intent);
    }
}
