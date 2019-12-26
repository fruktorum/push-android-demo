package com.devinotele.devinosdk.sdk;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * This class is used to send geo periodically, when setup with library
 */
public class DevinoLocationReceiver extends BroadcastReceiver {

    static final String CUSTOM_INTENT = "com.devinotele.devinosdk.ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        DevinoLocationIntentService.enqueueWork(context, intent);
    }

    static void cancelAlarm(Context context) {
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarms.cancel(getPendingIntent(context));
    }

    static void setAlarm(Context context, long intervalMinutes) {
        cancelAlarm(context);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long delay = (1000 * 60 * intervalMinutes);
        long when = System.currentTimeMillis();
        if (intervalMinutes > 0) when += delay;

        int sdkInt = Build.VERSION.SDK_INT;
        PendingIntent intent = getPendingIntent(context);

        if (sdkInt < Build.VERSION_CODES.KITKAT) alarm.set(AlarmManager.RTC_WAKEUP, when, intent);
        else if (sdkInt < Build.VERSION_CODES.M) alarm.setExact(AlarmManager.RTC_WAKEUP, when, intent);
        else alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, intent);

    }

    static PendingIntent getPendingIntent(Context context) {
        Intent alarmIntent = new Intent(context, DevinoLocationReceiver.class);
        alarmIntent.setAction(CUSTOM_INTENT);
        return PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

}