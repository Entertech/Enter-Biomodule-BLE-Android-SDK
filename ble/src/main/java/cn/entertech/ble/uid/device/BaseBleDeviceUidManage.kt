package cn.entertech.ble.uid.device

/**
 * 蓝牙设备 uid 管理类
 * */
abstract class BaseBleDeviceUidManage {

    protected var deviceUuidBean: DeviceUuidBean? = null

    /**
     * 获取广播uuid
     * 扫描搜索靠这个uuid 查找到设备
     * */
    abstract fun getBroadcastUUid(): String?

    abstract fun initDeviceUuidBean()

    /**
     * 制造商信息：公司ID+自定义数据
     * */
    abstract fun getDeviceManufacturerUuid(): String
    abstract fun getCharacteristicDeviceSerialUUid(): String
    abstract fun getCharacteristicDeviceFirmwareUUid(): String
    abstract fun getCharacteristicDeviceHardwareUUid(): String
    abstract fun getCharacteristicDeviceMacUUid(): String

    abstract fun getCharacteristicCommandDownload(): String
    abstract fun getCharacteristicBatteryLevelUUid(): String
    abstract fun getCharacteristicHrUUid(): String
    abstract fun getCharacteristicEEGUUid(): String
    abstract fun getCharacteristicContactDateMacUUid(): String
}