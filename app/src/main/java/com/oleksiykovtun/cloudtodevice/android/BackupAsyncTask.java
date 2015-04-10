package com.oleksiykovtun.cloudtodevice.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;

import java.util.Map;

/**
 * BackupAsyncTask
 */
public class BackupAsyncTask extends AsyncTask<String, String, String> {

    private Context context;
    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;

    public BackupAsyncTask(Context context) {
        this.context = context;
    }

    public boolean isRunningNow() {
        return (getStatus() == Status.RUNNING) && (! isCancelled());
    }

    @Override
    protected void onPreExecute() {
        wakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wakeLock.acquire();
        wifiLock = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "");
        wifiLock.acquire();
    }

    @Override
    protected String doInBackground(String... urls) {
        // todo string to xml
        String backupDirectoryPath = Environment.getExternalStorageDirectory() + "/" +
                "Cloud-to-Device" + "/";
        // todo string to xml
        Preferences.prependLog(context, "Started backup...");
        String cursor = Preferences.get(context, Preferences.CURSOR);
        FileEntry[] newCloudFileEntries = new FileEntry[] { };
        try {
            waitForNetwork(Preferences.getInt(context, Preferences.NETWORK_CHECK_INTERVAL_MILLIS),
                    Preferences.getInt(context, Preferences.NETWORK_TIMEOUT_MILLIS));
            Map.Entry<FileEntry[], String> cloudChanges = Files.getCloudChanges(
                    CloudApi.get(context), cursor,
                    Preferences.getStringList(context, Preferences.EXCLUDED_PATTERNS,
                            Files.PATTERNS_DELIMITER_REGEX));
            newCloudFileEntries = cloudChanges.getKey();
            cursor = cloudChanges.getValue();
        } catch (Exception e) {
            cancel(true);
            // todo string to xml
            Preferences.processException(context, "Error while analyzing change: ", e);
        }
        int counter = 0;
        for (FileEntry fileEntry : newCloudFileEntries) {
            if (isCancelled()) {
                break;
            }
            try {
                if (! Files.fileExists(backupDirectoryPath, fileEntry)) {
                    // todo string to xml
                    Preferences.set(context, Preferences.STATUS, "Processed "
                            + String.format("%6d", counter)
                            + " of " + String.format("%6d", newCloudFileEntries.length)
                            + "\nCopying from: Dropbox\n" + fileEntry.getPath() + "\nTo: "
                            + backupDirectoryPath + "\n" + Files.getLocalPath(fileEntry));
                    Files.writeEntryToFile(CloudApi.get(context), fileEntry, backupDirectoryPath,
                            Files.getLocalPath(fileEntry));
                }
                counter++;
            } catch (Exception e) {
                cancel(true);
                // todo string to xml
                Preferences.processException(context, "Error while writing file: ", e);
            }
        }
        if (isCancelled()) {
            // todo string to xml
            Preferences.prependLog(context, "Aborted backup. Saved " + String.format("%6d", counter)
                    + " files of " + newCloudFileEntries.length);
        } else {
            Preferences.set(context, Preferences.CURSOR, cursor);
            // todo string to xml
            Preferences.prependLog(context, "Processed " + counter + " files. Ready.");
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        wifiLock.release();
        wakeLock.release();
    }

    @Override
    protected void onCancelled(String result) {
        wifiLock.release();
        wakeLock.release();
    }

    private void waitForNetwork(int checkIntervalMillis, int timeoutMillis) throws Exception {
        for (int i = 0; i < timeoutMillis; i += checkIntervalMillis) {
            NetworkInfo activeNetworkInfo = ((ConnectivityManager)context
                    .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                return;
            }
            Thread.sleep(checkIntervalMillis);
        }
        // todo string to xml
        throw new Exception("Cannot connect to the internet");
    }

}