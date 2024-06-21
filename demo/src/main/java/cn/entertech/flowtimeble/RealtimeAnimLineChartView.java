package cn.entertech.flowtimeble;

import android.animation.Animator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

import cn.entertech.ble.log.BleLogUtil;
import cn.entertech.flowtimeble.R;


public class RealtimeAnimLineChartView extends View {
    private Context mContext;
    private float mLineWidth;
    private float mRightPadding;
    private float mLeftPadding;
    private int mGridLineCount = 4;
    private int mGridLineColor = Color.parseColor("#383838");
    private int mAxisColor = Color.parseColor("#383838");
    private String mLineColor = "#ff6682";
    private int mBgColor = Color.parseColor("#f2f4fb");
    private Paint mCurvePaint;
    ArrayList<Double> mSourceData = new ArrayList<>();
    ArrayList<Double> realData = new ArrayList<>();
    List<Double> sampleData = new ArrayList<>();
    ArrayList<Double> screenData = new ArrayList<>();

    List<ArrayList<Double>> mSourceDataList = new ArrayList<>();
    List<ArrayList<Double>> mRealDataList = new ArrayList<>();
    List<ArrayList<Double>> mScreenDataList = new ArrayList<>();
    List<List<Integer>> mScreenSampleDataList = new ArrayList<>();
    List<Paint> mLinePaintList = new ArrayList<>();
    List<Path> mLinePathList = new ArrayList<>();
    public static int SCREEN_POINT_COUNT = 100;
    public int mBuffer = 2;
    private Paint mAxisPaint;
    private Paint mGridLinePaint;
    private Paint mBgPaint;
    private boolean isShowSampleData = false;
    private int mMaxValue = -1;
    private Paint mYAxisLabelPaint;
    private int mYAxisMargin;
    private int mRefreshTime = 600;
    private int mScreenPointCount = SCREEN_POINT_COUNT;
    private boolean mIsDrawXAxis = true;
    private int mWidth = 0;
    private Timer timer = new Timer();
    String[] lineColors;
    int lineCount;
    private Paint mValueLabelBgPaint;
    private Paint mTextPaint;
    private int rightOffset;
    private boolean isDrawValueText = false;
    private List<Integer> showLineIndexs;
    private boolean isDestroy;
    private Paint mPointBgPaint;
    private int mBgPointColor = Color.parseColor("#11152E");
    private int mTextRectBg = Color.parseColor("#ffffff");
    private OnDrawLastValueListener mOnDrawLastValueListener;
    private int mVerticalPadding = 1;
    private int linePointRadius = 0;

    public RealtimeAnimLineChartView(Context context) {
        this(context, null);
    }

    public RealtimeAnimLineChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RealtimeAnimLineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.RealtimeAnimLineChartView);
        if (typedArray != null) {
            mBgColor = typedArray.getColor(
                    R.styleable.RealtimeAnimLineChartView_ralcv_bgColor, mBgColor);
            mAxisColor = typedArray.getColor(
                    R.styleable.RealtimeAnimLineChartView_ralcv_yAxisColor, mAxisColor);
            mGridLineColor = typedArray.getColor(
                    R.styleable.RealtimeAnimLineChartView_ralcv_gridLineColor, mGridLineColor);
            mGridLineCount = typedArray.getInteger(
                    R.styleable.RealtimeAnimLineChartView_ralcv_gridLineCount, mGridLineCount);
            mLeftPadding = typedArray.getDimension(
                    R.styleable.RealtimeAnimLineChartView_ralcv_leftPadding, dip2px(context, 5));
            mRightPadding = typedArray.getDimension(
                    R.styleable.RealtimeAnimLineChartView_ralcv_rightPadding, dip2px(context, 5));
            mLineWidth = typedArray.getDimension(
                    R.styleable.RealtimeAnimLineChartView_ralcv_lineWidth, 3);
            mMaxValue = typedArray.getInteger(
                    R.styleable.RealtimeAnimLineChartView_ralcv_maxValue, mMaxValue);
            mBuffer = typedArray.getInteger(
                    R.styleable.RealtimeAnimLineChartView_ralcv_buffer, mBuffer);
            mRefreshTime = typedArray.getInteger(
                    R.styleable.RealtimeAnimLineChartView_ralcv_refreshTime, mRefreshTime);
            mScreenPointCount = typedArray.getInteger(
                    R.styleable.RealtimeAnimLineChartView_ralcv_screenPointCount, mScreenPointCount);
        }
        initPaint();
    }

    public void init() {
        initList();
        rightOffset = dip2px(mContext, 7f);
        linePointRadius = dip2px(mContext, 7f);
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    private void initList() {
        lineColors = mLineColor.split(",");
        lineCount = lineColors.length;
        mSourceDataList.clear();
        mRealDataList.clear();
        mScreenDataList.clear();
        mLinePaintList.clear();
        mLinePathList.clear();
        for (int i = 0; i < lineCount; i++) {
            ArrayList<Double> mSourceData = new ArrayList<>();
            ArrayList<Double> realData = new ArrayList<>();
            ArrayList<Double> screenData = new ArrayList<>();
            mSourceDataList.add(mSourceData);
            mRealDataList.add(realData);
            mScreenDataList.add(screenData);
            Paint paint = createLinePaint();
            paint.setColor(Color.parseColor(lineColors[i]));
            mLinePaintList.add(paint);
            Path path = new Path();
            mLinePathList.add(path);
        }
    }

    private Paint createLinePaint() {
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(mLineWidth);
        CornerPathEffect pathEffect = new CornerPathEffect(25);
        paint.setPathEffect(pathEffect);
        return paint;
    }

    private void initPaint() {
        setFocusable(true);
        setKeepScreenOn(true);
        setFocusableInTouchMode(true);
        mBgPaint = new Paint();
        mBgPaint.setColor(mBgColor);

        mAxisPaint = new Paint();
        mAxisPaint.setStyle(Paint.Style.STROKE);
        mAxisPaint.setColor(mAxisColor);
        mAxisPaint.setStrokeWidth(1f);

        mGridLinePaint = new Paint();
        mGridLinePaint.setStyle(Paint.Style.STROKE);
        mGridLinePaint.setColor(mGridLineColor);
        mGridLinePaint.setStrokeWidth(3);
//        initData();

        mYAxisLabelPaint = new Paint();
        mYAxisLabelPaint.setColor(Color.parseColor("#9AA1A9"));
        mYAxisLabelPaint.setTextSize(dip2px(mContext, 12));
        mYAxisLabelPaint.setTextAlign(Paint.Align.RIGHT);

        mValueLabelBgPaint = new Paint();
        mValueLabelBgPaint.setColor(mTextRectBg);
        mValueLabelBgPaint.setAlpha((int) (0.8 * 255));
        mValueLabelBgPaint.setStyle(Paint.Style.FILL);

        mTextPaint = new Paint();
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(dip2px(mContext, 12f));

        mPointBgPaint = new Paint();
        mPointBgPaint.setColor(mBgPointColor);
        mPointBgPaint.setStrokeWidth(dip2px(mContext, 2f));
        mPointBgPaint.setAntiAlias(true);
        mPointBgPaint.setStyle(Paint.Style.STROKE);
        mPointBgPaint.setStrokeCap(Paint.Cap.ROUND);
        mPointBgPaint.setAlpha((int) (0.2f * 255));
        mPointBgPaint.setPathEffect(new DashPathEffect(new float[]{0.02f,
                dip2px(mContext, 4f)}, 0));
    }

    public synchronized void setData(int lineIndex, List<Double> data) {
        if (data == null) {
            return;
        }
        for (int i = 0; i < data.size(); i++) {
            setData(lineIndex, data.get(i));
        }
    }


    public synchronized void setData(int lineIndex, double data) {
        List<Double> sourceData = mSourceDataList.get(lineIndex);
        sourceData.add(data);
        //  BleLogUtil.d("cpTest11","mBuffer size "+mBuffer);
        if (sourceData.size() > mBuffer) {
            for (int i = 0; i < sourceData.size() - mBuffer; i++) {
                sourceData.remove(0);
            }
        }
        boolean isDataNotInit = false;
        for (int i = 0; i < mSourceDataList.size(); i++) {
            if (mSourceDataList.get(i).size() == 0) {
                isDataNotInit = true;
                break;
            }
        }
        if (!isDataNotInit && !isTimerStart) {
            startTimer();
        }
    }

    private boolean isTimerStart;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 0:
                    handler.removeMessages(0);
                    if (!isDestroy) {
                        invalidate();
                        handler.sendEmptyMessageDelayed(0, mRefreshTime);
                    }
                    break;
                case 1:
                    break;
                default:
                    break;
            }
        }
    };

    private void startTimer() {
        isTimerStart = true;
        initRealData();
        handler.sendEmptyMessageDelayed(0, mRefreshTime);
    }

    private void initData() {
        for (int i = 0; i < mScreenPointCount + 2; i++) {
            realData.add(0.0);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = getWidth();
//        initSampleData();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //   onDrawBg(canvas);
        //  onDrawBgBitmap(canvas);
        if (isShowSampleData) {
            onDrawSampleData(canvas);
        } else {
            if (isTimerStart) {
                onDrawHrv(canvas);
                onDrawLeftRectCover(canvas);
                onDrawRightRectCover(canvas);
                onDrawLastPoint(canvas);
            }
        }
        if (canPlayAnim() && !isShowSampleData) {
            startAnim();
        }
    }

    public boolean canPlayAnim() {
        boolean isSourceDataAvailable = false;
        for (int i = 0; i < mSourceDataList.size(); i++) {
            if (mSourceDataList.get(i).size() != 0 && Collections.max(mSourceDataList.get(i)) != 0) {
                isSourceDataAvailable = true;
                break;
            }
        }
        return !isAnim && mScreenDataList.size() != 0 &&
                mScreenDataList.get(0).size() > mScreenPointCount + 1;
    }

    private float axisOffset = 0f;

    private boolean isAnim = false;




    public void onDrawRectCover(Canvas canvas, RectF rectF) {
        canvas.drawRect(rectF, mBgPaint);
    }

    public void onDrawLeftRectCover(Canvas canvas) {
        RectF rectF = new RectF(0, 0, mLeftPadding + mYAxisMargin, getHeight());
        onDrawRectCover(canvas, rectF);
    }

    public void onDrawRightRectCover(Canvas canvas) {
        RectF rectF = new RectF(getWidth() - rightOffset, 0, getWidth(), getHeight());
        onDrawRectCover(canvas, rectF);
    }

    Path path = new Path();

    public void onDrawHrv(Canvas canvas) {
        canvas.save();
        realDataMaxValue = Math.max(mMaxValue, realDataMaxValue);


        mScreenDataList.clear();
        for (int i = 0; i < mSourceDataList.size(); i++) {
            mScreenDataList.add(dealData(mSourceDataList.get(i), mRealDataList.get(i)));
        }

        float pointOffset = (getWidth() - rightOffset) * 1f / mScreenPointCount;
        canvas.translate(mLeftPadding + mYAxisMargin - axisOffset, getHeight() - linePointRadius);
        for (int i = 0; i < mScreenDataList.size(); i++) {
            if (mOnDrawLastValueListener != null
                    && mScreenDataList.get(i).size() < mScreenPointCount) {
                mOnDrawLastValueListener.onLastValueDraw(i,
                        (mRealDataList.get(i).get(mRealDataList.get(i).size() - 1)).intValue());
            }
            if (showLineIndexs != null && !showLineIndexs.contains(i)) {
                continue;
            }
            mLinePathList.get(i).reset();
//          BleLogUtil.d("####", "draw data is " + drawData.toString());
            for (int j = 0; j < mScreenDataList.get(i).size(); j++) {
                if (j == 0)
                    mLinePathList.get(i)
                            .moveTo(j * pointOffset, (float) (-(mScreenDataList.get(i).get(j))));
                mLinePathList.get(i)
                        .lineTo(j * pointOffset, (float) (-(mScreenDataList.get(i).get(j))));
            }
            canvas.drawPath(mLinePathList.get(i), mLinePaintList.get(i));
        }
        canvas.restore();
    }

    public void onDrawLastPoint(Canvas canvas) {
        canvas.save();
        canvas.translate(0, getHeight() - linePointRadius);
        for (int i = 0; i < mScreenDataList.size(); i++) {
            if (showLineIndexs != null && !showLineIndexs.contains(i)) {
                continue;
            }
            float lastPointX = getWidth() - rightOffset;
            if (realtimeLastPointYMap.isEmpty()) {
                return;
            }
            float lastPointY = realtimeLastPointYMap.get(i);
            canvas.drawCircle(lastPointX, lastPointY, linePointRadius, mValueLabelBgPaint);
            mLinePaintList.get(i).setStyle(Paint.Style.FILL);
            canvas.drawCircle(lastPointX, lastPointY,
                    dip2px(mContext, 3f), mLinePaintList.get(i));
            mLinePaintList.get(i).setStyle(Paint.Style.STROKE);
            if (isDrawValueText) {
                int rectWidth = dip2px(mContext, 40f);
                int rectHeight = dip2px(mContext, 18f);
                int rectTop = (int) (lastPointY - rectHeight / 2f);
                int left = (int) lastPointX - dip2px(mContext, 7f)
                        - dip2px(mContext, 3f) - rectWidth;
                RectF valueTextRect =
                        new RectF(left, rectTop, left + rectWidth, rectTop + rectHeight);
                float rectRadius = dip2px(mContext, 4f);
                canvas.drawRoundRect(valueTextRect, rectRadius, rectRadius, mValueLabelBgPaint);
                drawText(canvas, valueTextRect,
                        ((mRealDataList.get(i)
                                .get(mRealDataList.get(i).size() - 1)).intValue()) + "", i);
                if (mOnDrawLastValueListener != null) {
                    mOnDrawLastValueListener.onLastValueDraw(i,
                            (mRealDataList.get(i).get(mRealDataList.get(i).size() - 1)).intValue());
                }
            }
        }
        canvas.restore();
    }

    public void drawText(Canvas canvas, RectF textBg, String text, int lineIndex) {
        mTextPaint.setColor(Color.parseColor(lineColors[lineIndex]));
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float top = fontMetrics.top;
        float bottom = fontMetrics.bottom;
        int baseLineY = (int) (textBg.centerY() - top / 2 - bottom / 2);
        canvas.drawText(text, textBg.centerX(), baseLineY, mTextPaint);
    }

    public void onDrawSampleData(Canvas canvas) {
        canvas.save();
        float pointOffset = (getWidth() - rightOffset) * 1f / mScreenPointCount;
        canvas.translate(mLeftPadding + mYAxisMargin - axisOffset, getHeight());
        float time = 1;
        if (mMaxValue > 0) {
            time = getHeight() * 1f / mMaxValue;
        }
        for (int i = 0; i < mScreenSampleDataList.size(); i++) {
            if (showLineIndexs != null && !showLineIndexs.contains(i)) {
                continue;
            }
            mLinePathList.get(i).reset();
//        BleLogUtil.d("####", "draw data is " + drawData.toString());
            for (int j = 0; j < mScreenSampleDataList.get(i).size(); j++) {
                if (j == 0)
                    mLinePathList.get(i).moveTo(j * pointOffset,
                            (float) (-(mScreenSampleDataList.get(i).get(j)) * time));
                mLinePathList.get(i).lineTo(j * pointOffset,
                        (float) (-(mScreenSampleDataList.get(i).get(j)) * time));
            }
            canvas.drawPath(mLinePathList.get(i), mLinePaintList.get(i));
        }
        canvas.restore();
    }

    int realDataMaxValue = 100;
    int realDataMinValue = 0;

    public ArrayList<Double> dealData(List<Double> mSourceData, List<Double> realData) {
        if (mSourceData.size() == 0) {
//           realData.add(0.0);
//           realData.remove(0);
        } else {
            if (mSourceData.get(0) == 0) {
                if (realData.size() == 0) {
                    realData.add(mSourceData.get(0));
                    mSourceData.remove(0);
                } else {
                    realData.add(realData.get(realData.size() - 1));
                }
            } else {
                realData.add((mSourceData.get(0)));
                mSourceData.remove(0);
            }

            if (realData.size() > mScreenPointCount + 2) {
                realData.remove(0);
            }
        }
        ArrayList<Double> screenData = new ArrayList<>();
        if (realData.isEmpty()) {
            return screenData;
        }
        float times = (getHeight() - linePointRadius) / ((realDataMaxValue - realDataMinValue) * 1.0f);
        if (times != 0) {
            for (int i = 0; i < realData.size(); i++) {
                if (realData.get(i) != 0) {
                    screenData.add((realData.get(i) - realDataMinValue) * times);
                } else {
                    if (i - 1 >= 0) {
                        screenData.add((realData.get(i - 1) - realDataMinValue) * times);
                    } else {
                        screenData.add(0.0);
                    }
                }
            }
        } else {
            for (int i = 0; i < realData.size(); i++) {
                screenData.add(0.0);
            }
        }
        return screenData;
    }

    void initRealData() {
        BleLogUtil.INSTANCE.d("cpTest11", "mScreenPointCount " + mScreenPointCount);
        for (int i = 0; i < mRealDataList.size(); i++) {
            for (int j = 0; j < mScreenPointCount; j++) {
                mRealDataList.get(i).add(0.0);
            }
        }
    }

    private float lastPointY = 0f;

    private HashMap<Integer, Float> realtimeLastPointYMap = new HashMap<>();
    private List<Animator> animators = new ArrayList<>();

    public void startAnim() {
        int lastPointIndex = 0;
        if (mScreenDataList.get(0).size() >= mScreenPointCount) {
            for (int i = 0; i < mScreenDataList.size(); i++) {
                lastPointIndex = mScreenDataList.get(i).size() - 2;
                realtimeLastPointYMap.put(i, -mScreenDataList.get(i).get(lastPointIndex + 1).floatValue());
            }
        }
    }

    public void showSampleData(List<List<Integer>> sampleData) {
        this.mScreenSampleDataList = sampleData;
        this.isShowSampleData = true;
        invalidate();
    }


    public void setLineColor(String color) {
        this.mLineColor = color;
        invalidate();
    }

    public void setLineWidth(float lineWidth) {
        this.mLineWidth = lineWidth;
        if (mLinePaintList != null) {
            for (int i = 0; i < mLinePaintList.size(); i++) {
                mLinePaintList.get(i).setStrokeWidth(mLineWidth);
            }
        }
        invalidate();
    }

    public void setGridLineColor(int gridLineColor) {
        this.mGridLineColor = gridLineColor;
        mGridLinePaint.setColor(mGridLineColor);
        invalidate();
    }

    public void setAxisColor(int color) {
        this.mAxisColor = color;
        mAxisPaint.setColor(mAxisColor);
        invalidate();
    }

    public void setMaxValue(int maxValue) {
        this.mMaxValue = maxValue;
        invalidate();
    }

    public void setRefreshTime(int refreshTime) {
        this.mRefreshTime = refreshTime;
        invalidate();
    }

    @Override
    public void setBackgroundColor(int color) {
        this.mBgColor = color;
        mBgPaint.setColor(mBgColor);
        invalidate();
    }

    public void isDrawXAxis(boolean flag) {
        this.mIsDrawXAxis = flag;
        invalidate();
    }

    public void setBuffer(int buffer) {
        this.mBuffer = buffer;
    }

    public void setScreenPointCount(int pointCount) {
        this.mScreenPointCount = pointCount;
        invalidate();
    }


    public void hideSampleData() {
        this.isShowSampleData = false;
    }

    public float getLastPointY() {
        return lastPointY;
    }

    public void setLastPointY(float lastPointY) {
        this.lastPointY = lastPointY;
    }

    public void setDrawValueText(boolean enable) {
        this.isDrawValueText = enable;
    }

    public boolean isDrawValueText() {
        return isDrawValueText;
    }

    public void setLineShowIndexs(List<Integer> indexs) {
        this.showLineIndexs = indexs;
    }

    public interface OnDrawLastValueListener {
        void onLastValueDraw(int index, int value);
    }

    public void setOnDrawLastValueListener(OnDrawLastValueListener lastValueListener) {
        this.mOnDrawLastValueListener = lastValueListener;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isDestroy = false;
        handler.removeCallbacksAndMessages(null);
    }

    public void setBgPointColor(int color) {
        this.mBgPointColor = color;
        mPointBgPaint.setColor(mBgPointColor);
        mPointBgPaint.setAlpha((int) (0.2f * 255));
        invalidate();
    }

    public void setTextRectBgColor(int color) {
        this.mTextRectBg = color;
        mValueLabelBgPaint.setColor(mTextRectBg);
        mValueLabelBgPaint.setAlpha((int) (0.8 * 255));
        invalidate();
    }

    public void setVerticalPadding(int padding) {
        this.mVerticalPadding = padding;
        invalidate();
    }
}
