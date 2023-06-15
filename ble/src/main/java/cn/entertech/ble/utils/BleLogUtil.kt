package cn.entertech.ble.utils

import android.util.Log

/**
 * 蓝牙日志
 * 先封装一层，使用系统日志，方便后续变更日志库
 * */
object BleLogUtil {

    fun d(tag:String,msg:String){
        Log.d(tag,msg)
    }

    fun i(tag:String,msg:String){
        Log.i(tag,msg)
    }

    fun e(tag: String,msg:String){
        Log.e(tag,msg)
    }

}