package cn.entertech.ble.util

import com.orhanobut.logger.Logger

/**
 * Created by EnterTech on 2018/2/8.
 */
object BatteryUtil {

    fun getMinutesLeft(byte: Byte): NapBattery {
        ((byte / 100.0) * (4.1 - 3.1) + 3.1).let {
            val minutes = -12050 * Math.pow(it, 4.0) + 137175 * Math.pow(it, 3.0) - 517145 * Math.pow(it, 2.0) + 644850 * it + 5034
//            val minutes = 229607.31 * Math.pow(it, 4.0)  - 3581456.13 * Math.pow(it, 3.0) + 20922054.15 * Math.pow(it, 2.0) - 54241619.24* it + 52651001.51
            val percent = (minutes * 100 / (81 * 60)).toInt()
            val hours = (minutes / 60).toInt()
            val minutesLast = (minutes % 60).toInt()

            return NapBattery(hours, minutesLast, percent)
        }
    }
}

data class NapBattery(val hours: Int, val minutes: Int, val percent: Int){
    override fun toString(): String {
        return "NapBattery(hours=$hours, minutes=$minutes, percent=$percent)"
    }
}