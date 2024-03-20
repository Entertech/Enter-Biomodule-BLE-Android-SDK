package cn.entertech.ble.single

import android.content.Context
import cn.entertech.ble.BaseBleConnectManager
import cn.entertech.ble.uid.device.BaseBleDeviceFactory
import cn.entertech.ble.uid.device.HeadbandFactory

/**
 * 单设备
 * */
class BiomoduleBleManager private constructor(
    context: Context,
    uuidManager: BaseBleDeviceFactory
) : BaseBleConnectManager(context, uuidManager) {

    companion object {
        @Volatile
        var mBleDeviceManager: BiomoduleBleManager? = null

        fun getInstance(context: Context): BiomoduleBleManager {
            return getInstance(context, HeadbandFactory)
        }


        fun getInstance(
            context: Context,
            uuidManager: BaseBleDeviceFactory
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