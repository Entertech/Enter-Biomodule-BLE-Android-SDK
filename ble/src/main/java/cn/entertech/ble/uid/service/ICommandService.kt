package cn.entertech.ble.uid.service

import cn.entertech.ble.uid.BleUUIDConstants
import cn.entertech.ble.uid.characteristic.BleCharacteristicConstants
import cn.entertech.ble.uid.device.DeviceUuidBean
import cn.entertech.ble.uid.property.BluetoothProperty

/**
 * 指令服务
 * */
interface ICommandService {

    fun addCommandService(deviceUuidBean: DeviceUuidBean) {
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
        deviceUuidBean.addService(BleServiceConstants.BLE_SERVICE_UUID_COMMAND, commandService)

    }

    fun getCharacteristicCommandUploadUUid(): String

    fun getCharacteristicCommandUploadUUid(deviceUuidBean: DeviceUuidBean): String {
        return deviceUuidBean.getService(BleServiceConstants.BLE_SERVICE_UUID_COMMAND)
            ?.getCharacteristic(BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_COMMAND_UPLOAD)
            ?.uid ?: throw IllegalAccessException("do not hava CommandUpload")
    }
}