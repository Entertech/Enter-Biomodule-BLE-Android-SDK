package cn.entertech.bleuisdk.ui.widget

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import cn.entertech.ble.single.BiomoduleBleManager
import cn.entertech.ble.utils.BleLogUtil
import cn.entertech.ble.utils.NapBattery
import cn.entertech.bleuisdk.R
import cn.entertech.bleuisdk.ui.activity.DeviceActivity
import cn.entertech.bleuisdk.utils.getBatteryResId

class DeviceConnectView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, def: Int = 0) :
    RelativeLayout(context, attributeSet, def) {
    var self = LayoutInflater.from(context).inflate(R.layout.layout_device_connect_view, null)
    var image: ImageView? = null

    enum class IconType {
        COLOR, WHITE
    }

    companion object{
        private const val TAG="DeviceConnectView"
    }

    var connectListener = fun(str: String) {
        BleLogUtil.d(TAG,"connect success:${str}")
        BiomoduleBleManager.getInstance(context).readBattery(fun(napBattery: NapBattery) {
            (context as Activity).runOnUiThread {
                image?.setImageResource(getBatteryResId(napBattery.percent, mIconType))
            }
        }, fun(error: String) {
            BleLogUtil.d(TAG,"connect error:${error}")
            (context as Activity).runOnUiThread {
                image?.setImageResource(R.mipmap.ic_device_disconnect_color)
            }
        })
    }

    var disconnectListener = fun(str: String) {
        BleLogUtil.d(TAG,"disconnect:${str}")
        (context as Activity).runOnUiThread {
            if (mIconType == IconType.COLOR) {
                image?.setImageResource(R.mipmap.ic_device_disconnect_color)
            } else {
                image?.setImageResource(R.mipmap.ic_device_disconnect_white)
            }
        }
    }

    init {
        val fp = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        self.layoutParams = fp
        addView(self)
        image = self.findViewById(R.id.iv_icon)
        initListener()

    }


    fun initListener() {
        BiomoduleBleManager.getInstance(context).addConnectListener(connectListener)
        BiomoduleBleManager.getInstance(context).addDisConnectListener(disconnectListener)
    }

    private var mIconType: IconType? = null

    fun setType(type: IconType) {
        this.mIconType = type
        if (mIconType == IconType.COLOR) {
            image?.setImageResource(R.mipmap.ic_device_disconnect_color)
        } else {
            image?.setImageResource(R.mipmap.ic_device_disconnect_white)
        }
        if (BiomoduleBleManager.getInstance(context).isConnected()) {
            BiomoduleBleManager.getInstance(context).readBattery(fun(napBattery: NapBattery) {
                (context as Activity).runOnUiThread {
                    image?.setImageResource(getBatteryResId(napBattery.percent, mIconType))
                }
            }, fun(error: String) {
                BleLogUtil.d(TAG,"read battery error:${error}")
                (context as Activity).runOnUiThread {
                    if (mIconType == IconType.COLOR) {
                        image?.setImageResource(R.mipmap.ic_device_disconnect_color)
                    } else {
                        image?.setImageResource(R.mipmap.ic_device_disconnect_white)
                    }
                }
            })
        }
    }

    fun release() {
        BiomoduleBleManager.getInstance(context).removeConnectListener(connectListener)
        BiomoduleBleManager.getInstance(context).removeDisConnectListener(disconnectListener)
    }

    override fun onDetachedFromWindow() {
        release()
        super.onDetachedFromWindow()
    }

}