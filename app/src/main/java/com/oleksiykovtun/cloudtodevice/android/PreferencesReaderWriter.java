package com.oleksiykovtun.cloudtodevice.android;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Preferences reader writer
 */
public class PreferencesReaderWriter {

    public static final String TAG = "log";

    private static final ReentrantLock ACCESS_LOCK = new ReentrantLock();

    public static void write(final Context context, final String tag, final String value) {
        new AsyncTask<String, String, String> () {

            protected String doInBackground(String... inputs) {
                ACCESS_LOCK.lock();
                FileOutputStream outputStream;
                try {
                    outputStream = context.openFileOutput(tag, Context.MODE_PRIVATE);
                    outputStream.write(value.getBytes());
                    outputStream.close();
                } catch (Exception e) {
                    Log.e(TAG, "Preferences writing failed", e);
                }
                ACCESS_LOCK.unlock();
                return "";
            }

        }.doInBackground();
    }

    public static String read(Context context, String tag) {
        ACCESS_LOCK.lock();
        FileInputStream inputStream;
        String value = "";
        try {
            inputStream = context.openFileInput(tag);
            int content;
            while ((content = inputStream.read()) != -1) {
                value = value + ((char) content);
            }
            inputStream.close();
        } catch (FileNotFoundException e) {
            value = "";
        } catch (Exception e) {
            Log.e(TAG, "Preferences reading failed", e);
        }
        ACCESS_LOCK.unlock();
        return value;
    }

}
