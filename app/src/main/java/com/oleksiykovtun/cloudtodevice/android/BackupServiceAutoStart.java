package com.oleksiykovtun.cloudtodevice.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * BackupServiceAutoStart
 */
public class BackupServiceAutoStart extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Preferences.get(context, Preferences.AUTO_START).equals(Preferences.TRUE)) {
            Intent service = new Intent(context, BackupService.class);
            context.startService(service);
        }
    }

}
