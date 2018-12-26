
package iqube.surya.testapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicInteger;


import io.realm.Realm;
import io.realm.RealmResults;

import static android.widget.Toast.LENGTH_LONG;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraRecorder extends Activity implements SurfaceHolder.Callback {

    private static final String TAG = CameraRecorder.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")
    public static SurfaceView mSurfaceView;
    public static SurfaceHolder mSurfaceHolder;
    public static Camera mCamera;
    private MqttAndroidClient client;
    private PahoMqttClient pahoMqttClient;
//    public Handler subscribe_handler = null;
//    public static Runnable subscribe_runnable = null;
    public Handler publish_handler = null;
    public static Runnable publish_runnable = null;
    public static String ip;
    Realm realm;





    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Full Screen Acitivty

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);

        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(this));

        mSurfaceView = findViewById(R.id.surfaceView1);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.setFixedSize(1, 1);
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
//            turnOnHotspot();
            startService(new Intent(CameraRecorder.this, LocationService.class));
            startService (new Intent(CameraRecorder.this, MqttMessageService.class));
            ip=IP();

            if(isOnline()){
                try{
                    pahoMqttClient = new PahoMqttClient();
                    client = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.MQTT_BROKER_URL, Constants.CLIENT_ID);
                }
                catch (Exception e){
                    Log.d("CameraActivity:",""+e);
                }
            }
            else
                System.out.println("no mqtt connection");

            try {
               MyServer server = new MyServer();
            } catch (IOException e) {
                e.printStackTrace();
            }

//            startServer();
        }
//        final Handler handler_start = new Handler();
//        handler_start.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                startCamera();
//                }
//
//        }, 10900);

//        final Handler handler_stop = new Handler();
//        handler_stop.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                stopCamera();
//            }
//        }, 50000);


//MQTT Publish and Subscribe Handler Code

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    UDPClient udpClient =new UDPClient();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(e);
                }
            }
        });

        thread.start();



        try {
            publish_handler = new Handler();
            publish_runnable = new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                public void run() {
                    if(isOnline()) {
                        if(client.isConnected())
                        try {
                            if (LocationService.latitude == null && LocationService.longitude == null && LocationService.date_new == null) {
                                Toast.makeText(CameraRecorder.this,"not sent NUll!!",LENGTH_LONG).show();
                                Log.d("LOCATION VALUE", "null");
                            } else {
                                String infoR = "" + LocationService.longitude + "" + LocationService.latitude + "" + LocationService.date_new;
                                pahoMqttClient.publishMessage(client, infoR, 1, Constants.PUBLISH_TOPIC);
                                Toast.makeText(CameraRecorder.this,"SENT:"+infoR,LENGTH_LONG).show();
                                Log.d("MQTT2", infoR);
                            }
                        } catch (Exception ignored) {
                        }
                        else
                            Log.d("Connection MQTT","mqtt connection not made");

                        if(client.isConnected())
                            Subscribe();
                        else
                            Log.d("Connection MQTT","mqtt connection not made");
                    }
                    else {
                        Log.d("Network Error", "No internet , adding to realmdb");
                        Realm.init(getApplicationContext());
                        realm = Realm.getDefaultInstance();
                        try{
                            if (LocationService.latitude != null && LocationService.longitude != null && LocationService.date_new != null) {
                                writeToDB(LocationService.latitude, LocationService.longitude, LocationService.date_new, LocationService.count);
                                Log.d("Realm Status", "addedto realm");
                            }
                        }
                        catch(Exception e){
                            Log.d("Realm Error",""+e);
                        }

                    }
                    publish_handler.postDelayed(publish_runnable, 1000);
                }
            };
            publish_handler.postDelayed(publish_runnable, 1000);
        }
        catch (Exception e){
            Toast.makeText(this,""+e,LENGTH_LONG).show();
        }

////Realm Delete Button
//        Button delete = findViewById(R.id.del);
//        delete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                delteData();
//            }
//        });
//
//// Shutdown Code Button
//        final Button shutdown = findViewById(R.id.shutdown);
//        shutdown.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                shutDown();
//            }
//        });
//
////Hotspot Button
//        Button hotspot = findViewById(R.id.hotspot);
//        hotspot.setOnClickListener(new View.OnClickListener() {
//            @RequiresApi(api = Build.VERSION_CODES.O)
//            public void onClick(View v) {
//                turnOnHotspot();
//
//            }
//        });
//
////Mount Button
//        Button mount = findViewById(R.id.mount);
//        mount.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mountStorage();
//            }
//        });
//
////Grant Button
//        Button grant = findViewById(R.id.grant);
//        grant.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                grantPower();
//            }
//        });
//
////Revoke Button
//        Button revoke = findViewById(R.id.revoke);
//        revoke.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                revokePower();
//            }
//        });
//
////Reboot Button
//        Button reboot = findViewById(R.id.reboot);
//        reboot.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                rebootSystem();
//            }
//        });
//
//        Button lock = findViewById(R.id.lock);
//        lock.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                lockScreen();
//            }
//        });
//
////Realm Data
//
//        Button real=findViewById(R.id.realm);
//        real.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showData();
//            }
//        });



//MQtt Subsribe Handler
//
//        try {
//            subscribe_handler = new Handler();
//            subscribe_runnable = new Runnable() {
//                @RequiresApi(api = Build.VERSION_CODES.O)
//                public void run() {
//                    Subscribe();
//                    subscribe_handler.postDelayed(subscribe_runnable, 10000);
//                }
//            };
//            subscribe_handler.postDelayed(subscribe_runnable, 15000);
//        }
//        catch (Exception e){
//            Toast.makeText(this,""+e,LENGTH_LONG).show();
//        }

    }



    public  void delteData(){
        final RealmResults<Model> results = realm.where(Model.class).findAll();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                // remove single match
                results.deleteFirstFromRealm();
                results.deleteLastFromRealm();
                results.deleteAllFromRealm();
            }
        });
    }

    public void showData(){
        RealmResults<Model> guests = realm.where(Model.class).findAll();

        StringBuilder op= new StringBuilder();
        for (Model guest : guests) {
        op.append(guest.toString());
        }
        Log.d("Realm Data", op.toString());
    }

    public void writeToDB(final String latitude, final String longitude, final String date_new,final long count){
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm bgRealm) {
                Model model = bgRealm.createObject(Model.class);
                model.latitude=latitude;
                model.longitude=longitude;
                model.date=date_new;
                model.id= count;
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Log.d("Realm Log","Data Entered Successfuly");
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(@NonNull Throwable error) {
                Log.d("Realm Log","Data has not been Entered");
            }
        });

    }



//Shutdown
    public  void shutDown(){
        try {
            Process proc = Runtime.getRuntime()
                    .exec(new String[]{"su", "-c", "reboot -p"});
            proc.waitFor();
            Toast.makeText(CameraRecorder.this,"Shuting Down",LENGTH_LONG).show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

//Reboot

public  void rebootSystem(){
    try {
        Runtime rt = Runtime.getRuntime();
        String[] commands = {"su","-c","reboot"};
        Process proc = rt.exec(commands);
        proc.waitFor();
        Toast.makeText(CameraRecorder.this,"Rebooting",LENGTH_LONG).show();
    } catch (Exception ex) {
        ex.printStackTrace();
        Log.d("Power Status:","Rebooting system");
    }
}

//Mount
    public  void mountStorage(){
        try {
            Runtime rt = Runtime.getRuntime();
            String[] commands = {"su","-c","mount -o remount,rw /system"};
            Process proc = rt.exec(commands);
            proc.waitFor();
            Log.d("Mounting","Mounted");
//                    BufferedReader stdInput = new BufferedReader(new
//                            InputStreamReader(proc.getInputStream()));
//
//                    BufferedReader stdError = new BufferedReader(new
//                            InputStreamReader(proc.getErrorStream()));
//
//// read the output from the command
//                    System.out.println("Here is the standard output of the command:\n");
//                    String s = null;
//                    while ((s = stdInput.readLine()) != null) {
//                        System.out.println(s);
//                    }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d("Power Status","Mounting System");
        }
    }

//Grant
    public  void grantPower(){
        try {
            Runtime rt = Runtime.getRuntime();
            String[] commands = {"su","-c","sed -i  '/key 116   POWER/ s/# *//' /system/usr/keylayout/Generic.kl"};
            Process proc = rt.exec(commands);
            proc.waitFor();
            Toast.makeText(CameraRecorder.this,"Granted",LENGTH_LONG).show();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d("Power Status","Granted Power");
        }
    }


//Lock
    public  void lockScreen(){
        try {
            Runtime rt = Runtime.getRuntime();
            String[] commands = {"su","-c","input keyevent 26"};
            Process proc = rt.exec(commands);
            proc.waitFor();
            Toast.makeText(CameraRecorder.this,"Locked",LENGTH_LONG).show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

//Revoke
    public  void revokePower(){
        try {
            Runtime rt = Runtime.getRuntime();
            String[] commands = {"su","-c","sed  -i '/key 116   POWER/s/^/#/g' /system/usr/keylayout/Generic.kl"};
            Process proc = rt.exec(commands);
            proc.waitFor();
            Toast.makeText(CameraRecorder.this,"Revoked",LENGTH_LONG).show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    //Find ip
    public static String IP(){
        String retval = null;
        try {
            Runtime rt = Runtime.getRuntime();
            String[] commands = {"su","-c","ifconfig | awk '\n" +
                    "    /^[^ ]/ {interface = $1}\n" +
                    "    $1==\"inet\" && interface ~ /^(wlan0)$/ {sub(/^addr:/, \"\", $2); print $2}\n" +
                    "'"};
            Process proc = rt.exec(commands);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(proc.getInputStream()));

            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            retval= String.valueOf(output);
            Log.d("IP Valuesss",retval);
            reader.close();
            proc.waitFor();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return  retval;

    }

//Check Network


    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }



//Server Code
    public  void startServer(){
        new Server().start();
        Toast.makeText(CameraRecorder.this,"Server Stared",LENGTH_LONG).show();
    }


//Function to start camera

    public void startCamera(){
        Intent intent = new Intent(CameraRecorder.this, RecorderService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(intent);
        Log.d("Camera Status","started camera");
        finish();
    }

//Function to stop Camera

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void stopCamera(){

        stopService(new Intent(CameraRecorder.this, RecorderService.class));
        Intent intent = new Intent(CameraRecorder.this, CameraRecorder.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // You need this if starting
        //  the activity from a service
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        turnOffHotspot();
//        Toast.makeText(CameraRecorder.this,""+batLevel,Toast.LENGTH_LONG).show();
        Log.d("Camera Status","started camera");
        startActivity(intent);
    }

//Hotspot

    @RequiresApi(api = Build.VERSION_CODES.O)


//Subscription
    public void Subscribe(){
        String topic = "trackR/"+busNumber.busId;
        try {
            pahoMqttClient.subscribe(client, topic, 1);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

// Permissions

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE
    };

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }





//Hotspot code

    private WifiManager.LocalOnlyHotspotReservation mReservation;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void turnOnHotspot() {
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);


        assert manager != null;
        manager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

            @Override
            public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                super.onStarted(reservation);
                Log.d(TAG, "Wifi Hotspot is on now");
                mReservation = reservation;
            }

            @Override
            public void onStopped() {
                super.onStopped();
                Log.d(TAG, "onStopped: ");
            }

            @Override
            public void onFailed(int reason) {
                super.onFailed(reason);
                Log.d(TAG, "onFailed: ");
            }
        }, new Handler());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public  void turnOffHotspot() {
        if (mReservation != null) {
            mReservation.close();
        }
    }






    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0) {
            startService(new Intent(this, LocationService.class));
        } else {
            Toast.makeText(this, "Location Service Denied", LENGTH_LONG).show();
        }
    }
}
