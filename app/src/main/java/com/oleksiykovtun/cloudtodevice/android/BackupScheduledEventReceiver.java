package com.oleksiykovtun.cloudtodevice.android;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by alx on 2015-03-17.
 */
public class BackupScheduledEventReceiver extends WakefulBroadcastReceiver {

    private BackupAsyncTask backupAsyncTask = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (backupAsyncTask == null || backupAsyncTask.isCancelled()) {
            backupAsyncTask = null;
            backupAsyncTask = new BackupAsyncTask(context);
            backupAsyncTask.execute();
        }
    }
}