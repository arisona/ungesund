/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.corebounce.ungesund;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import java.lang.ref.WeakReference;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public class UngesundWatchFace extends CanvasWatchFaceService {

    static final int MSG_ID_UPDATE_TIME = 0;

    private static class UpdateHandler extends Handler {

        final WeakReference<UngesundWatchFace.Engine> ref;

        public UpdateHandler(UngesundWatchFace.Engine reference) {
            ref = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            UngesundWatchFace.Engine engine = ref.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_ID_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private static final long INTERACTIVE_UPDATE_RATE_MS = 500;

    private static final Typeface NORMAL_TYPEFACE = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);


    /**
     * Handler message id for updating the time periodically in interactive mode.
     */

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        final Handler updateHandler = new UpdateHandler(this);

        Paint backgroundPaint;
        Paint textPaint;

        Time time;

        int counter;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(UngesundWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_PERSISTENT)
                    .setShowSystemUiTime(false)
                    .build());

            Resources resources = UngesundWatchFace.this.getResources();

            backgroundPaint = new Paint();
            backgroundPaint.setColor(resources.getColor(R.color.ungesund_background));

            textPaint = new Paint();
            textPaint.setColor(resources.getColor(R.color.ungesund_foreground));
            textPaint.setTypeface(NORMAL_TYPEFACE);
            textPaint.setAntiAlias(true);

            time = new Time();


            // Hack to keep display alive
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "WatchFaceWakelockTag");
            wakeLock.acquire();
        }

        @Override
        public void onDestroy() {
            updateHandler.removeMessages(MSG_ID_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                time.clear(TimeZone.getDefault().getID());
                time.setToNow();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = UngesundWatchFace.this.getResources();
            float textSize = resources.getDimension(R.dimen.digital_text_size);
            textPaint.setTextSize(textSize);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            invalidate();
            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Draw the background.
            canvas.drawRect(0, 0, bounds.width(), bounds.height(), backgroundPaint);

            // Draw H:MM in ambient mode or H:MM:SS in interactive mode.
            time.setToNow();
            String text = String.format("%d:%02d:%02d:%d", time.hour, time.minute, time.second, counter++);
            canvas.drawText(text, 10, 100, textPaint);
        }

        boolean shouldTimerBeRunning() {
            return isVisible();
        }

        void updateTimer() {
            updateHandler.removeMessages(MSG_ID_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                updateHandler.sendEmptyMessage(MSG_ID_UPDATE_TIME);
            }
        }

        void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                updateHandler.sendEmptyMessageDelayed(MSG_ID_UPDATE_TIME, delayMs);
            }
        }
    }
}
