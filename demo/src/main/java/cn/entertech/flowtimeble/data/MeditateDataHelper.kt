package cn.entertech.flowtimeble.data


import cn.android.file.FileHelper
import cn.entertech.base.util.getValueWithInit
import cn.entertech.flowtimeble.App
import java.text.SimpleDateFormat
import java.util.Locale

class MeditateDataHelper(private val parentFileName: String) {

    private val dataHelperMap by lazy {
        HashMap<String, FileHelper?>()
    }

    fun getRawDataFile() = App.getInstance().getExternalFilesDir(parentFileName)

    private fun getFileHelper(dataFileName: String) = dataHelperMap.getValueWithInit(dataFileName, {
        val current = System.currentTimeMillis()
        val helper = FileHelper()
        val targetFile = App.getInstance()
            .getExternalFilesDir("${parentFileName}/${getFileDirName(dataFileName)}")
        helper.setFilePath(
            "$targetFile", getFileName(dataFileName, current)
        )
        helper
    })


    fun saveData(dataFileName: String, data: ByteArray) {
        getFileHelper(dataFileName)?.writeData(data)
    }

    fun saveStringData(dataFileName: String, data: String) {
        getFileHelper(dataFileName)?.writeData(data)
    }

    fun saveStringDataWithTime(dataFileName: String, data: String) {
        saveStringData(
            dataFileName, getFormatTime(
                System.currentTimeMillis(), "HH-mm-ss.SSSXXX"
            ) + ":  $data"
        )
    }

    fun close() {
        dataHelperMap.forEach {
            it.value?.close()
        }
        dataHelperMap.clear()
    }

    fun closeData(dataFileName: String) {
        dataHelperMap.remove(dataFileName)?.close()
    }

    private fun getFileDirName(fileSuffixName: String): String {
        return "${
            getFormatTime(
                System.currentTimeMillis(), "yyyy-MM-dd-HH-mm-ss"
            )
        }-${getFilePrefixName(fileSuffixName)}"
    }

    private fun getFileName(dataFileName: String, current: Long): String {
        val formattedTime = getFormatTime(current, "yyyy-MM-dd-HH-mm-ss")
        val extensionIndex = dataFileName.lastIndexOf('.')
        if (extensionIndex <= 0 || extensionIndex == dataFileName.lastIndex) {
            return "${dataFileName.lowercase()}-$formattedTime"
        }
        val prefix = dataFileName.substring(0, extensionIndex).lowercase()
        val extension = dataFileName.substring(extensionIndex)
        return "$prefix-$formattedTime$extension"
    }

    private fun getFilePrefixName(dataFileName: String): String {
        val extensionIndex = dataFileName.lastIndexOf('.')
        if (extensionIndex <= 0) {
            return dataFileName
        }
        return dataFileName.substring(0, extensionIndex)
    }

    private fun getFormatTime(time: Long, pattern: String?): String? {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return dateFormat.format(time)
    }

}
