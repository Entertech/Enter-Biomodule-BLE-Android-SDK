package cn.entertech.ble

/**
 * Created by EnterTech on 2018/1/29.
 */
enum class ContactState {
    GOOD, POOR, BAD
}

fun toEnum(value: Byte): ContactState {
    when(value) {
        24.toByte() -> return ContactState.BAD
        0.toByte() -> return ContactState.GOOD
        else -> return ContactState.POOR
    }
}