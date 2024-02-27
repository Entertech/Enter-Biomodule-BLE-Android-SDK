package cn.entertech.ble.uid.device

import cn.entertech.ble.uid.service.ICommandService
import cn.entertech.ble.uid.service.IDeviceInfoService

/**
 * 蓝牙设备 uid 管理类
 * */
abstract class BaseBleDeviceUidManage : IDeviceInfoService, ICommandService {

    protected val deviceUuidBean by lazy {
        val deviceUuidBean = DeviceUuidBean(getBroadcastUUid())
        initDeviceUuidBean(deviceUuidBean)
        deviceUuidBean
    }

    /**
     * 获取广播uuid
     * 扫描搜索靠这个uuid 查找到设备
     * */
    abstract fun getBroadcastUUid(): String

    protected open fun initDeviceUuidBean(deviceUuidBean: DeviceUuidBean) {
        addDeviceInfoService(deviceUuidBean)
        addCommandService(deviceUuidBean)
    }

    override fun getCharacteristicCommandDownload(): String {
        return getCharacteristicCommandDownload(deviceUuidBean)
    }

    override fun getDeviceManufacturerUuid(): String {
        return getDeviceManufacturerUuid(deviceUuidBean)
    }

    override fun getCharacteristicDeviceSerialUUid(): String {
        return getCharacteristicDeviceSerialUUid(deviceUuidBean)
    }

    override fun getCharacteristicDeviceFirmwareUUid(): String {
        return getCharacteristicDeviceFirmwareUUid(deviceUuidBean)
    }

    override fun getCharacteristicDeviceHardwareUUid(): String {
        return getCharacteristicDeviceHardwareUUid(deviceUuidBean)
    }

    override fun getCharacteristicDeviceMacUUid(): String {
        return getCharacteristicDeviceMacUUid(deviceUuidBean)
    }
}