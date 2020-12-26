package com.okraku.android.heartrate.watch;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;

import com.okraku.android.heartrate.watch.sensor.AbstractHeartRateEventListener;
import com.okraku.android.heartrate.watch.sensor.HeartRateSensorEventListener;
import com.okraku.android.heartrate.watch.sensor.MockHeartRateSensorEventListener;

/**
 * Main activity of the smartwatch.
 */
public class MainActivity extends Activity implements AbstractHeartRateEventListener.OnHeartRateChangeListener, ServiceConnection {
    /**
     * Text view that displays the current heart rate.
     */
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.text);

        /**
         * Start the service that listens for heart rate sensor events.
         *
         * Use {@link HeartRateSensorEventListener} to use the heart rate sensor of the smartwatch.
         * Use {@link MockHeartRateSensorEventListener} to use a mockup that generates random numbers.
         */
        Intent intent = new Intent(MainActivity.this, MockHeartRateSensorEventListener.class);
        bindService(intent, this, Service.BIND_AUTO_CREATE);
    }

    @Override
    public void onHeartRateChanged(int newValue) {
        textView.setText(Integer.toString(newValue));
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        ((HeartRateSensorEventListener.ServiceBinder) service).setChangeListener(MainActivity.this);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    }
}
