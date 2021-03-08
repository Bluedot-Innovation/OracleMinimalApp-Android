package au.com.bluedot.oracleminimalintegration;

import android.content.Context;
import android.util.Log;

import com.pushio.manager.PIOGeoRegion;
import com.pushio.manager.PIORegionCompletionListener;
import com.pushio.manager.PIORegionEventType;
import com.pushio.manager.PIORegionException;
import com.pushio.manager.PushIOManager;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

import au.com.bluedot.point.net.engine.GeoTriggeringEventReceiver;
import au.com.bluedot.point.net.engine.ZoneEntryEvent;
import au.com.bluedot.point.net.engine.ZoneExitEvent;
import au.com.bluedot.point.net.engine.ZoneInfo;

public class BluedotGeoTriggerReceiver extends GeoTriggeringEventReceiver {
    private final String TAG = "BluedotApp";

    @Override
    public void onZoneInfoUpdate(@NotNull List<ZoneInfo> list, @NotNull Context context) {
        Log.i(TAG, "Zones updated at: " + new Date().toString()
                + " ZoneInfos count: " + list.size());
    }

    @Override
    public void onZoneEntryEvent(@NotNull ZoneEntryEvent zoneEntryEvent, @NotNull Context context) {
        Log.i(TAG, "Zones entered at: " + new Date().toString()
                + " Zone name:" + zoneEntryEvent.toString());

        //Building GeoRegion  with Fence details
        PIOGeoRegion geoRegion= new PIOGeoRegion();
        geoRegion.setGeofenceId(zoneEntryEvent.getFenceInfo().getId());
        geoRegion.setGeofenceName(zoneEntryEvent.getFenceInfo().getName());
        geoRegion.setZoneId(zoneEntryEvent.getZoneInfo().getZoneId());
        geoRegion.setZoneName(zoneEntryEvent.getZoneInfo().getZoneName());
        geoRegion.setSource("Bluedot SDK");

        //Reporting Checkin to Responsys
        PushIOManager.getInstance(context).onGeoRegionEntered
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

    @Override
    public void onZoneExitEvent(@NotNull ZoneExitEvent zoneExitEvent, @NotNull Context context) {
        Log.i(TAG, "Zones exited at: " + new Date().toString()
                + " Zone name:" + zoneExitEvent.toString());

        //Building GeoRegion with Fence details
        PIOGeoRegion geoRegion= new PIOGeoRegion();
        geoRegion.setGeofenceId(zoneExitEvent.getFenceInfo().getId());
        geoRegion.setGeofenceName(zoneExitEvent.getFenceInfo().getName());
        geoRegion.setDwellTime(zoneExitEvent.getDwellTime());
        geoRegion.setZoneId(zoneExitEvent.getZoneInfo().getZoneId());
        geoRegion.setZoneName(zoneExitEvent.getZoneInfo().getZoneName());
        geoRegion.setSource("Bluedot SDK");

        //Reporting Checkout to Responsys
        PushIOManager.getInstance(context).onGeoRegionExited
                (geoRegion, new PIORegionCompletionListener() {
                    @Override
                    public void onRegionReported(String s, PIORegionEventType pioRegionEventType, PIORegionException e) {
                        Log.i(TAG,"onGeoRegionExited "+s);
                    }
                });
    }
}
