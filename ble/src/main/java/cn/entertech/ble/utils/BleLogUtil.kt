package cn.entertech.ble.utils

import android.util.Log

/**
 * 蓝牙日志
 * 先封装一层，使用系统日志，方便后续变更日志库
 * */
object BleLogUtil {

    private const val TAG = "BleLogUtil"

    fun d(tag: String = TAG, msg: Any) {
        Log.d(tag, msg.toString())
    }

    fun i(tag: String = TAG, msg: String) {
        Log.i(tag, msg)
    }

    fun e(tag: String = TAG, msg: String) {
        Log.e(tag, msg)
    }

}