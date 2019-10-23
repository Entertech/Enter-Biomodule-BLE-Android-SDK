package cn.entertech.ble.ui.activity

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import cn.entertech.ble.R
import cn.entertech.ble.single.BiomoduleBleManager
import cn.entertech.ble.ui.BaseActivity
import cn.entertech.ble.ui.DeviceUIConfig
import cn.entertech.ble.ui.service.DfuService
import cn.entertech.ble.utils.SettingManager
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_device_update.*
import kotlinx.android.synthetic.main.layout_common_title.*
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter
import no.nordicsemi.android.dfu.DfuServiceInitiator
import no.nordicsemi.android.dfu.DfuServiceListenerHelper

class DeviceUpdateActivity : BaseActivity() {
//
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFullScreenDisplay()
        setContentView(R.layout.activity_device_update)
        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener)
        initView()
        updateTip()
    }
//
    private val mDfuProgressListener = object : DfuProgressListenerAdapter() {
        override fun onDeviceConnecting(deviceAddress: String) {
            Log.d("onDfuUpdate", "onDeviceConnecting")
        }

        override fun onDfuProcessStarting(deviceAddress: String) {
            Log.d("onDfuUpdate", "onDfuProcessStarting")
        }

        override fun onEnablingDfuMode(deviceAddress: String) {

            Log.d("onDfuUpdate", "onEnablingDfuMode")
        }

        override fun onFirmwareValidating(deviceAddress: String) {

            Log.d("onDfuUpdate", "onFirmwareValidating")
        }

        override fun onDeviceDisconnecting(deviceAddress: String) {

            Log.d("onDfuUpdate", "onDeviceDisconnecting")
        }

        override fun onDfuCompleted(deviceAddress: String) {
            Log.d("onDfuUpdate", "onDfuCompleted")
            updateCompleted()
        }

        override fun onDfuAborted(deviceAddress: String) {
            Log.d("onDfuUpdate", "onDfuAborted")
            updateFailed()
        }

        override fun onProgressChanged(
            deviceAddress: String,
            percent: Int,
            speed: Float,
            avgSpeed: Float,
            currentPart: Int,
            partsTotal: Int
        ) {
            Log.d("onDfuUpdate", "onProgressChanged")
            Logger.d("onProgressChanged percent = $percent")
        }

        override fun onError(deviceAddress: String, error: Int, errorType: Int, message: String) {
            Log.d("onDfuUpdate", "onError:" + error + "::" + message + "::" + errorType)
            updateFailed()
        }
    }

    fun startUpdate(view: View) {
        updating()
    }

    fun initView() {
        initTitle()
    }

    fun initTitle() {
        bg.setBackgroundColor(DeviceUIConfig.getInstance(this).mainColor)
        iv_back.setImageResource(R.mipmap.ic_back_white)
        iv_back.setOnClickListener {
            finish()
        }
        tv_title.text = getString(R.string.device_update_title)
        tv_title.setTextColor(Color.WHITE)
        iv_menu_icon.visibility = View.GONE
    }

    fun updateTip() {
        btn_update.visibility = View.VISIBLE
        ic_update.visibility = View.VISIBLE
//        anim_update_loading.visibility = View.GONE
        ic_update.setImageResource(R.drawable.vector_drawable_cloud_download)
        tv_update_state.text = getString(R.string.update_tip_title)
        tv_update_tip.text = "sdafasfadsas"
    }

    fun updating() {
        btn_update.visibility = View.INVISIBLE
        ic_update.visibility = View.GONE
//        anim_update_loading.visibility = View.VISIBLE
        tv_update_state.text = getString(R.string.updating)
        tv_update_tip.text = getString(R.string.updating_tip)
        var bleMac = SettingManager.getInstance(this).bleMac
        var firmwarePath = intent.getStringExtra("firmwarePath")
        val starter = DfuServiceInitiator(bleMac)
            .setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DfuServiceInitiator.createDfuNotificationChannel(this)
        }
//        starter.setKeepBond(true)
//        starter.setZip(R.raw.dfu)
        starter.setZip(firmwarePath)
        starter.start(this, DfuService::class.java)
    }

    fun updateCompleted() {
        btn_update.visibility = View.INVISIBLE
        ic_update.visibility = View.VISIBLE
        ic_update.setImageResource(R.mipmap.ic_right_green)
//        anim_update_loading.visibility = View.GONE
        tv_update_state.text = getString(R.string.update_completed)
        tv_update_tip.text = getString(R.string.update_completed_tip)
        reconnectDevice()
    }

    fun reconnectDevice() {
        BiomoduleBleManager.getInstance(this)
            .scanMacAndConnect(SettingManager.getInstance(this).bleMac, fun(success: String) {
                BiomoduleBleManager.getInstance(this).readDeviceFirmware(fun(version: String) {
                    SettingManager.getInstance(this).bleFirmware = version
                }, fun(error: String) {

                })
            }, fun(error: String) {

            })
    }

    fun updateFailed() {
        btn_update.visibility = View.INVISIBLE
        ic_update.visibility = View.VISIBLE
        ic_update.setImageResource(R.mipmap.ic_failed_red)
//        anim_update_loading.visibility = View.GONE
        tv_update_state.text = getString(R.string.update_failed)
        tv_update_tip.text = getString(R.string.update_failed_tip)
    }

    override fun onDestroy() {
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener)
        super.onDestroy()
    }
}
