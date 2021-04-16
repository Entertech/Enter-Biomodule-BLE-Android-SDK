package cn.entertech.bleuisdk.ui.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import cn.entertech.ble.multiple.MultipleBiomoduleBleManager
import cn.entertech.ble.utils.NapBattery
import cn.entertech.bleuisdk.R
import cn.entertech.bleuisdk.ui.DeviceUIConfig
import cn.entertech.bleuisdk.ui.widget.BatteryCircle
import cn.entertech.bleuisdk.utils.Constant.Companion.INTENT_BLE_MANAGER_INDEX
import cn.entertech.bleuisdk.utils.Constant.Companion.INTENT_WEB_TITLE
import cn.entertech.bleuisdk.utils.Constant.Companion.INTENT_WEB_URL
import cn.entertech.bleuisdk.utils.SettingManager
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_device.*
import kotlinx.android.synthetic.main.layout_common_title.*

/**
 * Created by EnterTech on 2017/11/21.
 */

class DeviceActivity : BaseActivity() {
    private lateinit var mMultipleBiomoduleBleManager: MultipleBiomoduleBleManager
    lateinit var bleDisConnectedListener: (String) -> Unit
    lateinit var bleConnectedListener: (String) -> Unit
    var mDeviceIndex: Int = 0
    lateinit var set: SettingManager
    lateinit var mDeviceUIConfig: DeviceUIConfig
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)
        initFullScreenDisplay()
        setStatusBarLight()
        mDeviceUIConfig = DeviceUIConfig.getInstance(this)
        set = SettingManager.getInstance(this)
        mDeviceIndex = intent.getIntExtra(INTENT_BLE_MANAGER_INDEX, 0)
        mMultipleBiomoduleBleManager = DeviceUIConfig.getInstance(this).managers[mDeviceIndex]
        initViews()
    }


    fun addConnectListener() {
        bleConnectedListener = fun(result: String) {
            Logger.d("device connect success:${result}")
            runOnUiThread {
                toConnect()
                updateDeviceInfo()
                initListview()
            }
        }
        mMultipleBiomoduleBleManager.addConnectListener(bleConnectedListener)
    }

    fun removeConnectedListener() {
        mMultipleBiomoduleBleManager.removeConnectListener(bleConnectedListener)
    }


    fun addDisConnectListener() {
        bleDisConnectedListener = fun(result: String) {
            Logger.d("connect failure:${result}")
            runOnUiThread {
                toDisConnect()
            }
        }
        mMultipleBiomoduleBleManager.addDisConnectListener(bleDisConnectedListener)
    }

    fun removeDisConnectedListener() {
        mMultipleBiomoduleBleManager.removeDisConnectListener(bleDisConnectedListener)
    }

    private fun initTitle() {
        rl_menu_ic.visibility = View.GONE
        findViewById<TextView>(R.id.tv_title).text = getString(R.string.device_status)
        findViewById<TextView>(R.id.tv_title).setTextColor(Color.parseColor("#2c2c2c"))
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }
    }


    private fun initViews() {
        initTitle()
        addDisConnectListener()
        addConnectListener()
        if (mMultipleBiomoduleBleManager.isConnected()) {
            toConnect()
            updateDeviceInfo()
            initListview()
        } else if (mMultipleBiomoduleBleManager.isConnecting()) {
            findViewById<View>(R.id.device_battery).visibility = View.GONE
            findViewById<View>(R.id.device_disconnect_layout).visibility = View.GONE
            findViewById<View>(R.id.device_connecting_layout).visibility = View.VISIBLE
        } else {
            toConnecting()
        }
        initDeleteButton()

    }

    fun initDeleteButton() {
        if (mDeviceUIConfig.isDeviceBind && set.getStringValue("ble_mac_$mDeviceIndex") != "") {
            tv_delete_device.visibility = View.VISIBLE
            line_delete_device_top.visibility = View.VISIBLE
            line_delete_device_bottom.visibility = View.VISIBLE
        } else {
            tv_delete_device.visibility = View.GONE
            line_delete_device_top.visibility = View.GONE
            line_delete_device_bottom.visibility = View.GONE
        }
        tv_delete_device.setOnClickListener {
            var alertDialog = AlertDialog.Builder(this)
                    .setMessage(getString(R.string.delete_device_tip))
                    .setTitle(getString(R.string.delete_device_title))
                    .setNegativeButton(getString(R.string.cancel), object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            dialog?.dismiss()
                        }

                    })
                    .setPositiveButton(getString(R.string.confirm), object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            dialog?.dismiss()
                            set.setStringValue("ble_mac_$mDeviceIndex", "")
                            set.setStringValue("ble_hardware_$mDeviceIndex", "")
                            set.setStringValue("ble_firmware_$mDeviceIndex", "")
                            mMultipleBiomoduleBleManager.disConnect()
                            finish()
                        }
                    })
                    .create()
            alertDialog.show()
        }
    }

    val onBatteryLevel = fun(battery: NapBattery) {
        runOnUiThread {
            val batteryView = findViewById<BatteryCircle>(R.id.device_battery)
            batteryView.setPercent(battery.percent)
            batteryView.setDescription(String.format(getString(R.string.device_time_left), battery.hours))
        }
    }

    fun isNewVersion(localVersion: String, cloudVersion: String): Boolean {
        var cloudVersions = cloudVersion.split(".")
        var localVersions = localVersion.split(".")
        if (cloudVersions.size == 3 && localVersions.size == 3) {
            var cloudVersionMajor = cloudVersions[0]
            var cloudVersionMinor = cloudVersions[1]
            var cloudVersionPatch = cloudVersions[2]
            var localVersionsMajor = localVersions[0]
            var localVersionsMinor = localVersions[1]
            var localVersionsPatch = localVersions[2]
            if (cloudVersionMajor > localVersionsMajor) {
                return true
            } else if (cloudVersionMajor == localVersionsMajor) {
                if (cloudVersionMinor > localVersionsMinor) {
                    return true
                } else if (cloudVersionMinor == localVersionsMinor) {
                    if (cloudVersionPatch > localVersionsPatch) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun updateDeviceInfo() {

        var mac = mMultipleBiomoduleBleManager.getDevice()?.macAddress
        findViewById<TextView>(R.id.device_mac).text = mac
        if (mac != null) {
            set.setStringValue("ble_mac_$mDeviceIndex", mac)
        }
        val failure = fun(error: String) {
            Logger.d(error)
        }

        Thread.sleep(100)
        mMultipleBiomoduleBleManager.readDeviceHardware(fun(msg: String) {
            Logger.d(msg)
            set.setStringValue("ble_hardware_$mDeviceIndex", msg)
            runOnUiThread {
                findViewById<TextView>(R.id.device_hardware).text = msg
            }
        }, failure)

        Thread.sleep(200)
        mMultipleBiomoduleBleManager.readDeviceFirmware(fun(msg: String) {
            Logger.d(msg)
            set.setStringValue("ble_firmware_$mDeviceIndex", msg)
            runOnUiThread {
                findViewById<TextView>(R.id.device_firmware).text = msg
                var deviceUIConfig = DeviceUIConfig.getInstance(this)
                var firmwareOldVersion = msg
                var firmwareNewVersion = deviceUIConfig.firmwareNewVersion
                if (deviceUIConfig.isForceUpdate) {
                    tv_firmware_update_flag.visibility = View.VISIBLE
                    device_firmware_layout.setOnClickListener {
                        startActivity(Intent(this@DeviceActivity , DeviceUpdateActivity::class.java)
                                .putExtra("firmwarePath", deviceUIConfig.firmwareUpdatePath))
                    }
                } else {
                    if (firmwareNewVersion != null && isNewVersion(firmwareOldVersion, firmwareNewVersion)) {
                        tv_firmware_update_flag.visibility = View.VISIBLE
                        device_firmware_layout.setOnClickListener {
                            startActivity(Intent(this@DeviceActivity, DeviceUpdateActivity::class.java)
                                    .putExtra("firmwarePath", deviceUIConfig.firmwareUpdatePath))
                        }
                    } else {
                        tv_firmware_update_flag.visibility = View.GONE
                        device_firmware_layout.setOnClickListener {

                        }
                    }
                }
            }
        }, failure)

        mMultipleBiomoduleBleManager.readBattery(onBatteryLevel, null)

        mMultipleBiomoduleBleManager.addBatteryListener(onBatteryLevel)

        initDeleteButton()
    }

    private fun initListview() {
        val hardware = findViewById<TextView>(R.id.device_hardware)
        val firmware = findViewById<TextView>(R.id.device_firmware)
        val mac = findViewById<TextView>(R.id.device_mac)
        hardware.text = set.getStringValue("ble_hardware_$mDeviceIndex")
        firmware.text = set.getStringValue("ble_firmware_$mDeviceIndex")
        mac.text = set.getStringValue("ble_mac_$mDeviceIndex")
    }

    private fun toDisConnect() {
        findViewById<View>(R.id.device_battery).visibility = View.GONE
        findViewById<View>(R.id.device_disconnect_layout).visibility = View.VISIBLE
        findViewById<View>(R.id.device_connecting_layout).visibility = View.GONE
        findViewById<View>(R.id.device_guide_layout).visibility = View.VISIBLE
        cover_disconnected.visibility = View.VISIBLE
        findViewById<View>(R.id.toolbar_layout).setBackgroundColor(mDeviceUIConfig.mainColor)
        findViewById<View>(R.id.device_hardware_layout).alpha = 0.5f
        findViewById<View>(R.id.device_firmware_layout).alpha = 0.5f
        findViewById<View>(R.id.device_mac_layout).alpha = 0.5f
        findViewById<View>(R.id.device_search).alpha = 0.5f
    }

    private fun toConnecting() {
        cover_disconnected.visibility = View.GONE
        findViewById<View>(R.id.device_battery).visibility = View.GONE
        findViewById<View>(R.id.device_disconnect_layout).visibility = View.GONE
        findViewById<View>(R.id.device_connecting_layout).visibility = View.VISIBLE
        findViewById<View>(R.id.toolbar_layout).setBackgroundColor(mDeviceUIConfig.mainColor)
        var mac = set.getStringValue("ble_mac_$mDeviceIndex")
        if (mDeviceUIConfig.isDeviceBind && mac != null && mac != "") {
            mMultipleBiomoduleBleManager.scanMacAndConnect(mac, successConnect = fun(mac: String) {
                Logger.d("connect success mac:${mac}")
                runOnUiThread {
                    SettingManager.getInstance(this).isConnectBefore = true
                    toConnect()
                    updateDeviceInfo()
                    initListview()
                }
            },failure = fun(error: String) {
                Logger.d("connect failure:${error}")
                runOnUiThread {
                    toDisConnect()
                }
            })
        } else {
            mMultipleBiomoduleBleManager.scanNearDeviceAndConnect(
                    fun() {
                        Logger.d("scan succ")
                    },
                    fun(e: Exception) {
                        Logger.d("scan error:${e}")
                        runOnUiThread {
                            toDisConnect()
                        }
                    },
                    fun(mac: String) {
                        Logger.d("connect succ shake succ" + mac)
                        runOnUiThread {
                            SettingManager.getInstance(this).isConnectBefore = true
                            toConnect()
                            updateDeviceInfo()
                            initListview()
                        }
                    },
                    fun(error: String) {
                        Logger.d("connect failure:${error}")
                        runOnUiThread {
                            toDisConnect()
                        }
                    })
        }

    }

    private fun toConnect() {
        cover_disconnected.visibility = View.GONE
        findViewById<View>(R.id.device_battery).visibility = View.VISIBLE
        findViewById<View>(R.id.device_disconnect_layout).visibility = View.GONE
        findViewById<View>(R.id.device_connecting_layout).visibility = View.GONE
        findViewById<View>(R.id.device_guide_layout).visibility = View.GONE
        findViewById<View>(R.id.device_hardware_layout).alpha = 1f
        findViewById<View>(R.id.device_firmware_layout).alpha = 1f
        findViewById<View>(R.id.device_mac_layout).alpha = 1f
        findViewById<View>(R.id.toolbar_layout).setBackgroundColor(mDeviceUIConfig.mainColor)

//        initListview()

//        val onBatteryLevel = fun(battery: NapBattery) {
//            runOnUiThread {
//                val batteryView = findViewById<BatteryCircle>(R.id.device_battery)
//                batteryView.setPercent(battery.percent)
//                batteryView.setDescription(String.format(getString(R.string.device_time_left), battery.hours))
//            }
//        }
//
//        rxBleManager.readBattery(onBatteryLevel, null)
//
//        rxBleManager.notifyBattery(onBatteryLevel,null)
    }

    fun onConnectGuide(@Suppress("UNUSED_PARAMETER")view: View) {
//        startActivity(Intent(this, WebActivity::class.java).putExtra(INTENT_WEB_TITLE, getString(R.string.can_t_connect_to_the_device))
//                .putExtra(INTENT_WEB_URL, getString(R.string.device_can_not_connect_url)))
        startActivity(Intent(this, DeviceCanNotConnectActivity::class.java))
    }

    fun onReConnect(@Suppress("UNUSED_PARAMETER")view: View) {
        toConnecting()
    }

    fun onHardware(@Suppress("UNUSED_PARAMETER")view: View) {
    }

    fun onFirmware(@Suppress("UNUSED_PARAMETER")view: View) {
    }

    fun onMac(@Suppress("UNUSED_PARAMETER")view: View) {
    }

    fun onUnbind(@Suppress("UNUSED_PARAMETER")view: View) {
//        startActivity(Intent(this, DeviceDeleteActivity::class.java))
    }

    fun onFindConnectedDevice(@Suppress("UNUSED_PARAMETER")view: View) {
        mMultipleBiomoduleBleManager.findConnectedDevice()
    }

    override fun onDestroy() {
        removeDisConnectedListener()
        removeConnectedListener()
        mMultipleBiomoduleBleManager.removeBatteryListener(onBatteryLevel)
        super.onDestroy()
    }
}