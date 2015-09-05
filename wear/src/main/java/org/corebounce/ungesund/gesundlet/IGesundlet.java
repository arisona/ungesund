package org.corebounce.ungesund.gesundlet;

import android.graphics.Canvas;
import android.graphics.Rect;

public interface IGesundlet {
    void draw(Canvas canvas, Rect bounds, float time, float rate);
}
