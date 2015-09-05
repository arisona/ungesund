package org.corebounce.ungesund.gesundlet;

import android.graphics.Canvas;
import android.graphics.RectF;

public interface IGesundlet {
    void draw(Canvas canvas, RectF bounds, float time, float rate);
}
