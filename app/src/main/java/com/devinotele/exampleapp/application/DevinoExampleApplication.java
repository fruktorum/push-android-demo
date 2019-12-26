package com.devinotele.exampleapp.application;

import android.app.Application;

import com.devinotele.devinosdk.sdk.DevinoSdk;
import com.devinotele.exampleapp.BuildConfig;
import com.google.firebase.iid.FirebaseInstanceId;

public class DevinoExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseInstanceId fb = FirebaseInstanceId.getInstance();
        String appId = BuildConfig.DEVINO_APP_ID;

        DevinoSdk.Builder builder = new DevinoSdk.Builder(this, BuildConfig.DEVINO_API_KEY, appId, fb);
        builder.build();

    }
}
