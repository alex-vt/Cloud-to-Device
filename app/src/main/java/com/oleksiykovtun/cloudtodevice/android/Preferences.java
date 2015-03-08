package com.oleksiykovtun.cloudtodevice.android;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Preferences
 */
public class Preferences {

    public static final String STATUS = "status";
    public static final String LOG = "log";
    public static final String APP_KEY = "APP_KEY";
    public static final String APP_SECRET = "APP_SECRET";
    public static final String TOKEN = "token";
    public static final String CURSOR = "cursor";
    public static final String EXCLUDED_EXTENSIONS = "excludedExtensions";
    public static final String EXCLUDED_PATHS = "excludedPaths";
    public static final String BACKUP_INTERVAL_SECONDS = "backupIntervalSeconds";
    public static final String UI_UPDATE_INTERVAL_MILLISECONDS = "uiUpdateIntervalMilliseconds";

    private static final String EMPTY = "";
    private static final ReentrantLock ACCESS_LOCK = new ReentrantLock();

    public static void prependLog(Context context, String value) {
        // todo string to xml
        String newWholeValue = new SimpleDateFormat("HH:mm:ss  ").format(new Date())
                + value + "\n" + get(context, LOG);
        setReliably(context, LOG, newWholeValue);
        // todo string to xml
        Log.d(LOG, "Writing shared preference: " + LOG + "\n" + value);
        set(context, STATUS, value);
    }

    public static void reset(Context context, String tag) {
        setReliably(context, tag, EMPTY);
    }

    public static void set(Context context, String tag, String value) {
        // todo string to xml
        Log.d(tag, "Writing shared preference: " + tag + "\n" + value);
        setReliably(context, tag, value);
    }

    private static void setReliably(final Context context, final String tag, final String value) {
        new AsyncTask<String, String, String> () {

            protected String doInBackground(String... inputs) {
                ACCESS_LOCK.lock();
                FileOutputStream outputStream;
                try {
                    outputStream = context.openFileOutput(tag, Context.MODE_PRIVATE);
                    outputStream.write(value.getBytes());
                    outputStream.close();
                } catch (Exception e) {
                    // todo string to constant
                    Log.e(LOG, "SETTING SAVING FAILED: ", e);
                }
                ACCESS_LOCK.unlock();
                return EMPTY;
            }

        }.doInBackground();
    }

    public static int getInt(Context context, String tag) {
        return Integer.parseInt(get(context, tag));
    }

    public static List<String> getStringList(Context context, String tag, String splitRegex) {
        return Arrays.asList(get(context, tag).split(splitRegex));
    }

    public static String get(Context context, String tag) {
        ACCESS_LOCK.lock();
        FileInputStream inputStream;
        String value = EMPTY;
        try {
            inputStream = context.openFileInput(tag);
            int content;
            while ((content = inputStream.read()) != -1) {
                value = value + ((char) content);
            }
            inputStream.close();
        } catch (FileNotFoundException e) {
            value = EMPTY;
        } catch (Exception e) {
            // todo string to constant
            Log.e(LOG, "SETTING LOADING FAILED: ", e);
        }
        ACCESS_LOCK.unlock();
        return value;
    }

    public static void processException(Context context, String message, Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        // todo string to constant
        message = message + "\n" + e.getMessage() + "\n" + stringWriter.toString();
        Log.e(LOG, message);
        prependLog(context, message);
    }

}
