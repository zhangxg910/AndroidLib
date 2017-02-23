package com.shinezhang.android.loading;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.shinezhang.android.R;

/**
 * Created by ShineZhang on 2017/2/20.
 */

public class DoubleCircleLoadingView extends View {

    private static final long DEFAULT_INVALIDATE_DELAY_TIME     = 50L;

    private static final int DEFAULT_MAIN_CIRCLE_COLOR          = 0xffff5400;
    private static final int DEFAULT_ASSIST_CIRCLE_COLOR        = 0xffa9a9a9;

    private static final int DEFAULT_DIMEN_DP_WIDTH             = 50;

    /**
     * make sure the value is in (0, 180),
     * <br/>if the value is 0 or smaller, exception will throw;
     * <br/>if the value is 180 or larger, you can not see the dynamic effect
     */
    private static final int DEGREE_STEP                        = 15;

    private static final float[] ARRAY_COS_VALUES;
    private static final float[] ARRAY_SIN_VALUES;
    private static final int ARRAY_VALUES_LENGTH;

    private int mDimenDefaultWidth;

    private Paint mPaintMainCircle;
    private Paint mPaintAssistCircle;

    private int mCurValueIndex = 0;
    private long mInvalidateDelayTime;

    public DoubleCircleLoadingView(Context context) {
        super(context);
        init(context, null);
    }

    public DoubleCircleLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DoubleCircleLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(21)
    public DoubleCircleLoadingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        mDimenDefaultWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_DIMEN_DP_WIDTH, displayMetrics);

        mPaintMainCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintMainCircle.setStyle(Paint.Style.FILL);

        mPaintAssistCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintAssistCircle.setStyle(Paint.Style.FILL);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DoubleCircleLoadingView);

            try {
                int mainCircleColor = a.getColor(R.styleable.DoubleCircleLoadingView_main_circle_color, DEFAULT_MAIN_CIRCLE_COLOR);
                int assistCircleColor = a.getColor(R.styleable.DoubleCircleLoadingView_assist_circle_color, DEFAULT_ASSIST_CIRCLE_COLOR);
                int invalidateDelayTime = a.getInt(R.styleable.DoubleCircleLoadingView_double_circle_loading_invalid_delay_time, (int) DEFAULT_INVALIDATE_DELAY_TIME);

                setMainCircleColor(mainCircleColor);
                setAssistCircleColor(assistCircleColor);
                setInvalidateDelayTime(invalidateDelayTime);
            } finally {
                a.recycle();
            }
        } else {
            setMainCircleColor(DEFAULT_MAIN_CIRCLE_COLOR);
            setAssistCircleColor(DEFAULT_ASSIST_CIRCLE_COLOR);
            setInvalidateDelayTime(DEFAULT_INVALIDATE_DELAY_TIME);
        }
    }

    /**
     * set the main circle(left circle) color
     * @param color the color of the circle
     */
    public final void setMainCircleColor(int color) {
        mPaintMainCircle.setColor(color);
    }

    /**
     * get the main circle(left circle) color
     * @return the color of the circle
     */
    public final int getMainCircleColor() {
        return mPaintMainCircle.getColor();
    }

    /**
     * set the main circle(right circle) color
     * @param color the color of the circle
     */
    public final void setAssistCircleColor(int color) {
        mPaintAssistCircle.setColor(color);
    }

    /**
     * get the main circle(right circle) color
     * @return the color of the circle
     */
    public final int getAssistCircleColor() {
        return mPaintAssistCircle.getColor();
    }

    /**
     * set the view invalidate time delay
     * @param delayTimeMills the unit is milliseconds,
     *                       if negative number is set, do nothing.
     *                       the view will invalidate faster if the value is small
     */
    public final void setInvalidateDelayTime(long delayTimeMills) {
        if (delayTimeMills < 0) {
            return;
        }

        mInvalidateDelayTime = delayTimeMills;
    }

    /**
     * get the view invalidate time delay
     * @return the view invalidate time delay
     */
    public final long getInvalidateDelayTime() {
        return mInvalidateDelayTime;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mCurValueIndex = 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthSpecMode != MeasureSpec.AT_MOST && heightSpecMode != MeasureSpec.AT_MOST) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.AT_MOST) {

            if (heightSpecMode == MeasureSpec.AT_MOST) {
                //width and height are both warp_content
                int horizontalPadding = super.getPaddingLeft() + super.getPaddingRight();
                int minWithSize = Math.min(widthSpecSize, mDimenDefaultWidth);
                float radius = (minWithSize - horizontalPadding) * 0.2f;
                int height = super.getPaddingTop() + super.getPaddingBottom();
                if (radius > 0f) {
                    height = (int) (height + radius * 2);
                }
                super.setMeasuredDimension(minWithSize, height);
            } else {
                //width is wrap_content, height is exact
                int verticalPadding = super.getPaddingTop() + super.getPaddingBottom();
                float radius = (heightSpecSize - verticalPadding) * 0.5f;
                int width = super.getPaddingLeft() + super.getPaddingRight();
                if (radius > 0f) {
                    width = (int) (width + radius * 5);
                }
                super.setMeasuredDimension(Math.min(width, widthSpecSize), heightSpecSize);
            }
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            //width is exact, height is wrap_content
            int horizontalPadding = super.getPaddingLeft() + super.getPaddingRight();
            float radius = (widthSpecSize - horizontalPadding) * 0.2f;
            int height = super.getPaddingTop() + super.getPaddingBottom();
            if (radius > 0f) {
                height = (int) (height + radius * 2);
            }
            super.setMeasuredDimension(widthSpecSize, height);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int paddingLeft = super.getPaddingLeft();
        int availableDrawWidth = super.getWidth() - paddingLeft - super.getPaddingRight();
        if (availableDrawWidth <= 0) {
            return;
        }

        int paddingTop = super.getPaddingTop();
        int availableDrawHeight = super.getHeight() - paddingTop - super.getPaddingBottom();
        if (availableDrawHeight <= 0) {
            return;
        }

        float halfHeight = availableDrawHeight * 0.5f;
        float avgX = availableDrawWidth * 0.2f;
        float mainCircleCenterX = avgX;
        float assistCircleCenterX = avgX * 4f;
        float maxRadius = Math.min(avgX, halfHeight);

        if (mCurValueIndex >= ARRAY_VALUES_LENGTH || mCurValueIndex < 0) {
            mCurValueIndex = 0;
        }

        //the circle radius is 0.75~1 of max radius
        float ratioMainCircleRadius = (0.75f + ARRAY_COS_VALUES[mCurValueIndex] * 0.25f);
        float ratioAssistCircleRadius = (0.75f + ARRAY_SIN_VALUES[mCurValueIndex] * 0.25f);

        canvas.drawCircle(mainCircleCenterX + paddingLeft, halfHeight + paddingTop, maxRadius * ratioMainCircleRadius, mPaintMainCircle);
        canvas.drawCircle(assistCircleCenterX + paddingLeft, halfHeight + paddingTop, maxRadius * ratioAssistCircleRadius, mPaintAssistCircle);

        mCurValueIndex++;

        postInvalidateDelayed(mInvalidateDelayTime);
    }

    static {
        //cache the sin and cos values to avoid unnecessary calculate at onDraw()
        if (DEGREE_STEP <= 0) {
            throw new RuntimeException("degree step should be a positive number");
        }

        ARRAY_VALUES_LENGTH = (int) Math.ceil(180d / DEGREE_STEP);
        ARRAY_COS_VALUES = new float[ARRAY_VALUES_LENGTH];
        ARRAY_SIN_VALUES = new float[ARRAY_VALUES_LENGTH];

        double radians;
        for (int i = 0; i < ARRAY_VALUES_LENGTH; i++) {
            radians = Math.toRadians(DEGREE_STEP * i);
            //make sure all the values are positive number
            ARRAY_COS_VALUES[i] = (float) Math.abs(Math.cos(radians));
            ARRAY_SIN_VALUES[i] = (float) Math.abs(Math.sin(radians));
        }
    }
}

