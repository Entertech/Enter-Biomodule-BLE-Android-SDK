package cn.entertech.ble.single

import android.content.Context
import cn.entertech.ble.BaseBleConnectManager
import cn.entertech.ble.uid.device.BaseBleDeviceUidManage
import cn.entertech.ble.uid.device.headband.HeadbandUidManage

/**
 * 单设备
 * */
class BiomoduleBleManager private constructor(
    context: Context,
    uuidManager: BaseBleDeviceUidManage
) : BaseBleConnectManager(context, uuidManager) {

    companion object {
        @Volatile
        var mBleDeviceManager: BiomoduleBleManager? = null

        fun getInstance(context: Context): BiomoduleBleManager {
            return getInstance(context, HeadbandUidManage)
        }


        fun getInstance(
            context: Context,
            uuidManager: BaseBleDeviceUidManage
        ): BiomoduleBleManager {
            if (mBleDeviceManager == null) {
                synchronized(BiomoduleBleManager::class.java) {
                    if (mBleDeviceManager == null) {
                        mBleDeviceManager = BiomoduleBleManager(context, uuidManager)
                    }
                }
            }
            return mBleDeviceManager!!
        }

        private const val TAG = "BiomoduleBleManager"
    }

}