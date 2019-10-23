package cn.entertech.ble.utils

import android.graphics.Color

fun getOpacityColor(color: Int, opacity: Float): Int {
    var colorHex = String.format("#%06X", 0xFFFFFF and color)
    var transparentHex = if (opacity < 0) {
        String.format("%02x", 0)
    } else {
        var transparent = 255 * opacity
        String.format("%02x", transparent.toInt())
    }
    var opacityColorHex = "#$transparentHex${colorHex.subSequence(1, colorHex.length)}"
    return Color.parseColor(opacityColorHex)
}

fun convertTransparentToHex(percent: Int): String {
    if (percent < 0){
        return String.format("%02x", 0)
    }
    var transparent = 255 * (percent * 1f / 100)
    return String.format("%02x", transparent.toInt())
}