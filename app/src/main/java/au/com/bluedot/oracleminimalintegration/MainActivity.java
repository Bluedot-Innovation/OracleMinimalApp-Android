package au.com.bluedot.oracleminimalintegration;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import org.jetbrains.annotations.Nullable;

import au.com.bluedot.point.net.engine.BDError;
import au.com.bluedot.point.net.engine.GeoTriggeringService;
import au.com.bluedot.point.net.engine.GeoTriggeringStatusListener;

import static androidx.core.app.NotificationCompat.PRIORITY_MAX;

/*
 * @author Bluedot Innovation
 * Copyright (c) 2018 Bluedot Innovation. All rights reserved.
 */
public class MainActivity extends Activity implements View.OnClickListener, GeoTriggeringStatusListener{
    Button bStartStopSDK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bStartStopSDK = findViewById(R.id.bStartStopSDK);
        bStartStopSDK.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (GeoTriggeringService.isRunning()) {
            bStartStopSDK.setText(R.string.stop_sdk);
        } else {
            bStartStopSDK.setText(R.string.start_sdk);
        }
    }

    @Override
    public void onClick(View v) {
        if (GeoTriggeringService.isRunning()) {
            GeoTriggeringService.stop(getApplicationContext(), this);
            bStartStopSDK.setText(R.string.start_sdk);
        } else {
            Notification notification = createNotification();
            GeoTriggeringService.builder()
                    .notification(notification)
                    .start(getApplicationContext(), this);
            bStartStopSDK.setText(R.string.stop_sdk);
        }
    }

    /**
     * Creates notification channel and notification, required for foreground service notification.
     * @return notification
     */
    private Notification createNotification() {
        String channelId;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = "Bluedot" + getString(R.string.app_name);
            String channelName = "Bluedot Service" + getString(R.string.app_name);
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableLights(false);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(false);
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

            Notification.Builder notification = new Notification.Builder(getApplicationContext(), channelId)
                    .setContentTitle(getString(R.string.foreground_notification_title))
                    .setContentText(getString(R.string.foreground_notification_text))
                    .setStyle(new Notification.BigTextStyle().bigText(getString(R.string.foreground_notification_text)))
                    .setOngoing(true)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setSmallIcon(R.mipmap.ic_launcher);

            return notification.build();
        } else {

            NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext())
                    .setContentTitle(getString(R.string.foreground_notification_title))
                    .setContentText(getString(R.string.foreground_notification_text))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.foreground_notification_text)))
                    .setOngoing(true)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setPriority(PRIORITY_MAX)
                    .setSmallIcon(R.mipmap.ic_launcher);

            return notification.build();
        }
    }

    @Override
    public void onGeoTriggeringResult(@Nullable BDError bdError) {
        if (bdError == null) return;

        Toast.makeText(getApplicationContext(),
                "GeoTrigger Start Error " + bdError.getReason(),
                Toast.LENGTH_LONG).show();
    }
}
