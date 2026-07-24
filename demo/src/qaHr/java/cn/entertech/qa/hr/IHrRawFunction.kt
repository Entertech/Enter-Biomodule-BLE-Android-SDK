package cn.entertech.qa.hr

interface IHrRawFunction {

    fun notifyHrRawData(
        success: (ByteArray) -> Unit, failure: (String) -> Unit
    )

    fun stopNotifyHrRawData(
        success: () -> Unit, failure: (String) -> Unit
    )
}