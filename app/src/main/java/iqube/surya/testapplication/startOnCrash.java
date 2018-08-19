package iqube.surya.testapplication;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;


@SuppressLint("Registered")
public class startOnCrash extends Application
{
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    @SuppressLint("StaticFieldLeak")
    public static startOnCrash instace;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        instace = this;
    }

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    public static startOnCrash getIntance() {
        return instace;
    }
}