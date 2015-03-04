package com.oleksiykovtun.cloudtodevice.android;

import android.content.Context;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

import static android.content.Context.MODE_MULTI_PROCESS;

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

    private static void setReliably(Context context, String tag, String value) {
        ACCESS_LOCK.lock();
        context.getSharedPreferences(EMPTY, MODE_MULTI_PROCESS).edit().putString(tag, value)
                .apply();
        ACCESS_LOCK.unlock();
    }

    public static int getInt(Context context, String tag) {
        return Integer.parseInt(get(context, tag));
    }

    public static String get(Context context, String tag) {
        ACCESS_LOCK.lock();
        String value = context.getSharedPreferences(EMPTY, MODE_MULTI_PROCESS).getString(tag, EMPTY);
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
