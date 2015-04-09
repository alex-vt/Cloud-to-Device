package com.oleksiykovtun.cloudtodevice.android;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

/**
 * BackupScheduler
 */
public class BackupScheduler {

    private static final int NOTIFICATION_ID = 1;

    public static void startRepeated(Context context, int repeatTimeMilliseconds) {
        getAlarmManager(context).setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                repeatTimeMilliseconds, getPendingIntent(context));
        showNotification(context);
    }

    public static void stop(Context context) {
        BackupScheduledEventReceiver.abortReceiving();
        getAlarmManager(context).cancel(getPendingIntent(context));
        hideNotification(context);
    }

    public static boolean isScheduled(Context context) {
        return Preferences.getBoolean(context, Preferences.AUTO_START);
    }

    private static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, BackupScheduledEventReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    private static AlarmManager getAlarmManager(Context context) {
        return (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    }

    private static void showNotification(Context context) {
        NotificationCompat.Builder mBuilder =  new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.logo).setContentTitle(context.getText(R.string.app_name));
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(new Intent(context, MainActivity.class));
        PendingIntent resultPendingIntent
                = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        getNotificationManager(context).notify(NOTIFICATION_ID, mBuilder.build());
    }

    private static void hideNotification(Context context) {
        getNotificationManager(context).cancel(NOTIFICATION_ID);
    }

    private static NotificationManager getNotificationManager(Context context) {
        return (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

}
