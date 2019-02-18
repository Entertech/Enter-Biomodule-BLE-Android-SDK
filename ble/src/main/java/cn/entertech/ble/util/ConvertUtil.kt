package cn.entertech.ble.util

fun convertByteArrayToDoubleList(data:ByteArray):List<Double>{
    //convert byteArray to double[]
    val datas = DoubleArray(data.size / 3)
    for (i in 0..(data.size - 2) / 3 - 1) {
        val index = i * 3 + 2
        val value = toBrainData(data[index], data[index + 1], data[index + 2])
        datas[i] = value.toDouble()
    }
    return datas.toList()
}
fun toBrainData(vararg bytes: Byte): Int {
    val uintData = IntArray(bytes.size){ i ->
        if (bytes[i] < 0) {
            return@IntArray (bytes[i].toInt() and 0xff)
        } else {
            return@IntArray bytes[i].toInt()
        }
    }
    when(uintData.size) {
        2 -> return uintData[0] * 256 + uintData[1]
        3 -> return uintData[0] * 65536 + uintData[1] * 256 + uintData[2]
        else -> return 0
    }
}