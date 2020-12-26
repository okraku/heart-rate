package com.okraku.android.heartrate.watch.sensor;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.okraku.android.heartrate.watch.DataLayerService;

public abstract class AbstractHeartRateEventListener extends Service {
    /**
     * The most recent heart rate.
     */
    public static int lastHeartRate = 0;

    /**
     * This listener will be notified when the heart rate has changed.
     */
    protected OnHeartRateChangeListener onHeartRateChangeListener;

    /**
     * A binder to connect this service to an activity.
     */
    protected IBinder binder = new ServiceBinder();

    public class ServiceBinder extends Binder {
        public void setChangeListener(OnHeartRateChangeListener listener) {
            onHeartRateChangeListener = listener;

            // Initial value
            listener.onHeartRateChanged(lastHeartRate);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * Interface which will be called when the heart rate has changed.
     */
    public interface OnHeartRateChangeListener {
        /**
         * Called on the listener when the heart rate has changed.
         *
         * @param value The new heart rate
         */
        void onHeartRateChanged(int value);
    }

    /**
     * Called when a new heart rate has been detected.
     *
     * @param value The new heart rate
     */
    protected void onNewHeartRate(int value) {
        lastHeartRate = value;

        if (onHeartRateChangeListener != null) {
            onHeartRateChangeListener.onHeartRateChanged(value);
        }

        DataLayerService.sendBroadcastMessage(getApplicationContext(), lastHeartRate);
    }
}
