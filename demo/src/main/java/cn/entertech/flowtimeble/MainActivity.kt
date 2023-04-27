package cn.entertech.flowtimeble

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import android.view.View
import android.widget.Toast
import cn.entertech.ble.single.BiomoduleBleManager
import cn.entertech.ble.utils.NapBattery

import com.orhanobut.logger.Logger
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var biomoduleBleManager: BiomoduleBleManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        biomoduleBleManager = BiomoduleBleManager.getInstance(applicationContext)
        Log.d("cpTest","oncreate")
        initPermission()
    /*    DeviceUIConfig.getInstance(this).init(false, false, 1)
        DeviceUIConfig.getInstance(this).updateFirmware(
            "1.2.0",
            "${Environment.getExternalStorageDirectory()}/dfufile.zip",
            true
        )*/
    }


    /**
     * Android6.0 auth
     */
    fun initPermission() {
        val needPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        val needRequestPermissions = ArrayList<String>()
        for (i in needPermission.indices) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    needPermission[i]
                ) != PackageManager.PERMISSION_GRANTED
            ) {
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

    fun onDeviceUI(@Suppress("UNUSED_PARAMETER") view: View) {
    //    startActivity(Intent(this@MainActivity, DeviceManagerActivity::class.java))
    }

    var contactListener = fun(contactState: Int) {
        Logger.d("contace state is ${contactState}")
    }

    var connectedListener = fun(string: String) {
        Logger.d("connect success${string}")
        runOnUiThread {
            Toast.makeText(this@MainActivity, "connect success", Toast.LENGTH_SHORT).show()
        }
    }
    var disConnectedListener = fun(string: String) {
        Logger.d("disconnect ${string}")
        runOnUiThread {
            Toast.makeText(this@MainActivity, "disconnect ", Toast.LENGTH_SHORT).show()
        }
    }

    fun onConnect(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.scanNearDeviceAndConnect(fun() {
            Logger.d("sacn success")
        }, fun(e: Exception) {
            Logger.d("sacn failed：$e")
        }, fun(mac: String) {
            Logger.d("connect success$mac")
            runOnUiThread {
                Toast.makeText(this@MainActivity, "connect to device success", Toast.LENGTH_SHORT)
                    .show()
            }
        }) { msg ->
            Logger.d("connect failed")
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "failed to connect to device：${msg}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun onDisconnect(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.disConnect()
    }


    fun onAddConnectedListener(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.addConnectListener(connectedListener)
    }

    fun onRemoveConnectedListener(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.removeConnectListener(connectedListener)
    }

    fun onAddDisconnectedListener(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.addDisConnectListener(disConnectedListener)
    }

    fun onRemoveDisconnectedListener(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.removeDisConnectListener(disConnectedListener)
    }

    fun onStopContact(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.stopContact()
    }

    fun onStartContact(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.startContact()
    }

    fun onCollectHeartStart(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.startHeartRateCollection()
    }

    fun onCollectHeartStop(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.stopHeartRateCollection()
    }

    fun onCollectBrainStart(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.startBrainCollection()
    }

    fun onCollectBrainStop(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.stopBrainCollection()
    }

    fun onCollectBrainAndHeartStart(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.startHeartAndBrainCollection()
    }

    fun onCollectBrainAndHeartStop(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.stopHeartAndBrainCollection()
    }

    var rawListener = fun(bytes: ByteArray) {
//        Logger.d("firmware fixing hex " + HexDump.toHexString(bytes))
        Log.d("######", "braindata: " + HexDump.toHexString(bytes))
//        Logger.d("brain data is " + Arrays.toString(bytes))
    }

    var heartRateListener = fun(heartRate: Int) {
        Logger.d("heart rate data is " + heartRate)
    }

    fun onAddRawListener(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.addRawDataListener(rawListener)
    }

    fun onRemoveRawListener(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.removeRawDataListener(rawListener)
    }

    fun onAddHeartRateListener(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.addHeartRateListener(heartRateListener)
    }

    fun onRemoveHeartRateListener(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.removeHeartRateListener(heartRateListener)
    }

    fun onAddContactListener(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.addContactListener(contactListener)
    }

    fun onRemoveContactListener(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.removeContactListener(contactListener)
    }

    fun onBattery(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.readBattery(fun(battery: NapBattery) {
            Logger.d("battery = " + battery)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "电量:" + battery, Toast.LENGTH_SHORT).show()
            }
        }, fun(error: String) {
            Logger.d("error is ${error}")
        })
    }

    fun onGetState(@Suppress("UNUSED_PARAMETER") view: View) {
        Logger.d(biomoduleBleManager.isConnected())
        Toast.makeText(
            this, if (biomoduleBleManager.isConnected()) {
                "connected"
            } else {
                "disconnect"
            }, Toast.LENGTH_SHORT
        ).show()
    }

    val batteryListener = fun(napBattery: NapBattery) {
        Logger.d("battery = ${napBattery}")
    }
    val batteryVoltageListener = fun(voltage: Double) {
        Logger.d("battery voltage = ${voltage}")
    }

    fun onAddBatteryListener(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.addBatteryListener(batteryListener)
    }

    fun onRemoveBatteryListener(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.removeBatteryListener(batteryListener)
    }

    fun onAddBatteryVoltageListener(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.addBatteryVoltageListener(batteryVoltageListener)
    }

    fun onRemoveBatteryVoltageListener(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.removeBatteryVoltageListener(batteryVoltageListener)
    }

    fun onReadHardware(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.readDeviceHardware(fun(hardware: String) {
            Logger.d("hardware is " + hardware)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "hardware is：${hardware}", Toast.LENGTH_SHORT)
                    .show()
            }
        }, fun(error: String) {
            Logger.d("error is " + error)
        })
    }

    fun onReadFirmware(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.readDeviceFirmware(fun(firmware: String) {
            Logger.d("firmware is " + firmware)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "firmware is：${firmware}", Toast.LENGTH_SHORT)
                    .show()
            }
        }, fun(error: String) {
            Logger.d("error is " + error)
        })
    }

    fun onReadDeviceSerial(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.readDeviceSerial(fun(serial: String) {
            Logger.d("serial is " + serial)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "serial is：${serial}", Toast.LENGTH_SHORT).show()
            }
        }, fun(error: String) {
            Logger.d("error is " + error)
        })
    }

    fun oReadDeviceManufacturer(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.readDeviceManufacturer(fun(manufacturer: String) {
            Logger.d("manufacturer is " + manufacturer)
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "manufacturer is：${manufacturer}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, fun(error: String) {
            Logger.d("error is " + error)
        })
    }

    fun onFindConnectedDevice(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.findConnectedDevice()
    }

    override fun onDestroy() {
        biomoduleBleManager.removeRawDataListener(rawListener)
        biomoduleBleManager.removeContactListener(contactListener)
        biomoduleBleManager.removeBatteryListener(batteryListener)
        biomoduleBleManager.removeHeartRateListener(heartRateListener)
        biomoduleBleManager.stopHeartRateCollection()
        biomoduleBleManager.stopBrainCollection()
        biomoduleBleManager.stopHeartAndBrainCollection()
        super.onDestroy()
    }

}
