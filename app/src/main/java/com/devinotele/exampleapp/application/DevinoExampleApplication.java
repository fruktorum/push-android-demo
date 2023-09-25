package com.devinotele.exampleapp.application;

import android.app.Application;
import android.util.Log;
import com.devinotele.devinosdk.sdk.DevinoSdk;
import com.devinotele.exampleapp.BuildConfig;
import com.devinotele.exampleapp.R;
import com.google.firebase.messaging.FirebaseMessaging;
import io.reactivex.plugins.RxJavaPlugins;

public class DevinoExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseMessaging fb = FirebaseMessaging.getInstance();
        String appId = BuildConfig.DEVINO_APP_ID;
        String appVersion = BuildConfig.VERSION_NAME;

        DevinoSdk.Builder builder = new DevinoSdk.Builder(this, BuildConfig.DEVINO_API_KEY, appId, appVersion, fb);
        builder.build();

        DevinoSdk.getInstance().setDefaultNotificationIcon(R.drawable.ic_android_black_24dp);
        DevinoSdk.getInstance().setDefaultNotificationIconColor(0x00FF00);

        RxJavaPlugins.setErrorHandler(e -> {
            Log.d(
                    getString(R.string.tag),
                    getString(R.string.error_rxJavaPlugins)
                            + " "
                            + e.getMessage());
        });
    }
}