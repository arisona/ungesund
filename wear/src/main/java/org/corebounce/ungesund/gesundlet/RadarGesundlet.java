package org.corebounce.ungesund.gesundlet;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;

import org.corebounce.ungesund.UngesundWatchFace;

public class RadarGesundlet implements IGesundlet {
    private static class Point {
        Point(float alpha, float radius) {
            this.alpha = alpha;
            this.radius = radius;
        }

        float alpha;
        float radius;
    }

    private static final int NUM_POINTS = 20;
    private static final float POINT_ALPHA_SPEED = 1f;
    private static final float POINT_RADIUS_SPEED = 0.01f;

    private static final int COLOR_RADAR = Color.argb(255, 255, 0, 0);
    private static final int COLOR_DECOR = Color.argb(255, 200, 0, 0);
    private static final int COLOR_BLUR = Color.argb(255, 127, 0, 0);

    private static final float SPEED = 180f; // degrees/s

    private float angle = 0;

    private Matrix radarRotation;
    private SweepGradient radarGradient;
    private Paint radarPaint;

    private Point[] points = new Point[NUM_POINTS];
    private Paint pointPaint;

    public RadarGesundlet(UngesundWatchFace.Context context) {
        radarRotation = new Matrix();

        RectF bounds = context.getBounds();
        radarGradient = new SweepGradient(bounds.centerX(), bounds.centerY(),
                new int[] { Color.BLACK, Color.BLACK, COLOR_BLUR, COLOR_RADAR, Color.BLACK },
                new float[] { 0.0f , 0.5f, 0.98f, 0.99f, 1 });

        radarPaint = new Paint();
        radarPaint.setShader(radarGradient);
        radarPaint.setAntiAlias(true);

        for (int i = 0; i < NUM_POINTS; ++i) {
            points[i] = new Point((float)Math.random() * 360, (float)Math.random());
        }

        pointPaint = new Paint();
        pointPaint.setStrokeCap(Paint.Cap.ROUND);
        pointPaint.setStrokeWidth(7);
        pointPaint.setAntiAlias(true);
    }

    @Override
    public void draw(Canvas canvas, RectF bounds, float time, float rate) {
        angle += SPEED / rate;

        float cx = bounds.centerX();
        float cy = bounds.centerY();
        float whalf = bounds.width() / 2;
        float hhalf = bounds.height() / 2;

        radarRotation.setRotate(angle, cx, cy);
        radarGradient.setLocalMatrix(radarRotation);
        canvas.drawOval(bounds, radarPaint);

        for (Point point : points) {
            float distance = 255 - ((angle - point.alpha) % 360f) / 360f * 200f;

            if (distance > 253) {
                point.alpha = clamp(0, 360, point.alpha + POINT_ALPHA_SPEED * (Math.random() > 0.5 ? 1 : -1));
                point.radius = clamp(0, 1, point.radius + POINT_RADIUS_SPEED * (Math.random() > 0.5 ? 1 : -1));
            }

            pointPaint.setARGB((int)distance, 255, 255, 255);
            double radians = Math.toRadians(point.alpha);
            float x = cx + whalf * point.radius * (float)Math.cos(radians);
            float y = cy + hhalf * point.radius * (float)Math.sin(radians);
            canvas.drawPoint(x, y, pointPaint);
        }
    }

    private static float clamp(float min, float max, float value) {
        return Math.max(min, Math.min(max, value));
    }
}
