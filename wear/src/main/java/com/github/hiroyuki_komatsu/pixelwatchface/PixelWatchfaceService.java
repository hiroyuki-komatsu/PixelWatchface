package com.github.hiroyuki_komatsu.pixelwatchface;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.util.Log;
import android.view.SurfaceHolder;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Pixel Watchface.
 */
public class PixelWatchfaceService extends CanvasWatchFaceService {
    private static final String TAG = "PixelWatchfaceService";

    @Override
    public Engine onCreateEngine() {
        /* provide your watch face implementation */
        return new Engine();
    }

    /* implement service callback methods */
    private class Engine extends CanvasWatchFaceService.Engine {
        static final int MSG_UPDATE_TIME = 0;
        static final long INTERACTIVE_UPDATE_RATE_MS = 500;

        // Time to be displayed
        Calendar mCalendar;
        SimpleDateFormat mDateFormat;

        // Background bitmap
        Bitmap mBackgroundBitmap;
        Bitmap mBackgroundScaledBitmap;

        // Paint for drawing text
        Paint mPaint;

        // Handler to update the time once a second in interactive mode
        final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = INTERACTIVE_UPDATE_RATE_MS
                                    - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                            mUpdateTimeHandler
                                    .sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                        break;
                }
            }
        };

        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            /* initialize your watch face */

            /* load the background image */
            Resources resources = PixelWatchfaceService.this.getResources();
            Drawable backgroundDrawable = resources.getDrawable(R.drawable.preview, null);
            mBackgroundBitmap = ((BitmapDrawable) backgroundDrawable).getBitmap();

            mDateFormat = new SimpleDateFormat("kk:mm:ss");

            mPaint = new Paint();
            mPaint.setColor(Color.WHITE);
            mPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
            mPaint.setAntiAlias(true);
            mPaint.setTextSize(48);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            /* get device features (burn-in, low-bit ambient) */
        }

        @Override
        public void onTimeTick() {
            Log.d(TAG, "onTimeTick");
            super.onTimeTick();
            /* the time changed */

            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            Log.d(TAG, "onAmbientModeChanged");
            super.onAmbientModeChanged(inAmbientMode);
            /* the wearable switched between modes */
            invalidate();
            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            Log.d(TAG, "onDraw");
            /* draw your watch face */
            int width = bounds.width();
            int height = bounds.height();

            // Draw the background, scaled to fit.
            if (mBackgroundScaledBitmap == null
                || mBackgroundScaledBitmap.getWidth() != width
                || mBackgroundScaledBitmap.getHeight() != height) {
                mBackgroundScaledBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap,
                        width, height, true /* filter */);
            }
            canvas.drawBitmap(mBackgroundScaledBitmap, 0, 0, null);

            // Draw text
            mCalendar = Calendar.getInstance();
            canvas.drawText(mDateFormat.format(mCalendar.getTime()), 20, 80, mPaint);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            /* the watch face became visible or invisible */
            updateTimer();
        }
    }
}
