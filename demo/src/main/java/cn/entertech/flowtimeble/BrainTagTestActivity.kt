package cn.entertech.flowtimeble

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import cn.entertech.ble.device.BrainTagManager

class BrainTagTestActivity : AppCompatActivity(), View.OnClickListener {
    private var btnDisConnectBrainTag: Button? = null
    private var btnConnectBrainTag: Button? = null
    private var btnStartHrAndBrainCollection: Button? = null
    private var btnStopHrAndBrainCollection: Button? = null
    private var btnCheckConnectedDevice: Button? = null
    private val brainTagManager by lazy {
        BrainTagManager(this)
    }

    companion object {
        private const val TAG = "BrainTagTestActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.brain_tag_test_activity)
        btnDisConnectBrainTag = findViewById(R.id.btnDisConnectBrainTag)
        btnConnectBrainTag = findViewById(R.id.btnConnectBrainTag)
        btnStartHrAndBrainCollection = findViewById(R.id.btnStartHrAndBrainCollection)
        btnStopHrAndBrainCollection = findViewById(R.id.btnStopHrAndBrainCollection)
        btnCheckConnectedDevice = findViewById(R.id.btnCheckConnectedDevice)

        btnDisConnectBrainTag?.setOnClickListener(this)
        btnConnectBrainTag?.setOnClickListener(this)
        btnStartHrAndBrainCollection?.setOnClickListener(this)
        btnStopHrAndBrainCollection?.setOnClickListener(this)
        btnCheckConnectedDevice?.setOnClickListener(this)
        brainTagManager.addConnectListener {
            Log.d(TAG, "success connect $it")
        }
        brainTagManager.addDisConnectListener {
            Log.e(TAG, "DisConnect $it")
        }
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.btnDisConnectBrainTag -> {
                brainTagManager.disConnect()
            }

            R.id.btnConnectBrainTag -> {
                brainTagManager.connectDevice(successConnect = {
                    Log.d(TAG, "connectDevice success  $it")
                }, failure = {
                    Log.e(TAG, "connectDevice fail $it")
                })
            }

            R.id.btnStartHrAndBrainCollection -> {
                brainTagManager.startHeartAndBrainCollection(BrainTagManager.Mode.MODE_SLEEP, {
                    Log.d(TAG, "startHeartAndBrainCollection hr notify $it")
                }, {
                    Log.d(TAG, "startHeartAndBrainCollection brain data notify $it")
                }, writeCommandSuccess = {
                    Log.d(TAG, "startHeartAndBrainCollection writeCommandSuccess")
                }, writeCommandFailure = {
                    Log.e(TAG, "startHeartAndBrainCollection writeCommandFailure $it")
                })
            }

            R.id.btnStopHrAndBrainCollection -> {
                brainTagManager.stopHeartAndBrainCollection(
                    BrainTagManager.Mode.MODE_SLEEP,
                    writeCommandSuccess = {
                        Log.d(TAG, "stopHeartAndBrainCollection writeCommandSuccess")
                    },
                    writeCommandFailure = {
                        Log.e(TAG, "stopHeartAndBrainCollection writeCommandFailure")
                    })
            }

            R.id.btnCheckConnectedDevice -> {
                brainTagManager.findConnectedDevice({
                    Log.d(TAG, "findConnectedDevice writeCommandSuccess")
                }) {
                    Log.e(TAG, "findConnectedDevice writeCommandFailure")
                }
            }
        }
    }
}