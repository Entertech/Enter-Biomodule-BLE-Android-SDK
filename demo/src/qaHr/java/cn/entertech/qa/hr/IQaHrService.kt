package cn.entertech.qa.hr

import cn.entertech.ble.uid.BleUUIDConstants
import cn.entertech.ble.uid.property.BluetoothProperty
import cn.entertech.ble.uid.service.BluetoothService
import cn.entertech.ble.uid.service.IBluetoothDeviceFunctionUuidService

const val BLE_CHARACTERISTIC_UUID_HR_RAW_NAME = "BLE_CHARACTERISTIC_UUID_HR_RAW_NAME"
const val BLE_CHARACTERISTIC_UUID_HR_NAME = "BLE_CHARACTERISTIC_UUID_HR_NAME"
const val BLE_SERVICE_UUID_HR_NAME = "BLE_SERVICE_UUID_HR_NAME"

interface IQaHrService : IBluetoothDeviceFunctionUuidService {

    fun getHrCharacteristicOrNull(): cn.entertech.ble.uid.characteristic.BluetoothCharacteristic?

    fun getHrCharacteristicOrNull(deviceUuidBean: cn.entertech.ble.DeviceUuidBean?): cn.entertech.ble.uid.characteristic.BluetoothCharacteristic? {
        return getCharacteristicOrNull(
            deviceUuidBean,
            BLE_SERVICE_UUID_HR_NAME,
            BLE_CHARACTERISTIC_UUID_HR_NAME
        )
    }

    fun getHrRawCharacteristicOrNull(): cn.entertech.ble.uid.characteristic.BluetoothCharacteristic?

    fun getHrRawCharacteristicOrNull(deviceUuidBean: cn.entertech.ble.DeviceUuidBean?): cn.entertech.ble.uid.characteristic.BluetoothCharacteristic? {
        return getCharacteristicOrNull(
            deviceUuidBean,
            BLE_SERVICE_UUID_HR_NAME,
            BLE_CHARACTERISTIC_UUID_HR_RAW_NAME
        )
    }

    override fun initDeviceUuidService(
        deviceUuidBean: cn.entertech.ble.DeviceUuidBean?,
        initDeviceServiceCharacteristic: (BluetoothService) -> BluetoothService
    ) {
        val hrService =
            BluetoothService(BleUUIDConstants.UUID_0000FF50_1212_ABCD_1523_785FEABCD123)
        hrService.addCharacteristic(
            BLE_CHARACTERISTIC_UUID_HR_NAME,
            BleUUIDConstants.UUID_0000FF51_1212_ABCD_1523_785FEABCD123,
            listOf(
                BluetoothProperty.BLUETOOTH_PROPERTY_NOTIFY
            )
        )
        hrService.addCharacteristic(
            BLE_CHARACTERISTIC_UUID_HR_RAW_NAME,
            BleUUIDConstants.UUID_0000FF52_1212_ABCD_1523_785FEABCD123,
            listOf(
                BluetoothProperty.BLUETOOTH_PROPERTY_NOTIFY
            )
        )
        deviceUuidBean?.addService(
            BLE_SERVICE_UUID_HR_NAME, initDeviceServiceCharacteristic(hrService)
        )
    }

}