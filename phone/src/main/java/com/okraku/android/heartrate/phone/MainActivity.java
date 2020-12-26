package com.okraku.android.heartrate.phone;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Main activity for smartphones.
 */
public class MainActivity extends AppCompatActivity implements DataLayerService.OnHeartRateChangeListener {
    /**
     * Text view that displays the current heart rate.
     */
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.textView = findViewById(R.id.heartRate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Listen for heart rate changes when the activity comes to foreground
        DataLayerService.setCallBack(this);
    }

    @Override
    protected void onPause() {
        // Stop listening for heart rate changes when the activity has been paused
        DataLayerService.setCallBack(null);
        super.onPause();
    }

    /**
     * Called when the heart rate has changed.
     *
     * @param value The new heart rate
     */
    @Override
    public void onHeartRateChanged(final int value) {
        // Update text view
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(Integer.toString(value));
            }
        });
    }
}
