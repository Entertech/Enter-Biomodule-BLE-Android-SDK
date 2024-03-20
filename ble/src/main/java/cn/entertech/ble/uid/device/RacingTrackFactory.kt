package cn.entertech.ble.uid.device

import cn.entertech.ble.uid.BleUUIDConstants
import cn.entertech.ble.uid.device.BaseBleDeviceFactory

/**
 * 赛车轨道
 * */
object RacingTrackFactory : BaseBleDeviceFactory() {
    override fun getBroadcastUUid(): String {
        return BleUUIDConstants.UUID_0000FF05_1212_ABCD_1523_785FEABCD123
    }
}