package com.shinezhang.android.loading;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.shinezhang.android.R;

/**
 * Created by ShineZhang on 2017/2/21.
 */
public class RotateLoadingView extends View {

    private static final String TAG                         = "RotateLoadingView";

    private static final int DEFAULT_SIZE_DP                = 20;
    private static final long DEFAULT_INVALID_DELAY_TIME    = 80L;
    private static final float DEFAULT_ROTATE_STEP          = 30f;

    private int mDefaultSizePixel;

    private Bitmap mRotateBitmap;
    private Matrix mMatrix;

    private long mInvalidDelayTime;
    private float mRotateStep;

    private boolean mIsBitmapIllegal;

    public RotateLoadingView(Context context) {
        super(context);
        init(context, null);
    }

    public RotateLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RotateLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mDefaultSizePixel = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_SIZE_DP,
                getContext().getResources().getDisplayMetrics());

        mMatrix = new Matrix();

        long invalidDelayTime;
        float rotateStep;
        int imgResId;
        if (attrs == null) {
            invalidDelayTime = DEFAULT_INVALID_DELAY_TIME;
            rotateStep = DEFAULT_ROTATE_STEP;
            imgResId = R.drawable.ic_rotate_loading;
        } else {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RotateLoadingView);
            try {
                invalidDelayTime = a.getInt(R.styleable.RotateLoadingView_rotate_loading_invalid_delay_time, (int) DEFAULT_INVALID_DELAY_TIME);
                rotateStep = a.getFloat(R.styleable.RotateLoadingView_rotate_step, DEFAULT_ROTATE_STEP);
                imgResId = a.getResourceId(R.styleable.RotateLoadingView_rotate_src, R.drawable.ic_rotate_loading);
            } finally {
                a.recycle();
            }
        }

        setInvalidateDelayTime(invalidDelayTime);
        setRotateStep(rotateStep);
        setImageResource(imgResId);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initMatrix();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int withSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

        if (withSpecMode != MeasureSpec.AT_MOST && heightSpecMode != MeasureSpec.AT_MOST) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int horizontalPadding = super.getPaddingLeft() + super.getPaddingRight();
        int verticalPadding = super.getPaddingTop() + super.getPaddingBottom();

        if (withSpecMode == MeasureSpec.AT_MOST) {

            if (heightSpecMode == MeasureSpec.AT_MOST) {
                //width is wrap_content, height is wrap_content

                int measuredWidth = horizontalPadding;
                int measuredHeight = verticalPadding;
                if (mRotateBitmap == null || mRotateBitmap.isRecycled()) {
                    measuredWidth += mDefaultSizePixel;
                    measuredHeight += mDefaultSizePixel;
                } else {
                    measuredWidth += mRotateBitmap.getWidth();
                    measuredHeight += mRotateBitmap.getHeight();
                }

                measuredWidth = Math.min(measuredWidth, widthSpecSize);
                measuredHeight = Math.min(measuredHeight, heightSpecSize);
                super.setMeasuredDimension(measuredWidth, measuredHeight);

            } else {
                //width is wrap_content, height is exact
                int availableHeight = heightSpecSize - verticalPadding;
                int measuredWidth = horizontalPadding;
                if (availableHeight > 0) {
                    if (mRotateBitmap == null || mRotateBitmap.isRecycled()) {
                        measuredWidth += availableHeight;
                    } else {
                        int desiredWidth = (int) ((mRotateBitmap.getWidth() * 1f * availableHeight) / mRotateBitmap.getHeight());
                        measuredWidth += desiredWidth;
                    }
                }

                measuredWidth = Math.min(measuredWidth, widthSpecSize);
                super.setMeasuredDimension(measuredWidth, heightSpecSize);
            }

        } else {

            if (heightSpecMode == MeasureSpec.AT_MOST) {
                //width is exact, height is wrap_content
                int measuredHeight = verticalPadding;
                int availableHeight = widthSpecSize - horizontalPadding;
                if (availableHeight > 0) {
                    if (mRotateBitmap == null || mRotateBitmap.isRecycled()) {
                        measuredHeight += availableHeight;
                    } else {
                        int desiredHeight = (int) ((mRotateBitmap.getHeight() * 1f * availableHeight) / mRotateBitmap.getWidth());
                        measuredHeight += desiredHeight;
                    }
                }

                measuredHeight = Math.min(measuredHeight, heightSpecSize);
                super.setMeasuredDimension(widthSpecSize, measuredHeight);
            } else {
                //width is exact, height is exact, but the code can run here
            }
        }
    }

    /**
     * initial the matrix after all the info is ready
     */
    private void initMatrix() {
        mIsBitmapIllegal = true;

        if (mRotateBitmap == null) {
            Log.w(TAG, "bitmap is null");
            return;
        }

        if (mRotateBitmap.isRecycled()) {
            Log.w(TAG, "bitmap is recycled");
            return;
        }

        int orgBitmapWidth = mRotateBitmap.getWidth();
        int orgBitmapHeight = mRotateBitmap.getHeight();
        int paddingLeft = super.getPaddingLeft();
        int paddingTop = super.getPaddingTop();
        int drawBitmapWidth = super.getWidth() - paddingLeft - super.getPaddingRight();
        int drawBitmapHeight = super.getHeight() - paddingTop - super.getPaddingBottom();

        mMatrix.reset();
        if (drawBitmapWidth <= 0 || drawBitmapHeight <= 0) {
            return;
        }

        mIsBitmapIllegal = false;

        float scale;
        if (orgBitmapWidth * drawBitmapHeight > orgBitmapHeight * drawBitmapWidth) {
            scale = drawBitmapWidth * 1f / orgBitmapWidth;
        } else {
            scale = drawBitmapHeight * 1f / orgBitmapHeight;
        }

        float finalDrawWidth = orgBitmapWidth * scale;
        float finalDrawHeight = orgBitmapHeight * scale;

        float translateX = paddingLeft + (drawBitmapWidth - finalDrawWidth) * 0.5f;
        float translateY = paddingTop + (drawBitmapHeight - finalDrawHeight) * 0.5f;

        mMatrix.postScale(scale, scale);
        mMatrix.postTranslate(translateX, translateY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mIsBitmapIllegal) {
            return;
        }

        int paddingLeft = super.getPaddingLeft();
        int paddingTop = super.getPaddingTop();
        int drawWidth = super.getWidth() - paddingLeft - super.getPaddingRight();
        int drawHeight = super.getHeight() - paddingTop - super.getPaddingBottom();

        float rotateCenterX = paddingLeft + drawWidth * 0.5f;
        float rotateCenterY = paddingTop + drawHeight * 0.5f;

        mMatrix.postRotate(mRotateStep, rotateCenterX, rotateCenterY);
        canvas.drawBitmap(mRotateBitmap, mMatrix, null);

        postInvalidateDelayed(mInvalidDelayTime);
    }

    /**
     * set the rotate bitmap, exception will throw when the bitmap is null or is recycled
     * @param bitmap the bitmap that will draw on this view
     */
    public final void setImageBitmap(Bitmap bitmap) {
        mRotateBitmap = bitmap;
        if (mRotateBitmap == null) {
            throw new IllegalArgumentException("bitmap can not be null");
        }

        if (mRotateBitmap.isRecycled()) {
            throw new RuntimeException("bitmap is recycled");
        }

        post(new Runnable() {

            @Override
            public void run() {
                requestLayout();
            }
        });
    }

    /**
     * set the rotate image by resource id
     * @param resId the image resource id
     */
    public final void setImageResource(int resId) {
        Drawable drawable = ContextCompat.getDrawable(getContext(), resId);
        if (drawable == null) {
            throw new RuntimeException("the resource with id " + resId + " is not found");
        }

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            setImageBitmap(bitmapDrawable.getBitmap());
        } else {
            int w = drawable.getIntrinsicWidth();
            int h = drawable.getIntrinsicHeight();
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, w, h);
            drawable.draw(canvas);

            setImageBitmap(bitmap);
        }
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

        mInvalidDelayTime = delayTimeMills;
        initMatrix();
        super.invalidate();
    }

    /**
     * set the rotate step during invalid
     * @param rotateStep the rotate step
     */
    public final void setRotateStep(float rotateStep) {
        mRotateStep = rotateStep;
        initMatrix();
        super.invalidate();
    }
}

