package cn.entertech.ble.single

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import cn.entertech.ble.BaseBleConnectManager
import cn.entertech.ble.ConnectionBleStrategy
import cn.entertech.ble.RxBleManager
import cn.entertech.ble.RxBleManager.Companion.SCAN_TIMEOUT
import cn.entertech.ble.utils.*
import io.reactivex.disposables.Disposable
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.Exception

/**
 * 单设备
 * */
class BiomoduleBleManager private constructor(context: Context) : BaseBleConnectManager(context) {

    companion object {
        @Volatile
        var mBleDeviceManager: BiomoduleBleManager? = null

        fun getInstance(context: Context): BiomoduleBleManager {
            if (mBleDeviceManager == null) {
                synchronized(BiomoduleBleManager::class.java) {
                    if (mBleDeviceManager == null) {
                        mBleDeviceManager = BiomoduleBleManager(context)
                    }
                }
            }
            return mBleDeviceManager!!
        }

        private const val TAG = "BiomoduleBleManager"
    }

}