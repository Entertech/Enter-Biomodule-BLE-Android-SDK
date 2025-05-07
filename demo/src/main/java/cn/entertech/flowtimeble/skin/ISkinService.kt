package cn.entertech.flowtimeble.skin

import cn.entertech.ble.uid.BleUUIDConstants
import cn.entertech.ble.uid.characteristic.BluetoothCharacteristic
import cn.entertech.ble.uid.device.DeviceUuidBean
import cn.entertech.ble.uid.property.BluetoothProperty
import cn.entertech.ble.uid.service.BluetoothService
import cn.entertech.ble.uid.service.IBluetoothDeviceFunctionUuidService

interface ISkinService : IBluetoothDeviceFunctionUuidService {

    companion object{
        private const val BLE_CHARACTERISTIC_UUID_SKIN_DATA="BLE_CHARACTERISTIC_UUID_SKIN_DATA"
        private const val BLE_SERVICE_UUID_SKIN="BLE_SERVICE_UUID_SKIN"
    }

    override fun initDeviceUuidService(
        deviceUuidBean: DeviceUuidBean?,
        initDeviceServiceCharacteristic: (BluetoothService) -> BluetoothService
    ) {
        val hrsService =
            BluetoothService(BleUUIDConstants.UUID_0000FF40_1212_ABCD_1523_785FEABCD123)
        hrsService.addCharacteristic(
            BLE_CHARACTERISTIC_UUID_SKIN_DATA,
            BleUUIDConstants.UUID_0000FF41_1212_ABCD_1523_785FEABCD123,
            listOf(
                BluetoothProperty.BLUETOOTH_PROPERTY_NOTIFY
            )
        )
        deviceUuidBean?.addService(
            BLE_SERVICE_UUID_SKIN, initDeviceServiceCharacteristic(hrsService)
        )
    }

    fun getSkinCharacteristicOrNull(): BluetoothCharacteristic? = null

    fun getSkinCharacteristicOrNull(deviceUuidBean: DeviceUuidBean?): BluetoothCharacteristic? {
        return getCharacteristicOrNull(
            deviceUuidBean,
            BLE_SERVICE_UUID_SKIN,
            BLE_CHARACTERISTIC_UUID_SKIN_DATA
        )
    }
}