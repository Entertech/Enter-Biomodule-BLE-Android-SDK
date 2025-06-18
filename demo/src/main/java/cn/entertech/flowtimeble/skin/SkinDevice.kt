package cn.entertech.flowtimeble.skin

import cn.entertech.device.api.IDeviceType

object SkinDevice: IDeviceType{
    override fun getDeviceType(): Int {
        return -1
    }

    override fun getDeviceTypeName(): String {
        return "SkinDevice"
    }
}