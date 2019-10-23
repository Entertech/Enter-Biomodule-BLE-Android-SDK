package cn.entertech.ble.utils

import cn.entertech.ble.R
import cn.entertech.ble.ui.widget.DeviceConnectView
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Created by EnterTech on 2018/2/8.
 */
object BatteryUtil {

    fun getMinutesLeft(byte: Byte): NapBattery {
        ((byte / 100.0) * (4.1 - 3.1) + 3.1).let {
            var a1 = 99.84
            var b1 = 4.244
            var c1 = 0.3781
            var a2 = 21.38
            var b2 = 3.953
            var c2 = 0.1685
            var a3 = 15.21
            var b3 = 3.813
            var c3 = 0.09208
            var q1 = a1 * exp(-((it - b1) / c1).pow(2.0))
            var q2 = a2 * exp(-((it - b2) / c2).pow(2.0))
            var q3 = a3 * exp(-((it - b3) / c3).pow(2.0))
            var percent = (q1 + q2 + q3).toInt()
            percent = max(min(percent, 100), 0)
            var minutes = 4.52 * percent
            val hours = (minutes / 60).toInt()
            val minutesLast = (minutes % 60).toInt()
            return NapBattery(hours, minutesLast, percent)
        }
    }
}

data class NapBattery(val hours: Int, val minutes: Int, val percent: Int) {
    override fun toString(): String {
        return "NapBattery(hours=$hours, minutes=$minutes, percent=$percent)"
    }
}

/**
 * Created by EnterTech on 2017/11/23.
 */
fun getBatteryResId(level: Int, type: DeviceConnectView.IconType?): Int {
    if (type == DeviceConnectView.IconType.COLOR) {
        when (level) {
            in 0..9 -> return R.mipmap.ic_battery_1
            in 10..29 -> return R.mipmap.ic_battery_2
            in 30..59 -> return R.mipmap.ic_battery_3
            in 60..89 -> return R.mipmap.ic_battery_4
            in 90..100 -> return R.mipmap.ic_battery_5
            else -> return R.mipmap.ic_battery_1
        }
    } else {
        when (level) {
            in 0..9 -> return R.mipmap.ic_battery_1
            in 10..29 -> return R.mipmap.ic_battery_white_2
            in 30..59 -> return R.mipmap.ic_battery_white_3
            in 60..89 -> return R.mipmap.ic_battery_white_4
            in 90..100 -> return R.mipmap.ic_battery_white_5
            else -> return R.mipmap.ic_battery_1
        }
    }
}