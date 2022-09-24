package com.github.thomasfox.saildata.sender;

import android.util.Log;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import com.github.thomasfox.saildata.ui.main.MainActivity;
import com.github.thomasfox.saildata.R;

import org.junit.Test;
import org.junit.runner.RunWith;


@SmallTest
@RunWith(AndroidJUnit4.class)
public class BluetoothAndroidUnitTest {

    private static final String LOG_TAG ="saildata:BLEUnitTest";

    /**
     * Tests sending data to the selected BLE device.
     * The data can be viewed on the BLE device.
     * No check is made here that the data is actually sent.
     */
    @Test
    public void testBluetooth() {
        ActivityScenario.launch(MainActivity.class).onActivity(activity -> {
            TextView statusTextView = activity.findViewById(R.id.statusBleText);
            BleSender bluetoothSender = new BleSender(activity, statusTextView);
            for (int j = 0; j < 10; j++) {
                for (int i = 0; i < 10; i++) {
                    Log.d(LOG_TAG, "sending " + i);
                    bluetoothSender.sendFieldsIfConnected(
                            "f1:" + i + ".0",
                            "f2:" + j + "00",
                            "f3:" + (i - 5) * 10);
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}