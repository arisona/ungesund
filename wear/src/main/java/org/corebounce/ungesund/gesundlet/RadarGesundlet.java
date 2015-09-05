package org.corebounce.ungesund.gesundlet;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;

import org.corebounce.ungesund.R;
import org.corebounce.ungesund.UngesundWatchFace;

public class RadarGesundlet implements IGesundlet {
    private Matrix rotation;
    private SweepGradient gradient;
    private Paint paint;

    public RadarGesundlet(UngesundWatchFace.Context context) {

        rotation = new Matrix();

        //gradient = new SweepGradient(context.getBounds().centerX(), context.getBounds().centerY(),
        //                             context.getColor(R.color.radar_start), context.getColor(R.color.radar_end));

        RectF bounds = context.getBounds();
        float startPos = 0;
        float endPos = 0.4f;
        int startColor = Color.RED;
        int endColor = Color.BLACK;
        gradient = new SweepGradient(bounds.centerX(), bounds.centerY(),
                new int[] { endColor, endColor, startColor, endColor },
                new float[] { startPos, endPos, 0.99f, 1 });

        paint = new Paint();
        paint.setShader(gradient);
        paint.setAntiAlias(true);
    }

    @Override
    public void draw(Canvas canvas, RectF bounds, float time, float rate) {
        rotation.postRotate(4, bounds.centerX(), bounds.centerY());
        gradient.setLocalMatrix(rotation);
        canvas.drawOval(bounds, paint);
    }
}
