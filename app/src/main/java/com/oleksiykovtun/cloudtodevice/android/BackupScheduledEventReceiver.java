package com.oleksiykovtun.cloudtodevice.android;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * BackupScheduledEventReceiver
 */
public class BackupScheduledEventReceiver extends WakefulBroadcastReceiver {

    private static BackupAsyncTask backupAsyncTask;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (backupAsyncTask == null || backupAsyncTask.isCancelled()) {
            backupAsyncTask = new BackupAsyncTask(context);
            backupAsyncTask.execute();
        }
    }

    public static void abortReceiving() {
        if (backupAsyncTask != null) {
            backupAsyncTask.cancel(true);
        }
    }

}