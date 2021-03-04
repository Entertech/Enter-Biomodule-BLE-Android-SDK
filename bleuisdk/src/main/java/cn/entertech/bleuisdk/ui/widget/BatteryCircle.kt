package cn.entertech.bleuisdk.ui.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import cn.entertech.bleuisdk.R
import cn.entertech.bleuisdk.utils.getBatteryResId

/**
 * Created by EnterTech on 2017/11/20.
 */
class BatteryCircle @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): RelativeLayout(context, attrs, defStyleAttr) {

    private val mPaint = Paint()
    private val mCircleColor = Color.WHITE
    private val mDefColor = 0x66e1e1e1.toInt()
    private var mCircleWidth: Int
    private var mScore: TextView
    private var mBattery: ImageView
    private var mDescription: TextView
    private var mValue: Int = 0
    private var mValueTarget: Int = 0
    private var mRectF: RectF
    private var mDrawRunnable: DrawRunnable

    init {
        setBackgroundColor(0x00000000)
        mCircleWidth = context.resources.getDimensionPixelSize(R.dimen.dp_4)

        val rl = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        rl.addRule(RelativeLayout.CENTER_IN_PARENT)
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.layoutParams = rl
        linearLayout.setPadding(0, 0, 0, mCircleWidth * 2)
        addView(linearLayout)

        var ll = LinearLayout.LayoutParams(mCircleWidth * 6, mCircleWidth * 6)
        ll.gravity = Gravity.CENTER

        mBattery = ImageView(context)
        mBattery.layoutParams = ll

        ll = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        ll.gravity = Gravity.CENTER
        mScore = TextView(context)
        mScore.layoutParams = ll
        mScore.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimensionPixelSize(R.dimen.text_size_start).toFloat())
        mScore.setTextColor(Color.WHITE)

        mDescription = TextView(context)
        mDescription.layoutParams = ll
        mDescription.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimensionPixelSize(R.dimen.text_size_min).toFloat())
        mDescription.setTextColor(Color.WHITE)

        linearLayout.addView(mBattery)
        linearLayout.addView(mScore)
        linearLayout.addView(mDescription)

        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.STROKE
        mPaint.color = mCircleColor
        mPaint.strokeWidth = mCircleWidth.toFloat()
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.strokeJoin = Paint.Join.ROUND
        mRectF = RectF()

        mDrawRunnable = DrawRunnable()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val offset = mCircleWidth / 2
        mRectF.set(offset.toFloat(), offset.toFloat(), (getWidth() - offset).toFloat(), (getHeight() - offset).toFloat())
        mPaint.color = mDefColor
        canvas.drawArc(mRectF, 0f, 360f, false, mPaint)
        drawScale(canvas)

        mPaint.color = mCircleColor
        canvas.drawArc(mRectF, -90f, (mValue * 360 / 100).toFloat(), false, mPaint)
    }

    private fun drawScale(canvas: Canvas) {
        val saveCount = canvas.save()
        mPaint.strokeWidth = mCircleWidth / 2.toFloat()

        val width = width
        val height = height

        val path = Path()
        path.moveTo((width / 2).toFloat(), mCircleWidth.toFloat() * 2f)
        path.lineTo((width / 2).toFloat(), mCircleWidth.toFloat() * 2.5f)
        path.close()
        val angle = 6
        for (i in 0..360 / angle - 1) {
            canvas.rotate(angle.toFloat(), (width / 2).toFloat(), (height / 2).toFloat())
            canvas.drawPath(path, mPaint)
        }
        mPaint.strokeWidth = mCircleWidth.toFloat()
        canvas.restoreToCount(saveCount)
    }

    fun setPercent(percent: Int) {
        mBattery.setImageResource(getBatteryResId(percent, DeviceConnectView.IconType.WHITE))
        mScore.text = (percent.toString() + "%")
        mValue = 0
        mValueTarget = percent
        mDrawRunnable.startAnimotion()
    }
    fun setDescription(str: String) {
        mDescription.text = str
    }

    internal inner class DrawRunnable : Runnable {

        private var isFinished = true
            set

        fun abortAnimation() {
            this.isFinished = true
        }

        override fun run() {
            while (!isFinished) {
                try {
                    Thread.sleep(10)
                    if (mValue == mValueTarget) {
                        abortAnimation()
                    }
                    mValue++
                    postInvalidate()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
        }

        fun startAnimotion() {
            if (!isFinished) {
                return
            }
            this.isFinished = false
            Thread(this).start()
        }
    }
}