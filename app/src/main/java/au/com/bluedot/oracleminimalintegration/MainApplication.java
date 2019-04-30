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
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.pushio.manager.PIOBeaconRegion;
import com.pushio.manager.PIOGeoRegion;
import com.pushio.manager.PIORegionCompletionListener;
import com.pushio.manager.PIORegionEventType;
import com.pushio.manager.PIORegionException;
import com.pushio.manager.PushIOManager;

import java.util.List;
import java.util.Map;

import au.com.bluedot.application.model.Proximity;
import au.com.bluedot.point.ApplicationNotificationListener;
import au.com.bluedot.point.ServiceStatusListener;
import au.com.bluedot.point.net.engine.BDError;
import au.com.bluedot.point.net.engine.BeaconInfo;
import au.com.bluedot.point.net.engine.FenceInfo;
import au.com.bluedot.point.net.engine.LocationInfo;
import au.com.bluedot.point.net.engine.ServiceManager;
import au.com.bluedot.point.net.engine.ZoneInfo;

import static android.app.Notification.PRIORITY_MAX;

/*
 * @author Bluedot Innovation
 * Copyright (c) 2018 Bluedot Innovation. All rights reserved.
 * MainApplication demonstrates the implementation Bluedot Point SDK and related callbacks.
 */
public class MainApplication extends Application implements ServiceStatusListener, ApplicationNotificationListener {


    ServiceManager mServiceManager;

    private String apiKey = ""; //API key for the App 
    // set this to true if you want to start the SDK with service sticky and auto-start mode on boot complete.
    // Please refer to Bluedot Developer documentation for further information.
    boolean restartMode = true;
    private static String TAG = "BDApp";


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
                mServiceManager.sendAuthenticationRequest(apiKey,this, restartMode);
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
     * <p>It is called when BlueDotPointService started successful, your app logic code using the Bluedot service could start from here.</p>
     * <p>This method is off the UI thread.</p>
     */
    @Override
    public void onBlueDotPointServiceStartedSuccess() {
        mServiceManager.subscribeForApplicationNotification(this);
        //Register with Installref with Responsys SDk once Bluedot Service is started successfully
        PushIOManager.getInstance(getApplicationContext()).registerUserId(mServiceManager.getInstallRef());

    }

    /**
     * <p>This method notifies the client application that BlueDotPointService is stopped. Your app could release the resources related to Bluedot service from here.</p>
     * <p>It is called off the UI thread.</p>
     */
    @Override
    public void onBlueDotPointServiceStop() {
        mServiceManager.unsubscribeForApplicationNotification(this);
    }

    /**
     * <p>The method delivers the error from BlueDotPointService by a generic BDError. There are several types of error such as
     * - BDAuthenticationError (fatal)
     * - BDNetworkError (fatal / non fatal)
     * - LocationServiceNotEnabledError (fatal / non fatal)
     * - RuleDownloadError (non fatal)
     * - BLENotAvailableError (non fatal)
     * - BluetoothNotEnabledError (non fatal)
     * <p> The BDError.isFatal() indicates if error is fatal and service is not operable.
     * Followed by onBlueDotPointServiceStop() indicating service is stopped.
     * <p> The BDError.getReason() is useful to analyse error cause.
     * @param bdError
     */
    @Override
    public void onBlueDotPointServiceError(BDError bdError) {

    }

    /**
     * <p>The method deliveries the ZoneInfo list when the rules are updated. Your app is able to get the latest ZoneInfo when the rules are updated.</p>
     * @param list
     */
    @Override
    public void onRuleUpdate(List<ZoneInfo> list) {

    }

    /**
     * This callback happens when user is subscribed to Application Notification
     * and check into any fence under that Zone
     * @param fenceInfo      - Fence triggered
     * @param zoneInfo   - Zone information Fence belongs to
     * @param location   - geographical coordinate where trigger happened
     * @param customData - custom data associated with this Custom Action
     * @param isCheckOut - CheckOut will be tracked and delivered once device left the Fence
     */
    @Override
    public void onCheckIntoFence(final FenceInfo fenceInfo, ZoneInfo zoneInfo, LocationInfo location, Map<String, String> customData, boolean isCheckOut) {

        //Building GeoRegion  with Fence details
        PIOGeoRegion geoRegion= new PIOGeoRegion();
        geoRegion.setGeofenceId(fenceInfo.getId());
        geoRegion.setGeofenceName(fenceInfo.getName());
        geoRegion.setZoneId(zoneInfo.getZoneId());
        geoRegion.setZoneName(zoneInfo.getZoneName());
        geoRegion.setSource("Bluedot SDK");

        //Reporting Checkin to Responsys
        PushIOManager.getInstance(getApplicationContext()).onGeoRegionEntered
                (geoRegion, new PIORegionCompletionListener() {
                    @Override
                    public void onRegionReported(String s, PIORegionEventType pioRegionEventType, PIORegionException e) {
                        Log.i(TAG,"onGeoRegionEntered  pioRegionEventType"+pioRegionEventType);
                        if(e != null) {
                            Log.i(TAG, "onGeoRegionEntered PIORegionException" + e.getErrorMessage() + "###" + e.getErrorDescription());
                        }
                    }
                });
    }

    /**
     * This callback happens when user is subscribed to Application Notification
     * and checked out from fence under that Zone
     * @param fenceInfo     - Fence user is checked out from
     * @param zoneInfo  - Zone information Fence belongs to
     * @param dwellTime - time spent inside the Fence; in minutes
     * @param customData - custom data associated with this Custom Action
     */
    @Override
    public void onCheckedOutFromFence(final FenceInfo fenceInfo, ZoneInfo zoneInfo, final int dwellTime, Map<String, String> customData) {

        //Building GeoRegion with Fence details
        PIOGeoRegion geoRegion= new PIOGeoRegion();
        geoRegion.setGeofenceId(fenceInfo.getId());
        geoRegion.setGeofenceName(fenceInfo.getName());
        geoRegion.setDwellTime(dwellTime);
        geoRegion.setZoneId(zoneInfo.getZoneId());
        geoRegion.setZoneName(zoneInfo.getZoneName());
        geoRegion.setSource("Bluedot SDK");

        //Reporting Checkout to Responsys
        PushIOManager.getInstance(getApplicationContext()).onGeoRegionExited
                (geoRegion, new PIORegionCompletionListener() {
                    @Override
                    public void onRegionReported(String s, PIORegionEventType pioRegionEventType, PIORegionException e) {
                        Log.i(TAG,"onGeoRegionExited "+s);
                    }
                });
    }

    /**
     * This callback happens when user is subscribed to Application Notification
     * and check into any beacon under that Zone
     * @param beaconInfo - Beacon triggered
     * @param zoneInfo   - Zone information Beacon belongs to
     * @param location   - geographical coordinate where trigger happened
     * @param proximity  - the proximity at which the trigger occurred
     * @param customData - custom data associated with this Custom Action
     * @param isCheckOut - CheckOut will be tracked and delivered once device left the Beacon advertisement range
     */
    @Override
    public void onCheckIntoBeacon(final BeaconInfo beaconInfo, ZoneInfo zoneInfo, LocationInfo location, Proximity proximity, Map<String, String> customData, boolean isCheckOut) {

        //Building BeaconRegion  with Beacon & Zone details
        PIOBeaconRegion beaconRegion = new PIOBeaconRegion();
        beaconRegion.setBeaconId(beaconInfo.getId());
        beaconRegion.setBeaconName(beaconInfo.getName());
        beaconRegion.setZoneId(zoneInfo.getZoneId());
        beaconRegion.setZoneName(zoneInfo.getZoneName());
        beaconRegion.setSource("Bluedot SDK");

        //Reporting Beacon Checkin to Responsys
        PushIOManager.getInstance(getApplicationContext()).onBeaconRegionEntered
                (beaconRegion, new PIORegionCompletionListener() {
                    @Override
                    public void onRegionReported(String s, PIORegionEventType pioRegionEventType, PIORegionException e) {
                        Log.i(TAG,"onBeaconRegionEntered "+s);
                    }
                });
    }

    /**
     * This callback happens when user is subscribed to Application Notification
     * and checked out from beacon under that Zone
     * @param beaconInfo - Beacon is checked out from
     * @param zoneInfo   - Zone information Beacon belongs to
     * @param dwellTime  - time spent inside the Beacon area; in minutes
     * @param customData - custom data associated with this Custom Action
     */
    @Override
    public void onCheckedOutFromBeacon(final BeaconInfo beaconInfo, ZoneInfo zoneInfo, final int dwellTime, Map<String, String> customData) {

        //Building BeaconRegion  with Beacon & Zone details
        PIOBeaconRegion beaconRegion = new PIOBeaconRegion();
        beaconRegion.setBeaconId(beaconInfo.getId());
        beaconRegion.setBeaconName(beaconInfo.getName());
        beaconRegion.setZoneId(zoneInfo.getZoneId());
        beaconRegion.setZoneName(zoneInfo.getZoneName());
        beaconRegion.setSource("Bluedot SDK");

        //Reporting Beacon Checkout to Responsys
        PushIOManager.getInstance(getApplicationContext()).onBeaconRegionExited
                (beaconRegion, new PIORegionCompletionListener() {
                    @Override
                    public void onRegionReported(String s, PIORegionEventType pioRegionEventType, PIORegionException e) {
                        Log.i(TAG,"onBeaconRegionExited "+s);
                    }
                });
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
}
