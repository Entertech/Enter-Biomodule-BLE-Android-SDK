package cn.entertech.flowtimeble

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.View
import android.widget.Toast
import cn.entertech.ble.ContactState
import cn.entertech.ble.FlowtimeBleManager
import cn.entertech.ble.util.NapBattery
import com.orhanobut.logger.Logger
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var bleDeviceManager: FlowtimeBleManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bleDeviceManager = FlowtimeBleManager.getInstance(this)
        initPermission()
    }


    /**
     * Android6.0 auth
     */
    fun initPermission() {
        val needPermission = arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val needRequestPermissions = ArrayList<String>()
        for (i in needPermission.indices) {
            if (ActivityCompat.checkSelfPermission(this, needPermission[i]) != PackageManager.PERMISSION_GRANTED) {
                needRequestPermissions.add(needPermission[i])
            }
        }
        if (needRequestPermissions.size != 0) {
            val permissions = arrayOfNulls<String>(needRequestPermissions.size)
            for (i in needRequestPermissions.indices) {
                permissions[i] = needRequestPermissions[i]
            }
            ActivityCompat.requestPermissions(this@MainActivity, permissions, 1)
        }
    }

    var contactListener = fun(contactState: ContactState) {
        Logger.d("contace state is ${contactState}")
    }

    var connectedListener = fun(string: String) {
        runOnUiThread {
            Toast.makeText(this@MainActivity, "连接成功", Toast.LENGTH_SHORT).show()
        }
    }
    var disConnectedListener = fun(string: String) {
        runOnUiThread {
            Toast.makeText(this@MainActivity, "已断开连接", Toast.LENGTH_SHORT).show()
        }
    }

    fun onConnect(view: View) {
        bleDeviceManager.scanNearDeviceAndConnect(0, fun() {
            Logger.d("扫描成功")
        }, fun(mac: String) {
            Logger.d("连接成功$mac")
            runOnUiThread {
                Toast.makeText(this@MainActivity, "设备连接成功", Toast.LENGTH_SHORT).show()
            }
        }) { msg ->
            Logger.d("连接失败")
            runOnUiThread {
                Toast.makeText(this@MainActivity, "设备连接失败：${msg}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onDisconnet(view: View) {
        bleDeviceManager.disConnect()
    }


    fun onAddConnectedListener(view: View) {
        bleDeviceManager.addConnectListener(connectedListener)
    }

    fun onRemoveConnectedListener(view: View) {
        bleDeviceManager.removeConnectListener(connectedListener)
    }

    fun onAddDisconnectedListener(view: View) {
        bleDeviceManager.addDisConnectListener(disConnectedListener)
    }

    fun onRemoveDisconnectedListener(view: View) {
        bleDeviceManager.removeDisConnectListener(disConnectedListener)
    }


    fun onCollectStart(view: View) {
        bleDeviceManager.startHeartAndBrainCollection()
    }

    fun onCollectStop(view: View) {
        bleDeviceManager.stopHeartAndBrainCollection()
    }

    var rawListener = fun(bytes: ByteArray) {
        Logger.d("brain data is " + Arrays.toString(bytes))
    }

    var heartRateListener = fun(bytes: ByteArray) {
        Logger.d("heart rate data is " + Arrays.toString(bytes))
    }

    fun onAddRawListener(view: View) {
        bleDeviceManager.addRawDataListener(rawListener)
    }

    fun onRemoveRawListener(view: View) {
        bleDeviceManager.removeRawDataListener(rawListener)
    }

    fun onAddHeartRateListener(view: View) {
        bleDeviceManager.addRawDataListener(heartRateListener)
    }

    fun onRemoveHeartRateListener(view: View) {
        bleDeviceManager.removeRawDataListener(heartRateListener)
    }

    fun onAddContactListener(view: View) {
        bleDeviceManager.addContactListener(contactListener)
    }

    fun onRemoveContactListener(view: View) {
        bleDeviceManager.removeContactListener(contactListener)
    }

    fun onBattery(view: View) {
        bleDeviceManager.readBattery(fun(battery: NapBattery) {
            Logger.d("battery = " + battery)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "电量:" + battery, Toast.LENGTH_SHORT).show()
            }
        }, fun(error: String) {
            Logger.d("error is ${error}")
        })
    }

    fun onGetState(view: View) {
        Logger.d(bleDeviceManager.isConnected())
        Toast.makeText(this, if (bleDeviceManager.isConnected()) {
            "已连接"
        } else {
            "未连接"
        }, Toast.LENGTH_SHORT).show()
    }

    val batteryListener = fun(napBattery: NapBattery) {
        Logger.d("battery = ${napBattery}")
    }

    fun onAddBtteryListener(view: View) {
        bleDeviceManager.addBatteryListener(batteryListener)
    }

    fun onRemoveBtteryListener(view: View) {
        bleDeviceManager.removeBatteryListener(batteryListener)
    }

    fun onReadHardware(view: View) {
        bleDeviceManager.readDeviceHardware(fun(hardware: String) {
            Logger.d("hardware is " + hardware)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "硬件版本：${hardware}", Toast.LENGTH_SHORT).show()
            }
        }, fun(error: String) {
            Logger.d("error is " + error)
        })
    }

    fun onReadFirmware(view: View) {
        bleDeviceManager.readDeviceFirmware(fun(firmware: String) {
            Logger.d("firmware is " + firmware)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "固件版本：${firmware}", Toast.LENGTH_SHORT).show()
            }
        }, fun(error: String) {
            Logger.d("error is " + error)
        })
    }

    fun onReadDeviceSerial(view: View) {
        bleDeviceManager.readDeviceSerial(fun(serial: String) {
            Logger.d("serial is " + serial)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "序列号：${serial}", Toast.LENGTH_SHORT).show()
            }
        }, fun(error: String) {
            Logger.d("error is " + error)
        })
    }

    fun oReadDeviceManufacturer(view: View) {
        bleDeviceManager.readDeviceManufacturer(fun(manufacturer: String) {
            Logger.d("manufacturer is " + manufacturer)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "制造商：${manufacturer}", Toast.LENGTH_SHORT).show()
            }
        }, fun(error: String) {
            Logger.d("error is " + error)
        })
    }


    override fun onDestroy() {
        bleDeviceManager.removeRawDataListener(rawListener)
        bleDeviceManager.removeContactListener(contactListener)
        bleDeviceManager.removeBatteryListener(batteryListener)
        bleDeviceManager.stopCollection()
        super.onDestroy()
    }

}
