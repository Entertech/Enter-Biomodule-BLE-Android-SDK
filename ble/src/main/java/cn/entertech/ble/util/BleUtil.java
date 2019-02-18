package cn.entertech.ble.util;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Set;

public class BleUtil {
    @SuppressLint("MissingPermission")
    public static void removePairDevice() {
        try {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null) {
                Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
                for (BluetoothDevice device : bondedDevices) {
                    unpairDevice(device);
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
            Log.e("BleUtil", e.getMessage());
        }
    }
}
