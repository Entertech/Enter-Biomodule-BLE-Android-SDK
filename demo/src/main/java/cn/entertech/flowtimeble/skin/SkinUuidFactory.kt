package cn.entertech.flowtimeble.skin

import cn.entertech.ble.uid.BleUUIDConstants
import cn.entertech.ble.uid.characteristic.BluetoothCharacteristic
import cn.entertech.ble.uid.device.BaseBluetoothDeviceUuidFactory
import cn.entertech.ble.uid.device.DeviceUuidBean
import cn.entertech.ble.uid.service.BluetoothService
import cn.entertech.ble.uid.service.IBatteryService
import cn.entertech.ble.uid.service.IEegService
import cn.entertech.ble.uid.service.IHrsService

object SkinUuidFactory : BaseBluetoothDeviceUuidFactory(),
    IHrsService, IBatteryService, IEegService,ISkinService{

    override fun initDeviceUuidService(
        deviceUuidBean: DeviceUuidBean?,
        initDeviceServiceCharacteristic: (BluetoothService) -> BluetoothService
    ) {
        super<BaseBluetoothDeviceUuidFactory>.initDeviceUuidService(deviceUuidBean,initDeviceServiceCharacteristic)
        super<IHrsService>.initDeviceUuidService(deviceUuidBean,initDeviceServiceCharacteristic)
        super<IBatteryService>.initDeviceUuidService(deviceUuidBean,initDeviceServiceCharacteristic)
        super<IEegService>.initDeviceUuidService(deviceUuidBean,initDeviceServiceCharacteristic)
        super<ISkinService>.initDeviceUuidService(deviceUuidBean,initDeviceServiceCharacteristic)
    }

    override fun getBroadcastUUid(): String {
        return BleUUIDConstants.UUID_0000FF10_1212_ABCD_1523_785FEABCD123
    }

    override fun getEEGCharacteristicOrNull(): BluetoothCharacteristic? {
        return getEEGCharacteristicOrNull(deviceUuidBean)
    }

    override fun getContactDataCharacteristicOrNull(): BluetoothCharacteristic? {
        return getContactDataCharacteristicOrNull(deviceUuidBean)
    }

    override fun getHrCharacteristicOrNull(): BluetoothCharacteristic? {
        return getHrCharacteristicOrNull(deviceUuidBean)
    }

    override fun getBatteryLevelCharacteristicOrNull(): BluetoothCharacteristic? {
        return getBatteryLevelCharacteristicOrNull(deviceUuidBean)
    }

    override fun getSkinCharacteristicOrNull(): BluetoothCharacteristic? {
        return getSkinCharacteristicOrNull(deviceUuidBean)
    }
}