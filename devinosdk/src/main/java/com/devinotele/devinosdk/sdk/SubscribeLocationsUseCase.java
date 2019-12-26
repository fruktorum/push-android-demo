package com.devinotele.devinosdk.sdk;


import android.content.Context;


class SubscribeLocationsUseCase extends BaseUC {

    private DevinoLogsCallback logsCallback;

    SubscribeLocationsUseCase(HelpersPackage hp, DevinoLogsCallback callback) {
        super(hp);
        logsCallback = callback;
    }

    void run(Context context, int intervalMinutes) {
        sharedPrefsHelper.saveData(SharedPrefsHelper.KEY_GPS_SUBSCRIPTION_ACTIVE, true);
        sharedPrefsHelper.saveData(SharedPrefsHelper.KEY_GPS_INTERVAL, intervalMinutes);
        DevinoLocationReceiver.setAlarm(context, 0);
    }

}
