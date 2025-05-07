package cn.entertech.flowtimeble.skin

import android.content.Context
import cn.entertech.ble.device.headband.HeadbandManger
import cn.entertech.ble.uid.device.BaseBluetoothDeviceUuidFactory

class SkinManage(context: Context) : HeadbandManger(context),ISkinFunction{

    override fun getBaseBluetoothDeviceUuidFactory(): BaseBluetoothDeviceUuidFactory {
        return SkinUuidFactory
    }
}