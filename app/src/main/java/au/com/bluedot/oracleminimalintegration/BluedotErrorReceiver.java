package au.com.bluedot.oracleminimalintegration;

import android.content.Context;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import au.com.bluedot.point.net.engine.BDError;
import au.com.bluedot.point.net.engine.BluedotServiceReceiver;

public class BluedotErrorReceiver extends BluedotServiceReceiver {

    @Override
    public void onBluedotServiceError(@NotNull BDError bdError, @NotNull Context context) {
        Toast.makeText(context, "Bluedot Service Error " + bdError.getReason(),
                Toast.LENGTH_LONG).show();
    }
}
