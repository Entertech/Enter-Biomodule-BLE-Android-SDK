package cn.entertech.ble.uid.device.headband

import cn.entertech.ble.uid.BleUUIDConstants
import cn.entertech.ble.uid.device.BaseBleDeviceUidManage
import cn.entertech.ble.uid.service.IBatteryService
import cn.entertech.ble.uid.service.IEegService
import cn.entertech.ble.uid.service.IHrsService

/**
 * 头环
 * */
object HeadbandUidManage : BaseBleDeviceUidManage(),
    IHrsService, IBatteryService, IEegService {


    override fun initDeviceUuidBean() {
        super.initDeviceUuidBean()
        addHrsService(deviceUuidBean)
        addBatteryService(deviceUuidBean)
        addEegService(deviceUuidBean)
    }

    override fun getBroadcastUUid(): String {
        return BleUUIDConstants.UUID_0000FF10_1212_ABCD_1523_785FEABCD123
    }

    override fun getCharacteristicBatteryLevelUUid(): String {
        if (deviceUuidBean == null) {
            initDeviceUuidBean()
        }
        return getCharacteristicBatteryLevelUUid(deviceUuidBean)
    }

    override fun getCharacteristicEEGUUid(): String {
        if (deviceUuidBean == null) {
            initDeviceUuidBean()
        }
        return getCharacteristicEEGUUid(deviceUuidBean)
    }

    override fun getCharacteristicContactDateMacUUid(): String {
        if (deviceUuidBean == null) {
            initDeviceUuidBean()
        }
        return getCharacteristicContactDateMacUUid(deviceUuidBean)
    }

    override fun getCharacteristicHrUUid(): String {
        if (deviceUuidBean == null) {
            initDeviceUuidBean()
        }
        return getCharacteristicHrUUid(deviceUuidBean)
    }
}