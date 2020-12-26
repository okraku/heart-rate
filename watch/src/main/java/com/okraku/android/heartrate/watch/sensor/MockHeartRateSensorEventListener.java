package com.okraku.android.heartrate.watch.sensor;

import android.os.Handler;
import android.util.Log;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates random numbers for the heart rate.
 */
public class MockHeartRateSensorEventListener extends AbstractHeartRateEventListener {
    /**
     * Delay between number generation.
     */
    private static final int UPDATE_DELAY = 2000;

    /**
     * Minimum number.
     */
    private static final int MIN = 80;

    /**
     * Maximum number.
     */
    private static final int MAX = 120;

    /**
     * Log tag.
     */
    private static final String LOG_TAG = MockHeartRateSensorEventListener.class.getName();

    @Override
    public void onCreate() {
        super.onCreate();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                int random = ThreadLocalRandom.current().nextInt(MIN, MAX + 1);
                // Notify listeners that the heart rate has changed.
                onNewHeartRate(random);
                handler.postDelayed(this, UPDATE_DELAY);
            }
        }, 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "Event listener has been destroyed.");
    }
}
