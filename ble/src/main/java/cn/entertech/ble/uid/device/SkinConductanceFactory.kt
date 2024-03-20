package cn.entertech.ble.uid.device

import cn.entertech.ble.uid.BleUUIDConstants
import cn.entertech.ble.uid.service.ISkinConductanceService

object SkinConductanceFactory : BaseBleDeviceFactory(), ISkinConductanceService {
    override fun getBroadcastUUid(): String {
        return BleUUIDConstants.UUID_0000FF10_1212_ABCD_1523_785FEABCD123
    }

    override fun initDeviceUuidBean(deviceUuidBean: DeviceUuidBean) {
        super.initDeviceUuidBean(deviceUuidBean)
        addSkinConductanceService(deviceUuidBean)
    }

    override fun getSkinConductanceUUId(): String {
        return getSkinConductanceUUId(deviceUuidBean)
    }
}