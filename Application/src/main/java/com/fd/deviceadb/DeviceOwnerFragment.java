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

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Demonstrates the usage of the most common device management APIs for the device owner case.
 * In addition to various features available for profile owners, device owners can perform extra
 * actions, such as configuring global settings and enforcing a preferred Activity for a specific
 * IntentFilter.
 */
public class DeviceOwnerFragment extends Fragment {
    private DevicePolicyManager mDevicePolicyManager;
    /**
     * @return A newly instantiated {@link DeviceOwnerFragment}.
     */
    public static DeviceOwnerFragment newInstance() {
        return new DeviceOwnerFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return null;
        //return inflater.inflate(R.layout.fragment_device_owner, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Retain references
    }

    @Override
    public void onAttach(Context context) {
        FDLog.d("FDIAL","onAttach");
        super.onAttach(context);
        mDevicePolicyManager =
                (DevicePolicyManager) context.getSystemService(Activity.DEVICE_POLICY_SERVICE);
        setBooleanGlobalSetting(Settings.Global.ADB_ENABLED, true);
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.O)
        {
            mDevicePolicyManager.setSecureSetting(
                    // The ComponentName of the device owner
                    DeviceOwnerReceiver.getComponentName(getActivity()),
                    // The settings to be set
                    Settings.Secure.INSTALL_NON_MARKET_APPS,
                    // The value we write here is a string representation for SQLite
                    "1");
        }else {
            mDevicePolicyManager.clearUserRestriction(DeviceOwnerReceiver.getComponentName(getActivity()),
                    UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES);
        }

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            mDevicePolicyManager.setGlobalSetting(
                    // The ComponentName of the device owner
                    DeviceOwnerReceiver.getComponentName(getActivity()),
                    // The settings to be set
                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                    // The value we write here is a string representation for SQLite
                    "7");
        }

    }



    /**
     * Retrieves the current boolean value of the specified global setting.
     *
     * @param resolver The ContentResolver
     * @param setting  The setting to be retrieved
     * @return The current boolean value
     */
    private static boolean getBooleanGlobalSetting(ContentResolver resolver, String setting) {
        return 0 != Settings.Global.getInt(resolver, setting, 0);
    }

    /**
     * Sets the boolean value of the specified global setting.
     *
     * @param setting The setting to be set
     * @param value   The value to be set
     */
    private void setBooleanGlobalSetting(String setting, boolean value) {
        mDevicePolicyManager.setGlobalSetting(
                // The ComponentName of the device owner
                DeviceOwnerReceiver.getComponentName(getActivity()),
                // The settings to be set
                setting,
                // The value we write here is a string representation for SQLite
                value ? "1" : "0");
    }

    @Override
    public void onDetach() {
        mDevicePolicyManager=null;
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

}
