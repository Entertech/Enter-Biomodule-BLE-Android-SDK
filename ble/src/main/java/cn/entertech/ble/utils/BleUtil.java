package cn.entertech.ble.utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.lang.reflect.Method;
import java.util.Set;

import cn.entertech.ble.log.BleLogUtil;

public class BleUtil {
    @SuppressLint("MissingPermission")
    public static void removePairDevice() {
        try {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null) {
                Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
                for (BluetoothDevice device : bondedDevices) {
                    if ("Flowtime".equals(device.getName())) {
                        unpairDevice(device);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            BleLogUtil.INSTANCE.e("BleUtil", e.getMessage());
        }
    }
}
