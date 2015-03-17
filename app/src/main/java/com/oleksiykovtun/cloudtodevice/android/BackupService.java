package com.oleksiykovtun.cloudtodevice.android;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

/**
 * BackupService
 */
@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class BackupService extends Service  {

    private NotificationManager notificationManager;
    private final int NOTIFICATION_ID = 1;

    private void showNotification() {
        NotificationCompat.Builder mBuilder =  new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.logo).setContentTitle(getText(R.string.app_name));
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(new Intent(this, MainActivity.class));
        PendingIntent resultPendingIntent
                = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public BackupService() {  }

    @Override
    public void onCreate() {
        // todo string to xml
        Preferences.prependLog(getApplicationContext(), "Service started.");
        // todo expose to AsyncTask
        showNotification();
        int backupIntervalMilliseconds = 1000 * Preferences.getInt(getApplicationContext(),
                Preferences.BACKUP_INTERVAL_SECONDS);
        BackupScheduler.startRepeated(this, backupIntervalMilliseconds);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // todo string to xml
        Preferences.prependLog(getApplicationContext(), "Service stopped.");
        BackupScheduler.stop(this);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}