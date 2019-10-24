package cn.entertech.bleuisdk.utils

import cn.entertech.bleuisdk.R
import cn.entertech.bleuisdk.ui.widget.DeviceConnectView


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