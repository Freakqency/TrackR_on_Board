package iqube.surya.testapplication;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by singapore on 25-06-2016.
 */
public class NetworkCheck extends BroadcastReceiver {

    SQLiteDatabase db;
    public static boolean Connected = false;

    public NetworkCheck() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                Connected = true;
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                Connected = true;
        } else
            Connected = false;

        if (Connected) {
            if(LocationService.userid!=null) {
                //Updating locations
                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }

            }
         }
        else {

        }
    }
    public static class ServerSync extends AsyncTask<String,Void,Void> {

        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnection conn;
            try {
                URL url = new URL(params[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.connect();
                conn.getInputStream();
                conn.disconnect();

            } catch (MalformedURLException e) {
                Log.d("Rating Upload","URL Malformed");
                e.printStackTrace();
            } catch (IOException e) {
                Log.d("Rating Upload","IOException");
                e.printStackTrace();
            }
            return null;
        }
    }

}
