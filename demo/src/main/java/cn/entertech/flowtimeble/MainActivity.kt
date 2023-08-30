package cn.entertech.flowtimeble

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import androidx.core.app.ActivityCompat
import android.view.View
import android.widget.Toast
import cn.entertech.ble.ConnectionBleStrategy
import cn.entertech.ble.single.BiomoduleBleManager
import cn.entertech.ble.utils.BleLogUtil
import cn.entertech.ble.utils.NapBattery
import cn.entertech.bleuisdk.ui.DeviceUIConfig
import cn.entertech.bleuisdk.ui.activity.DeviceManagerActivity
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var biomoduleBleManager: BiomoduleBleManager

    companion object {
        private const val TAG = "MainActivity"

        /**
         * 2s
         * */
        private const val RECONNECT_DELAY_TIME = 2000L

        /**
         * 30s
         * */
        private const val CHECK_CONNECT_TIME = 1000 * 30L
    }

    private val mainHandler by lazy {
        Handler(Looper.getMainLooper())
    }


    /**
     * 是否是持久性实验
     * true 是，
     * 连接失败/断开 都会尝试重连，
     * 连接成功后会每隔[CHECK_CONNECT_TIME]检查连接状态
     * 连接成功后会进行脑波心率数据的采集
     * */
    private var isPersistenceExperiment = false

    private val reconnectRunnable: Runnable by lazy {
        Runnable {
            BleLogUtil.i(TAG, "reconnectRunnable")
            onConnectBound()
        }
    }

    private val checkConnectRunnable by lazy {
        object : Runnable {
            override fun run() {
                BleLogUtil.i(
                    TAG,
                    "checkConnectRunnable biomoduleBleManager.isConnected()： ${biomoduleBleManager.isConnected()}"
                )
                mainHandler.postDelayed(this, CHECK_CONNECT_TIME)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        biomoduleBleManager = BiomoduleBleManager.getInstance(this)
        initPermission()
        DeviceUIConfig.getInstance(this).init(false, false, 1)
        DeviceUIConfig.getInstance(this).updateFirmware(
            "1.2.0",
            "${Environment.getExternalStorageDirectory()}/dfufile.zip",
            true
        )
        btnConnect.setOnClickListener {
            onConnectBound()
        }
        initPersistenceState()

    }


    /**
     * Android6.0 auth
     */
    private fun initPermission() {
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
        startActivity(Intent(this@MainActivity, DeviceManagerActivity::class.java))
    }

    var contactListener = fun(contactState: Int) {
        BleLogUtil.i(TAG, "contace state is ${contactState}")
    }

    var connectedListener = fun(string: String) {
        BleLogUtil.i(TAG, "connect success${string}")
        runOnUiThread {
            Toast.makeText(this@MainActivity, "connect success", Toast.LENGTH_SHORT).show()
        }
    }
    var disConnectedListener = fun(string: String) {
        BleLogUtil.i(TAG, "disconnect $string")
        reconnect()
        runOnUiThread {
            Toast.makeText(this@MainActivity, "disconnect ", Toast.LENGTH_SHORT).show()
        }
    }

    private fun reconnect() {
        mainHandler.removeCallbacks(reconnectRunnable)
        mainHandler.postDelayed(reconnectRunnable, RECONNECT_DELAY_TIME)
    }


    @OptIn(ExperimentalStdlibApi::class)
    fun onConnectBound() {
        mainHandler.removeCallbacks(reconnectRunnable)
        biomoduleBleManager.connectDevice({
            BleLogUtil.i(TAG, "connect Bound success")
            if(isPersistenceExperiment) {
                mainHandler.post(checkConnectRunnable)
                onCollectBrainAndHeartStart()
            }
            runOnUiThread {
                Toast.makeText(this@MainActivity, "connect success ", Toast.LENGTH_SHORT).show()
            }

        }, {
            BleLogUtil.i(TAG, "connect Bound failed error $it ")
            if(isPersistenceExperiment) {
                reconnect()
            }
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity, "connect Bound failed error $it ", Toast.LENGTH_SHORT
                ).show()
            }
        }, ConnectionBleStrategy.CONNECT_BONDED, { name, macAdress ->
            name?.lowercase()?.startsWith("flowtime") ?: false
        }
        )

    }

    fun onConnect(@Suppress("UNUSED_PARAMETER") view: View) {
        mainHandler.removeCallbacks(reconnectRunnable)
        biomoduleBleManager.connectDevice(fun(mac: String) {
            BleLogUtil.i(TAG, "connect success $mac")
            runOnUiThread {
                Toast.makeText(this@MainActivity, "connect to device success", Toast.LENGTH_SHORT)
                    .show()
            }
        }, { msg ->
            BleLogUtil.i(TAG, "connect failed")
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "failed to connect to device：${msg}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, ConnectionBleStrategy.SCAN_AND_CONNECT_HIGH_SIGNAL)
    }

    private fun initPersistenceState() {
        btnSwapPersistenceState.text = if (isPersistenceExperiment) {
            "当前是处于持续性实验状态"
        } else {
            "当前是处于非持续性实验状态"
        }
    }


    fun onSwapPersistenceState(@Suppress("UNUSED_PARAMETER") view: View) {
        isPersistenceExperiment = !isPersistenceExperiment
        initPersistenceState()
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
        onCollectBrainAndHeartStart()
    }

    fun onCollectBrainAndHeartStart(){
        biomoduleBleManager.startHeartAndBrainCollection()
    }

    fun onCollectBrainAndHeartStop(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.stopHeartAndBrainCollection()
    }

    var rawListener = fun(bytes: ByteArray) {
//        BleLogUtil.d(TAG,"firmware fixing hex " + HexDump.toHexString(bytes))
        Log.d("######", "braindata: " + HexDump.toHexString(bytes))
//        BleLogUtil.d(TAG,"brain data is " + Arrays.toString(bytes))
    }

    var heartRateListener = fun(heartRate: Int) {
        BleLogUtil.d(TAG, "heart rate data is " + heartRate)
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
            BleLogUtil.d(TAG, "battery = $battery")
            runOnUiThread {
                Toast.makeText(this@MainActivity, "电量:$battery", Toast.LENGTH_SHORT).show()
            }
        }, fun(error: String) {
            BleLogUtil.d(TAG, "error is ${error}")
        })
    }

    fun onGetState(@Suppress("UNUSED_PARAMETER") view: View) {
        BleLogUtil.d(TAG, "biomoduleBleManager.isConnected()： ${biomoduleBleManager.isConnected()}")
        Toast.makeText(
            this, if (biomoduleBleManager.isConnected()) {
                "connected"
            } else {
                "disconnect"
            }, Toast.LENGTH_SHORT
        ).show()
    }

    val batteryListener = fun(napBattery: NapBattery) {
        BleLogUtil.d(TAG, "battery = ${napBattery}")
    }
    val batteryVoltageListener = fun(voltage: Double) {
        BleLogUtil.d(TAG, "battery voltage = ${voltage}")
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
            BleLogUtil.d(TAG, "hardware is " + hardware)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "hardware is：${hardware}", Toast.LENGTH_SHORT)
                    .show()
            }
        }, fun(error: String) {
            BleLogUtil.d(TAG, "error is " + error)
        })
    }

    fun onReadFirmware(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.readDeviceFirmware(fun(firmware: String) {
            BleLogUtil.d(TAG, "firmware is " + firmware)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "firmware is：${firmware}", Toast.LENGTH_SHORT)
                    .show()
            }
        }, fun(error: String) {
            BleLogUtil.d(TAG, "error is " + error)
        })
    }

    fun onReadDeviceSerial(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.readDeviceSerial(fun(serial: String) {
            BleLogUtil.d(TAG, "serial is " + serial)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "serial is：${serial}", Toast.LENGTH_SHORT).show()
            }
        }, fun(error: String) {
            BleLogUtil.d(TAG, "error is " + error)
        })
    }

    fun oReadDeviceManufacturer(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.readDeviceManufacturer(fun(manufacturer: String) {
            BleLogUtil.d(TAG, "manufacturer is " + manufacturer)
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "manufacturer is：${manufacturer}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, fun(error: String) {
            BleLogUtil.d(TAG, "error is " + error)
        })
    }

    fun showLog(view: View) {
        startActivity(Intent(this, ShowLogActivity::class.java))
    }

    fun onFindConnectedDevice(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.findConnectedDevice()
    }

    fun goToSkinConductivity(view: View){
        if (!biomoduleBleManager.isConnected()) {
            Toast.makeText(applicationContext, "请先连接设备", Toast.LENGTH_SHORT).show()
        }
        startActivity(Intent(this, SkinConductivity::class.java))
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
