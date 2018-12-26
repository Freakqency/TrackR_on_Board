package iqube.surya.testapplication;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.*;
import java.net.*;

class UDPClient
{
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    UDPClient()
    {

        DatagramSocket clientSocket = null;
        try {
            clientSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        InetAddress IPAddress = null;
        try {
            IPAddress = InetAddress.getByName("192.168.43.64");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        byte[] sendData;
        String sentence = CameraRecorder.ip;
        sendData = sentence.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
        try {
            clientSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientSocket.close();
    }
}