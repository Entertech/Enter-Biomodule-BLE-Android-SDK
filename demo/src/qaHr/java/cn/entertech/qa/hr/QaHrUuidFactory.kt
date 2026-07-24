package cn.entertech.qa.hr

import cn.entertech.ble.BaseBluetoothDeviceUuidFactory
import cn.entertech.ble.DeviceUuidBean
import cn.entertech.ble.uid.characteristic.BluetoothCharacteristic
import cn.entertech.ble.uid.service.IBatteryService
import cn.entertech.ble.uid.service.IBrainWaveContactService

object QaHrUuidFactory : BaseBluetoothDeviceUuidFactory(), IQaHrService, IBatteryService,
    IBrainWaveContactService {

    override fun initDeviceUuidService(
        deviceUuidBean: DeviceUuidBean?,
        initDeviceServiceCharacteristic: (cn.entertech.ble.uid.service.BluetoothService) -> cn.entertech.ble.uid.service.BluetoothService
    ) {
        super<BaseBluetoothDeviceUuidFactory>.initDeviceUuidService(
            deviceUuidBean, initDeviceServiceCharacteristic
        )
        super<IQaHrService>.initDeviceUuidService(deviceUuidBean, initDeviceServiceCharacteristic)
        super<IBatteryService>.initDeviceUuidService(
            deviceUuidBean, initDeviceServiceCharacteristic
        )
        super<IBrainWaveContactService>.initDeviceUuidService(
            deviceUuidBean, initDeviceServiceCharacteristic
        )
    }

    override fun getHrRawCharacteristicOrNull(): BluetoothCharacteristic? {
        return getHrRawCharacteristicOrNull(deviceUuidBean)
    }

    override fun getHrCharacteristicOrNull(): BluetoothCharacteristic? {
        return getHrCharacteristicOrNull(deviceUuidBean)
    }

    override fun getBroadcastUUid(): String {
        return cn.entertech.ble.uid.BleUUIDConstants.UUID_0000FF10_1212_ABCD_1523_785FEABCD123
    }

    override fun getBrainWaveCharacteristicOrNull(): BluetoothCharacteristic? {
        return getBrainWaveCharacteristicOrNull(deviceUuidBean)
    }

    override fun getContactDataCharacteristicOrNull(): BluetoothCharacteristic? {
        return getContactDataCharacteristicOrNull(deviceUuidBean)
    }

    override fun getBatteryLevelCharacteristicOrNull(): BluetoothCharacteristic? {
        return getBatteryLevelCharacteristicOrNull(deviceUuidBean)
    }
}