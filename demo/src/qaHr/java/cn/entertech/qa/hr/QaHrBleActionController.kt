package cn.entertech.qa.hr

internal interface QaHrBleActions {
    fun notifyHr(success: (ByteArray) -> Unit, failure: (String) -> Unit)

    fun notifyHrRaw(success: (ByteArray) -> Unit, failure: (String) -> Unit)

    fun startCollect(success: (ByteArray) -> Unit, failure: (Int, String) -> Unit)

    fun stopNotifyHr(success: () -> Unit, failure: (String) -> Unit)

    fun stopNotifyHrRaw(success: () -> Unit, failure: (String) -> Unit)

    fun stopCollect(success: (ByteArray) -> Unit, failure: (Int, String) -> Unit)
}

internal class QaHrBleActionController(
    private val actions: QaHrBleActions
) {
    fun notifyHr(success: (ByteArray) -> Unit, failure: (String) -> Unit) {
        actions.notifyHr(success, failure)
    }

    fun notifyHrRaw(success: (ByteArray) -> Unit, failure: (String) -> Unit) {
        actions.notifyHrRaw(success, failure)
    }

    fun startCollect(success: (ByteArray) -> Unit, failure: (Int, String) -> Unit) {
        actions.startCollect(success, failure)
    }

    fun stopNotifyHr(success: () -> Unit, failure: (String) -> Unit) {
        actions.stopNotifyHr(success, failure)
    }

    fun stopNotifyHrRaw(success: () -> Unit, failure: (String) -> Unit) {
        actions.stopNotifyHrRaw(success, failure)
    }

    fun stopCollect(
        isHrRawNotifyActive: Boolean,
        isHrNotifyActive: Boolean,
        success: (ByteArray) -> Unit,
        failure: (Int, String) -> Unit
    ) {
        stopHrRawNotifyIfActive(isHrRawNotifyActive, success = {
            stopHrNotifyIfActive(isHrNotifyActive, success = {
                actions.stopCollect(success, failure)
            }, failure = { error ->
                failure(STOP_NOTIFY_FAILURE_CODE, error)
            })
        }, failure = { error ->
            failure(STOP_NOTIFY_FAILURE_CODE, error)
        })
    }

    private fun stopHrRawNotifyIfActive(
        isActive: Boolean,
        success: () -> Unit,
        failure: (String) -> Unit
    ) {
        if (!isActive) {
            success()
            return
        }
        actions.stopNotifyHrRaw(success, failure)
    }

    private fun stopHrNotifyIfActive(
        isActive: Boolean,
        success: () -> Unit,
        failure: (String) -> Unit
    ) {
        if (!isActive) {
            success()
            return
        }
        actions.stopNotifyHr(success, failure)
    }

    companion object {
        const val STOP_NOTIFY_FAILURE_CODE = -1
    }
}
