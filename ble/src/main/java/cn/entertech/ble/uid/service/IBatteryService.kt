package cn.entertech.ble.uid.service

import cn.entertech.ble.uid.BleUUIDConstants
import cn.entertech.ble.uid.characteristic.BleCharacteristicConstants
import cn.entertech.ble.uid.device.DeviceUuidBean
import cn.entertech.ble.uid.property.BluetoothProperty

interface IBatteryService {

    fun addBatteryService(deviceUuidBean: DeviceUuidBean?) {
        val batteryService =
            BluetoothService(BleUUIDConstants.UUID_0000180F_0000_1000_8000_00805F9B34FB)
        batteryService.addCharacteristic(
            BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_BATTERY_LEVEL,
            BleUUIDConstants.UUID_00002A19_0000_1000_8000_00805F9B34FB,
            listOf(
                BluetoothProperty.BLUETOOTH_PROPERTY_NOTIFY,
                BluetoothProperty.BLUETOOTH_PROPERTY_READ
            )
        )
        deviceUuidBean?.addService(BleServiceConstants.BLE_SERVICE_UUID_BATTERY, batteryService)

    }
}