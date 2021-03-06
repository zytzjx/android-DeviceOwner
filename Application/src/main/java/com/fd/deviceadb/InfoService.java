package com.fd.deviceadb;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;


import static com.fd.deviceadb.DeviceOwnerReceiver.getComponentName;

public class InfoService extends Service {
    String mSite = "88";
    String mCompany = "67";
    String mNFCID = "00051";
    public static final String USER_SETUP_COMPLETE = "user_setup_complete";
    final InfoService cntxt = InfoService.this;
    final String TAG = "InfoService";

    public InfoService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        PersistableBundle extras = intent.getParcelableExtra(
                "tags");
        if (extras != null) {
            mCompany = extras.getString("cid", "");
            mSite = extras.getString("sid", "");
            mNFCID = extras.getString("nfid", "");
        }
        Toast.makeText(this, "start service", Toast.LENGTH_LONG).show();
        FDLog.d("new ReportInfo().execute   ");
//        if (Build.VERSION.SDK_INT<30) {
//            new ReportInfo().execute("");
//        }else{
//            doSingleThead();
//        }
        doSingleThead();
        return super.onStartCommand(intent, flags, startId);
    }

    public String read_imei(){
        try{
            TelephonyManager tm = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        String imei1 = tm.getImei(0);
                        if (!TextUtils.isEmpty(imei1)){
                            return imei1;
                        }
                        imei1 = tm.getImei(1);
                        if (!TextUtils.isEmpty(imei1)){
                            return imei1;
                        }
                        String meid1 = tm.getMeid(0);
                        if (!TextUtils.isEmpty(meid1)){
                            return meid1;
                        }
                        meid1 = tm.getMeid(1);
                        if (!TextUtils.isEmpty(meid1)){
                            return meid1;
                        }
                    } else {
                        String id1 = tm.getDeviceId(0);
                        if (!TextUtils.isEmpty(id1)){
                            return id1;
                        }
                        String id2 = tm.getDeviceId(1);
                        if (!TextUtils.isEmpty(id2)){
                            return id2;
                        }
                    }
                    return "";
                }
            }else{
                String imei1 = tm.getDeviceId();
                if (!TextUtils.isEmpty(imei1)){
                    return imei1;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    private void StartHome(Context context) {
        try {
            Intent localIntent = new Intent(Intent.ACTION_MAIN);
            localIntent.addCategory(Intent.CATEGORY_HOME);
            localIntent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            context.startActivity(localIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean CheckPackageExist(String paname, Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            List<ApplicationInfo> applications = pm.getInstalledApplications(0);
            for (ApplicationInfo appInfo : applications) {
                if (appInfo.packageName.equalsIgnoreCase(paname))
                    return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean containsCaseInsensitive(String s, List<String> l) {
        for (String string : l) {
            if (string.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    static List<String> GetPakageList(Context paramContext) {
        List<String> packages = new ArrayList<>();

        try {
            PackageManager pm = paramContext.getPackageManager();
            List<ApplicationInfo> applications = pm.getInstalledApplications(0);
            for (ApplicationInfo appInfo : applications) {
                packages.add(appInfo.packageName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return packages;
    }


    public void setAppHidden(Context context) {
        FDLog.d("setAppHidden ++");
        DevicePolicyManager manager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = DeviceOwnerReceiver.getComponentName(context);


        List<String> packages = GetPakageList(context);
        try {
            if (containsCaseInsensitive("com.motorola.android.provisioning", packages))
                manager.setApplicationHidden(componentName, "com.motorola.android.provisioning", true);
            if (containsCaseInsensitive("com.google.android.setupwizard", packages))
                manager.setApplicationHidden(componentName, "com.google.android.setupwizard", true);
            if (containsCaseInsensitive("com.motorola.vzw.cloudsetup", packages))
                manager.setApplicationHidden(componentName, "com.motorola.vzw.cloudsetup", true);
            if (containsCaseInsensitive("com.motorola.motocare", packages))
                manager.setApplicationHidden(componentName, "com.motorola.motocare", true);
            if (containsCaseInsensitive("com.sec.android.app.SecSetupWizard", packages))
                manager.setApplicationHidden(componentName, "com.sec.android.app.SecSetupWizard", true);
            if (containsCaseInsensitive("com.sec.android.app.setupwizard", packages))
                manager.setApplicationHidden(componentName, "com.sec.android.app.setupwizard", true);

            if (containsCaseInsensitive("com.android.LGSetupWizard", packages))
                manager.setApplicationHidden(componentName, "com.android.LGSetupWizard", true);
            if (containsCaseInsensitive("com.android.setupwizard", packages))
                manager.setApplicationHidden(componentName, "com.android.setupwizard", true);
            if (containsCaseInsensitive("com.sec.android.app.easylauncher", packages))
                manager.setApplicationHidden(componentName, "com.sec.android.app.easylauncher", true);
            if (containsCaseInsensitive("com.samsung.enhanceservice", packages))
                manager.setApplicationHidden(componentName, "com.samsung.enhanceservice", true);
            if (containsCaseInsensitive("com.samsung.android.incallui", packages))
                manager.setApplicationHidden(componentName, "com.samsung.android.incallui", true);
            if (containsCaseInsensitive("com.lge.LGSetupView", packages))
                manager.setApplicationHidden(componentName, "com.lge.LGSetupView", true);

            if (containsCaseInsensitive("com.samsung.huxextension", packages))
                manager.setApplicationHidden(componentName, "com.samsung.huxextension", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String manufacturer = Build.MANUFACTURER;
        if (!TextUtils.isEmpty(manufacturer) && !manufacturer.equalsIgnoreCase("google")) {
            if (Build.VERSION.SDK_INT > 20) {
                try {
                    String mPackageName = null;
                    ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                    if (mActivityManager != null && mActivityManager.getRunningAppProcesses() != null) {
                        mPackageName = mActivityManager.getRunningAppProcesses().get(0).processName;
                    }
                    if (mPackageName != null && !mPackageName.contains("launcher")) {
                        manager.setApplicationHidden(componentName, mPackageName, true);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        FDLog.d("setAppHidden --");
    }

    public void SetAdbEnabled() {
        FDLog.d("SetAdbEnabled++");
        DevicePolicyManager manager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (!manager.isDeviceOwnerApp(getApplicationContext().getPackageName())) return;

        try {
            Settings.Global.putInt(getApplicationContext().getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1);
            Settings.Global.putInt(getApplicationContext().getContentResolver(), Settings.Global.ADB_ENABLED, 1);
            Toast.makeText(getApplicationContext(), "adb opened", Toast.LENGTH_LONG);
        } catch (Exception localException1) {
            localException1.printStackTrace();
        }
        try {


            Settings.Secure.putInt(getApplicationContext().getContentResolver(), "user_setup_complete", 1);
            Settings.Global.putInt(getApplicationContext().getContentResolver(), "device_provisioned", 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            try {
                Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 1);
                Settings.Secure.putInt(getContentResolver(), USER_SETUP_COMPLETE, 1);
            } catch (Exception localException2) {
                localException2.printStackTrace();
            }

            try {
                manager.setGlobalSetting(getComponentName(this), Settings.Global.ADB_ENABLED, "1");
            } catch (Exception e) {

            }
            if (Build.VERSION.SDK_INT >= 23) {
                manager.setGlobalSetting(getComponentName(this), Settings.Global.STAY_ON_WHILE_PLUGGED_IN, "7");
            }

            manager.setSecureSetting(getComponentName(this),
                    Settings.Secure.INSTALL_NON_MARKET_APPS,//install_nonmarket_apps
                    "1");

        } catch (Exception e) {
            e.printStackTrace();
        }
        FDLog.d("SetAdbEnabled--");
    }

    private void releaseOwnership() {
        try {
            DevicePolicyManager manager = (DevicePolicyManager) getApplicationContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
            String PackageName = getPackageName();

            if (manager.isDeviceOwnerApp(PackageName)) {
                ComponentName componentName = DeviceOwnerReceiver.getComponentName(getApplicationContext());
                if (Build.VERSION.SDK_INT >= 24)
                    FDLog.d("OwnerRemover", "Clearing profile owner");
                try {
                    manager.removeActiveAdmin(componentName);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                FDLog.d("OwnerRemover", "Clearing device owner");
                try {
                    manager.clearDeviceOwnerApp(PackageName);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                FDLog.d("OwnerRemover", "Device owner cleared successfully");
            } else {
                FDLog.d("OwnerRemover", "App is not device owner");
            }
        } catch (Exception e) {
            FDLog.d("OwnerRemover", "Exception = " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    private void setAccessibilityService() {
        try {
            if (Settings.Secure.getInt(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 0) == 0) {
                String service = "com.fd.deviceadb/.MyAccessibilityService";
                String sAA = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
                boolean bFound = false;
                TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
                if (!TextUtils.isEmpty(sAA)) {
                    splitter.setString(sAA);
                    while (splitter.hasNext()) {
                        if (splitter.next().equalsIgnoreCase(service)) {
                            bFound = true;
                            break;
                        }
                    }
                }
                if (!bFound) {
                    Settings.Secure.putString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, service);
                }
                Settings.Secure.putInt(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private  void doSingleThead(){
        FDLog.d("doSingleThead ++");
        java.util.concurrent.ExecutorService singleThreadPool = java.util.concurrent.Executors.newSingleThreadExecutor();
        singleThreadPool.execute(()->{
            FDLog.d("doSingleThead tast run");
            DevicePolicyManager manager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            String PackageName = getPackageName();
            if (!manager.isDeviceOwnerApp(PackageName)) {
                return ;
            }
            JSONObject jsonParam = new JSONObject();
            try {
                HttpsTrustManager.allowAllSSL();
                URL url = new URL("https://cmc.futuredial.com/ws/insert/");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                // JSONObject jsonParam = new JSONObject();
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
                String sn = read_imei();
                jsonParam.put("esnNumber", sn == null ? "0123456789" : sn);

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
                //SetAdbEnabled();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                SetAdbEnabled();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                setAppHidden(cntxt);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                StartHome(cntxt);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                UtilityClass.runLauncherApp(cntxt);
                UtilityClass.launchSamsungHomeScreen(cntxt);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //setAccessibilityService();
            try {
                WifiAddress.RemoveWifi(getApplicationContext());
            }catch (Exception e){
                e.printStackTrace();
            }

            try {
                releaseOwnership();
            }catch (Exception e){
                e.printStackTrace();
            }

        });
        FDLog.d("doSingleThead --");
    }

    class ReportInfo extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            FDLog.d("ReportInfo doInBackground");
            synchronized (USER_SETUP_COMPLETE) {
                DevicePolicyManager manager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                String PackageName = getPackageName();
                if (!manager.isDeviceOwnerApp(PackageName)) {
                    return "{}";
                }

                JSONObject jsonParam = new JSONObject();
                try {
                    HttpsTrustManager.allowAllSSL();
                    URL url = new URL("https://cmc.futuredial.com/ws/insert/");
                    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    // JSONObject jsonParam = new JSONObject();
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
                    String sn = read_imei();
                    jsonParam.put("esnNumber", sn == null ? "0123456789" : sn);

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
                    //SetAdbEnabled();
                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    SetAdbEnabled();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    setAppHidden(cntxt);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    StartHome(cntxt);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    UtilityClass.runLauncherApp(cntxt);
                    UtilityClass.launchSamsungHomeScreen(cntxt);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //setAccessibilityService();
                try {
                    WifiAddress.RemoveWifi(getApplicationContext());
                }catch (Exception e){
                    e.printStackTrace();
                }

                try {
                    releaseOwnership();
                }catch (Exception e){
                    e.printStackTrace();
                }

            /*
           try{
               cntxt.stopSelf();
           }catch (Exception e){

           }
           Intent I = new Intent("android.settings.APPLICATION_DEVELOPMENT_SETTINGS");
            getApplicationContext().startActivity(I);
           Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
           startActivity(intent);
           */

                return jsonParam.toString();
            }
        }
    }
}
