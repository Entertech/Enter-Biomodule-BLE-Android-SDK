package cn.entertech.ble.multiple

import android.content.Context
import cn.entertech.ble.BaseBleConnectManager
import cn.entertech.ble.uid.device.BaseBleDeviceFactory
import cn.entertech.ble.uid.device.HeadbandFactory

/**
 * 多设备
 * */
class MultipleBiomoduleBleManager constructor(
    context: Context,
    uuidManager: BaseBleDeviceFactory
) : BaseBleConnectManager(context, uuidManager) {
    constructor(context: Context) : this(context, HeadbandFactory)
}