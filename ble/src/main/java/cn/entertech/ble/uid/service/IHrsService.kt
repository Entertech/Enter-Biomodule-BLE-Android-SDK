package cn.entertech.ble.uid.service

import cn.entertech.ble.uid.BleUUIDConstants
import cn.entertech.ble.uid.characteristic.BleCharacteristicConstants
import cn.entertech.ble.uid.device.DeviceUuidBean
import cn.entertech.ble.uid.property.BluetoothProperty

interface IHrsService {

    fun addHrsService(deviceUuidBean: DeviceUuidBean?) {
        val hrsService =
            BluetoothService(BleUUIDConstants.UUID_0000FF50_1212_ABCD_1523_785FEABCD123)
        hrsService.addCharacteristic(
            BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_HRS_DATA,
            BleUUIDConstants.UUID_0000FF51_1212_ABCD_1523_785FEABCD123,
            listOf(
                BluetoothProperty.BLUETOOTH_PROPERTY_NOTIFY
            )
        )
        deviceUuidBean?.addService(BleServiceConstants.BLE_SERVICE_UUID_HRS, hrsService)
    }


    fun getCharacteristicHrUUid(): String

    fun getCharacteristicHrUUid(deviceUuidBean: DeviceUuidBean?): String {
        return deviceUuidBean?.getService(BleServiceConstants.BLE_SERVICE_UUID_HRS)
            ?.getCharacteristic(BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_HRS_DATA)
            ?.uid ?: throw IllegalAccessException("do not hava Hrs")
    }
}