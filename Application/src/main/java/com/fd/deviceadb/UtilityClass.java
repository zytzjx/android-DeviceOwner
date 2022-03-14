package com.fd.deviceadb;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;

public class UtilityClass {

    public static final String NOTIFICATION_CHANNEL_ID = "MVDACTIVE_KONX_DEVICE";
    private static final String NOTIFICATION_CHANNEL_NAME = "MVDACTIVE_KONX_DEVICE";
    private static final String NOTIFICATION_CHANNEL_DESCRIPTION = "This notification will be displayed when device is ready after setup done.";
    public static final String notiKnoxClearContentTitle = "Knox is cleared";
    public static final String notiKnoxClearContentText = "Device is ready to use.";
    public static final String notiKnoxNotClearContentTitle = "Knox is active";
    public static final String notiKnoxNotClearContentText = "Device is not ready to use.";
    public static final int NOTIFICATION_ID = 8001;
    private  static String TAG="deviceadb";
    public static void setNotificationChannel(NotificationManager mNotifyMgr) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = mNotifyMgr.getNotificationChannel(NOTIFICATION_CHANNEL_ID);
            if (channel == null) {
                channel = new NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        NOTIFICATION_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription(NOTIFICATION_CHANNEL_DESCRIPTION);
                mNotifyMgr.createNotificationChannel(channel);
            }
        }
    }



    public static boolean isNotificationVisible(Context context) {

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();
            for (StatusBarNotification notification : notifications) {
                if (notification.getId() == NOTIFICATION_ID) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isServiceRunning(Context context, String serviceClassName){
        final ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            FDLog.d(TAG,"services class name : "+runningServiceInfo.service.getClassName());
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)){
                return true;
            }
        }
        return false;
    }

    public static boolean isPackageExist(String targetPackage, Context mContext) {
        List<ApplicationInfo> packages;
        PackageManager pm;

        pm = mContext.getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equalsIgnoreCase(targetPackage))
                return true;
        }
        return false;
    }


    public static boolean isWifiEnabled(Context context) {
        WifiManager wifi = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (wifi != null) {
            return wifi.isWifiEnabled();
        }
        return false;
    }

    public static boolean isWifiConnected(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

            if (wifiInfo.getNetworkId() == -1) {
                return false; // Not connected to an access point
            }
            return true; // Connected to an access point
        } else {
            return false; // Wi-Fi adapter is OFF
        }
    }

    public static boolean isPreloaded(ApplicationInfo info) {
        return info != null && (info.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    public static boolean isPreloadedUpdate(ApplicationInfo info) {
        return info != null
                && (info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
    }



    public static void setBooleanGlobalSetting(Context context, String setting, boolean value) {
        DevicePolicyManager mDevicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDevicePolicyManager.setGlobalSetting(DeviceOwnerReceiver.getComponentName(context), setting, value ? "1" : "0");
    }


    public static void enableADBWithDeviceManager(Context context) {
        DevicePolicyManager mDevicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        try {
            try {
                Settings.Secure.putInt(context.getApplicationContext().getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1);
                Settings.Secure.putInt(context.getApplicationContext().getContentResolver(), Settings.Global.ADB_ENABLED, 1);
            } catch (Exception e) {
                FDLog.d(TAG,"enableADBWithDeviceManager exception  >>>> " + e.getMessage());
            }
            try {


                Settings.Secure.putInt(context.getApplicationContext().getContentResolver(), "user_setup_complete", 1);
                Settings.Global.putInt(context.getApplicationContext().getContentResolver(), "device_provisioned", 1);
            } catch (Exception e) {
                FDLog.d(TAG,e.getMessage());
            }
            if (mDevicePolicyManager != null) {
                UtilityClass.setBooleanGlobalSetting(context, Settings.Global.ADB_ENABLED, true);
                if (Build.VERSION.SDK_INT >= 23) {
                    mDevicePolicyManager.setGlobalSetting(DeviceOwnerReceiver.getComponentName(context), Settings.Global.STAY_ON_WHILE_PLUGGED_IN, "7");
                }
            }

            UtilityClass.setBooleanGlobalSetting(context, Settings.Global.WIFI_DEVICE_OWNER_CONFIGS_LOCKDOWN, false);
            //  mDevicePolicyManager.setGlobalSetting(UtilityClass.getComponentName(context), Settings.Global.WIFI_DEVICE_OWNER_CONFIGS_LOCKDOWN, "0");


        } catch (Exception e) {

        }
    }


    public static void enableApps(Context context) {
        Log.d(TAG,"enableApps called    ");
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName cName = DeviceOwnerReceiver.getComponentName(context);
        dpm.setApplicationHidden(cName, "com.google.android.setupwizard", false);
        dpm.enableSystemApp(cName, "com.google.android.setupwizard");
    }


    public static void disableDeviceAdminOwnerPrivilage(Context context) {
        Log.d(TAG,"disableDeviceAdminOwnerPrivilage started ");
        //  enableApps(context);
        try {
            Log.d(TAG,"disableDeviceAdminOwnerPrivilage called ");
            ComponentName componentName = DeviceOwnerReceiver.getComponentName(context);
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            if (dpm.isDeviceOwnerApp("com.example.placeholder.test")) {
                FDLog.d(TAG,"Revoked all the privilages");
                dpm.removeActiveAdmin(componentName);
                dpm.clearDeviceOwnerApp("com.example.placeholder.test");
            }
            if (dpm != null && dpm.isAdminActive(DeviceOwnerReceiver.getComponentName(context))) {
                dpm.removeActiveAdmin(componentName);
                dpm.clearDeviceOwnerApp("com.example.placeholder.test");
                FDLog.d(TAG,"Revoked Admin previlages");
            }

        } catch (Exception e) {
            FDLog.e(TAG,"Exception while revoke admin privilage " + e.getMessage());
        }
    }


    public static void killAppBypackage(String packageTokill, Context context) {
        try {
            List<ApplicationInfo> packages;
            PackageManager pm;
            pm = context.getPackageManager();
            packages = pm.getInstalledApplications(0);
            ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ApplicationInfo packageInfo : packages) {
                if (packageInfo.packageName.equals(packageTokill)) {
                    Intent startMain = new Intent(Intent.ACTION_MAIN);
                    startMain.addCategory(Intent.CATEGORY_HOME);
                    startMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(startMain);
                    mActivityManager.killBackgroundProcesses(packageInfo.packageName);
                    break;
                }
            }
            ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> activityes = ((ActivityManager) manager).getRunningAppProcesses();
            for (int iCnt = 0; iCnt < activityes.size(); iCnt++) {
                System.out.println("APP: " + iCnt + " " + activityes.get(iCnt).processName);
                if (activityes.get(iCnt).processName.contains(packageTokill)) {
                    android.os.Process.sendSignal(activityes.get(iCnt).pid, android.os.Process.SIGNAL_KILL);
                    android.os.Process.killProcess(activityes.get(iCnt).pid);
                    //manager.killBackgroundProcesses("com.android.email");
                    break;
                }
            }

        } catch (Exception e) {

        } catch (Error e) {

        }

    }

    public static void runLauncherApp(Context context) {
        try {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            for (ResolveInfo info : context.getPackageManager().queryIntentActivities(mainIntent, 0)) {
                if (info.loadLabel(context.getPackageManager()).equals("com.sec.android.app.launcher")) {
                    Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(info.activityInfo.applicationInfo.packageName);
                    context.startActivity(launchIntent);
                    return;
                }
            }
        } catch (Exception e) {

        }

    }

    public static String getSSIDName(Context context) {
        String ssid = "";
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifiManager.getConnectionInfo();
            ssid = info.getSSID();
        } catch (Exception e) {

        }
        return ssid;
    }


    public static void launchHomeIntent(Context context) {
        final String value = NFCPreference.getValue(context, NFCPreference.PREFES_KEY_RESPONSE_SUCCESS);
        if (TextUtils.isEmpty(value)) {
            context.startActivity(new Intent(Intent.ACTION_MAIN, null)
                    .addCategory(Intent.CATEGORY_HOME)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED));
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(startMain);
        }


    }

    public static void launchSamsungHomeScreen(Context context) {
        try {
            final String value = NFCPreference.getValue(context, NFCPreference.PREFES_KEY_RESPONSE_SUCCESS);
            if (TextUtils.isEmpty(value)) {
                PackageManager pm = context.getPackageManager();
                Intent launchIntent = pm.getLaunchIntentForPackage("com.sec.android.app.launcher");
                if (launchIntent != null) {
                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(launchIntent);
                }
            }
        } catch (Exception e) {

        }
    }

    public static void forgetWifi(Context context) {
        final String value = NFCPreference.getValue(context, NFCPreference.PREFES_KEY_RESPONSE_SUCCESS);
        if (TextUtils.isEmpty(value)) {
            return;
        }


        UtilityClass.setBooleanGlobalSetting(context, Settings.Global.WIFI_DEVICE_OWNER_CONFIGS_LOCKDOWN, false);

        FDLog.d(TAG,"forgetting wifi");
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> networks = wm.getConfiguredNetworks();
        try {
            Log.d(TAG,"forgetting wifi 11 networks " + networks);
            for (WifiConfiguration config : networks) {
                try {
                    FDLog.d(TAG,"forgetting wifi 22");

                    wm.removeNetwork(config.networkId);
                    wm.saveConfiguration();
                    FDLog.d(TAG,"forgetting wifi 33");
                } catch (Exception e) {
                    FDLog.e(TAG,"forgetwifi Exception: " + e.getMessage());
                    FDLog.e(TAG,e.getMessage());
                }
            }
        } catch (Exception e) {
            FDLog.e(TAG,e.getMessage());
        }
        forgotWifiCredentials(context);
        //setWifiState(false, context.getApplicationContext());
        FDLog.d(TAG,"forgetting wifi 44");
    }

    @TargetApi(29)
    private static void forgotWifiCredentials(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        try {
            FDLog.d(TAG,"forgotWifiCredentials ");

            int networkId = wifiManager.getConnectionInfo().getNetworkId();
            wifiManager.removeNetwork(networkId);
            wifiManager.saveConfiguration();
            FDLog.d(TAG,"forgotWifiCredentials Removed successfully ! ");

        } catch (Exception e) {
            FDLog.d(TAG,"Exception forgotWifiCredentials Removed android 10  " + e.getMessage());
        }

        try {

            FDLog.d(TAG,"forgotWifiCredentials Removed android 10  ");
            WifiNetworkSuggestion.Builder wifiNetworkSuggestionBuilder1 = new WifiNetworkSuggestion.Builder();
            wifiNetworkSuggestionBuilder1.setSsid("testing");
            // wifiNetworkSuggestionBuilder1.setWpa2Passphrase("abcd1234");


            WifiNetworkSuggestion wifiNetworkSuggestion = wifiNetworkSuggestionBuilder1.build();
            List<WifiNetworkSuggestion> list = new ArrayList<>();
            list.add(wifiNetworkSuggestion);
            wifiManager.removeNetworkSuggestions(list);
            FDLog.d(TAG,"forgotWifiCredentials Removed android 10  completed  ");


        } catch (Exception e) {
            FDLog.d(TAG,"Exception 2 forgotWifiCredentials Removed android 10  " + e.getMessage());
        }
    }
}
