package cn.entertech.ble.ui.widget

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import cn.entertech.ble.R
import cn.entertech.ble.single.BiomoduleBleManager
import cn.entertech.ble.utils.NapBattery
import cn.entertech.ble.utils.getBatteryResId

class DeviceConnectView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, def: Int = 0) :
    RelativeLayout(context, attributeSet, def) {
    var self = LayoutInflater.from(context).inflate(R.layout.layout_device_connect_view, null)
    var image: ImageView? = null

    enum class IconType {
        COLOR, WHITE
    }

    var connectLinstener = fun(str: String) {
        BiomoduleBleManager.getInstance(context).readBattery(fun(napBattery: NapBattery) {
            (context as Activity).runOnUiThread {
                image?.setImageResource(getBatteryResId(napBattery.percent, mIconType))
            }
        }, fun(error: String) {
            (context as Activity).runOnUiThread {
                image?.setImageResource(R.mipmap.ic_device_disconnect_color)
            }
        })
    }

    var disconnectListener = fun(str: String) {
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
        BiomoduleBleManager.getInstance(context).addConnectListener(connectLinstener)
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
        BiomoduleBleManager.getInstance(context).removeConnectListener(connectLinstener)
        BiomoduleBleManager.getInstance(context).removeDisConnectListener(disconnectListener)
    }

    override fun onDetachedFromWindow() {
        release()
        super.onDetachedFromWindow()
    }

}