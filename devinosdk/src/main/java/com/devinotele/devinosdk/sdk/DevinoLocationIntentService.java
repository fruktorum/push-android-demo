package com.devinotele.devinosdk.sdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * This class is used to send geo periodically, when setup with library
 */
public class DevinoLocationIntentService extends JobIntentService {

    private static final int JOB_ID = 1000;

    static void enqueueWork(Context ctx, Intent intent) {
        enqueueWork(ctx, DevinoLocationIntentService.class, JOB_ID, intent);
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        DevinoLocationHelper devinoLocationHelper = new DevinoLocationHelper(getApplicationContext());
        devinoLocationHelper.getNewLocation()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        location -> DevinoSdk.getInstance().sendGeo(location.getLatitude(), location.getLongitude()),
                        throwable -> {
                            throwable.printStackTrace();
                            DevinoSdk.getInstance().sendEvent("Geo Error: " + throwable.getMessage(), new HashMap<String, Object>() {{
                                put("Message", throwable.getMessage());
                            }});
                        });

        SharedPrefsHelper helper = new SharedPrefsHelper(getApplicationContext().getSharedPreferences("", Context.MODE_PRIVATE));
        int interval = helper.getInteger(SharedPrefsHelper.KEY_GPS_INTERVAL);
        DevinoLocationReceiver.setAlarm(getApplicationContext(), interval);
        stopSelf();
    }

}