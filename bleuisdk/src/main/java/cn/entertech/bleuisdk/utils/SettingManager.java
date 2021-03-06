package cn.entertech.bleuisdk.utils;

import android.content.Context;
import android.content.SharedPreferences;

import static cn.entertech.bleuisdk.utils.Constant.BLE_FIRMWARE;
import static cn.entertech.bleuisdk.utils.Constant.BLE_HARDWARE;
import static cn.entertech.bleuisdk.utils.Constant.BLE_MAC;
import static cn.entertech.bleuisdk.utils.Constant.IS_CONNECT_BEFORE;
import static cn.entertech.bleuisdk.utils.Constant.SP_SETTING;


/**
 * Created by EnterTech on 2017/1/10.
 */

public class SettingManager {
    private static SettingManager mInstance;
    private Context mContext;

    public static SettingManager getInstance(Context context) {
        if (null == mInstance) {
            synchronized (SettingManager.class) {
                if (null == mInstance) {
                    mInstance = new SettingManager(context);
                }
            }
        }
        return mInstance;
    }

    private SettingManager(Context context) {
        mContext = context;
    }

    private SharedPreferences getSharedPreferences() {
        return mContext.getSharedPreferences(SP_SETTING, Context.MODE_PRIVATE);
    }

    private SharedPreferences.Editor getEditor() {
        return getSharedPreferences().edit();
    }


    //BLE
    public synchronized void setBleMac(String mac) {
        getEditor().putString(BLE_MAC, mac).apply();
    }

    public synchronized String getBleMac() {
        return getSharedPreferences().getString(BLE_MAC, "");
    }

    public synchronized void setStringValue(String key, String value) {
        getEditor().putString(key, value).apply();
    }

    public synchronized String getStringValue(String key) {
        return getSharedPreferences().getString(key, "");
    }

    public synchronized void setBleHardware(String mac) {
        getEditor().putString(BLE_HARDWARE, mac).apply();
    }

    public synchronized String getBleHardware() {
        return getSharedPreferences().getString(BLE_HARDWARE, "");
    }

    public synchronized void setBleFirmware(String mac) {
        getEditor().putString(BLE_FIRMWARE, mac).apply();
    }

    public synchronized String getBleFirmware() {
        return getSharedPreferences().getString(BLE_FIRMWARE, "");
    }

    public synchronized void setConnectBefore(boolean isConnectBefore) {
        getEditor().putBoolean(IS_CONNECT_BEFORE, isConnectBefore).apply();
    }

    public synchronized boolean isConnectBefore() {
        return getSharedPreferences().getBoolean(IS_CONNECT_BEFORE, false);
    }


}
