package cn.entertech.flowtimeble.device.all

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import cn.entertech.ble.device.eyehead.EyeHeadManager
import cn.entertech.ble.device.headband.HeadbandManger
import cn.entertech.ble.device.tag.BrainTagManager
import cn.entertech.flowtimeble.R
import cn.entertech.flowtimeble.data.MeditateDataHelper
import cn.entertech.flowtimeble.device.BaseDeviceActivity

class FTBleDeviceDemoActivity : BaseDeviceActivity() {

    companion object {
        private const val TAG = "HeadBandDemoActivity"
        private const val DEVICE_TYPE_TAG = "device_type_tag"
        private const val DEVICE_TYPE_HEADBAND = "device_type_headband"
        private const val DEVICE_TYPE_EYE_HEAD = "device_type_eye_head"
    }

    private var spinnerDeviceTypeList: Spinner? = null
    private val deviceTypes by lazy {
        listOf(
            DEVICE_TYPE_TAG,
            DEVICE_TYPE_HEADBAND,
            DEVICE_TYPE_EYE_HEAD,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        spinnerDeviceTypeList = findViewById(R.id.spinnerDeviceTypeList)
        spinnerDeviceTypeList?.visibility = View.VISIBLE
        // 创建 ArrayAdapter
        val arrayAdapter: ArrayAdapter<String> =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, deviceTypes)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDeviceTypeList?.adapter = arrayAdapter
        spinnerDeviceTypeList?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    val item = parent?.getItemAtPosition(position) as? String
                    when (item) {
                        DEVICE_TYPE_TAG -> {
                            if (bluetoothDeviceManager is BrainTagManager) {
                                return
                            }
                            changeDeviceType()
                            bluetoothDeviceManager = BrainTagManager(this@FTBleDeviceDemoActivity)
                            meditateDataHelper = MeditateDataHelper("all_brain_tag")
                        }

                        DEVICE_TYPE_HEADBAND -> {
                            if (bluetoothDeviceManager is HeadbandManger) {
                                return
                            }
                            changeDeviceType()
                            bluetoothDeviceManager = HeadbandManger(this@FTBleDeviceDemoActivity)
                            meditateDataHelper = MeditateDataHelper("all_headband")
                        }

                        DEVICE_TYPE_EYE_HEAD -> {
                            if (bluetoothDeviceManager is EyeHeadManager) {
                                return
                            }
                            changeDeviceType()
                            bluetoothDeviceManager = EyeHeadManager(this@FTBleDeviceDemoActivity)
                            meditateDataHelper = MeditateDataHelper("all_eye_head")

                        }

                        else -> {
                            bluetoothDeviceManager = null
                        }
                    }
                    bluetoothDeviceManager?.addConnectListener(connectListener)
                    bluetoothDeviceManager?.addDisConnectListener(disconnectListener)
                    initBleFunctionList()

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    bluetoothDeviceManager = null
                    changeDeviceType()
                    initBleFunctionList()
                }
            }
    }


    private fun changeDeviceType() {
        bluetoothDeviceManager?.disConnect()
        bluetoothDeviceManager?.removeConnectListener(connectListener)
        bluetoothDeviceManager?.removeDisConnectListener(disconnectListener)
        meditateDataHelper?.close()
    }
}