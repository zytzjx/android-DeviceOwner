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

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.os.UserManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FDIAL";
    private static String mSite="";
    private static String mCompany="";
    private static String mNFCID="";

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_real);
        UserManager mUserManager;

        if (savedInstanceState == null) {
            DevicePolicyManager manager =
                    (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            mUserManager = (UserManager) getSystemService(Context.USER_SERVICE);
            if (manager.isDeviceOwnerApp(getApplicationContext().getPackageName())) {
                // This app is set up as the device owner. Show the main features.
                Log.d(TAG, "The app is the device owner.");
                showFragment(DeviceOwnerFragment.newInstance());


                manager.setGlobalSetting(
                        // The ComponentName of the device owner
                        DeviceOwnerReceiver.getComponentName(this),
                        // The settings to be set
                        Settings.Global.ADB_ENABLED,
                        // The value we write here is a string representation for SQLite
                        "1");

//                 Boolean unknownSource;
//                if (Build.VERSION.SDK_INT < JELLY_BEAN_MR1) {
//                    unknownSource = Settings.Secure.getInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 0)>0;
//                } else {
//                    unknownSource = Settings.Global.getInt(getContentResolver(), Settings.Global.INSTALL_NON_MARKET_APPS, 0)>0;
//                }
//                Log.d(TAG, "INSTALL_NON_MARKET_APPS is "+ unknownSource);
//                if(!unknownSource) {
//                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                        manager.setSecureSetting(
//                                // The ComponentName of the device owner
//                                DeviceOwnerReceiver.getComponentName(this),
//                                // The settings to be set
//                                Settings.Secure.INSTALL_NON_MARKET_APPS,
//                                // The value we write here is a string representation for SQLite
//                                "1");
//                    }else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
//                        mUserManager.setUserRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES, true);
//                    }
//                    else {
//                        manager.addUserRestriction(DeviceOwnerReceiver.getComponentName(this),
//                                UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES);
//                    }
//                }
//
//                if (Build.VERSION.SDK_INT < JELLY_BEAN_MR1) {
//                    unknownSource = Settings.Secure.getInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 0)>0;
//                } else {
//                    unknownSource = Settings.Global.getInt(getContentResolver(), Settings.Global.INSTALL_NON_MARKET_APPS, 0)>0;
//                }
//                Log.d(TAG, "INSTALL_NON_MARKET_APPS is "+ unknownSource);

                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
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
                Log.d(TAG, "The app is not the device owner.");
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
        //https://fsmc.futuredial.com/b/<TagID>/<CompanyID>/SiteID>/
//        Uri.Builder builder = new Uri.Builder();
//        builder.scheme("https")
//                .authority("fsmc.futuredial.com")
//                .appendPath("b")
//                .appendPath(mNFCID)
//                .appendPath(mCompany)
//                .appendPath(mSite);
//
//        new ProcessJSON().execute(builder.toString()+"/");
        sendPost();
    }

    private class ProcessJSON extends AsyncTask<String, Void, String> {

        public Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
            Map<String, Object> retMap = new HashMap<String, Object>();

            if(json != JSONObject.NULL) {
                retMap = toMap(json);
            }
            return retMap;
        }

        public  Map<String, Object> toMap(JSONObject object) throws JSONException {
            Map<String, Object> map = new HashMap<String, Object>();

            Iterator<String> keysItr = object.keys();
            while(keysItr.hasNext()) {
                String key = keysItr.next();
                Object value = object.get(key);

                if(value instanceof JSONArray) {
                    value = toList((JSONArray) value);
                }

                else if(value instanceof JSONObject) {
                    value = toMap((JSONObject) value);
                }
                map.put(key, value);
            }
            return map;
        }

        public List<Object> toList(JSONArray array) throws JSONException {
            List<Object> list = new ArrayList<Object>();
            for(int i = 0; i < array.length(); i++) {
                Object value = array.get(i);
                if(value instanceof JSONArray) {
                    value = toList((JSONArray) value);
                }

                else if(value instanceof JSONObject) {
                    value = toMap((JSONObject) value);
                }
                list.add(value);
            }
            return list;
        }

        public String GetHTTPData(String urlString){
            String  stream = null;
            try{
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                // Check the connection status
                if(urlConnection.getResponseCode() == 200)
                {
                    // if response code = 200 ok
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                    // Read the BufferedInputStream
                    BufferedReader r = new BufferedReader(new InputStreamReader(in));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        sb.append(line).append('\n');
                    }
                    stream = sb.toString();
                    // End reading...............

                    // Disconnect the HttpURLConnection
                    urlConnection.disconnect();
                }
                else
                {
                    // Do something
                }
            }catch (MalformedURLException e){
                e.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }finally {

            }
            // Return the data from specified url
            return stream;
        }
        protected String doInBackground(String... strings){
            String stream = null;
            String urlString = strings[0];

            stream = GetHTTPData(urlString);
            // Return the data from specified url
            return stream;
        }

        protected void onPostExecute(String stream){
            if(stream !=null && stream.length()>0){
                try{
                    JSONObject reader= new JSONObject(stream);
                }catch(JSONException e){
                    e.printStackTrace();
                }
            } // if statement end
        } // onPostExecute() end
    } // ProcessJSON class end



    private void installApk_1(){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(new File("/sdcard/filename.apk"));
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        startActivity(intent);
    }

    /**
     * 安装APK
     *
     * @param context
     * @param apkPath
     */
    public static void installApk(Context context, String apkPath) {
        if (context == null || TextUtils.isEmpty(apkPath)) {
            return;
        }
        File file = new File(apkPath);
        Intent intent = new Intent(Intent.ACTION_VIEW);

        //判读版本是否在7.0以上
        if (Build.VERSION.SDK_INT >= 24) {
            //provider authorities
            Uri apkUri = FileProvider.getUriForFile(context, "com.fd.deviceadb.fileProvider", file);
            //Granting Temporary Permissions to a URI
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }


    /**
     * void installPackageAsUser(in String originPath,
     * in IPackageInstallObserver2 observer,
     * int flags,
     * in String installerPackageName,
     * int userId);
     * @param installPath
     */
//    private void installApkInSilence(String installPath,String packageName) {
//        Class<?> pmService;
//        Class<?> activityTherad;
//        Method method;
//        try {
//            activityTherad = Class.forName("android.app.ActivityThread");
//            Class<?> paramTypes[] = getParamTypes(activityTherad, "getPackageManager");
//            method = activityTherad.getMethod("getPackageManager", paramTypes);
//            Object PackageManagerService = method.invoke(activityTherad);
//            pmService = PackageManagerService.getClass();
//            Class<?> paramTypes1[] = getParamTypes(pmService, "installPackageAsUser");
//            method = pmService.getMethod("installPackageAsUser", paramTypes1);
//            method.invoke(PackageManagerService, installPath, null, 0x00000040, packageName, getUserId(Binder.getCallingUid()));//getUserId
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
//    }

    private Class<?>[] getParamTypes(Class<?> cls, String mName) {
        Class<?> cs[] = null;
        Method[] mtd = cls.getMethods();
        for (int i = 0; i < mtd.length; i++) {
            if (!mtd[i].getName().equals(mName)) {
                continue;
            }

            cs = mtd[i].getParameterTypes();
        }
        return cs;
    }
//    public static final int PER_USER_RANGE = 100000;
//    public static int getUserId(int uid) {
//        return uid / PER_USER_RANGE;
//    }
//
//
//    private void downloadapk(){
//        try {
//            URL url = new URL("https://github.com/zytzjx/testfile/raw/master/readid.apk");
//            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
//            urlConnection.setRequestMethod("GET");
//            urlConnection.setDoOutput(true);
//            urlConnection.connect();
//
//            File sdcard = Environment.getExternalStorageDirectory();
//            File file = new File(sdcard, "filename.apk");
//
//            FileOutputStream fileOutput = new FileOutputStream(file);
//            InputStream inputStream = urlConnection.getInputStream();
//
//            byte[] buffer = new byte[1024];
//            int bufferLength = 0;
//
//            while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
//                fileOutput.write(buffer, 0, bufferLength);
//            }
//            fileOutput.close();
//            installApk(getApplicationContext(), "/sdcard/filename.apk");
//            //installApkInSilence(sdcard.getPath(), "filename.apk");
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public  void SetAdbEnabled()
    {
        DevicePolicyManager manager =
                (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        try
        {
            Settings.Secure.putInt(getApplicationContext().getContentResolver(), Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED,1);
            Settings.Secure.putInt(getApplicationContext().getContentResolver(), Settings.Global.ADB_ENABLED, 1);
        }
        catch (Exception localException1)
        {
        }
        try
        {
            try
            {
                Settings.Secure.putInt(getApplicationContext().getContentResolver(), "user_setup_complete", 1);
                Settings.Global.putInt(getApplicationContext().getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 1);
            }
            catch (Exception localException2)
            {
            }
            if (manager != null)
            {
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
            return;
        }
        catch (Exception paramContext)
        {
        }
    }

    private void releaseOwnership()
    {
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
                    Log.d("OwnerRemover", "Clearing profile owner");
                Log.d("OwnerRemover", "Clearing device owner");
                manager.removeActiveAdmin(componentName);
                manager.clearDeviceOwnerApp(PackageName);
                Log.d("OwnerRemover", "Device owner cleared succesfully");
            } else {
                Log.d("OwnerRemover", "App is not device owner");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void sendPost() {
        Log.d("FDAIL", "sendPost++");
        final  Context context = this;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //downloadapk();
                try {
                    URL url = new URL("http://cmc.futuredial.com/ws/insert/");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
                    SetAdbEnabled();
                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                WifiAddress.RemoveWifi(context);
                releaseOwnership();

                //WifiManager wifi = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                //wifi.setWifiEnabled(false);
                finish();
            }
        });

        thread.start();
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

}
