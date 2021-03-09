package au.com.bluedot.oracleminimalintegration;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.pushio.manager.PushIOManager;

import org.jetbrains.annotations.Nullable;

import au.com.bluedot.point.net.engine.BDError;
import au.com.bluedot.point.net.engine.InitializationResultListener;
import au.com.bluedot.point.net.engine.ServiceManager;


/*
 * @author Bluedot Innovation
 * Copyright (c) 2018 Bluedot Innovation. All rights reserved.
 * MainApplication demonstrates the implementation Bluedot Point SDK and related callbacks.
 */
public class MainApplication extends Application implements InitializationResultListener {


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

    @Override
    public void onInitializationFinished(@Nullable BDError bdError) {
        if (bdError != null){
            Toast.makeText(getApplicationContext(),
                    "Bluedot Initialization Error " + bdError.getReason(),
                    Toast.LENGTH_LONG).show();

            return;
        }

        PushIOManager.getInstance(getApplicationContext()).registerUserId(mServiceManager.getInstallRef());
    }
}
