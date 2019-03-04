package com.fd.deviceadb;

import android.app.ActivityManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.fd.deviceadb.DeviceOwnerReceiver.getComponentName;

public class InfoService extends Service {
    String mSite="88";
    String mCompany="67";
    String mNFCID="00051";
    public static final String USER_SETUP_COMPLETE = "user_setup_complete";
    final InfoService cntxt = InfoService.this;

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

        new ReportInfo().execute("");
        return super.onStartCommand(intent, flags, startId);
    }

    private void StartHome(Context context)
    {
        try {
            Intent localIntent = new Intent(Intent.ACTION_MAIN);
            localIntent.addCategory(Intent.CATEGORY_HOME);
            localIntent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            context.startActivity(localIntent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static boolean CheckPackageExist(String paname, Context context)
    {
        try {
            PackageManager pm = context.getPackageManager();
            List<ApplicationInfo> applications = pm.getInstalledApplications(0);
            for (ApplicationInfo appInfo : applications) {
                if (appInfo.packageName.equalsIgnoreCase(paname))
                    return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean containsCaseInsensitive(String s, List<String> l){
        for (String string : l){
            if (string.equalsIgnoreCase(s)){
                return true;
            }
        }
        return false;
    }

    static List<String> GetPakageList(Context paramContext){
        List<String> packages = new ArrayList<String>();

        try {
            PackageManager pm = paramContext.getPackageManager();
            List<ApplicationInfo> applications = pm.getInstalledApplications(0);
            for (ApplicationInfo appInfo : applications) {
                packages.add(appInfo.packageName);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return packages;
    }

    public void setAppHidden(Context context)
    {
        DevicePolicyManager manager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = DeviceOwnerReceiver.getComponentName(context);


        List<String> packages = GetPakageList(context);
        try {
            if (containsCaseInsensitive("com.google.android.setupwizard", packages))
                manager.setApplicationHidden(componentName, "com.google.android.setupwizard", true);
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
            if (containsCaseInsensitive("com.sec.android.app.setupwizard", packages))
                manager.setApplicationHidden(componentName, "com.sec.android.app.setupwizard", true);
            if (containsCaseInsensitive("com.google.android.setupwizard", packages))
                manager.setApplicationHidden(componentName, "com.google.android.setupwizard", true);
            if (containsCaseInsensitive("com.lge.LGSetupView", packages))
                manager.setApplicationHidden(componentName, "com.lge.LGSetupView", true);
            if (containsCaseInsensitive("com.sec.android.app.setupwizard", packages))
                manager.setApplicationHidden(componentName, "com.sec.android.app.setupwizard", true);
            if (containsCaseInsensitive("com.google.android.setupwizard", packages))
                manager.setApplicationHidden(componentName, "com.google.android.setupwizard", true);
            if (containsCaseInsensitive("com.samsung.android.incallui", packages))
                manager.setApplicationHidden(componentName, " com.samsung.android.incallui", true);
            if (containsCaseInsensitive("com.samsung.huxextension", packages))
                manager.setApplicationHidden(componentName, "com.samsung.huxextension", true);
            if (containsCaseInsensitive("com.google.android.setupwizard", packages))
                manager.setApplicationHidden(componentName, "com.google.android.setupwizard", true);
        }catch (Exception e){
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT > 20)
        {
            try {
                ActivityManager localActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                String procName = "";
                if (localActivityManager != null) {

                    if (localActivityManager.getRunningAppProcesses() != null)
                        procName = ((ActivityManager.RunningAppProcessInfo) localActivityManager.getRunningAppProcesses().get(0)).processName;
                }
                if ((procName != null) && (!procName.contains("launcher")))
                    manager.setApplicationHidden(componentName, procName, true);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    public  void SetAdbEnabled()
    {
        DevicePolicyManager manager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        if(!manager.isDeviceOwnerApp(getApplicationContext().getPackageName())) return;

        try
        {
            Settings.Global.putInt(getApplicationContext().getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,1);
            Settings.Global.putInt(getApplicationContext().getContentResolver(), Settings.Global.ADB_ENABLED, 1);
        }
        catch (Exception localException1)
        {
            localException1.printStackTrace();
        }
        try
        {
            try
            {
                Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 1);
                Settings.Secure.putInt(getContentResolver(),USER_SETUP_COMPLETE, 1);
            }
            catch (Exception localException2)
            {
                localException2.printStackTrace();
            }
            if (manager != null)
            {
                manager.setSecureSetting(getComponentName(this),
                        Settings.Secure.INSTALL_NON_MARKET_APPS,//install_nonmarket_apps
                        "1");
                manager.setGlobalSetting(
                        // The ComponentName of the device owner
                        getComponentName(this),
                        // The settings to be set
                        Settings.Global.ADB_ENABLED,
                        // The value we write here is a string representation for SQLite
                        "1");
                if (Build.VERSION.SDK_INT >= 23)
                    manager.setGlobalSetting(getComponentName(this), Settings.Global.STAY_ON_WHILE_PLUGGED_IN, "7");
            }
            return;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void releaseOwnership()
    {
        try {
            DevicePolicyManager manager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            String PackageName = getPackageName();

            if (manager.isDeviceOwnerApp(PackageName)) {
                ComponentName componentName = DeviceOwnerReceiver.getComponentName(getApplicationContext());
                if (Build.VERSION.SDK_INT >= 24)
                    Log.d("OwnerRemover", "Clearing profile owner");
                Log.d("OwnerRemover", "Clearing device owner");
                manager.removeActiveAdmin(componentName);
                manager.clearDeviceOwnerApp(PackageName);
                Log.d("OwnerRemover", "Device owner cleared succesfully");
            } else {
                Log.d("OwnerRemover", "App is not device owner");
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private  void setAccessibilityService(){
        try {
            if (Settings.Secure.getInt(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 0) == 0) {
                String service = "com.fd.deviceadb/.MyAccessibilityService";
                String sAA = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
                Boolean bFound = false;
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
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    class ReportInfo extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            synchronized (USER_SETUP_COMPLETE) {
                DevicePolicyManager manager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                String PackageName = getPackageName();
                if (!manager.isDeviceOwnerApp(PackageName)){
                    return "{}";
                }

                try {
                    SetAdbEnabled();
                    //setAppHidden(cntxt);
                    StartHome(cntxt);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                JSONObject jsonParam = new JSONObject();
                try {
                    URL url = new URL("http://cmc.futuredial.com/ws/insert/");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
                    String sn = null;
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            sn = mTelephony.getImei();
                        } else {
                            sn = mTelephony.getDeviceId();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    jsonParam.put("esnNumber", sn == null ? "" : sn);

                    String address = WifiAddress.getMacAddress(getApplicationContext());
                    jsonParam.put("MacAddress", address);
                    jsonParam.put("sourceMake", Build.MANUFACTURER);
                    jsonParam.put("sourceModel", Build.MODEL);
                    jsonParam.put("serialnumber", Build.SERIAL);
                    jsonParam.put("AndroidVersion", Build.VERSION.RELEASE);
                    jsonParam.put("buildnumber", Build.DISPLAY);

                    Log.i("JSON", jsonParam.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(jsonParam.toString());

                    os.flush();
                    os.close();

                    Log.d("FDAIL", String.valueOf(conn.getResponseCode()));
                    Log.d("FDAIL", conn.getResponseMessage());
                    //SetAdbEnabled();
                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //setAccessibilityService();
                WifiAddress.RemoveWifi(getApplicationContext());

                releaseOwnership();

//            try{
//                cntxt.stopSelf();
//            }catch (Exception e){
//
//            }
                //Intent I = new Intent("android.settings.APPLICATION_DEVELOPMENT_SETTINGS");
                // getApplicationContext().startActivity(I);
                //Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                //startActivity(intent);

                return jsonParam.toString();
            }
        }
    }
}
