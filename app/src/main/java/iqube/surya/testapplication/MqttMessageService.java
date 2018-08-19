package iqube.surya.testapplication;


import android.annotation.SuppressLint;
import android.app.Service;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttMessageService extends Service {



    private static final String TAG = "MqttMessageService";

    public MqttMessageService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        PahoMqttClient pahoMqttClient = new PahoMqttClient();
        MqttAndroidClient mqttAndroidClient = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.MQTT_BROKER_URL, Constants.CLIENT_ID);

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @SuppressLint("NewApi")
            @Override
            public void messageArrived(String topic, MqttMessage message) {
                CameraRecorder cameraRecorder = new CameraRecorder();
                switch (message.toString()) {
                    case "Shutdown":
                        cameraRecorder.shutDown();
                        break;
                    case "Start Hotspot":
                        cameraRecorder.turnOnHotspot();
                        break;
                    case "Mount":
                        cameraRecorder.mountStorage();
                        break;
                    case "Grant":
                        cameraRecorder.grantPower();
                        break;
                    case "Revoke":
                        cameraRecorder.revokePower();
                        break;
                    case "Reboot":
                        cameraRecorder.rebootSystem();
                        break;
                    case "Lock":
                        cameraRecorder.lockScreen();
                        break;
                    case "Stop Hotspot":
                        cameraRecorder.turnOffHotspot();
                        break;

                    case "Stop Server":
                        cameraRecorder.turnOffHotspot();
                        break;

                    case "Start Camera":
                        cameraRecorder.startCamera();
                        break;

                    case "Stop Camera":
                        cameraRecorder.stopCamera();
                        break;

                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }


}
