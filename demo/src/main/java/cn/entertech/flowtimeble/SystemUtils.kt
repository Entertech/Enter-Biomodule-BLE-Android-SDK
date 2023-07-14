package cn.entertech.flowtimeble

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.text.format.Formatter
import cn.entertech.ble.utils.BleLogUtil
import cn.entertech.bleuisdk.utils.Constant
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 系统的工具方法集
 * @author JPlus
 * @date 2019/9/18.
 */
/**
 * 获取当前手机的cpu架构
 * @return
 */
fun getCpuABI(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) Arrays.toString(Build.SUPPORTED_ABIS) else Build.CPU_ABI
}

/**
 * 获取当前手机Android API版本
 * @return
 */
fun getApiLevel(): Int {
    return Build.VERSION.SDK_INT
}

/**
 * 获取手机Android 版本
 * @return
 */
fun getOsLevel(): String {
    return Build.VERSION.RELEASE
}

/**
 * 获取当前手机设备信息
 * @return 生产厂家_cpu型号_品牌
 */
fun getDeviceInfo(): String {
    return Build.MANUFACTURER + "_" + Build.BRAND + "_" + Build.MODEL
}

/**
 * 获取设备mac地址
 * <p>需要在manifest中声明权限{@link android.Manifest.permission.ACCESS_WIFI_STATE}</p>
 * @return
 */
@SuppressLint("MissingPermission")
fun Context.getMacAddress(): String {
    return (this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager).connectionInfo.macAddress
}

/**
 * 获取品牌名
 * @return
 */
fun getDeviceProduct(): String {
    return Build.PRODUCT
}

/**
 *  获取可用显示尺寸的绝对宽度（以像素为单位）
 *  @return
 */

fun Context.getScreenWidth(): Int {
    return this.resources.displayMetrics.widthPixels
}

/**
 *  获取可用显示尺寸的绝对高度（以像素为单位）
 *  @return
 */
fun Context.getScreenHeight(): Int {
    return this.resources.displayMetrics.heightPixels
}

/**
 * 判断 sdcard 是否存在
 * @return
 */
fun isExistSDCard(): Boolean {
    return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}

/**
 * 获取已安装Apk文件的源Apk文件
 * 如：/data/app/com.sina.weibo.apk
 *
 * @param packageName
 * @return
 */
fun Context.getApkSource(packageName: String): String? {
    if (TextUtils.isEmpty(packageName)) return null
    return try {
        val appInfo = this.packageManager.getApplicationInfo(packageName, 0)
        appInfo.sourceDir
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        null
    }
}

/**
 *  根据包名跳转到第三方应用，不重复启动
 *  @param packageName
 *  @return
 */
fun Context.startAppByPackageName(packageName: String) {
    Intent(Intent.ACTION_MAIN).let {
        var mainAct = ""
        it.addCategory(Intent.CATEGORY_LAUNCHER)
        it.flags = Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_NEW_TASK
        val list = this.packageManager.queryIntentActivities(it, PackageManager.MATCH_DEFAULT_ONLY)
        for (i in list.indices) {
            val info = list[i]
            if (info.activityInfo.packageName == packageName) {
                mainAct = info.activityInfo.name
                break
            }
        }
        if (mainAct.isEmpty()) return

        it.component = ComponentName(packageName, mainAct)
        this.startActivity(it)
    }
}

/**
 * 安装apk
 * @param path apk文件路径
 * @param authority fileProvider的authority
fun Activity.installApk(path: String, authority: String, requestCode:Int) {
    Intent(Intent.ACTION_VIEW).let {
        it.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        it.setDataAndType(
            this.path2Uri(path, authority),
            "application/vnd.android.package-archive"
        )
        this.startActivityForResult(it, requestCode);
    }
}*/

/**
 *  卸载第三方应用
 *  @param packageName 应用包名
 *  @return
 */
fun Context.deleteAppByPackageName(packageName: String) {
    Intent(Intent.ACTION_DELETE).let {
        it.data = Uri.parse("package:$packageName")
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(it)
    }
}

/**
 * 获取系统已有的传感器
 * @return
 */
fun Context.getSupportSensors(): List<Sensor> {
    val sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    return sensorManager.getSensorList(Sensor.TYPE_ALL)
}

/**
 * 获取cpu详细信息
 * @return cpu信息
 */
fun getCpuInfo(): String {
    var result = ""
    try {
        val args = arrayOf("/system/bin/cat", "/proc/cpuinfo")
        val cmd = ProcessBuilder(*args)
        val process = cmd.start()
        val sb = StringBuffer()
        var readLine = ""
        val responseReader = BufferedReader(InputStreamReader(process.inputStream, "utf-8"))
        while (responseReader.readLine().also { readLine = it } != null) {
            sb.append(readLine)
        }
        responseReader.close()
        result = sb.toString().toLowerCase(Locale.ROOT)
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return result
}

/**
 * 获取运营商名称
 * @return 运营商名称
 */
fun Context.getOperatorNameAndroid(): String {
    return (this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).networkOperatorName
}

/**
 * 获取手机内存和SD卡内存
 * @param isCard 是否是SD卡
 */
fun Context.getRomTotalSize(isCard: Boolean): String? {
    var path = Environment.getDataDirectory()
    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
        if (isCard) {
            path = Environment.getExternalStorageDirectory()
        }
        val statFs = StatFs(path.path)
        return Formatter.formatFileSize(this, statFs.blockSizeLong * statFs.blockCountLong)
    }
    return null
}

/**
 * 获取可用内存大小
 * @param isCard 是否SD卡
 * @return
 */
fun Context.getAvailableSize(isCard: Boolean): String? {
    var path = Environment.getDataDirectory()
    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
        if (isCard) {
            path = Environment.getExternalStorageDirectory()
        }
        val statFs = StatFs(path.path)
        return Formatter.formatFileSize(this, statFs.blockSizeLong * statFs.availableBlocksLong)
    }
    return null
}

/**
 * 文件字符串写入
 * @param content
 * @param append
 * @return
 */
fun File.writeFile(content: String, append: Boolean): Boolean {
    var writer: BufferedWriter? = null
    try {
        BleLogUtil.d("SystemUtils", "writeFile:$absoluteFile")
        //FileWriter 文件不存在则会创建
        writer = BufferedWriter(FileWriter(this, append))
        writer.write(content)
        return true
    } catch (e: FileNotFoundException) {
        throw RuntimeException("FileNotFoundException occurred. ", e)
    } catch (e: IOException) {
        throw RuntimeException("IOException occurred. ", e)
    } finally {
        try {
            writer?.close()
        } catch (e: IOException) {
            throw RuntimeException("IOException occurred. ", e)
        }
    }
}

/**
 *  App基本方法
 * @author JPlus
 * @date 2019/2/22.
 */

/**
 * 获取当前应用名称
 * @return
 */
fun Context.getAppName(): String {
    return try {
        this.packageManager.let {
            it.getApplicationLabel(it.getApplicationInfo(this.packageName, 0)) as String
        }
    } catch (e: PackageManager.NameNotFoundException) {
        "Application"
    }
}



/**
 * 获取指定包名的应用的权限列表
 * @param packageName 包名
 * @return
 */
private fun Context.getPermissions(packageName: String): MutableList<String> {
    this.packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS).requestedPermissions?.let {
        return it.toMutableList()
    }
    return mutableListOf()
}

/**
 * 获取当前包名
 * @return
 */
fun Context.getPackageName(): String {
    return this.packageName
}

/**
 * 获取设备标识
 * @return
 */
fun Context.getDeviceIMEI(): String {
    val tm = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    val deviceId = Settings.System.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
    return deviceId ?: "" // ?:左边为空才执行右边
}

/**
 * 获取应用版本
 * @return
 */
fun Context.getAppVersionName(): String {
    return try {
        this.packageManager.getPackageInfo(this.packageName, PackageManager.GET_ACTIVITIES).versionName
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        "0"
    }
}

/**
 * 获取应用版本号
 * @return
 */
fun Context.getAppVersionCode(): Int {
    return try {
        this.packageManager.getPackageInfo(this.packageName, PackageManager.GET_ACTIVITIES).let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) it.longVersionCode.toInt() else it.versionCode
        }
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        0
    }
}

/**
 * 获取当前App最大可用内存
 * @return
 */
fun getMaxMemory(): Int {
    return (Runtime.getRuntime().maxMemory() / 1024).toInt()
}

/**
 * 获取当前时间
 * @param isIncludeBlank 是否包含空格
 * @return yyyy-MM-dd_HH:mm:ss格式的时间
 */
@SuppressLint("SimpleDateFormat")
fun Date.getDateTimeByMillis(isIncludeBlank: Boolean): String {
    return if (isIncludeBlank) {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this)
    } else {
        SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(this)
    }
}

/**
 * 生成私有文件夹
 * @param dir
 * return
 */
fun Context.writePrivateDir(dir: String): File {
    return getDir(dir, Context.MODE_PRIVATE)
}