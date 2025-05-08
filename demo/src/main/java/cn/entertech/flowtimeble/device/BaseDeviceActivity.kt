package cn.entertech.flowtimeble.device

import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.entertech.ble.BaseBleConnectManager
import cn.entertech.ble.log.BleLogUtil

abstract class BaseDeviceActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "BaseDeviceActivity"
    }

    protected var bluetoothDeviceManager: BaseBleConnectManager? = null

    fun onDisconnect(@Suppress("UNUSED_PARAMETER") view: View) {
        bluetoothDeviceManager?.disConnect {
            deviceDisconnect()
        }
    }

    protected open fun deviceDisconnect() {

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
        Toast.makeText(this.applicationContext, msg, Toast.LENGTH_SHORT).show()
    }
}