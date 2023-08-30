package cn.entertech.flowtimeble.skin

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.entertech.ble.single.BiomoduleBleManager
import cn.entertech.ble.utils.BleLogUtil
import cn.entertech.flowtimeble.R
import cn.entertech.flowtimeble.RealtimeAnimLineChartView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.util.LinkedList
import kotlin.random.Random


class SkinConductivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SkinConductivity"
    }

    private val bioModuleBleManager by lazy {
        BiomoduleBleManager.getInstance(this)
    }

    private val dataCache by lazy {
        LinkedList<Byte>()
    }
    private var maxValue=0
    private var realtimeChart: RealtimeAnimLineChartView? = null
    private var tvSkinDeviceMac: TextView? = null
    private var tvSkinRealtimeData: TextView? = null
    val entries = ArrayList<Entry>()
    var dataSet = LineDataSet(entries, "Random Data")


    private var lineChart: LineChart? = null
    private var entriesQueue: LinkedList<Entry>? = null
    private var random: Random? = null

    private val MAX_DATA_POINTS = 10 // Set the maximum number of data points


    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            BleLogUtil.d(TAG,"entriesQueue size: ${entriesQueue?.size}")
            if (entriesQueue!!.size >= MAX_DATA_POINTS) {
                entriesQueue?.pollFirst() // Remove the oldest entry
            }
            val randomValue = random!!.nextFloat() * 100 // Generate random value
            entriesQueue?.addLast(Entry(entriesQueue?.size?.toFloat()?:0f, randomValue)) // Add to queue
            updateChartData()
            this.sendEmptyMessageDelayed(0, 1000) // Send message to update every 1 second
        }
    }

    private val skinConductivityServiceListener: (ByteArray) -> Unit by lazy {
        { byte: ByteArray ->
            BleLogUtil.d(TAG, byte.contentToString())
            var sb: StringBuilder
            if (dataCache.isEmpty()) {
                sb = StringBuilder()
                if (byte.size == 3) {
                    byte.forEach {
                        sb.append(String.format("%02X", it))
                    }
                    setData(sb)
                } else {
                    dataCache.addAll(byte.asIterable())
                }
            } else {
                dataCache.addAll(byte.asIterable())
                sb = StringBuilder()
                //个数大于3
                while (dataCache.size / 3 > 0) {
                    for (i in 0..2) {
                        sb.append(String.format("%02X", dataCache.pollFirst()))
                    }
                    setData(sb)
                }
            }
        }
    }

    private fun setData(sb: StringBuilder) {
        BleLogUtil.d(TAG, "sb： $sb")
        try {
            val dataInt = Integer.parseInt(sb.toString(), 16)
            BleLogUtil.d(TAG, "电阻为： $dataInt")
            SkinConductivityHelper.addSkinConductivityData(dataInt,this)
            tvSkinRealtimeData?.text="实时数据： $sb    $dataInt"
            if (dataInt > maxValue) {
                maxValue = dataInt
                realtimeChart?.setMaxValue((dataInt*1.1f).toInt())
            }
            realtimeChart?.setData(0, dataInt.toDouble())

        } catch (e: Exception) {
            BleLogUtil.d(TAG, "sb： $sb")
            tvSkinRealtimeData?.text="格式错误： $sb"
        } finally {
            sb.delete(0, sb.length)
        }

    }

    fun generateRandomHexadecimal(length: Int): String {
        val hexChars = "0123456789ABCDEF"
        val random = Random.Default
        val sb = StringBuilder()

        repeat(length) {
            val randomIndex = random.nextInt(hexChars.length)
            sb.append(hexChars[randomIndex])
        }

        return sb.toString()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.skin_conductivity_activity)
        if (!bioModuleBleManager.isConnected()) {
            Toast.makeText(applicationContext, "未连接设备", Toast.LENGTH_SHORT).show()
            finish()
        }
        initView()
        initData()
    }

    private fun initView() {
        tvSkinRealtimeData = findViewById(R.id.tvSkinRealtimeData)
        tvSkinDeviceMac = findViewById(R.id.tvSkinDeviceMac)
        realtimeChart = findViewById(R.id.realtime_chart)
        realtimeChart?.setRefreshTime(200)
        realtimeChart?.setScreenPointCount(100)
        realtimeChart?.isDrawValueText = true
        realtimeChart?.init()
        lineChart = findViewById(R.id.chart2)

        entriesQueue = LinkedList()
        dataSet = LineDataSet(ArrayList(entriesQueue), "Real-time Data")
        dataSet.setDrawCircles(false)
        dataSet.color = resources.getColor(R.color.colorPrimary)

        val lineDataSet: ILineDataSet = dataSet
        lineChart?.data = LineData(lineDataSet)
        lineChart?.setScaleEnabled(true);
        lineChart?.setDragEnabled(true);
        random = Random.Default

        val xAxis = lineChart?.xAxis
        xAxis?.axisMinimum = 0f
        xAxis?.axisMaximum = (MAX_DATA_POINTS - 1).toFloat()
        xAxis?.granularity = 1f

//        handler.sendEmptyMessage(0) // Start generating real-time data

    }
    private fun updateChartData() {
        val entries = ArrayList(entriesQueue)
        val newDataSet = LineDataSet(entries, "Real-time Data")
        newDataSet.setDrawCircles(false)
        newDataSet.color = resources.getColor(R.color.colorPrimary)
        lineChart?.data = LineData(newDataSet)
        lineChart?.notifyDataSetChanged()
        lineChart?.invalidate()
    }


    private fun initData(){
        val mac=bioModuleBleManager.getDevice()?.macAddress
        tvSkinDeviceMac?.text="mac:   $mac"
       /* thread {
            try {
                for(i in 0..300){
                    Thread.sleep(500)
                    runOnUiThread {
                        setData(java.lang.StringBuilder(generateRandomHexadecimal(3)))
                    }
                }
            }catch (e:Exception){

            }
        }*/


    }

    override fun onResume() {
        super.onResume()
        bioModuleBleManager.addSkinConductivityServiceListener(skinConductivityServiceListener)
    }

    override fun onPause() {
        super.onPause()
        bioModuleBleManager.removeSkinConductivityServiceListener(skinConductivityServiceListener)
    }
}