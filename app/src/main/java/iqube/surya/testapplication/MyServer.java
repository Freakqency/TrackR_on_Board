package iqube.surya.testapplication;

import android.util.Log;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;
import okhttp3.Response;

public class MyServer extends NanoHTTPD {
    private final static int PORT = 1337;
    public static String rf_val = "";
    public static String card_flag = "";


    public MyServer() throws IOException {
        super(PORT);
        start();
        System.out.println("\nRunning! Point your browers to http://localhost:1337/ \n");
    }

    @Override
    public Response serve(IHTTPSession session) {
        String msg = "<html><body><h1>Success</h1>\n";
        String errmsg = "<html><body><h1>Invalid lenght</h1>\n";
        msg += "<p>We serve " + session.getUri() + " !</p>";
        String rfid = session.getParms().get("rfid");
        if (rfid.length() >= 7 && rfid.length() <= 9) {
            rf_val=rfid;
            card_flag="True";
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            card_flag="False";
            return newFixedLengthResponse(msg + "</body></html>\n");

        } else
            return newFixedLengthResponse(errmsg + "</body></html>\n");


    }

}