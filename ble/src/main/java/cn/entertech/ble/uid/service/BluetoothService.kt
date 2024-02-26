package cn.entertech.ble.uid.service

import cn.entertech.ble.uid.characteristic.BleCharacteristicConstants
import cn.entertech.ble.uid.characteristic.BluetoothCharacteristic
import cn.entertech.ble.uid.property.BluetoothProperty

class BluetoothService(val uuid: String) {
    private val characteristics: MutableMap<String, BluetoothCharacteristic> = HashMap()

    fun addCharacteristic(serviceName: String, characteristic: BluetoothCharacteristic) {
        characteristics[serviceName] = characteristic
    }

    fun addCharacteristic(
        serviceName: String,
        characteristicUuid: String,
        properties: List<BluetoothProperty>
    ) {
        addCharacteristic(serviceName, BluetoothCharacteristic(characteristicUuid, properties))
    }

    fun getDeviceManufacturer(): BluetoothCharacteristic? =
        characteristics[BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_MANUFACTURER_NAME_STRING]

    fun getCharacteristic(characteristicName: String): BluetoothCharacteristic? =
        characteristics[characteristicName]

}