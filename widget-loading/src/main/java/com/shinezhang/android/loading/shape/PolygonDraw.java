package com.shinezhang.android.loading.shape;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by ShineZhang on 2017/2/20.
 */

/* package */ class PolygonDraw extends AbstractShapeDraw {

    private final float mMaxRotateDegree;

    /**
     * this value should be larger than 2
     */
    private final int mSideCount;

    public PolygonDraw(int sideCount) {
        this(sideCount, 360f);
    }

    public PolygonDraw(int sideCount, float maxRotateDegree) {
        mMaxRotateDegree = maxRotateDegree;
        mSideCount = sideCount;
        if (mSideCount <= 2) {
            throw new IllegalArgumentException("side count should be larger than 2");
        }
    }

    @Override
    protected void initPath(Path path, float radius) {
        final float rotateDegreeByStep = 360f / mSideCount;
        float rotatedDegree;
        double radians;
        float x;
        float y;

        if ((mSideCount & 0x1) == 0x0) {
            rotatedDegree = rotateDegreeByStep * 0.5f;
            radians = Math.toRadians(rotatedDegree);
            x = (float) (radius + radius * Math.sin(radians));
            y = (float) (radius - radius * Math.cos(radians));
            path.moveTo(x, y);
        } else {
            rotatedDegree = 0f;
            path.moveTo(radius, 0);
        }

        for (int i = 1; i < mSideCount; i++) {
            rotatedDegree += rotateDegreeByStep;
            radians = Math.toRadians(rotatedDegree);
            x = (float) (radius + radius * Math.sin(radians));
            y = (float) (radius - radius * Math.cos(radians));
            path.lineTo(x, y);
        }

        path.close();
    }

    @Override
    public void draw(Canvas canvas, Paint paint, float ratio) {
        float radius = super.getRadius();
        canvas.rotate(ratio * mMaxRotateDegree, radius, radius);
        canvas.drawPath(super.getPath(), paint);
    }

}
