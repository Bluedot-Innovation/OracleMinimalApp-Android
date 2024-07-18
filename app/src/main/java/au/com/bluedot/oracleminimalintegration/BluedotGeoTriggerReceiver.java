package au.com.bluedot.oracleminimalintegration;

import android.content.Context;
import android.util.Log;

import com.pushio.manager.PIOGeoRegion;
import com.pushio.manager.PushIOManager;
import com.pushio.manager.exception.PIOInitException;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

import au.com.bluedot.point.net.engine.GeoTriggeringEventReceiver;
import au.com.bluedot.point.net.engine.ServiceManager;
import au.com.bluedot.point.net.engine.event.NotificationZoneInfo;
import au.com.bluedot.point.net.engine.event.GeoTriggerEvent;

public class BluedotGeoTriggerReceiver extends GeoTriggeringEventReceiver {
    private final String TAG = "BluedotApp";

    @Override
    public void onZoneInfoUpdate(@NotNull Context context) {
        Log.i(TAG, "Zones updated at: " + new Date().toString()
                + " ZoneInfos count: " + ServiceManager.getInstance(context).getZonesAndFences().size());
    }

    @Override
    public void onZoneEntryEvent(@NotNull GeoTriggerEvent zoneEntryEvent, @NotNull Context context) {
        Log.i(TAG, "Zones entered at: " + new Date().toString()
                + " Zone name:" + zoneEntryEvent.getZoneInfo().getName());

        //Building GeoRegion  with Fence details
        PIOGeoRegion geoRegion= new PIOGeoRegion();
        geoRegion.setGeofenceId(zoneEntryEvent.entryEvent().getFenceId().toString());
        geoRegion.setGeofenceName(zoneEntryEvent.entryEvent().getFenceName());
        geoRegion.setZoneId(zoneEntryEvent.getZoneInfo().getId().toString());
        geoRegion.setZoneName(zoneEntryEvent.getZoneInfo().getName());
        geoRegion.setSource("Bluedot SDK");

        //Reporting Checkin to Responsys
        try {
            PushIOManager.sharedInstance(context).onGeoRegionEntered(geoRegion,
                    (s, pioRegionEventType, e) -> {
                        Log.i(TAG, "onGeoRegionEntered  pioRegionEventType" + pioRegionEventType);
                        if (e != null) {
                            Log.i(TAG, "onGeoRegionEntered PIORegionException" + e.getErrorMessage() + "###" + e.getErrorDescription());
                        }
                    });
        } catch(PIOInitException error) {
            //Error
        }
    }

    @Override
    public void onZoneExitEvent(@NotNull GeoTriggerEvent zoneExitEvent, @NotNull Context context) {
        Log.i(TAG, "Zones exited at: " + new Date().toString()
                + " Zone name:" + zoneExitEvent.getZoneInfo().getName());

        //Building GeoRegion with Fence details
        PIOGeoRegion geoRegion= new PIOGeoRegion();
        geoRegion.setGeofenceId(zoneExitEvent.exitEvent().getFenceId().toString());
        geoRegion.setGeofenceName(zoneExitEvent.exitEvent().getFenceName());
        geoRegion.setDwellTime((int)zoneExitEvent.exitEvent().getDwellTime());
        geoRegion.setZoneId(zoneExitEvent.getZoneInfo().getId().toString());
        geoRegion.setZoneName(zoneExitEvent.getZoneInfo().getName());
        geoRegion.setSource("Bluedot SDK");

        //Reporting Checkout to Responsys
        try {
            PushIOManager.sharedInstance(context).onGeoRegionExited(geoRegion,
                    (s, pioRegionEventType, e) -> Log.i(TAG, "onGeoRegionExited " + s));
        } catch(PIOInitException error) {
            //Error
        }
    }
}
