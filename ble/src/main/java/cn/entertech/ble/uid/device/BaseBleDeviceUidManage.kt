package cn.entertech.ble.uid.device

import cn.entertech.ble.uid.BleUUIDConstants
import cn.entertech.ble.uid.characteristic.BleCharacteristicConstants
import cn.entertech.ble.uid.characteristic.BluetoothCharacteristic
import cn.entertech.ble.uid.property.BluetoothProperty
import cn.entertech.ble.uid.service.BleServiceConstants
import cn.entertech.ble.uid.service.BluetoothService

/**
 * 蓝牙设备 uid 管理类
 * */
abstract class BaseBleDeviceUidManage() {

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
        addDufService(deviceUuidBean)
        addCommandService(deviceUuidBean)
    }

    private fun addDeviceInfoService(deviceUuidBean: DeviceUuidBean) {
        val deviceInfoService =
            BluetoothService(BleUUIDConstants.UUID_0000180A_0000_1000_8000_00805F9B34FB)
        deviceInfoService.apply {
            addCharacteristic(
                BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_SERIAL_NUMBER_STRING,
                BluetoothCharacteristic(
                    BleUUIDConstants.UUID_00002A24_0000_1000_8000_00805F9B34FB, listOf(
                        BluetoothProperty.BLUETOOTH_PROPERTY_READ
                    )
                )
            )

            addCharacteristic(
                BleCharacteristicConstants.BLE_UUID_MODEL_NUMBER_STRING_CHAR,
                BluetoothCharacteristic(
                    BleUUIDConstants.UUID_00002A25_0000_1000_8000_00805F9B34FB,
                    listOf(
                        BluetoothProperty.BLUETOOTH_PROPERTY_READ
                    )
                )
            )
            addCharacteristic(
                BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_FIRMWARE_REVISION_STRING,
                BleUUIDConstants.UUID_00002A26_0000_1000_8000_00805F9B34FB,
                listOf(
                    BluetoothProperty.BLUETOOTH_PROPERTY_READ
                )
            )

            addCharacteristic(
                BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_HARDWARE_REVISION_STRING,
                BleUUIDConstants.UUID_00002A27_0000_1000_8000_00805F9B34FB,
                listOf(
                    BluetoothProperty.BLUETOOTH_PROPERTY_READ
                )
            )

            addCharacteristic(
                BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_MANUFACTURER_NAME_STRING,
                BleUUIDConstants.UUID_00002A29_0000_1000_8000_00805F9B34FB,
                listOf(
                    BluetoothProperty.BLUETOOTH_PROPERTY_READ
                )
            )
        }
        deviceUuidBean?.addService(
            BleServiceConstants.BLE_SERVICE_UUID_DEVICE_INFORMATION,
            deviceInfoService
        )
    }

    private fun addCommandService(deviceUuidBean: DeviceUuidBean) {
        val commandService =
            BluetoothService(BleUUIDConstants.UUID_0000FF20_1212_ABCD_1523_785FEABCD123)
        commandService.addCharacteristic(
            BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_COMMAND_UPLOAD,
            BleUUIDConstants.UUID_0000FF21_1212_ABCD_1523_785FEABCD123,
            listOf(BluetoothProperty.BLUETOOTH_PROPERTY_WRITE)

        )
        commandService.addCharacteristic(
            BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_COMMAND_DOWNLOAD,
            BleUUIDConstants.UUID_0000FF22_1212_ABCD_1523_785FEABCD123,
            listOf(BluetoothProperty.BLUETOOTH_PROPERTY_NOTIFY)
        )
        deviceUuidBean?.addService(BleServiceConstants.BLE_SERVICE_UUID_COMMAND, commandService)

    }

    private fun addDufService(deviceUuidBean: DeviceUuidBean) {
        val dufService =
            BluetoothService(BleUUIDConstants.UUID_0000FF40_1212_ABCD_1523_785FEABCD123)
        dufService.apply {
            addCharacteristic(
                BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_DFU_CTRL_PT,
                BleUUIDConstants.UUID_0000FF41_1212_ABCD_1523_785FEABCD123,
                listOf(
                    BluetoothProperty.BLUETOOTH_PROPERTY_NOTIFY,
                    BluetoothProperty.BLUETOOTH_PROPERTY_WRITE
                )
            )

            addCharacteristic(
                BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_DFU_PKT_CHAR,
                BleUUIDConstants.UUID_0000FF42_1212_ABCD_1523_785FEABCD123,
                listOf(
                    BluetoothProperty.BLUETOOTH_PROPERTY_WRITE_WITHOUT_RESPONSE,
                )
            )
        }
        deviceUuidBean?.addService(BleServiceConstants.BLE_SERVICE_UUID_DFU, dufService)

    }

    /**
     * 制造商信息：公司ID+自定义数据
     * */
    fun getDeviceManufacturerUuid(): String {
        return deviceUuidBean?.getService(BleServiceConstants.BLE_SERVICE_UUID_DEVICE_INFORMATION)
            ?.getCharacteristic(BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_MANUFACTURER_NAME_STRING)
            ?.uid ?: throw IllegalAccessException("do not hava Manufacturer")
    }

    fun getCharacteristicDeviceSerialUUid(): String {
        return deviceUuidBean?.getService(BleServiceConstants.BLE_SERVICE_UUID_DEVICE_INFORMATION)
            ?.getCharacteristic(BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_SERIAL_NUMBER_STRING)
            ?.uid ?: throw IllegalAccessException("do not hava Manufacturer")
    }

    fun getCharacteristicDeviceFirmwareUUid(): String {

        return deviceUuidBean?.getService(BleServiceConstants.BLE_SERVICE_UUID_DEVICE_INFORMATION)
            ?.getCharacteristic(BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_FIRMWARE_REVISION_STRING)
            ?.uid ?: throw IllegalAccessException("do not hava Manufacturer")
    }

    fun getCharacteristicDeviceHardwareUUid(): String {
        return deviceUuidBean?.getService(BleServiceConstants.BLE_SERVICE_UUID_DEVICE_INFORMATION)
            ?.getCharacteristic(BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_HARDWARE_REVISION_STRING)
            ?.uid ?: throw IllegalAccessException("do not hava Manufacturer")
    }

    fun getCharacteristicDeviceMacUUid(): String {
        return deviceUuidBean?.getService(BleServiceConstants.BLE_SERVICE_UUID_DEVICE_INFORMATION)
            ?.getCharacteristic(BleCharacteristicConstants.BLE_UUID_MODEL_NUMBER_STRING_CHAR)
            ?.uid ?: throw IllegalAccessException("do not hava Manufacturer")
    }

    fun getCharacteristicCommandDownload(): String {
        return deviceUuidBean?.getService(BleServiceConstants.BLE_SERVICE_UUID_COMMAND)
            ?.getCharacteristic(BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_COMMAND_DOWNLOAD)
            ?.uid ?: throw IllegalAccessException("do not hava Manufacturer")
    }
}