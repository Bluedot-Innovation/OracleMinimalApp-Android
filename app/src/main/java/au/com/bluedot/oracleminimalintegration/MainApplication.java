package au.com.bluedot.oracleminimalintegration;

import android.Manifest;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.pushio.manager.PushIOManager;

import org.jetbrains.annotations.Nullable;

import au.com.bluedot.point.net.engine.BDError;
import au.com.bluedot.point.net.engine.GeoTriggeringService;
import au.com.bluedot.point.net.engine.GeoTriggeringStatusListener;
import au.com.bluedot.point.net.engine.InitializationResultListener;
import au.com.bluedot.point.net.engine.ServiceManager;

import static android.app.Notification.PRIORITY_MAX;

/*
 * @author Bluedot Innovation
 * Copyright (c) 2018 Bluedot Innovation. All rights reserved.
 * MainApplication demonstrates the implementation Bluedot Point SDK and related callbacks.
 */
public class MainApplication extends Application implements InitializationResultListener, GeoTriggeringStatusListener {


    ServiceManager mServiceManager;

    private final String projectId = ""; //Project ID// for the App 


    @Override
    public void onCreate() {
        super.onCreate();

        //Registering App with Responsys SDK
        PushIOManager.getInstance(getApplicationContext()).registerApp();
        //Set this you need InApp Push feature from Responsys
        PushIOManager.getInstance(this).setInAppFetchEnabled(true);

        // initialize Bluedot point sdk
        initPointSDK();
    }

    public void initPointSDK() {

        int checkPermissionCoarse = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        int checkPermissionFine = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);

        if(checkPermissionCoarse == PackageManager.PERMISSION_GRANTED && checkPermissionFine == PackageManager.PERMISSION_GRANTED) {
            mServiceManager = ServiceManager.getInstance(this);

            if(!mServiceManager.isBlueDotPointServiceRunning()) {
                // Setting Notification for foreground service, required for Android Oreo and above.
                // Setting targetAllAPIs to TRUE will display foreground notification for Android versions lower than Oreo
                mServiceManager.setForegroundServiceNotification(createNotification(), false);
                mServiceManager.initialize(projectId, this);
            }
        }
        else
        {
            requestPermissions();
        }

    }

    private void requestPermissions() {

        Intent intent = new Intent(getApplicationContext(), RequestPermissionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
    public void onInitializationFinished(@Nullable BDError bdError) {
        PushIOManager.getInstance(getApplicationContext()).registerUserId(mServiceManager.getInstallRef());

        if (GeoTriggeringService.isRunning()) return;

        Notification notification = createNotification();
        GeoTriggeringService.builder()
                .notification(notification)
                .start(getApplicationContext(), this);
    }

    @Override
    public void onGeoTriggeringResult(@Nullable BDError bdError) {
        if (bdError == null) return;

        Toast.makeText(getApplicationContext(),
                "GeoTrigger Start Error " + bdError.getReason(),
                Toast.LENGTH_LONG).show();
    }
}
