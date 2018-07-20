package iqube.surya.testapplication;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;


/**
 * Created by singapore on 23-07-2016.
 */
class Locationer implements LocationListener {

    private Context ctx;
    private long mLastLocationMillis;
    private static final String DEBUG_TAG = "Locationer";
    private static final String[] Status = {"out of service", "temporarily unavailable", "available"};
    private static final double ACCU_THRESHOLD = 100.0;

    public Locationer(Context context) {
        ctx = context;
    }

    @Override
    public void onLocationChanged(Location location) {
//		Intent intent = new Intent("locationer");
        if ((location == null)||(location.getAccuracy() > ACCU_THRESHOLD)) {
            return;
        }
        mLastLocationMillis = SystemClock.elapsedRealtime();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(DEBUG_TAG, provider + " disabled.");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(DEBUG_TAG, provider + " enabled.");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(DEBUG_TAG, provider + " statu changed" + status );
    }

    /** Describe the given location, which might be null */
    private String dumpLocation(Location location) {
        String msg;
        StringBuilder builder = new StringBuilder();
        builder
                .append("P:")
                .append(location.getProvider())
//    		.append("|V:" )
//    		.append(location.getSpeed())
                .append("|A:" )
                .append(location.getAccuracy());
//    		.append("|D:")
//    		.append(location.getBearing());

        msg = builder.toString();

        return msg;
    }


}