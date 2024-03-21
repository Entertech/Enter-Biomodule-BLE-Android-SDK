package cn.entertech.ble.uid.service

import cn.entertech.ble.uid.BleUUIDConstants
import cn.entertech.ble.uid.characteristic.BleCharacteristicConstants
import cn.entertech.ble.uid.characteristic.BluetoothCharacteristic
import cn.entertech.ble.uid.device.DeviceUuidBean
import cn.entertech.ble.uid.property.BluetoothProperty

/**
* 设备信息服务
* */
interface IDeviceInfoService {

    fun addDeviceInfoService(deviceUuidBean: DeviceUuidBean) {
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
        deviceUuidBean.addService(
            BleServiceConstants.BLE_SERVICE_UUID_DEVICE_INFORMATION,
            deviceInfoService
        )
    }

    fun getDeviceManufacturerUuid(): String

    /**
     * 制造商信息：公司ID+自定义数据
     * */
    fun getDeviceManufacturerUuid(deviceUuidBean: DeviceUuidBean): String {
        return deviceUuidBean.getService(BleServiceConstants.BLE_SERVICE_UUID_DEVICE_INFORMATION)
            ?.getCharacteristic(BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_MANUFACTURER_NAME_STRING)
            ?.uid ?: throw IllegalAccessException("do not hava Manufacturer")
    }


    fun getCharacteristicDeviceSerialUUid(): String
    fun getCharacteristicDeviceSerialUUid(deviceUuidBean: DeviceUuidBean): String {
        return deviceUuidBean.getService(BleServiceConstants.BLE_SERVICE_UUID_DEVICE_INFORMATION)
            ?.getCharacteristic(BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_SERIAL_NUMBER_STRING)
            ?.uid ?: throw IllegalAccessException("do not hava DeviceSerial")
    }

    fun getCharacteristicDeviceFirmwareUUid(): String
    fun getCharacteristicDeviceFirmwareUUid(deviceUuidBean: DeviceUuidBean): String {

        return deviceUuidBean.getService(BleServiceConstants.BLE_SERVICE_UUID_DEVICE_INFORMATION)
            ?.getCharacteristic(BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_FIRMWARE_REVISION_STRING)
            ?.uid ?: throw IllegalAccessException("do not hava DeviceFirmware")
    }

    fun getCharacteristicDeviceHardwareUUid(): String
    fun getCharacteristicDeviceHardwareUUid(deviceUuidBean: DeviceUuidBean): String {
        return deviceUuidBean.getService(BleServiceConstants.BLE_SERVICE_UUID_DEVICE_INFORMATION)
            ?.getCharacteristic(BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_HARDWARE_REVISION_STRING)
            ?.uid ?: throw IllegalAccessException("do not hava DeviceHardware")
    }

    fun getCharacteristicDeviceMacUUid(): String
    fun getCharacteristicDeviceMacUUid(deviceUuidBean: DeviceUuidBean): String {
        return deviceUuidBean.getService(BleServiceConstants.BLE_SERVICE_UUID_DEVICE_INFORMATION)
            ?.getCharacteristic(BleCharacteristicConstants.BLE_UUID_MODEL_NUMBER_STRING_CHAR)
            ?.uid ?: throw IllegalAccessException("do not hava DeviceMac")
    }
}