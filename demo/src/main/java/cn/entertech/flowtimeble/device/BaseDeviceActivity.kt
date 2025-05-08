package cn.entertech.flowtimeble.device

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import cn.entertech.ble.BaseBleConnectManager
import cn.entertech.ble.log.BleLogUtil
import cn.entertech.flowtimeble.log.LogAdapter
import java.text.SimpleDateFormat

abstract class BaseDeviceActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "BaseDeviceActivity"
    }

    protected var needLog = false
    protected val mainHandler by lazy {
        Handler(Looper.getMainLooper())
    }
    protected val adapter by lazy {
        LogAdapter()
    }
    protected val simple by lazy {
        SimpleDateFormat("yyyy/MM/dd  hh:mm:ss:SSS")
    }
    protected var cbShowLog: CheckBox? = null
    protected var bluetoothDeviceManager: BaseBleConnectManager? = null
    protected var scrollView_logs: RecyclerView? = null
    protected var btnClearLog: Button? = null
    @Volatile
    protected var needReConnected = false
    protected val reconnectRunnable: Runnable by lazy {
        Runnable {
            showMsg("reconnectRunnable needReConnected:   $needReConnected")
            if (needReConnected) {
                showMsg("start reconnect")
                connectDevice()
            }
        }
    }

    fun onDisconnect(@Suppress("UNUSED_PARAMETER") view: View) {
        bluetoothDeviceManager?.disConnect {
            deviceDisconnect()
        }
    }

    protected open fun showMsg(msg: String) {
        Log.d(TAG, "msg: $msg")
    }

    protected open fun deviceDisconnect() {

    }

    protected open fun deviceConnect(mac: String) {

    }

    fun onGetState(@Suppress("UNUSED_PARAMETER") view: View) {
        BleLogUtil.d(
            TAG, "biomoduleBleManager.isConnected()： ${bluetoothDeviceManager?.isConnected()}"
        )
        Toast.makeText(
            this, if (bluetoothDeviceManager?.isConnected() == true) {
                "connected"
            } else {
                "disconnect"
            }, Toast.LENGTH_SHORT
        ).show()
    }

    fun showToast(msg: String) {
        runOnUiThread {
            Toast.makeText(this.applicationContext, msg, Toast.LENGTH_SHORT).show()
        }
    }

    fun onConnect(@Suppress("UNUSED_PARAMETER") view: View) {
        connectDevice()
    }

    protected fun connectDevice() {
        mainHandler.removeCallbacks(reconnectRunnable)
        if (bluetoothDeviceManager?.isConnected() == true) {
            showMsg("已连接  $bluetoothDeviceManager")
            return
        }

        if (bluetoothDeviceManager?.isConnecting() == true) {
            showMsg("正在连接中  $bluetoothDeviceManager")
            return
        }
        showMsg("开始寻找设备 ，准备连接 $bluetoothDeviceManager")
        bluetoothDeviceManager?.connectDevice(fun(mac: String) {
            showMsg("connect success $mac")
            deviceConnect(mac)
        }, { msg ->
            showMsg("connect failed $msg")
            runOnUiThread {
                Toast.makeText(
                    this, "failed to connect to device：${msg}", Toast.LENGTH_SHORT
                ).show()
            }
        }, cn.entertech.ble.api.ConnectionBleStrategy.SCAN_AND_CONNECT_HIGH_SIGNAL)
    }
}