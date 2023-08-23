package cn.entertech.ble

import java.util.*
import kotlin.experimental.xor

/**
 * Created by EnterTech on 2017/11/2.
 */

//hand shake length 5
private val HANDSHAKE_LEN = 5

//encode
//format：random of hour, min, second
fun encode(input: ByteArray? =  null): ByteArray {
    val output = ByteArray(HANDSHAKE_LEN) { 0 }
    output[4] = (Math.random() * 255).toInt().toByte()
    if (null == input) {
        val calendar = Calendar.getInstance()
        output[0] = 1
        output[1] = output[4] xor calendar.get(Calendar.HOUR_OF_DAY).toByte()
        output[2] = output[4] xor calendar.get(Calendar.MINUTE).toByte()
        output[3] = output[4] xor calendar.get(Calendar.SECOND).toByte()
        return output
    }

    output.apply {
        this[1] = input[1] xor input[4]
        this[2] = input[2] xor input[4]
        this[3] = input[3] xor input[4]
    }.apply {
        this[0] = 3
        this[1] = this[1] xor output[4]
        this[2] = this[2] xor output[4]
        this[3] = this[3] xor output[4]
    }

    return output
}

/**
 * shake hand back
 * @param send send data
 * @param receiver receive data
 * @return is success
 */
fun decode(send: ByteArray, receiver: ByteArray): Boolean {
    if (send.size < HANDSHAKE_LEN || receiver.size < HANDSHAKE_LEN) return false

    send[1] = send[1] xor send[4]
    send[2] = send[2] xor send[4]
    send[3] = send[3] xor send[4]

    receiver[0] = receiver[0] xor receiver[4]
    receiver[1] = receiver[1] xor receiver[4]
    receiver[2] = receiver[2] xor receiver[4]

//    plusOneSecond(send)

    return Arrays.equals(send, receiver)
}


private fun plusOneSecond(input: ByteArray) {
    input.apply {
        if (this[2] + 1 >= 60) {
            this[2] = 0
            this[1] = (input[1] + 1).toByte()
        }

        if (this[1] >= 60) {
            this[1] = 0
            this[0] = (this[0] + 1).toByte()
        }

        if (this[0] >= 24) {
            this[0] = 0
        }
    }
}
