package com.shinezhang.android.loading.shape;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.shinezhang.android.R;

/**
 * Created by ShineZhang on 2017/2/20.
 */

public class ShapeLoadingView extends View {

    private static final int DEFAULT_DIMEN_DP_RADIUS                = 25;
    private static final int DEFAULT_DIMEN_DP_SHADOW_HEIGHT         = 2;
    private static final int DEFAULT_DIMEN_DP_SHADOW_TOP_SPACE      = 5;

    private static final int DEFAULT_TIMES_OF_RADIUS                = 8;

    private static final int DEFAULT_SHAPE_COLOR                    = 0xffff5400;
    private static final int DEFAULT_SHADOW_COLOR                   = 0xffa9a9a9;

    private static final long DEFAULT_INVALID_DELAY_TIME            = 30L;

    private static final float DEFAULT_MAX_ALLOW_ROTATE_DEGREE      = 360f;
    private static final int DEFAULT_MAX_ALLOW_POLYGON_SIDE_COUNT   = 5;

    private static final float DEFAULT_INTERPOLATOR_FACTOR          = 1.2f;

    private int mDefaultRadius;
    private int mDefaultShadowHeight;
    private int mDefaultShadowTopSpace;

    private int mShadowHeight;
    private int mShadowTopSpace;
    private final RectF mRectFShadow                                = new RectF();

    private Paint mPaintShape;
    private Paint mPaintShadow;

    private AccelerateInterpolator mAccelerateInterpolator;
    private DecelerateInterpolator mDecelerateInterpolator;

    private int mMaxAllowPolygonSideCount;
    private float mMaxAllowRotateDegree;

    private AbstractShapeDraw[] mArrayShapeDraw;
    private int mCurDrawIndex;
    private int mCurStep;
    private boolean mRunByOddIndex;
    private long mInvalidDelayTime;

    public ShapeLoadingView(Context context) {
        super(context);
        init(context, null);
    }

    public ShapeLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ShapeLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(21)
    public ShapeLoadingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        mDefaultRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_DIMEN_DP_RADIUS, displayMetrics);

        mDefaultShadowHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_DIMEN_DP_SHADOW_HEIGHT, displayMetrics);

        mDefaultShadowTopSpace = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_DIMEN_DP_SHADOW_TOP_SPACE, displayMetrics);

        mPaintShape = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintShadow = new Paint(Paint.ANTI_ALIAS_FLAG);

        float maxAllowRotateDegree;
        int maxAllowPolygonSideCount;
        float interpolatorFactor;
        int shapeColor;
        int shadowColor;
        int shadowHeight;
        int shadowTopSpace;
        int invalidDelayTime;

        if (attrs == null) {
            maxAllowRotateDegree = DEFAULT_MAX_ALLOW_ROTATE_DEGREE;
            maxAllowPolygonSideCount = DEFAULT_MAX_ALLOW_POLYGON_SIDE_COUNT;
            interpolatorFactor = DEFAULT_INTERPOLATOR_FACTOR;
            shapeColor = DEFAULT_SHAPE_COLOR;
            shadowColor = DEFAULT_SHADOW_COLOR;
            shadowHeight = mDefaultShadowHeight;
            shadowTopSpace = mDefaultShadowTopSpace;
            invalidDelayTime = (int) DEFAULT_INVALID_DELAY_TIME;
        } else {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ShapeLoadingView);
            try {
                maxAllowRotateDegree = a.getFloat(R.styleable.ShapeLoadingView_max_shape_rotate_degree, DEFAULT_MAX_ALLOW_ROTATE_DEGREE);

                maxAllowPolygonSideCount = a.getInt(R.styleable.ShapeLoadingView_max_allow_polygon_side_count, DEFAULT_MAX_ALLOW_POLYGON_SIDE_COUNT);
                if (maxAllowPolygonSideCount < 0) {
                    maxAllowPolygonSideCount = DEFAULT_MAX_ALLOW_POLYGON_SIDE_COUNT;
                }

                interpolatorFactor = a.getFloat(R.styleable.ShapeLoadingView_interpolator_factor, DEFAULT_INTERPOLATOR_FACTOR);
                if (interpolatorFactor <= 0f) {
                    interpolatorFactor = DEFAULT_INTERPOLATOR_FACTOR;
                }

                shapeColor = a.getColor(R.styleable.ShapeLoadingView_shape_color, DEFAULT_SHAPE_COLOR);
                shadowColor = a.getColor(R.styleable.ShapeLoadingView_shadow_color, DEFAULT_SHADOW_COLOR);
                shadowHeight = a.getDimensionPixelOffset(R.styleable.ShapeLoadingView_shadow_height, mDefaultShadowHeight);
                if (shadowHeight <= 0) {
                    shadowHeight = mDefaultShadowHeight;
                }

                shadowTopSpace = a.getDimensionPixelOffset(R.styleable.ShapeLoadingView_shadow_top_space, mDefaultShadowTopSpace);
                if (shadowTopSpace < 0) {
                    shadowTopSpace = mDefaultShadowTopSpace;
                }

                invalidDelayTime = a.getInt(R.styleable.ShapeLoadingView_shape_loading_invalid_delay_time, (int) DEFAULT_INVALID_DELAY_TIME);
                if (invalidDelayTime < 0) {
                    invalidDelayTime = (int) DEFAULT_INVALID_DELAY_TIME;
                }
            } finally {
                a.recycle();
            }

        }

        setShapeColor(shapeColor);
        setShadowColor(shadowColor);
        setShadowHeight(shadowHeight);
        setShadowTopSpace(shadowTopSpace);
        setInvalidDelayTime(invalidDelayTime);
        setInterpolatorFactor(interpolatorFactor);

        setPolygonSideCountAndRotateDegree(maxAllowPolygonSideCount, maxAllowRotateDegree);
    }

    /**
     * set the delay time after {@link View#onDraw(Canvas)} called
     * @param delayTimeMills the delay time, if the value is negative, the value will be ignored
     */
    public final void setInvalidDelayTime(long delayTimeMills) {
        if (delayTimeMills < 0L) {
            //not allow negative number
            return;
        }
        mInvalidDelayTime = delayTimeMills;
    }

    /**
     * set the shape color
     * @param color the color of shape
     */
    public void setShapeColor(int color) {
        mPaintShape.setColor(color);
    }

    /**
     * set the bottom shadow color
     * @param color the color of shadow
     */
    public final void setShadowColor(int color) {
        mPaintShadow.setColor(color);
    }

    /**
     * set the bottom shadow height(px value)
     * @param height the height of shadow
     */
    public final void setShadowHeight(int height) {
        if (height <= 0) {
            //should be a positive number
            return;
        }

        mShadowHeight = height;
    }

    /**
     * set the extra space between the bottom shadow and shape
     * @param space the height of the space
     */
    public final void setShadowTopSpace(int space) {
        if (space < 0) {
            //should be a positive number
            return;
        }

        mShadowTopSpace = space;
    }

    /**
     * init the shape array by polygon side count
     * @param maxAllowPolygonSideCount the value should be 0, 3, 4, 5, 6...
     *                                 if the value is negative, do nothing;
     *                                 if the value is 2, it will only exist a circle
     */
    public final void setMaxAllowPolygonSideCount(int maxAllowPolygonSideCount) {
        setPolygonSideCountAndRotateDegree(maxAllowPolygonSideCount, mMaxAllowRotateDegree);
    }

    /**
     * init the shape array by rotate degree
     * @param maxAllowRotateDegree the max allow rotate degree during throw up or fall down
     */
    public final void setMaxAllowRotateDegree(float maxAllowRotateDegree) {
        setPolygonSideCountAndRotateDegree(mMaxAllowPolygonSideCount, maxAllowRotateDegree);
    }

    /**
     * init the shape array by polygon side count and rotate degree
     * @param maxAllowPolygonSideCount the value should be 0, 3, 4, 5, 6...
     *                                 if the value is negative, do nothing;
     *                                 if the value is 2, it will only exist a circle
     *
     * @param maxAllowRotateDegree the max allow rotate degree during throw up or fall down
     */
    public final void setPolygonSideCountAndRotateDegree(int maxAllowPolygonSideCount, float maxAllowRotateDegree) {
        if (maxAllowPolygonSideCount < 0) {
            return;
        }

        mMaxAllowPolygonSideCount = maxAllowPolygonSideCount;
        mMaxAllowRotateDegree = maxAllowRotateDegree;

        if (maxAllowPolygonSideCount <= 2) {
            mArrayShapeDraw = new AbstractShapeDraw[1];
            mArrayShapeDraw[0] = new CircleDraw();
        } else {
            int size = (maxAllowPolygonSideCount - 2) * 2 + 1;
            mArrayShapeDraw = new AbstractShapeDraw[size];
            mArrayShapeDraw[0] = new CircleDraw();

            for (int i = 3; i <= maxAllowPolygonSideCount; i++) {
                if ((i & 0x1) == 0x1) {
                    mArrayShapeDraw[(i - 3) * 2 + 1] = new PolygonDraw(i, maxAllowRotateDegree);
                    mArrayShapeDraw[(i - 3) * 2 + 2] = new PolygonDraw(i, maxAllowRotateDegree * -1f);
                } else {
                    mArrayShapeDraw[(i - 3) * 2 + 1] = new PolygonDraw(i, maxAllowRotateDegree * -1f);
                    mArrayShapeDraw[(i - 3) * 2 + 2] = new PolygonDraw(i, maxAllowRotateDegree);
                }
            }
        }

        resetState();
    }

    /**
     * set the interpolator for the movement track
     * @param interpolatorFactor the interpolator value,
     *                           if the value is negative, it will be ignored
     *
     * @see AccelerateInterpolator#AccelerateInterpolator(float)
     * @see DecelerateInterpolator#DecelerateInterpolator(float)
     */
    public final void setInterpolatorFactor(float interpolatorFactor) {
        if (interpolatorFactor <= 0f) {
            return;
        }
        mAccelerateInterpolator = new AccelerateInterpolator(interpolatorFactor);
        mDecelerateInterpolator = new DecelerateInterpolator(interpolatorFactor);
        resetState();
    }

    private void resetState() {
        mCurDrawIndex = 0;
        mCurStep = 0;
        mRunByOddIndex = true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        resetState();
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

                int measuredWidth = horizontalPadding + mDefaultRadius * 2;
                measuredWidth = Math.min(measuredWidth, widthSpecSize);

                int measuredHeight = verticalPadding + mDefaultRadius * DEFAULT_TIMES_OF_RADIUS
                        + mShadowHeight + mShadowTopSpace;
                measuredHeight = Math.min(measuredHeight, heightSpecSize);

                super.setMeasuredDimension(measuredWidth, measuredHeight);

            } else {
                //width is wrap_content, height is exact
                int availableHeight = heightSpecSize - verticalPadding - mShadowHeight - mShadowTopSpace;
                int measuredWidth = horizontalPadding;
                if (availableHeight > 0) {
                    measuredWidth += (availableHeight / DEFAULT_TIMES_OF_RADIUS);
                }

                measuredWidth = Math.min(measuredWidth, widthSpecSize);
                super.setMeasuredDimension(measuredWidth, heightSpecSize);
            }

        } else {

            if (heightSpecMode == MeasureSpec.AT_MOST) {
                //width is exact, height is wrap_content
                int radius = (widthSpecSize - horizontalPadding) / 2;
                int measuredHeight = verticalPadding + mShadowHeight + mShadowTopSpace;
                if (radius > 0) {
                    measuredHeight += (radius * DEFAULT_TIMES_OF_RADIUS);
                }
                measuredHeight = Math.min(measuredHeight, heightSpecSize);

                super.setMeasuredDimension(widthSpecSize, measuredHeight);
            } else {
                //width is exact, height is exact, but the code can run here
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = super.getWidth();
        if (width <= 0) {
            return;
        }

        int height = super.getHeight();
        if (height <= 0) {
            return;
        }

        int paddingLeft = super.getPaddingLeft();
        int paddingRight = super.getPaddingRight();
        int paddingTop = super.getPaddingTop();
        int paddingBottom = super.getPaddingBottom();

        int horizontalPadding = paddingLeft + paddingRight;
        int verticalPadding = paddingTop + paddingBottom;

        int radius = (getWidth() - horizontalPadding) / 2;
        if (radius <= 0) {
            return;
        }

        float ratio;
        if (mCurStep <= 180) {
            //throw up
            ratio = (1 - mDecelerateInterpolator.getInterpolation(mCurStep / 180f));
        } else {
            //throw down
            ratio = mAccelerateInterpolator.getInterpolation(mCurStep / 180f - 1f);
        }

        float shadowLeft = paddingLeft + radius - (ratio + 0.5f) * radius * 0.5f;
        float shadowRight = paddingLeft + radius + (ratio + 0.5f) * radius * 0.5f;
        if (height < mShadowHeight) {
            mRectFShadow.set(shadowLeft, 0, shadowRight, height);
            canvas.drawRect(mRectFShadow, mPaintShadow);
            postInvalidateDelayed(mInvalidDelayTime);
            return;
        } else {
            mRectFShadow.set(shadowLeft, height - paddingBottom - mShadowHeight, shadowRight, height - paddingBottom);
            canvas.drawRect(mRectFShadow, mPaintShadow);
        }

        int availableDrawHeight = height - verticalPadding - mShadowHeight - mShadowTopSpace - radius * 2;
        if (availableDrawHeight <= 0) {
            postInvalidateDelayed(mInvalidDelayTime);
            return;
        }

        AbstractShapeDraw shapeDraw = mArrayShapeDraw[mCurDrawIndex];
        shapeDraw.setRadius(radius);

        final int saveCount = canvas.save();
        float translateDy = ratio * availableDrawHeight + paddingTop;
        canvas.translate(0, translateDy);
        shapeDraw.draw(canvas, mPaintShape, ratio);
        canvas.restoreToCount(saveCount);

        mCurStep += 9;
        if (mCurStep > 360) {
            mCurStep = 0;
            if (mCurDrawIndex == 0) {
                mCurDrawIndex = mRunByOddIndex ? (mCurDrawIndex + 1) : (mCurDrawIndex + 2);
            } else {
                mCurDrawIndex += 2;
            }

            if (mCurDrawIndex >= mArrayShapeDraw.length) {
                mRunByOddIndex = ((mCurDrawIndex & 0x1) == 0);
                mCurDrawIndex = 0;
            }
        }

        postInvalidateDelayed(mInvalidDelayTime);
    }
}
