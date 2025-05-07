package cn.entertech.flowtimeble.ble

import android.content.Context
import cn.entertech.ble.BaseBleConnectManager
import cn.entertech.ble.api.ConnectionBleStrategy
import cn.entertech.ble.device.tag.BrainTagManager
import cn.entertech.ble.device.cushion.CushionManager
import cn.entertech.ble.device.headband.HeadbandManger
import cn.entertech.device.DeviceType
import java.util.concurrent.ConcurrentHashMap

abstract class BaseEnterBleDeviceManager<DeviceKey> {

    protected val deviceMap by lazy {
        HashMap<DeviceKey, BaseBleConnectManager?>()
    }

    protected val disConnectedListenersMap by lazy {
        ConcurrentHashMap<DeviceKey, MutableList<(String) -> Unit>?>()
    }

    protected val connectedListenersMap by lazy {
        ConcurrentHashMap<DeviceKey, MutableList<(String) -> Unit>?>()
    }
    protected val rawListenerMap by lazy {
        ConcurrentHashMap<DeviceKey, (ByteArray) -> Unit>()
    }

    protected val hrListenerMap by lazy {
        ConcurrentHashMap<DeviceKey, (Int) -> Unit>()
    }

    protected val hasNotifyMap by lazy {
        ConcurrentHashMap<DeviceKey, Boolean>()
    }
    protected val reconnectRunnableMap by lazy {
        ConcurrentHashMap<DeviceKey, Runnable>()
    }

    protected fun <T, K> MutableMap<T, K?>.getValueWithInit(
        key: T,
        initValue: () -> K?,
        checkValidValue: (K?) -> Boolean = {
            it != null
        },
    ): K? {
        var value = get(key)
        if (!checkValidValue(value)) {
            value = initValue()
            this[key] = value
        }
        return value
    }

    fun getBleConnectManager(
        deviceKe: DeviceKey,
        deviceType: DeviceType,
        context: Context,
    ): BaseBleConnectManager? {
        return deviceMap.getValueWithInit(deviceKe, initValue = {
            when (deviceType) {
                DeviceType.DEVICE_TYPE_HEADBAND -> HeadbandManger(context)
                DeviceType.DEVICE_TYPE_BRAIN_TAG -> BrainTagManager(context)
                DeviceType.DEVICE_TYPE_CUSHION -> CushionManager(context)
                else -> null
            }
        })
    }


    fun connectDevice(
        deviceKe: DeviceKey,
        deviceType: DeviceType,
        context: Context,
        mac: String = "",
        success: (String) -> Unit,
        failure: (String) -> Unit
    ) {
        val bleManager = getBleConnectManager(deviceKe, deviceType, context)
        val connectionBleStrategy = if (mac.isEmpty()) {
            ConnectionBleStrategy.SCAN_AND_CONNECT_HIGH_SIGNAL
        } else {
            ConnectionBleStrategy.CONNECT_DEVICE_MAC
        }
        bleManager?.connectDevice(
            {
                success(it)
                connectedListenersMap.getValueWithInit(deviceKe, initValue = { mutableListOf() })
                    ?.onEach { listener ->
                        listener(it)
                    }
            },
            {
                failure(it)
                disConnectedListenersMap.getValueWithInit(deviceKe, initValue = { mutableListOf() })
                    ?.onEach { listener ->
                        listener(it)
                    }
            },
            connectionBleStrategy = connectionBleStrategy,
            mac = mac
        )
    }

    fun addConnectedListener(deviceKe: DeviceKey, listener: (String) -> Unit) {
        connectedListenersMap.getValueWithInit(deviceKe, initValue = { mutableListOf() })?.apply {
            if (!contains(listener)) {
                add(listener)
            }
        }
    }

    fun removeConnectedListener(deviceKe: DeviceKey, listener: (String) -> Unit) {
        connectedListenersMap.getValueWithInit(deviceKe, initValue = { mutableListOf() })?.apply {
            remove(listener)
        }
    }

    fun addDisconnectedListener(deviceKe: DeviceKey, listener: (String) -> Unit) {
        disConnectedListenersMap.getValueWithInit(deviceKe, initValue = { mutableListOf() })
            ?.apply {
                if (!contains(listener)) {
                    add(listener)
                }
            }
    }

    fun removeDisconnectedListener(deviceKe: DeviceKey, listener: (String) -> Unit) {
        disConnectedListenersMap.getValueWithInit(deviceKe, initValue = { mutableListOf() })
            ?.apply {
                remove(listener)
            }
    }

    fun notifyBrain(deviceKe: DeviceKey) {

    }


}