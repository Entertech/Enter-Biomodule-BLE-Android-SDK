package cn.entertech.flowtimeble.device

import android.content.Context
import cn.entertech.ble.BaseBleConnectManager
import cn.entertech.ble.device.tag.BrainTagManager
import cn.entertech.device.DeviceType
import cn.entertech.device.api.IDeviceType

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