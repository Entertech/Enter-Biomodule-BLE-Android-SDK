package cn.entertech.flowtimeble

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.view.View
import android.widget.Toast
import cn.entertech.ble.single.BiomoduleBleManager
import cn.entertech.ble.utils.NapBattery
import cn.entertech.bleuisdk.ui.DeviceUIConfig
import cn.entertech.bleuisdk.ui.activity.DeviceManagerActivity
import com.orhanobut.logger.Logger
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var biomoduleBleManager: BiomoduleBleManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        biomoduleBleManager = BiomoduleBleManager.getInstance(this)
        initPermission()
        DeviceUIConfig.getInstance(this).init(false, true, 2)
        DeviceUIConfig.getInstance(this).updateFirmware("1.0.1","${Environment.getExternalStorageDirectory()}/firmware_1.0.0.zip",true)
    }


    /**
     * Android6.0 auth
     */
    fun initPermission() {
        val needPermission = arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
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

    fun onDeviceUI(view: View) {
        startActivity(Intent(this@MainActivity, DeviceManagerActivity::class.java))
    }

    var contactListener = fun(contactState: Int) {
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
        biomoduleBleManager.scanNearDeviceAndConnect(fun() {
            Logger.d("扫描成功")
        }, fun(e: Exception) {
            Logger.d("扫描失败：$e")
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
        biomoduleBleManager.disConnect()
    }


    fun onAddConnectedListener(view: View) {
        biomoduleBleManager.addConnectListener(connectedListener)
    }

    fun onRemoveConnectedListener(view: View) {
        biomoduleBleManager.removeConnectListener(connectedListener)
    }

    fun onAddDisconnectedListener(view: View) {
        biomoduleBleManager.addDisConnectListener(disConnectedListener)
    }

    fun onRemoveDisconnectedListener(view: View) {
        biomoduleBleManager.removeDisConnectListener(disConnectedListener)
    }

    fun onStopContact(view: View) {
        biomoduleBleManager.stopContact()
    }

    fun onStartContact(view: View) {
        biomoduleBleManager.startContact()
    }

    fun onCollectHeartStart(view: View) {
        biomoduleBleManager.startHeartRateCollection()
    }

    fun onCollectHeartStop(view: View) {
        biomoduleBleManager.stopHeartRateCollection()
    }

    fun onCollectBrainStart(view: View) {
        biomoduleBleManager.startBrainCollection()
    }

    fun onCollectBrainStop(view: View) {
        biomoduleBleManager.stopBrainCollection()
    }

    fun onCollectBrainAndHeartStart(view: View) {
        biomoduleBleManager.startHeartAndBrainCollection()
    }

    fun onCollectBrainAndHeartStop(view: View) {
        biomoduleBleManager.stopHeartAndBrainCollection()
    }

    var rawListener = fun(bytes: ByteArray) {
        Logger.d("firmware fixing hex " + HexDump.toHexString(bytes))
//        Logger.d("brain data is " + Arrays.toString(bytes))
    }

    var heartRateListener = fun(heartRate: Int) {
        Logger.d("heart rate data is " + heartRate)
    }

    fun onAddRawListener(view: View) {
        biomoduleBleManager.addRawDataListener(rawListener)
    }

    fun onRemoveRawListener(view: View) {
        biomoduleBleManager.removeRawDataListener(rawListener)
    }

    fun onAddHeartRateListener(view: View) {
        biomoduleBleManager.addHeartRateListener(heartRateListener)
    }

    fun onRemoveHeartRateListener(view: View) {
        biomoduleBleManager.removeHeartRateListener(heartRateListener)
    }

    fun onAddContactListener(view: View) {
        biomoduleBleManager.addContactListener(contactListener)
    }

    fun onRemoveContactListener(view: View) {
        biomoduleBleManager.removeContactListener(contactListener)
    }

    fun onBattery(view: View) {
        biomoduleBleManager.readBattery(fun(battery: NapBattery) {
            Logger.d("battery = " + battery)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "电量:" + battery, Toast.LENGTH_SHORT).show()
            }
        }, fun(error: String) {
            Logger.d("error is ${error}")
        })
    }

    fun onGetState(view: View) {
        Logger.d(biomoduleBleManager.isConnected())
        Toast.makeText(this, if (biomoduleBleManager.isConnected()) {
            "已连接"
        } else {
            "未连接"
        }, Toast.LENGTH_SHORT).show()
    }

    val batteryListener = fun(napBattery: NapBattery) {
        Logger.d("battery = ${napBattery}")
    }
    val batteryVoltageListener = fun(voltage: Double) {
        Logger.d("battery voltage = ${voltage}")
    }
    fun onAddBatteryListener(view: View) {
        biomoduleBleManager.addBatteryListener(batteryListener)
    }

    fun onRemoveBatteryListener(view: View) {
        biomoduleBleManager.removeBatteryListener(batteryListener)
    }
    fun onAddBatteryVoltageListener(view: View) {
        biomoduleBleManager.addBatteryVoltageListener(batteryVoltageListener)
    }

    fun onRemoveBatteryVoltageListener(view: View) {
        biomoduleBleManager.removeBatteryVoltageListener(batteryVoltageListener)
    }

    fun onReadHardware(view: View) {
        biomoduleBleManager.readDeviceHardware(fun(hardware: String) {
            Logger.d("hardware is " + hardware)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "硬件版本：${hardware}", Toast.LENGTH_SHORT).show()
            }
        }, fun(error: String) {
            Logger.d("error is " + error)
        })
    }

    fun onReadFirmware(view: View) {
        biomoduleBleManager.readDeviceFirmware(fun(firmware: String) {
            Logger.d("firmware is " + firmware)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "固件版本：${firmware}", Toast.LENGTH_SHORT).show()
            }
        }, fun(error: String) {
            Logger.d("error is " + error)
        })
    }

    fun onReadDeviceSerial(view: View) {
        biomoduleBleManager.readDeviceSerial(fun(serial: String) {
            Logger.d("serial is " + serial)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "序列号：${serial}", Toast.LENGTH_SHORT).show()
            }
        }, fun(error: String) {
            Logger.d("error is " + error)
        })
    }

    fun oReadDeviceManufacturer(view: View) {
        biomoduleBleManager.readDeviceManufacturer(fun(manufacturer: String) {
            Logger.d("manufacturer is " + manufacturer)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "制造商：${manufacturer}", Toast.LENGTH_SHORT).show()
            }
        }, fun(error: String) {
            Logger.d("error is " + error)
        })
    }

    fun onFindConnectedDevice(view:View){
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
