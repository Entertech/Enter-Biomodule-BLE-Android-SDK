package cn.entertech.ble.utils

import android.os.Handler
import android.os.Looper
import cn.entertech.ble.RxBleManager
import cn.entertech.ble.utils.CharUtil.converUnchart
import com.orhanobut.logger.Logger
import java.util.concurrent.CopyOnWriteArrayList

class FirmwareFixHelper constructor(var rxBleManager: RxBleManager) {

    private var mMainHandler: Handler? = null

    init {
        mMainHandler = Handler(Looper.getMainLooper())
    }

    companion object {
        @Volatile
        var mInstance: FirmwareFixHelper? = null

        fun getInstance(rxBleManager: RxBleManager): FirmwareFixHelper {
            if (mInstance == null) {
                synchronized(FirmwareFixHelper::class.java) {
                    if (mInstance == null) {
                        mInstance = FirmwareFixHelper(rxBleManager)
                    }
                }
            }
            return mInstance!!
        }
    }

    private var isFixing = false
    private var isFixing255 = false
    private var isPackage255List = CopyOnWriteArrayList<Boolean>()
    private var mFixFirmware255Runnable = Runnable {
        var is255PackageCount = 0
        for (is255 in isPackage255List) {
            if (is255) {
                is255PackageCount++
            }
        }
        if (is255PackageCount * 1f / isPackage255List.size >= 0.8f) {
            fix()
        }
        isPackage255List.clear()
        isFixing255 = false
    }

    private var mFixFirmware128Runnable = Runnable {
        fix()
        isFixing128 = false
    }

    private fun isFirmware255(bytes: ByteArray): Boolean {
        var count255 = 0
        for (byte in bytes) {
            var brainData = converUnchart(byte)
            if (brainData == 255) {
                count255++
            }
        }
        var is255 = count255 == 12
        isPackage255List.add(is255)
        return is255
    }

    private var isFixing128 = false
    private fun isFirmware128(bytes: ByteArray): Boolean {
        var count128 = 0
        for (byte in bytes) {
            var brainData = converUnchart(byte)
            if (brainData == 128) {
                count128++
            }
        }
        return count128 == 6
    }

    fun fixFirmware(bytes: ByteArray) {
        if (!isFixing128 && isFirmware128(bytes)) {
            isFixing128 = true
            mMainHandler?.post(mFixFirmware128Runnable)
        }
        if (isFirmware255(bytes) && !isFixing255) {
            isFixing255 = true
            mMainHandler?.postDelayed(mFixFirmware255Runnable, 20000)
        }
    }

    private fun fix() {
        if (isFixing) {
            Logger.d("firmware fixing....")
            rxBleManager.command(RxBleManager.Command.STOP_HEART_AND_BRAIN_COLLECT)
            rxBleManager.command(RxBleManager.Command.START_HEART_AND_BRAIN_COLLECT)
        }
    }

    fun startFix() {
        isFixing = true
    }


    fun stopFix() {
        isFixing = false
    }


}