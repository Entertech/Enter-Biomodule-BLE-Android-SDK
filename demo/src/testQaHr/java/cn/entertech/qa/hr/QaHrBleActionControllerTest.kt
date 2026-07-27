package cn.entertech.qa.hr

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QaHrBleActionControllerTest {

    private val actions = FakeQaHrBleActions()
    private val controller = QaHrBleActionController(actions)

    @Test
    fun notifyHr_success() {
        val expected = byteArrayOf(72)
        actions.notifyHrResult = ActionResult.Success(expected)

        var actual: ByteArray? = null
        controller.notifyHr(success = { actual = it }, failure = {})

        assertEquals(listOf("notifyHr"), actions.calls)
        assertArrayEquals(expected, actual)
    }

    @Test
    fun notifyHr_failure() {
        actions.notifyHrResult = ActionResult.Failure("notify hr failed")

        var actual = ""
        controller.notifyHr(success = {}, failure = { actual = it })

        assertEquals(listOf("notifyHr"), actions.calls)
        assertEquals("notify hr failed", actual)
    }

    @Test
    fun notifyHrRaw_success() {
        val expected = byteArrayOf(1, 2, 3, 4)
        actions.notifyHrRawResult = ActionResult.Success(expected)

        var actual: ByteArray? = null
        controller.notifyHrRaw(success = { actual = it }, failure = {})

        assertEquals(listOf("notifyHrRaw"), actions.calls)
        assertArrayEquals(expected, actual)
    }

    @Test
    fun notifyHrRaw_failure() {
        actions.notifyHrRawResult = ActionResult.Failure("notify raw failed")

        var actual = ""
        controller.notifyHrRaw(success = {}, failure = { actual = it })

        assertEquals(listOf("notifyHrRaw"), actions.calls)
        assertEquals("notify raw failed", actual)
    }

    @Test
    fun startCollect_success() {
        val expected = byteArrayOf(0x01, 0x02)
        actions.startCollectResult = CollectResult.Success(expected)

        var actual: ByteArray? = null
        controller.startCollect(success = { actual = it }, failure = { _, _ -> })

        assertEquals(listOf("startCollect"), actions.calls)
        assertArrayEquals(expected, actual)
    }

    @Test
    fun startCollect_failure() {
        actions.startCollectResult = CollectResult.Failure(1001, "start collect failed")

        var actualCode = 0
        var actualMessage = ""
        controller.startCollect(success = {}, failure = { code, message ->
            actualCode = code
            actualMessage = message
        })

        assertEquals(listOf("startCollect"), actions.calls)
        assertEquals(1001, actualCode)
        assertEquals("start collect failed", actualMessage)
    }

    @Test
    fun stopNotifyHr_success() {
        actions.stopNotifyHrResult = StopResult.Success

        var called = false
        controller.stopNotifyHr(success = { called = true }, failure = {})

        assertEquals(listOf("stopNotifyHr"), actions.calls)
        assertTrue(called)
    }

    @Test
    fun stopNotifyHr_failure() {
        actions.stopNotifyHrResult = StopResult.Failure("stop hr failed")

        var actual = ""
        controller.stopNotifyHr(success = {}, failure = { actual = it })

        assertEquals(listOf("stopNotifyHr"), actions.calls)
        assertEquals("stop hr failed", actual)
    }

    @Test
    fun stopNotifyHrRaw_success() {
        actions.stopNotifyHrRawResult = StopResult.Success

        var called = false
        controller.stopNotifyHrRaw(success = { called = true }, failure = {})

        assertEquals(listOf("stopNotifyHrRaw"), actions.calls)
        assertTrue(called)
    }

    @Test
    fun stopNotifyHrRaw_failure() {
        actions.stopNotifyHrRawResult = StopResult.Failure("stop raw failed")

        var actual = ""
        controller.stopNotifyHrRaw(success = {}, failure = { actual = it })

        assertEquals(listOf("stopNotifyHrRaw"), actions.calls)
        assertEquals("stop raw failed", actual)
    }

    @Test
    fun stopCollect_success_stopsActiveNotificationsBeforeStopCommand() {
        val expected = byteArrayOf(0x10, 0x20)
        actions.stopCollectResult = CollectResult.Success(expected)

        var actual: ByteArray? = null
        controller.stopCollect(
            isHrRawNotifyActive = true,
            isHrNotifyActive = true,
            success = { actual = it },
            failure = { _, _ -> }
        )

        assertEquals(listOf("stopNotifyHrRaw", "stopNotifyHr", "stopCollect"), actions.calls)
        assertArrayEquals(expected, actual)
    }

    @Test
    fun stopCollect_failure_returnsStopCommandFailureAfterNotificationsStopped() {
        actions.stopCollectResult = CollectResult.Failure(2001, "stop collect failed")

        var actualCode = 0
        var actualMessage = ""
        controller.stopCollect(
            isHrRawNotifyActive = true,
            isHrNotifyActive = true,
            success = {},
            failure = { code, message ->
                actualCode = code
                actualMessage = message
            }
        )

        assertEquals(listOf("stopNotifyHrRaw", "stopNotifyHr", "stopCollect"), actions.calls)
        assertEquals(2001, actualCode)
        assertEquals("stop collect failed", actualMessage)
    }

    @Test
    fun stopCollect_failure_doesNotSendStopCommandWhenHrRawUnsubscribeFails() {
        actions.stopNotifyHrRawResult = StopResult.Failure("stop raw failed")

        var actualCode = 0
        var actualMessage = ""
        controller.stopCollect(
            isHrRawNotifyActive = true,
            isHrNotifyActive = true,
            success = {},
            failure = { code, message ->
                actualCode = code
                actualMessage = message
            }
        )

        assertEquals(listOf("stopNotifyHrRaw"), actions.calls)
        assertEquals(QaHrBleActionController.STOP_NOTIFY_FAILURE_CODE, actualCode)
        assertEquals("stop raw failed", actualMessage)
    }

    @Test
    fun stopCollect_failure_doesNotSendStopCommandWhenHrUnsubscribeFails() {
        actions.stopNotifyHrResult = StopResult.Failure("stop hr failed")

        var actualCode = 0
        var actualMessage = ""
        controller.stopCollect(
            isHrRawNotifyActive = true,
            isHrNotifyActive = true,
            success = {},
            failure = { code, message ->
                actualCode = code
                actualMessage = message
            }
        )

        assertEquals(listOf("stopNotifyHrRaw", "stopNotifyHr"), actions.calls)
        assertEquals(QaHrBleActionController.STOP_NOTIFY_FAILURE_CODE, actualCode)
        assertEquals("stop hr failed", actualMessage)
    }

    @Test
    fun stopCollect_success_skipsInactiveNotifications() {
        controller.stopCollect(
            isHrRawNotifyActive = false,
            isHrNotifyActive = false,
            success = {},
            failure = { _, _ -> }
        )

        assertEquals(listOf("stopCollect"), actions.calls)
    }

    @Test
    fun stopCollect_success_fromInactiveHrRawStillStopsActiveHrBeforeStopCommand() {
        actions.stopCollectResult = CollectResult.Success(byteArrayOf(0x01))

        controller.stopCollect(
            isHrRawNotifyActive = false,
            isHrNotifyActive = true,
            success = {},
            failure = { _, _ -> }
        )

        assertEquals(listOf("stopNotifyHr", "stopCollect"), actions.calls)
    }

    private class FakeQaHrBleActions : QaHrBleActions {
        val calls = mutableListOf<String>()
        var notifyHrResult: ActionResult = ActionResult.Success(byteArrayOf(0x01))
        var notifyHrRawResult: ActionResult = ActionResult.Success(byteArrayOf(0x02))
        var startCollectResult: CollectResult = CollectResult.Success(byteArrayOf(0x03))
        var stopNotifyHrResult: StopResult = StopResult.Success
        var stopNotifyHrRawResult: StopResult = StopResult.Success
        var stopCollectResult: CollectResult = CollectResult.Success(byteArrayOf(0x04))

        override fun notifyHr(success: (ByteArray) -> Unit, failure: (String) -> Unit) {
            calls.add("notifyHr")
            notifyHrResult.complete(success, failure)
        }

        override fun notifyHrRaw(success: (ByteArray) -> Unit, failure: (String) -> Unit) {
            calls.add("notifyHrRaw")
            notifyHrRawResult.complete(success, failure)
        }

        override fun startCollect(success: (ByteArray) -> Unit, failure: (Int, String) -> Unit) {
            calls.add("startCollect")
            startCollectResult.complete(success, failure)
        }

        override fun stopNotifyHr(success: () -> Unit, failure: (String) -> Unit) {
            calls.add("stopNotifyHr")
            stopNotifyHrResult.complete(success, failure)
        }

        override fun stopNotifyHrRaw(success: () -> Unit, failure: (String) -> Unit) {
            calls.add("stopNotifyHrRaw")
            stopNotifyHrRawResult.complete(success, failure)
        }

        override fun stopCollect(success: (ByteArray) -> Unit, failure: (Int, String) -> Unit) {
            calls.add("stopCollect")
            stopCollectResult.complete(success, failure)
        }
    }

    private sealed class ActionResult {
        data class Success(val data: ByteArray) : ActionResult()
        data class Failure(val message: String) : ActionResult()

        fun complete(success: (ByteArray) -> Unit, failure: (String) -> Unit) {
            when (this) {
                is Success -> success(data)
                is Failure -> failure(message)
            }
        }
    }

    private sealed class CollectResult {
        data class Success(val data: ByteArray) : CollectResult()
        data class Failure(val code: Int, val message: String) : CollectResult()

        fun complete(success: (ByteArray) -> Unit, failure: (Int, String) -> Unit) {
            when (this) {
                is Success -> success(data)
                is Failure -> failure(code, message)
            }
        }
    }

    private sealed class StopResult {
        data object Success : StopResult()
        data class Failure(val message: String) : StopResult()

        fun complete(success: () -> Unit, failure: (String) -> Unit) {
            when (this) {
                Success -> success()
                is Failure -> failure(message)
            }
        }
    }
}
