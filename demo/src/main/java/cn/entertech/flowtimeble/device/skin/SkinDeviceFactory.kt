package cn.entertech.flowtimeble.device.skin

import android.content.Context
import cn.entertech.ble.BaseBleConnectManager
import cn.entertech.device.api.IDeviceType
import cn.entertech.flowtimeble.device.BaseDeviceFactory
import cn.entertech.flowtimeble.device.DeviceInfo

class SkinDeviceFactory : BaseDeviceFactory() {
    override fun getDeviceType(): IDeviceType {
        return SkinDevice
    }

    override fun createBleConnectManager(context: Context): BaseBleConnectManager {
        return SkinManage(context)
    }

    override fun getDeviceKeyList(): List<String> {
        return listOf("vr", "hand")
    }

    override fun getDeviceInfo(): Map<String, DeviceInfo> {
        return mapOf(
            Pair("vr", DeviceInfo("VR采集模块","vr", )),
            Pair("hand", DeviceInfo("手部皮电","hand", )),
        )
    }
}