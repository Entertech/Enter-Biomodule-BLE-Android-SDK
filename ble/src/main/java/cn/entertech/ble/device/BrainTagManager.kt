package cn.entertech.ble.device

import android.content.Context
import cn.entertech.ble.BaseBleConnectManager

class BrainTagManager constructor(
    context: Context,
) : BaseBleConnectManager(context){
    companion object {




        private const val BATTERY_LEVEL_MIN = 3.1
        private const val BATTERY_LEVEL_MAX = 4.1

        /**
         * 仰卧
         * */
        const val SLEEP_POSITION_UPRIGHT = 0x01

        /**
         * 俯卧
         * */
        const val sleep_position_BACK = 0x02

        /**
         * 左侧卧
         * */
        const val sleep_position_LEFT = 0x03

        /**
         * 右侧卧
         * */
        const val sleep_position_RIGHT = 0x04

        /**
         * 站立
         * */
        const val sleep_position_FRONT = 0x05

    }




}