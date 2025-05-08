package cn.entertech.flowtimeble.device.tag

import android.content.Context
import cn.entertech.ble.BaseBleConnectManager
import cn.entertech.ble.device.tag.BrainTagManager
import cn.entertech.device.DeviceType
import cn.entertech.device.api.IDeviceType
import cn.entertech.flowtimeble.device.BaseDeviceFactory
import cn.entertech.flowtimeble.device.DeviceInfo

class BrainTagFactory : BaseDeviceFactory() {
    override fun getDeviceType(): IDeviceType {
        return DeviceType.DEVICE_TYPE_BRAIN_TAG
    }

    override fun createBleConnectManager(context: Context): BaseBleConnectManager {
        return BrainTagManager(context)
    }

    override fun getDeviceKeyList(): List<String> {
        return listOf("head", "ear")
    }

    override fun getDeviceInfo(): Map<String, DeviceInfo> {
        return mapOf(
            Pair("head", DeviceInfo("头部", "head")), Pair("ear", DeviceInfo("耳部", "ear"))
        )
    }
}