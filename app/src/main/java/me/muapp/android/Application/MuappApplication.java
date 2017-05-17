package me.muapp.android.Application;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.zplesac.connectionbuddy.ConnectionBuddy;
import com.zplesac.connectionbuddy.ConnectionBuddyConfiguration;

import me.muapp.android.BuildConfig;

/**
 * Created by rulo on 21/03/17.
 */

public class MuappApplication extends Application {
    private static final String TAG = "MuappApplication";
    private static MixpanelAPI mixpanelAPI;
    private static final String projectTokenStagging = "c344cb123c0c544f0c617dc0185c7a5b";
    private static final String projectTokenProduction = "c15c6fe6ec4a9e0505dc0e4623b57";

    public static MixpanelAPI getMixpanelAPI() {
        return mixpanelAPI;
    }

    public static String DATABASE_REFERENCE = "devDB";

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        if (!BuildConfig.DEBUG) {
            DATABASE_REFERENCE = "prodDB";
        }
        Log.wtf("DATABASE_REFERENCE", DATABASE_REFERENCE);

        //MIXPANEL TOKEN
        mixpanelAPI = MixpanelAPI.getInstance(this, projectTokenStagging);
        ConnectionBuddyConfiguration networkInspectorConfiguration =
                new ConnectionBuddyConfiguration.Builder(this)
                        .registerForMobileNetworkChanges(true)
                        .registerForWiFiChanges(true)
                        .setNotifyImmediately(true)
                        .build();
        ConnectionBuddy.getInstance().init(networkInspectorConfiguration);
        this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                mixpanelAPI.timeEvent("App Close");
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                mixpanelAPI.track("App Close");
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }
}
