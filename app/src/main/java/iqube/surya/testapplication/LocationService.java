package iqube.surya.testapplication;

/**
 * Created by singapore on 23-07-2016.
 */

import android.Manifest;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class LocationService extends Service {
    private NotificationManager mNM;
    private LocationManager mgr;
    private Locationer gps_locationer, network_locationer;
    public static int START;
    private Location location;
    private Handler mHandler;
    SQLiteDatabase db;
    public static String userid;
    public static String longitude;
    public static String latitude;
    public static String date_new;
    public static String infoR;

    long mInterval=1000;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Display a notification about us starting.  We put an icon in the status bar.
//        android.os.Debug.waitForDebugger();
        mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        gps_locationer = new Locationer(getBaseContext());
        network_locationer = new Locationer(getBaseContext());

        mHandler = new Handler();
        mStatusChecker.run();
        return START_STICKY;
    }


    final Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {

                START = 1;
                Criteria criteria = new Criteria();
                criteria.setAltitudeRequired(false);
                criteria.setBearingRequired(false);
                criteria.setCostAllowed(false);
                criteria.setPowerRequirement(Criteria.POWER_HIGH);
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                String providerFine = mgr.getBestProvider(criteria, true);
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                String providerCoarse = mgr.getBestProvider(criteria, true);

                if (providerCoarse != null) {
                    if (ActivityCompat.checkSelfPermission(LocationService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(LocationService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    mgr.requestLocationUpdates(providerCoarse, 3000, 5, network_locationer);
                    location = mgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                if (providerFine != null&&(mgr.getAllProviders().contains(LocationManager.GPS_PROVIDER))) {
                    mgr.requestLocationUpdates(providerFine, 2000, 0, gps_locationer);
                    location = mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
            }finally {
                if(location!=null) {
                    double lat = location.getLatitude() % 0.00001;
                    lat = location.getLatitude() - lat;
                    double longt = location.getLongitude() % 0.00001;
                    longt = location.getLongitude() - longt;
                    //showNotification("Lat: "+ lat+"Long: "+ longt);
                    Date date = new Date();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);//2016-08-09%2010:45:01.253
                    latitude=""+lat;
                    longitude=""+longt;
                    date_new=" "+simpleDateFormat.format(date);
                    infoR=""+latitude+""+longitude+""+date_new;
//                    Log.d("Test",last);


                }
                mHandler.postDelayed(mStatusChecker,mInterval);
            }
        }
    };


    @Override
    public void onDestroy() {
        START=0;
        // Cancel the persistent notification.

//        mgr.removeUpdates(gps_locationer);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
//        mgr.removeUpdates(network_locationer);
        // Tell the user we stopped.
//        mHandler.removeCallbacks(mStatusChecker);
//        startService(new Intent(this,LocationService.class));
        Toast.makeText(this, "local service is stopped", Toast.LENGTH_SHORT).show();
    }


    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }


}
