package cn.entertech.flowtimeble.device

import android.content.Context
import cn.entertech.ble.BaseBleConnectManager
import cn.entertech.ble.device.headband.HeadbandManger
import cn.entertech.device.DeviceType
import cn.entertech.device.api.IDeviceType

class HandBandFactory : BaseDeviceFactory() {
    override fun getDeviceType(): IDeviceType {
        return DeviceType.DEVICE_TYPE_HEADBAND
    }

    override fun createBleConnectManager(context: Context): BaseBleConnectManager {
        return HeadbandManger(context)
    }

    override fun getDeviceKeyList(): List<String> {
        return listOf("1","2")
    }

    override fun getDeviceInfo(): Map<String, DeviceInfo> {
        return mapOf(
            Pair("1",DeviceInfo("头环1","1")),
            Pair("2",DeviceInfo("头环2","2")),
        )
    }
}