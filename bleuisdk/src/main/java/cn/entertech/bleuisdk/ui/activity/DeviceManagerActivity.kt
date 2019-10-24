package cn.entertech.bleuisdk.ui.activity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.location.LocationManager
import android.support.v4.app.ActivityCompat
import android.widget.ImageView
import android.widget.TextView
import cn.entertech.bleuisdk.R
import cn.entertech.bleuisdk.ui.DeviceUIConfig
import cn.entertech.bleuisdk.utils.SettingManager
import cn.entertech.bleuisdk.utils.hasLocationPermission
import kotlinx.android.synthetic.main.activity_device_manager.*


/**
 * Created by EnterTech on 2017/11/22.
 */
class DeviceManagerActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFullScreenDisplay()
        setStatusBarLight()
        if (isLocationEnable() && hasLocationPermission(this) && BluetoothAdapter.getDefaultAdapter().isEnabled) {
            if (DeviceUIConfig.getInstance(this).isMultipleDevice) {
                startActivity(Intent(this, MultipleDeviceListActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
            } else {
                if (!SettingManager.getInstance(this).isConnectBefore) {
                    startActivity(Intent(this, DeviceTurnOnActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                } else {
                    startActivity(Intent(this, DeviceActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                }
            }
            finish()
            return
        }
        setContentView(R.layout.activity_device_manager)

//        initToolBar()
    }

    override fun onResume() {
        super.onResume()
        initViews()
    }

    fun initViews() {
        var mainColor = DeviceUIConfig.getInstance(this).mainColor
        tv_open_location_access.setTextColor(mainColor)
        permisson_ble.setTextColor(mainColor)
        if (isLocationEnable() && hasLocationPermission(this) && BluetoothAdapter.getDefaultAdapter().isEnabled) {
            if (DeviceUIConfig.getInstance(this).isMultipleDevice) {
                startActivity(Intent(this, MultipleDeviceListActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
            } else {
                if (!SettingManager.getInstance(this).isConnectBefore) {
                    startActivity(Intent(this, DeviceTurnOnActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                } else {
                    startActivity(Intent(this, DeviceActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
                }
            }
            finish()
            return
        }

        findViewById<View>(R.id.permisson_location).visibility = if (isLocationEnable() && hasLocationPermission(this)) {
            View.GONE
        } else {
            View.VISIBLE
        }

        findViewById<View>(R.id.permisson_ble).visibility = if (BluetoothAdapter.getDefaultAdapter().isEnabled) {
            View.GONE
        } else {
            View.VISIBLE
        }

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.tv_title).visibility = View.INVISIBLE
    }

    fun onLocation(view: View) {
        if (!isLocationEnable()) {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            return
        }

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
            ActivityCompat.requestPermissions(this@DeviceManagerActivity, permissions, 1)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (1 == requestCode && grantResults[0] != PERMISSION_GRANTED) {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }
    }

    fun onBluetooth(view: View) {
        startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
    }

    fun onConnect(view: View) {
        startActivity(Intent(this, DeviceActivity::class.java))
    }

    fun isLocationEnable(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return gps || network
    }
}