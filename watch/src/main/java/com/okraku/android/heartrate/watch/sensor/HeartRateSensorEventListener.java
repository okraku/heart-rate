package com.okraku.android.heartrate.watch.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Collects data from the heart rate sensor.
 */
public class HeartRateSensorEventListener extends AbstractHeartRateEventListener implements SensorEventListener {
    /**
     * Log tag.
     */
    private static final String LOG_TAG = HeartRateSensorEventListener.class.getName();

    /**
     * A manager to access sensors of the smartwatch.
     */
    private SensorManager sensorManager;

    @Override
    public void onCreate() {
        super.onCreate();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        boolean ok = sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_UI);

        if(ok) {
            Log.d(LOG_TAG, "Registered listener for heart rate sensor");
        }
        else {
            Log.e(LOG_TAG, "Could not register listener for heart rate sensor");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        Log.d(LOG_TAG, "Event listener has been destroyed.");
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_HEART_RATE && sensorEvent.values.length > 0) {
            int newHeartRate = Math.round(sensorEvent.values[0]);

            if (newHeartRate != 0 && lastHeartRate != newHeartRate) {
                onNewHeartRate(newHeartRate);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
