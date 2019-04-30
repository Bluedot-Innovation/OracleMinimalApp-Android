package au.com.bluedot.oracleminimalintegration;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import au.com.bluedot.point.net.engine.ServiceManager;
import com.pushio.manager.PushIOManager;

/*
 * @author Bluedot Innovation
 * Copyright (c) 2018 Bluedot Innovation. All rights reserved.
 */
public class MainActivity extends Activity implements View.OnClickListener{

    Button bStopSDK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
    }

    private void initUI() {

        bStopSDK = (Button) findViewById(R.id.bStopSDK);

        bStopSDK.setOnClickListener(this);
    }

    private void stopSDK(){
        ServiceManager.getInstance(this).stopPointService();
        finish();
    }

    @Override
    public void onClick(View v) {
        int ID = v.getId();
        switch (ID){
            case R.id.bStopSDK:
                stopSDK();
                break;

        }
    }

}
