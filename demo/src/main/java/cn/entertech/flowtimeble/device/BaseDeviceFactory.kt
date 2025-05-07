package cn.entertech.flowtimeble.device

import android.content.Context
import cn.entertech.ble.BaseBleConnectManager
import cn.entertech.device.api.IDeviceType

abstract class BaseDeviceFactory {

    abstract fun getDeviceType(): IDeviceType

    abstract fun createBleConnectManager(context: Context): BaseBleConnectManager

    abstract fun getDeviceKeyList(): List<String>

    abstract fun getDeviceInfo(): Map<String, DeviceInfo>

}