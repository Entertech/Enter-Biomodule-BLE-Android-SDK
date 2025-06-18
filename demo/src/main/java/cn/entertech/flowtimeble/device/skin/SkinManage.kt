package cn.entertech.flowtimeble.device.skin

import android.content.Context
import cn.entertech.ble.BaseBluetoothDeviceUuidFactory

class SkinManage(context: Context) : cn.entertech.ble.device.headband.HeadbandManger(context),
    ISkinFunction {

    override fun getBaseBluetoothDeviceUuidFactory(): BaseBluetoothDeviceUuidFactory {
        return SkinUuidFactory
    }
}