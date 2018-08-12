/*
 * Copyright (c) 2015, Picker Weng
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of CameraRecorder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Project:
 *     CameraRecorder
 *
 * File:
 *     CameraRecorder.java
 *
 * Author:
 *     Picker Weng (pickerweng@gmail.com)
 */

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
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import java.io.IOException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class CameraRecorder extends Activity implements SurfaceHolder.Callback {

    private static final String TAG = CameraRecorder.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")
    public static SurfaceView mSurfaceView;
    public static SurfaceHolder mSurfaceHolder;
    public static Camera mCamera;
    private MqttAndroidClient client;
    private PahoMqttClient pahoMqttClient;
    boolean flag = true;
    public Handler subscribe_handler = null;
    public static Runnable subscribe_runnable = null;



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Full Screen Acitivty
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);

        mSurfaceView = findViewById(R.id.surfaceView1);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.setFixedSize(1, 1);
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            startService(new Intent(CameraRecorder.this, LocationService.class));
            startService (new Intent(CameraRecorder.this, MqttMessageService.class));
//            startService (new Intent(CameraRecorder.this, RecorderService.class));
            startServer();
        }

        try{
            pahoMqttClient = new PahoMqttClient();
            client = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.MQTT_BROKER_URL, Constants.CLIENT_ID);
        }
        catch (Exception e){
            Log.d("CameraActivity:",""+e);
        }


//MQTT Publish Code
        if (isOnline()) {
            final Handler handler = new Handler();
            Timer timer = new Timer();
            TimerTask doAsynchronousTask = new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        public void run() {
                            try {
                                if (LocationService.infoR == null) {
                                    Log.d("LOCATION VALUE","null");
//                                    Toast.makeText(CameraRecorder.this, "Location value is null check code", Toast.LENGTH_LONG).show();
                                } else {
                                    pahoMqttClient.publishMessage(client, "" + LocationService.infoR, 1, Constants.PUBLISH_TOPIC);
                                    Log.d("MQTT2", "" + LocationService.infoR);
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    });
                }
            };
            timer.schedule(doAsynchronousTask, 0, 1000); // 30 secs
        } else {
            Toast.makeText(CameraRecorder.this, "No internet available", Toast.LENGTH_LONG).show();
        }

// Shutdown Code
        final Button shutdown = findViewById(R.id.shutdown);
        shutdown.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                shutDown();
            }
        });

//Hotspot Button
        Button hotspot = findViewById(R.id.hotspot);
        hotspot.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onClick(View v) {
                startHotspot();
            }
        });

//Mount Button
        Button mount = findViewById(R.id.mount);
        mount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mountStorage();
            }
        });

//Grant Button
        Button grant = findViewById(R.id.grant);
        grant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grantPower();
            }
        });

//Revoke Button
        Button revoke = findViewById(R.id.revoke);
        revoke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revokePower();
            }
        });

//Reboot Button
        Button reboot = findViewById(R.id.reboot);
        reboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rebootSystem();
            }
        });


//MQtt Subsribe Handler

        subscribe_handler = new Handler();
        subscribe_runnable = new Runnable() {
            public void run() {
                Subscribe();
                subscribe_handler.postDelayed(subscribe_runnable, 10000);
            }
        };
        subscribe_handler.postDelayed(subscribe_runnable, 15000);


//        finish();
    }



//Shutdown
    public void shutDown(){
        try {
            Process proc = Runtime.getRuntime()
                    .exec(new String[]{"su", "-c", "reboot -p"});
            proc.waitFor();
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(CameraRecorder.this, "" + ex, Toast.LENGTH_LONG).show();
        }
    }

//Reboot

public static void rebootSystem(){
    try {
        Runtime rt = Runtime.getRuntime();
        String[] commands = {"su","-c","reboot"};
        Process proc = rt.exec(commands);
        proc.waitFor();
    } catch (Exception ex) {
        ex.printStackTrace();
        Log.d("Power Status:","Rebooting system");
    }
}

//Mount
    public static void mountStorage(){
        try {
            Runtime rt = Runtime.getRuntime();
            String[] commands = {"su","-c","mount -o remount,rw /system"};
            Process proc = rt.exec(commands);
            proc.waitFor();
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
    public static void grantPower(){
        try {
            Runtime rt = Runtime.getRuntime();
            String[] commands = {"su","-c","sed -i  '/key 116   POWER/ s/# *//' /system/usr/keylayout/Generic.kl"};
            Process proc = rt.exec(commands);
            proc.waitFor();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d("Power Status","Granted Power");
        }
    }

//Revoke
    public void revokePower(){
        try {
            Runtime rt = Runtime.getRuntime();
            String[] commands = {"su","-c","sed  -i '/key 116   POWER/s/^/#/g' /system/usr/keylayout/Generic.kl"};
            Process proc = rt.exec(commands);
            proc.waitFor();
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(CameraRecorder.this, "" + ex, Toast.LENGTH_LONG).show();
        }
    }



//Server Code
    public  void startServer(){
        new Server().start();
        Log.d("SERVER TEST:","Turning on server now");
    }


//Function to start camera

    public void startCamera(){
        Intent intent = new Intent(CameraRecorder.this, RecorderService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(intent);
        finish();
    }

//Function to stop Camera

    public void stopCamera(){
        stopService(new Intent(CameraRecorder.this, RecorderService.class));
        Intent intent = new Intent(CameraRecorder.this, CameraRecorder.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // You need this if starting
        //  the activity from a service
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        startActivity(intent);
    }

//Hotspot

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startHotspot(){
        if (flag) {
            turnOnHotspot();
            flag = false;
        } else {
            turnOffHotspot();
            flag = true;
        }
    }

//Check network

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

//Subscription
    public void Subscribe(){
        String topic = "test/topic";
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
    private void turnOnHotspot() {
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
    private void turnOffHotspot() {
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
            Toast.makeText(this, "Location Service Denied", Toast.LENGTH_LONG).show();
        }
    }
}
