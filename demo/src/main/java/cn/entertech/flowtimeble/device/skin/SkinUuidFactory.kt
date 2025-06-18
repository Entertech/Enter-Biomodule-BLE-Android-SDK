package cn.entertech.flowtimeble.device.skin

import cn.entertech.ble.BaseBluetoothDeviceUuidFactory
import cn.entertech.ble.DeviceUuidBean
import cn.entertech.ble.uid.service.IBatteryService
import cn.entertech.ble.uid.service.IBrainWaveContactService
import cn.entertech.ble.uid.service.IHrsService

object SkinUuidFactory : BaseBluetoothDeviceUuidFactory(),
    IHrsService, IBatteryService, IBrainWaveContactService, ISkinService {

    override fun initDeviceUuidService(
        deviceUuidBean: DeviceUuidBean?,
        initDeviceServiceCharacteristic: (cn.entertech.ble.uid.service.BluetoothService) -> cn.entertech.ble.uid.service.BluetoothService
    ) {
        super<BaseBluetoothDeviceUuidFactory>.initDeviceUuidService(deviceUuidBean,initDeviceServiceCharacteristic)
        super<IHrsService>.initDeviceUuidService(deviceUuidBean,initDeviceServiceCharacteristic)
        super<IBatteryService>.initDeviceUuidService(deviceUuidBean,initDeviceServiceCharacteristic)
        super<IBrainWaveContactService>.initDeviceUuidService(deviceUuidBean,initDeviceServiceCharacteristic)
        super<ISkinService>.initDeviceUuidService(deviceUuidBean,initDeviceServiceCharacteristic)
    }

    override fun getBroadcastUUid(): String {
        return cn.entertech.ble.uid.BleUUIDConstants.UUID_0000FF10_1212_ABCD_1523_785FEABCD123
    }

    override fun getBrainWaveCharacteristicOrNull(): cn.entertech.ble.uid.characteristic.BluetoothCharacteristic? {
        return getBrainWaveCharacteristicOrNull(deviceUuidBean)
    }

    override fun getContactDataCharacteristicOrNull(): cn.entertech.ble.uid.characteristic.BluetoothCharacteristic? {
        return getContactDataCharacteristicOrNull(deviceUuidBean)
    }

    override fun getHrCharacteristicOrNull(): cn.entertech.ble.uid.characteristic.BluetoothCharacteristic? {
        return getHrCharacteristicOrNull(deviceUuidBean)
    }

    override fun getBatteryLevelCharacteristicOrNull(): cn.entertech.ble.uid.characteristic.BluetoothCharacteristic? {
        return getBatteryLevelCharacteristicOrNull(deviceUuidBean)
    }

    override fun getSkinCharacteristicOrNull(): cn.entertech.ble.uid.characteristic.BluetoothCharacteristic? {
        return getSkinCharacteristicOrNull(deviceUuidBean)
    }
}