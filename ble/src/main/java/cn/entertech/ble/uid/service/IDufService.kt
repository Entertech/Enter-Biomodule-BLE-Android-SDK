package cn.entertech.ble.uid.service

import cn.entertech.ble.uid.BleUUIDConstants
import cn.entertech.ble.uid.characteristic.BleCharacteristicConstants
import cn.entertech.ble.uid.device.DeviceUuidBean
import cn.entertech.ble.uid.property.BluetoothProperty

interface IDufService {

    fun addDufService(deviceUuidBean: DeviceUuidBean) {
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
        deviceUuidBean.addService(BleServiceConstants.BLE_SERVICE_UUID_DFU, dufService)
    }
}