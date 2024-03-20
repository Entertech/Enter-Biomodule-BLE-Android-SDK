package cn.entertech.ble.uid.service

import cn.entertech.ble.uid.BleUUIDConstants
import cn.entertech.ble.uid.characteristic.BleCharacteristicConstants
import cn.entertech.ble.uid.device.DeviceUuidBean
import cn.entertech.ble.uid.property.BluetoothProperty

interface ISkinConductanceService {

    fun addSkinConductanceService(deviceUuidBean: DeviceUuidBean) {
        val skinConductance =
            BluetoothService(BleUUIDConstants.UUID_0000FF40_1212_ABCD_1523_785FEABCD123)
        skinConductance.apply {
            addCharacteristic(
                BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_SKIN_CONDUCTANCE_DATA,
                BleUUIDConstants.UUID_0000FF41_1212_ABCD_1523_785FEABCD123,
                listOf(
                    BluetoothProperty.BLUETOOTH_PROPERTY_NOTIFY
                )
            )
        }
        deviceUuidBean.addService(
            BleServiceConstants.BLE_SERVICE_UUID_SKIN_CONDUCTANCE,
            skinConductance
        )
    }

    fun getSkinConductanceUUId(): String

    fun getSkinConductanceUUId(deviceUuidBean: DeviceUuidBean): String {
        return deviceUuidBean.getService(BleServiceConstants.BLE_SERVICE_UUID_SKIN_CONDUCTANCE)
            ?.getCharacteristic(BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_SKIN_CONDUCTANCE_DATA)?.uid?: throw IllegalAccessException("do not hava SkinConductance")
    }
}