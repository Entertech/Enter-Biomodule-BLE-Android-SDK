package cn.entertech.ble.multiple

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
 * 多设备
 * */
class MultipleBiomoduleBleManager constructor(context: Context):BaseBleConnectManager(context)