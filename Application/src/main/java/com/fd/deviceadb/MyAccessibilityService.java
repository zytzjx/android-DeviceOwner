package com.fd.deviceadb;

import android.accessibilityservice.AccessibilityService;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

public class MyAccessibilityService extends AccessibilityService {
    private static final String TAG = MyAccessibilityService.class
            .getSimpleName();
    public MyAccessibilityService() {
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i(TAG, "ACC::onAccessibilityEvent: " + event.getPackageName());
        Toast.makeText(this, "ACC::onAccessibilityEvent: " + event.getPackageName(), Toast.LENGTH_SHORT).show();
        if (event.getPackageName().toString().equals("com.android.systemui"))
        {
            Boolean balwaysUse = false;
            //com.android.internal.R.id.alwaysUse
            Log.i(TAG, "ACC::onAccessibilityEvent: " + event.getEventType());
            if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == event.getEventType()) {
                AccessibilityNodeInfo info =  getRootInActiveWindow();

                String usb_always = "";
                try {
                    Resources res = this.getPackageManager().getResourcesForApplication("com.android.systemui");
                    int resId = res.getIdentifier("usb_debugging_always", "string", "com.android.systemui");
                    if(resId>0)
                        usb_always = res.getString(resId);
                }catch (Exception e){
                    e.printStackTrace();
                }
                List<AccessibilityNodeInfo> mList;
                if(!TextUtils.isEmpty(usb_always)) {
                   mList = getRootInActiveWindow().findAccessibilityNodeInfosByText(usb_always);  //根据文字
                    for (AccessibilityNodeInfo item : mList) {
                        if (!item.isChecked()) {
                            item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            balwaysUse = true;
                        }
                    }
                }

                mList = getRootInActiveWindow().findAccessibilityNodeInfosByText(getString(android.R.string.ok));  //根据文字
                for (AccessibilityNodeInfo item: mList) {
                    if(item.isEnabled()){
                        item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        if(balwaysUse) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                disableSelf();
                        }
                    }
                }

            }
        }
    }

    @Override
    public void onInterrupt() {

    }
    private AccessibilityNodeInfo foundinfo = null;

    public AccessibilityNodeInfo FindAccNodeInfo(AccessibilityNodeInfo paramAccessibilityNodeInfo, String paramString)
    {
        if (paramAccessibilityNodeInfo != null)
        {
            if ((paramAccessibilityNodeInfo.getText() == null) ||
                    (!paramAccessibilityNodeInfo.getText().toString().equalsIgnoreCase(paramString)))
            {
                for(int i=0; i< paramAccessibilityNodeInfo.getChildCount();i++){
                    foundinfo = FindAccNodeInfo(paramAccessibilityNodeInfo.getChild(i), paramString);
                    if(foundinfo!=null)break;
                }
            }
            else{
                foundinfo =  paramAccessibilityNodeInfo;
            }
        }
        return foundinfo;
    }

    @Override
    protected void onServiceConnected()
    {
        Log.d(TAG, "onServiceConnected");
    }

}
