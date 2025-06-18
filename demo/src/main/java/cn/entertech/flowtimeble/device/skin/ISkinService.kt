package cn.entertech.flowtimeble.device.skin

import cn.entertech.ble.DeviceUuidBean

interface ISkinService : cn.entertech.ble.uid.service.IBluetoothDeviceFunctionUuidService {

    companion object{
        private const val BLE_CHARACTERISTIC_UUID_SKIN_DATA="BLE_CHARACTERISTIC_UUID_SKIN_DATA"
        private const val BLE_SERVICE_UUID_SKIN="BLE_SERVICE_UUID_SKIN"
    }

    override fun initDeviceUuidService(
        deviceUuidBean: DeviceUuidBean?,
        initDeviceServiceCharacteristic: (cn.entertech.ble.uid.service.BluetoothService) -> cn.entertech.ble.uid.service.BluetoothService
    ) {
        val hrsService =
            cn.entertech.ble.uid.service.BluetoothService(cn.entertech.ble.uid.BleUUIDConstants.UUID_0000FF40_1212_ABCD_1523_785FEABCD123)
        hrsService.addCharacteristic(
            BLE_CHARACTERISTIC_UUID_SKIN_DATA,
            cn.entertech.ble.uid.BleUUIDConstants.UUID_0000FF41_1212_ABCD_1523_785FEABCD123,
            listOf(
                cn.entertech.ble.uid.property.BluetoothProperty.BLUETOOTH_PROPERTY_NOTIFY
            )
        )
        deviceUuidBean?.addService(
            BLE_SERVICE_UUID_SKIN, initDeviceServiceCharacteristic(hrsService)
        )
    }

    fun getSkinCharacteristicOrNull(): cn.entertech.ble.uid.characteristic.BluetoothCharacteristic? = null

    fun getSkinCharacteristicOrNull(deviceUuidBean: DeviceUuidBean?): cn.entertech.ble.uid.characteristic.BluetoothCharacteristic? {
        return getCharacteristicOrNull(
            deviceUuidBean,
            BLE_SERVICE_UUID_SKIN,
            BLE_CHARACTERISTIC_UUID_SKIN_DATA
        )
    }
}