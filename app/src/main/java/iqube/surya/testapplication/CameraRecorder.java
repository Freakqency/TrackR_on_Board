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
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

public class CameraRecorder extends Activity implements SurfaceHolder.Callback {

	private static final String TAG = CameraRecorder.class.getSimpleName();

	public static SurfaceView mSurfaceView;
	public static SurfaceHolder mSurfaceHolder;
	public static Camera mCamera;
    private MqttAndroidClient client;
    private PahoMqttClient pahoMqttClient;


    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mSurfaceView = findViewById(R.id.surfaceView1);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }else {
            startService(new Intent(CameraRecorder.this, LocationService.class));

            Button btnStart = (Button) findViewById(R.id.StartService);
            btnStart.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(CameraRecorder.this, RecorderService.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startService(intent);
                    finish();
                }
            });
            Button btnStop = findViewById(R.id.StopService);
            btnStop.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    stopService(new Intent(CameraRecorder.this, RecorderService.class));
                }
            });
        }
//MQTT Code

		if (isOnline() == true) {
			if (LocationService.infoR != null) {
				pahoMqttClient = new PahoMqttClient();
				client = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.MQTT_BROKER_URL, Constants.CLIENT_ID);
				final Handler handler = new Handler();
				Timer timer = new Timer();
				TimerTask doAsynchronousTask = new TimerTask() {
					@Override
					public void run() {
						handler.post(new Runnable() {
							public void run() {
								try {
									pahoMqttClient.publishMessage(client, "" + LocationService.infoR, 1, Constants.PUBLISH_TOPIC);
									Log.d("MQTT2", "" + LocationService.infoR);
								} catch (Exception e) {
								}
							}
						});
					}
				};
				timer.schedule(doAsynchronousTask, 0, 1000); // 30 secs
			}
            else{
                Toast.makeText(CameraRecorder.this,"Location Value is null",Toast.LENGTH_LONG).show();
            }
		}

		else {
			Toast.makeText(CameraRecorder.this,"No internet available",Toast.LENGTH_LONG).show();
		}

		// Shutdown Code
		Button shutdown = findViewById(R.id.shutdown);
		shutdown.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				try {
					Process proc = Runtime.getRuntime()
							.exec(new String[]{ "su", "-c", "reboot -p" });
					proc.waitFor();
				} catch (Exception ex) {
					ex.printStackTrace();
					Toast.makeText(CameraRecorder.this,""+ex,Toast.LENGTH_LONG).show();
				}
			}
		});

		//Hotspot On
        Button hotspot = findViewById(R.id.hotspot);

        hotspot.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Hotspot hotspot1 = new Hotspot();
                hotspot1.configApState(CameraRecorder.this);
//Through root
//                try {
//                    Process proc = Runtime.getRuntime()
//                            .exec(new String[]{ "su", "-c", "/system/bin/hostapd" });
//                    proc.waitFor();
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                    Toast.makeText(CameraRecorder.this,""+ex,Toast.LENGTH_LONG).show();
//                }
            }
        });
    }
//Check network
	public boolean isOnline() {
		Runtime runtime = Runtime.getRuntime();
		try {
			Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
			int     exitValue = ipProcess.waitFor();
			return (exitValue == 0);
		}
		catch (IOException e)          { e.printStackTrace(); }
		catch (InterruptedException e) { e.printStackTrace(); }

		return false;
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
		if(grantResults.length>0){
			startService(new Intent(this,LocationService.class));
		}else{
			Toast.makeText(this,"Location Service Denied",Toast.LENGTH_LONG).show();
		}

	}
}
