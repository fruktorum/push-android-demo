package com.devinotele.devinosdk.sdk;


import android.content.Context;


class UnsubscribeLocationsUseCase extends BaseUC {

    private DevinoLogsCallback logsCallback;

    UnsubscribeLocationsUseCase(HelpersPackage hp, DevinoLogsCallback callback) {
        super(hp);
        logsCallback = callback;
    }

    public void run(Context context) {
        sharedPrefsHelper.saveData(SharedPrefsHelper.KEY_GPS_SUBSCRIPTION_ACTIVE, false);
        DevinoLocationReceiver.cancelAlarm(context);
    }

}
