package cn.entertech.ble.uid.service

import cn.entertech.ble.uid.BleUUIDConstants
import cn.entertech.ble.uid.characteristic.BleCharacteristicConstants
import cn.entertech.ble.uid.device.DeviceUuidBean
import cn.entertech.ble.uid.property.BluetoothProperty

interface IEegService {

    fun addEegService(deviceUuidBean: DeviceUuidBean?) {
        val eegService =
            BluetoothService(BleUUIDConstants.UUID_0000FF30_1212_ABCD_1523_785FEABCD123)
        eegService.addCharacteristic(
            BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_EEG_DATA,
            BleUUIDConstants.UUID_0000FF31_1212_ABCD_1523_785FEABCD123,
            listOf(BluetoothProperty.BLUETOOTH_PROPERTY_NOTIFY)
        )

        eegService.addCharacteristic(
            BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_CONTACT_DATA,
            BleUUIDConstants.UUID_0000FF32_1212_ABCD_1523_785FEABCD123,
            listOf(
                BluetoothProperty.BLUETOOTH_PROPERTY_NOTIFY,
                BluetoothProperty.BLUETOOTH_PROPERTY_READ,
            )
        )
        deviceUuidBean?.addService(BleServiceConstants.BLE_SERVICE_UUID_EEG, eegService)
    }


    fun getCharacteristicEEGUUid(): String

    fun getCharacteristicEEGUUid(deviceUuidBean: DeviceUuidBean?): String {
        return deviceUuidBean?.getService(BleServiceConstants.BLE_SERVICE_UUID_EEG)
            ?.getCharacteristic(BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_EEG_DATA)
            ?.uid ?: throw IllegalAccessException("do not hava EEG")
    }


    fun getCharacteristicContactDateMacUUid(deviceUuidBean: DeviceUuidBean?): String {
        return deviceUuidBean?.getService(BleServiceConstants.BLE_SERVICE_UUID_EEG)
            ?.getCharacteristic(BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_CONTACT_DATA)
            ?.uid ?: throw IllegalAccessException("do not hava Mac")
    }

    fun getCharacteristicContactDateMacUUid(): String
}