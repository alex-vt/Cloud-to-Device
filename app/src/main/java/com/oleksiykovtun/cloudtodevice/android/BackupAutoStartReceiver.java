package com.oleksiykovtun.cloudtodevice.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * BackupServiceAutoStart
 */
public class BackupAutoStartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Preferences.getBoolean(context, Preferences.AUTO_START)) {
            int backupIntervalMilliseconds = 1000 * Preferences.getInt(
                    context.getApplicationContext(), Preferences.BACKUP_INTERVAL_SECONDS);
            BackupScheduler.startRepeated(context, backupIntervalMilliseconds);
        }
    }

}
