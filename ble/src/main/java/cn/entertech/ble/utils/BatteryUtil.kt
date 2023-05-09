package cn.entertech.ble.utils

import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Created by EnterTech on 2018/2/8.
 */
object BatteryUtil {

    fun getMinutesLeft(byte: Byte): NapBattery {
        return NapBattery(0, 0, CharUtil.converUnchart(byte))
    }


    fun getMinutesLeftOld(byte: Byte): NapBattery {
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
            var percent = ((q1 + q2 + q3) * 1.13 - 5).toInt()
            percent = max(min(percent, 100), 0)
//            The original duration estimation factor is 4.52, and the conservative estimate is 85%, so it is changed to 3.84
//            原时长估计因子为4.52，保守估计为85%，故改成3.84
            var minutes = 3.84 * percent
            val hours = (minutes / 60).toInt()
            val minutesLast = (minutes % 60).toInt()
            return NapBattery(hours, minutesLast, percent)
        }
    }

    fun getBatteryVoltage(byte: Byte): Double {
        return (byte / 100.0) * (4.1 - 3.1) + 3.1
    }
}

data class NapBattery(val hours: Int, val minutes: Int, val percent: Int) {
    override fun toString(): String {
        return "NapBattery(hours=$hours, minutes=$minutes, percent=$percent)"
    }
}
