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
        SimpleDateFormat mDateFormatBlink;

        // Background bitmap
        Bitmap mMascotBitmap;
        Bitmap mMascotScaledBitmap;

        // Paint for drawing text
        Paint mPaintBackground;
        Paint mPaintText;

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
            Drawable mascotDrawable = resources.getDrawable(R.drawable.droid, null);
            mMascotBitmap = ((BitmapDrawable) mascotDrawable).getBitmap();

            mDateFormat = new SimpleDateFormat("kk:mm");
            mDateFormatBlink = new SimpleDateFormat("kk mm");

            mPaintText = new Paint();
            mPaintText.setColor(Color.WHITE);
            mPaintText.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
            mPaintText.setAntiAlias(true);
            mPaintText.setTextSize(48);

            mPaintBackground = new Paint();
            mPaintBackground.setColor(Color.rgb(0, 0, 0));  // 0091EA - Light Blue A700
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

            // Draw background
            canvas.drawRect(0, 0, width, height, mPaintBackground);

            // Draw the mascot, scaled to fit.
            int mascotSize = width / 4;
            if (mMascotScaledBitmap == null || mMascotScaledBitmap.getWidth() != mascotSize) {
                // mMascotBitmap should be square.
                mMascotScaledBitmap = Bitmap.createScaledBitmap(
                        mMascotBitmap, mascotSize, mascotSize, false /* filter */);
            }
            canvas.drawBitmap(mMascotScaledBitmap, width * 3/4, height - mascotSize, null);

            // Draw text
            mCalendar = Calendar.getInstance();
            canvas.drawText(mDateFormat.format(mCalendar.getTime()), 30, 110, mPaintText);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            /* the watch face became visible or invisible */
            updateTimer();
        }
    }
}
