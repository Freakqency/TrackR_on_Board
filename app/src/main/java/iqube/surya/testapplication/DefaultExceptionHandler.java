package iqube.surya.testapplication;


import java.lang.Thread.UncaughtExceptionHandler;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;




public class DefaultExceptionHandler implements UncaughtExceptionHandler {


    private Activity activity;

    DefaultExceptionHandler(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        Intent intent = new Intent(activity, CameraRecorder.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                startOnCrash.getIntance().getBaseContext(), 0, intent, intent.getFlags());

        //Following code will restart your application after 2 seconds
        AlarmManager mgr = (AlarmManager) startOnCrash.getIntance().getBaseContext()
                .getSystemService(Context.ALARM_SERVICE);
        assert mgr != null;
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
                pendingIntent);

        //This will finish your activity manually
        activity.finish();

        //This will stop your application and take out from it.
        System.exit(2);

    }
}