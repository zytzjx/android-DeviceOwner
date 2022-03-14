package com.fd.deviceadb;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class NFCPreference {

    private static final String PREFES_NAME = "SAVE_Preference";
    public static final String PREFES_KEY_RESPONSE_SUCCESS="PREFES_KEY_RESPONSE_SUCCESS";
    public static final String PREFES_KEY_RESPONSE_ACTIVITY_LAUNCHED="PREFES_KEY_RESPONSE_ACTIVITY_LAUNCHED";
    public static final String PREFES_KEY_RESPONSE_ACTIVITY_LAUNCHED_SAMSUNG="PREFES_KEY_RESPONSE_ACTIVITY_LAUNCHED_SAMSUNG";
    public static final String PREFES_KEY_RESET_SSID="PREFES_KEY_RESET_SSID";
    public static final String PREFES_KEY_SAVE_SSID_NAME="PREFES_KEY_SAVE_SSID_NAME";
    public static final String PREFES_KEY_RETRY_COUNT="PREFES_KEY_RETRY_COUNT";
    public static  final String PREFES_KEY_FORGET_WIFI = "FORGET_WIFI";

    public static void saveValue(Context context, String key, String value)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFES_NAME, Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getValue(Context context , String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREFES_NAME, Context.MODE_PRIVATE);
        String value = prefs.getString(key, "");
        return value;
    }

    public static void saveValue(Context context, String key, int value)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFES_NAME, Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getIntValue(Context context , String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREFES_NAME, Context.MODE_PRIVATE);
        int value = prefs.getInt(key, 0);
        return value;
    }

}
