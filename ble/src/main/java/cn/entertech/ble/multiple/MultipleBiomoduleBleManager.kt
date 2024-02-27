package cn.entertech.ble.multiple

import android.content.Context
import cn.entertech.ble.BaseBleConnectManager
import cn.entertech.ble.uid.device.BaseBleDeviceUidManage
import cn.entertech.ble.uid.device.headband.HeadbandUidManage

/**
 * 多设备
 * */
class MultipleBiomoduleBleManager constructor(
    context: Context,
    uuidManager: BaseBleDeviceUidManage
) : BaseBleConnectManager(context, uuidManager) {
    constructor(context: Context) : this(context, HeadbandUidManage)
}