package iqube.surya.testapplication;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;



class Locationer implements LocationListener {

    private static final String DEBUG_TAG = "Locationer";
//    private static final String[] Status = {"out of service", "temporarily unavailable", "available"};
    private static final double ACCU_THRESHOLD = 100.0;

    Locationer(Context context) {
        Context ctx = context;
    }

    @Override
    public void onLocationChanged(Location location) {
        if ((location == null)||(location.getAccuracy() > ACCU_THRESHOLD)) {
        }
        long mLastLocationMillis = SystemClock.elapsedRealtime();

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


}