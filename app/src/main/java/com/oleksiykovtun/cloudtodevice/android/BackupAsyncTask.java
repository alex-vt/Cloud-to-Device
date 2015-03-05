package com.oleksiykovtun.cloudtodevice.android;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import java.util.Arrays;
import java.util.Map;

/**
 * BackupAsyncTask
 */
public class BackupAsyncTask extends AsyncTask<String, String, String> {

    private Context context;

    public BackupAsyncTask(Context context) {
        this.context = context;
    }

    protected String doInBackground(String... urls) {
        // todo string to xml
        String backupDirectoryPath = Environment.getExternalStorageDirectory() + "/" +
                "Cloud-to-Device" + "/";
        // todo string to xml
        Preferences.prependLog(context, "Started backup...");
        String cursor = Preferences.get(context, Preferences.CURSOR);
        FileEntry[] newCloudFileEntries = new FileEntry[] { };
        try {
            Map.Entry<FileEntry[], String> cloudChanges
                    = Files.getCloudChanges(CloudApi.get(context), cursor, Arrays.asList(
                    Preferences.get(context, Preferences.EXCLUDED_EXTENSIONS).split(" ")));
            newCloudFileEntries = cloudChanges.getKey();
            cursor = cloudChanges.getValue();
        } catch (Exception e) {
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
        cancel(true);
        // todo string to xml
        return "";
    }

}