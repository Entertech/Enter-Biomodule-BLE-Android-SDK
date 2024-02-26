package cn.entertech.ble.uid.device.headband

import cn.entertech.ble.uid.BleUUIDConstants
import cn.entertech.ble.uid.device.BaseBleDeviceUidManage

/**
 * 赛车轨道
 * */
object RacingTrackUuidManage : BaseBleDeviceUidManage() {
    override fun getBroadcastUUid(): String {
        return BleUUIDConstants.UUID_0000FF05_1212_ABCD_1523_785FEABCD123
    }
}