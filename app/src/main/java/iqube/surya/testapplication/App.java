package iqube.surya.testapplication;

import android.app.Application;
import com.bugfender.sdk.Bugfender;


public class App extends Application {

    @Override public void onCreate() {
        super.onCreate();

        // Initialize Bugfender
        Bugfender.init(this, "jPdQ4FTZn4UgMbHmOCXTV2lWayHbisWf", BuildConfig.DEBUG);
        Bugfender.enableLogcatLogging();
        Bugfender.enableUIEventLogging(this);
    }
}