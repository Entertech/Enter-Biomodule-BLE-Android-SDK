package cn.entertech.ble.utils

import android.os.Handler
import android.os.Looper
import cn.entertech.ble.RxBleManager
import cn.entertech.ble.utils.CharUtil.converUnchart

class FirmwareFixHelper constructor(var rxBleManager: RxBleManager) {

    private var mMainHandler: Handler = Handler(Looper.getMainLooper())

    var isLastByte128 = false
    var isCurrentByte128 = false

    /**期间内 255的总个数*/
    var count255Total = 0L
    var countByteTotal = 0L

    /**
     * 上一次fix 255 时间
     * */
    private var lastFix255Time = -1L


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

        private const val TAG = "FirmwareFixHelper"
    }

    private var isFixing = false
    private var isFixing255 = false
    private var isFixing128 = false



    private fun checkSourceData(bytes: ByteArray):Pair<Boolean,Boolean>{
        countByteTotal += bytes.size
        var count128 = 0
        var count255 = 0
        for (byte in bytes) {
            val brainData = converUnchart(byte)
            //0X80
            if (brainData == 128) {
                count128++
            }
            if (brainData == 255) {
                count255++
            }
        }
        count255Total += count255
        return Pair(count128 == 6, count255 == 12)
    }


    @Synchronized
    fun fixFirmware(bytes: ByteArray) {
        val pair = checkSourceData(bytes)
        isCurrentByte128 = pair.first
        if (isLastByte128 && !isCurrentByte128) {
            isFixing128 = false
        }
        isLastByte128 = isCurrentByte128
        if (!isFixing128 && isCurrentByte128) {
            isFixing128 = true
            fix()
        }
        if (pair.second && !isFixing255) {
            isFixing255 = true
            val currentTime = System.currentTimeMillis()
            if (lastFix255Time == -1L) {
                lastFix255Time = currentTime
            }
            if (currentTime - lastFix255Time >= 20000) {
                BleLogUtil.d(TAG, "check fix")
                if ((count255Total * 1f / countByteTotal) >= 0.8f) {
                    fix()
                }
                count255Total = 0L
                countByteTotal = 0L
                isFixing255 = false
                lastFix255Time = currentTime
            }
        }
    }

    private fun fix() {
        BleLogUtil.d(TAG,"fix")
        if (isFixing) {
            BleLogUtil.d(TAG,"firmware fixing....")
            mMainHandler.post {
                rxBleManager.command(RxBleManager.Command.STOP_HEART_AND_BRAIN_COLLECT)
                rxBleManager.command(RxBleManager.Command.START_HEART_AND_BRAIN_COLLECT)
            }

        }
    }

    fun startFix() {
        isFixing = true
    }


    fun stopFix() {
        isFixing = false
    }


}