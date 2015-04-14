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

    private static final String FILE_COUNT_FORMAT = "%6d";

    private Context context;
    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;
    private String cloudChangesCursor = null;

    public BackupAsyncTask(Context context) {
        this.context = context;
    }

    public boolean isRunningNow() {
        return (getStatus() == Status.RUNNING) && (! isCancelled());
    }


    @Override
    protected void onPreExecute() {
        acquireLocks();
    }

    @Override
    protected String doInBackground(String... urls) {
        Preferences.prependLog(context, getText(R.string.message_started_backup));
        waitForNetwork(Preferences.getInt(context, Preferences.NETWORK_CHECK_INTERVAL_MILLIS),
                    Preferences.getInt(context, Preferences.NETWORK_TIMEOUT_MILLIS));
        cloudChangesCursor = Preferences.get(context, Preferences.CURSOR);

        String backupDirectoryPath = getBackupDirectoryPath();
        FileEntry[] newCloudFileEntries = getNewFileEntriesFromCloud();
        int counter = writeNewFilesToBackupDirectory(backupDirectoryPath, newCloudFileEntries);

        finishBackupTask(newCloudFileEntries, counter);
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        releaseLocks();
    }

    @Override
    protected void onCancelled(String result) {
        releaseLocks();
    }


    private void acquireLocks() {
        wakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wakeLock.acquire();
        wifiLock = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "");
        wifiLock.acquire();
    }

    private void waitForNetwork(int checkIntervalMillis, int timeoutMillis) {
        try {
            for (int i = 0; i < timeoutMillis; i += checkIntervalMillis) {
                NetworkInfo activeNetworkInfo = ((ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                    return;
                }
                Thread.sleep(checkIntervalMillis);
            }
            throw new Exception(getText(R.string.error_connect_internet));
        }
        catch (Exception e) {
            cancel(true);
            Preferences.processException(context, getText(R.string.error_connect_internet), e);
        }
    }

    private String getBackupDirectoryPath() {
        return Environment.getExternalStorageDirectory() + "/"
                + getText(R.string.path_backup_directory_in_external_storage);
    }

    private FileEntry[] getNewFileEntriesFromCloud() {
        FileEntry[] newFileEntriesFromCloud = new FileEntry[] { };
        try {
            Map.Entry<FileEntry[], String> cloudChangesMapEntry
                    = Files.getCloudChanges(CloudApi.get(context), cloudChangesCursor,
                    Preferences.getStringList(context, Preferences.EXCLUDED_PATTERNS,
                            Files.PATTERNS_DELIMITER_REGEX));
            newFileEntriesFromCloud = cloudChangesMapEntry.getKey();
            cloudChangesCursor = cloudChangesMapEntry.getValue();
        } catch (Exception e) {
            cancel(true);
            Preferences.processException(context, getText(R.string.error_analyzing_changes), e);
        }
        return newFileEntriesFromCloud;
    }

    private int writeNewFilesToBackupDirectory(String backupDirectoryPath,
                                               FileEntry[] newFileEntriesFromCloud) {
        int writtenFileCounter = 0;
        for (FileEntry fileEntry : newFileEntriesFromCloud) {
            if (isCancelled()) {
                break;
            }
            try {
                if (! Files.fileExists(backupDirectoryPath, fileEntry)) {
                    Preferences.set(context, Preferences.STATUS,
                            getText(R.string.message_processed) + " "
                                + String.format(FILE_COUNT_FORMAT, writtenFileCounter) + " "
                                + getText(R.string.message_files_of) + " "
                                + String.format(FILE_COUNT_FORMAT, newFileEntriesFromCloud.length) + "\n"
                                + getText(R.string.message_copying_from) + " "
                                + CloudApi.getName() + "\n"
                                + fileEntry.getPath() + "\n"
                                + getText(R.string.message_copying_to) + " "
                                + backupDirectoryPath + "\n"
                                + "/" + Files.getLocalPath(fileEntry));
                    Files.writeEntryToFile(CloudApi.get(context), fileEntry, backupDirectoryPath,
                            Files.getLocalPath(fileEntry));
                }
                writtenFileCounter++;
            } catch (Exception e) {
                cancel(true);
                Preferences.processException(context, getText(R.string.error_writing_file), e);
            }
        }
        return writtenFileCounter;
    }

    private void finishBackupTask(FileEntry[] newFileEntriesFromCloud, int writtenFileCounter) {
        if (isCancelled()) {
            Preferences.prependLog(context, getText(R.string.message_aborted_saved) + " "
                    + String.format(FILE_COUNT_FORMAT, writtenFileCounter) + " "
                    + getText(R.string.message_files_of) + " "
                    + newFileEntriesFromCloud.length);
        } else {
            Preferences.set(context, Preferences.CURSOR, cloudChangesCursor);
            Preferences.prependLog(context, getText(R.string.message_processed) + " "
                    + writtenFileCounter + " "
                    + getText(R.string.message_files_ready));
        }
    }

    private void releaseLocks() {
        wifiLock.release();
        wakeLock.release();
    }


    private String getText(int stringResourceId) {
        return context.getResources().getString(stringResourceId);
    }

}