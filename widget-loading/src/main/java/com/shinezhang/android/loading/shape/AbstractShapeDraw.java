package com.shinezhang.android.loading.shape;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by ShineZhang on 2017/2/20.
 */

/* package */ abstract class AbstractShapeDraw {

    private float mShapeRadius = -1f;
    private Path mPath;

    /**
     * set the shape radius, the shape will draw in the circle with the assigned radius
     * @param radius the radius, if the value is negative, exception will throw
     */
    public final void setRadius(float radius) {
        if (radius <= 0) {
            throw new IllegalArgumentException("radius should be positive number");
        }

        if (mPath == null) {
            mPath = new Path();
        }

        if (radius != mShapeRadius) {
            mShapeRadius = radius;
            mPath.reset();
            initPath(mPath, mShapeRadius);
        }
    }

    /**
     * get the final draw path
     * @return the path ready to draw
     */
    protected final Path getPath() {
        if (mPath == null) {
            throw new RuntimeException("you should call setRadius() first");
        }

        return mPath;
    }

    /**
     * get the shape radius
     * @return the radius
     */
    protected final float getRadius() {
        return mShapeRadius;
    }

    /**
     * initial the path with assigned radius, create the shape in sub class
     * @param path the empty path is ready to create
     * @param radius the shape radius
     */
    protected abstract void initPath(Path path, float radius);

    /**
     * actual draw the path with the ratio, usually should be called at {@link android.view.View#onDraw(Canvas)}
     * @param canvas the canvas of the view
     * @param paint the draw paint
     * @param ratio the exact ratio, the range is in [0, 1]
     */
    public abstract void draw(Canvas canvas, Paint paint, float ratio);
}
