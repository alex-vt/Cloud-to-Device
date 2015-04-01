package com.oleksiykovtun.cloudtodevice.android;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * BackupScheduler
 */
public class BackupScheduler {

    public static void startRepeated(Context context, int repeatTimeMilliseconds) {
        getAlarmManager(context).setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                repeatTimeMilliseconds, getPendingIntent(context));
    }

    public static void stop(Context context) {
        BackupScheduledEventReceiver.abortReceiving();
        getAlarmManager(context).cancel(getPendingIntent(context));
    }

    private static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, BackupScheduledEventReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    private static AlarmManager getAlarmManager(Context context) {
        return (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    }

}
