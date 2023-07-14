package cn.entertech.ble.utils

import android.util.Log
import com.orhanobut.logger.Logger
import java.text.SimpleDateFormat

/**
 * 蓝牙日志
 * 先封装一层，使用系统日志，方便后续变更日志库
 * */
object BleLogUtil {
    private val sb = StringBuilder()
    private const val TAG = "BleLogUtil"
    private const val BLANK = "    "


    fun i(msg: String) {
        Log.i(TAG, msg)
    }

    fun d(tag: String, msg: String) {
        Log.d(tag, msg)
    }

    fun i(tag: String, msg: String) {
        Log.i(tag, msg)
        val current = System.currentTimeMillis()
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS")
        val formatTime = currentTime.format(current)
        sb.append(formatTime).append(BLANK).append(tag).append(BLANK).appendLine(msg)
        Logger.i(sb.toString())
    }

    fun e(tag: String, msg: String) {
        Log.e(tag, msg)
    }

}