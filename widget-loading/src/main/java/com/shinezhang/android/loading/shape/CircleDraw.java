package com.shinezhang.android.loading.shape;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by ShineZhang on 2017/2/20.
 */

/* package */ class CircleDraw extends AbstractShapeDraw {

    @Override
    protected void initPath(Path path, float radius) {
        path.addCircle(radius, radius, radius * 0.8f, Path.Direction.CW);
    }

    @Override
    public void draw(Canvas canvas, Paint paint, float ratio) {
        canvas.drawPath(super.getPath(), paint);
    }
}
