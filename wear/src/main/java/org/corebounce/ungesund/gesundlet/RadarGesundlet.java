package org.corebounce.ungesund.gesundlet;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;

import org.corebounce.ungesund.UngesundWatchFace;

public class RadarGesundlet implements IGesundlet {
    private static final int COLOR_RADAR = Color.argb(255, 255, 0, 0);
    private static final int COLOR_DECOR = Color.argb(255, 200, 0, 0);
    private static final int COLOR_BLUR = Color.argb(255, 127, 0, 0);

    private static final float SPEED = 360.0f; // degrees/s

    private Matrix rotation;
    private SweepGradient gradient;
    private Paint paint;

    public RadarGesundlet(UngesundWatchFace.Context context) {

        rotation = new Matrix();

        RectF bounds = context.getBounds();
        gradient = new SweepGradient(bounds.centerX(), bounds.centerY(),
                new int[] { Color.BLACK, Color.BLACK, COLOR_BLUR, COLOR_RADAR, Color.BLACK },
                new float[] { 0.0f , 0.5f, 0.98f, 0.99f, 1 });

        paint = new Paint();
        paint.setShader(gradient);
        paint.setAntiAlias(true);
    }

    @Override
    public void draw(Canvas canvas, RectF bounds, float time, float rate) {
        float delta = SPEED / rate;
        rotation.postRotate(delta, bounds.centerX(), bounds.centerY());
        gradient.setLocalMatrix(rotation);
        canvas.drawOval(bounds, paint);
    }
}
