package cn.entertech.flowtimeble

import android.util.Base64
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun addition_isCorrect() {
        // Context of the app under test.
        val bytes = byteArrayOf(0xAB.toByte(), 0x90.toByte(), 0x78, 0x56, 0x34, 0x12)
        val base64 = Base64.encode(bytes, Base64.DEFAULT)
        val hexStr = "AB9078563412"
        val byteArray = hexStr.hexToByteArray()
        val base64_2 = Base64.encode(byteArray, Base64.DEFAULT)
        println("base64: $base64")
        println("base64_2: $base64_2")
    }
}
