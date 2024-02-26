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

    override fun getDeviceManufacturerUuid(): String {
        return "00002A29-0000-1000-8000-00805F9B34FB"
    }

    override fun getCharacteristicCommandDownload(): String {
        return "0000FF21-1212-abcd-1523-785feabcd123"
    }

    override fun getCharacteristicBatteryLevelUUid(): String {
        return "00002A19-0000-1000-8000-00805F9B34FB"
    }

    override fun getCharacteristicHrUUid(): String {
        return "0000FF51-1212-abcd-1523-785feabcd123"
    }

    override fun getCharacteristicEEGUUid(): String {
        return "0000FF31-1212-abcd-1523-785feabcd123"
    }

    override fun getCharacteristicDeviceSerialUUid(): String {
        return "00002A25-0000-1000-8000-00805F9B34FB"
    }

    override fun getCharacteristicDeviceFirmwareUUid(): String {
        return "00002A26-0000-1000-8000-00805F9B34FB"
    }

    override fun getCharacteristicDeviceHardwareUUid(): String {
        return "00002A27-0000-1000-8000-00805F9B34FB"
    }

    override fun getCharacteristicDeviceMacUUid(): String {
        return "00002A24-0000-1000-8000-00805F9B34FB"
    }

    override fun getCharacteristicContactDateMacUUid(): String {
        return "0000FF32-1212-abcd-1523-785feabcd123"
    }
}