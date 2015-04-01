package com.oleksiykovtun.cloudtodevice.android;

import android.content.Context;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
    public static final String AUTO_START = "auto_start";
    public static final String EXCLUDED_EXTENSIONS = "excludedExtensions";
    public static final String EXCLUDED_PATHS = "excludedPaths";
    public static final String BACKUP_INTERVAL_SECONDS = "backupIntervalSeconds";
    public static final String UI_UPDATE_INTERVAL_MILLISECONDS = "uiUpdateIntervalMilliseconds";

    public static void processException(Context context, String message, Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        message = message + "\n" + e.getMessage() + "\n" + stringWriter.toString();
        Log.e(LOG, message);
        prependLog(context, message);
    }

    public static void prependLog(Context context, String value) {
        final String dateFormat = "HH:mm:ss  ";
        String newWholeValue = new SimpleDateFormat(dateFormat).format(new Date())
                + value + "\n" + PreferencesReaderWriter.read(context, LOG);
        // todo remove debug
        if (newWholeValue.length() > 500) {
            newWholeValue = newWholeValue.substring(0, 200);
        }
        PreferencesReaderWriter.write(context, LOG, newWholeValue);
        Log.d(LOG, "Writing shared preference: " + LOG + "\n" + value);
        set(context, STATUS, value);
    }

    public static void clear(Context context, String tag) {
        PreferencesReaderWriter.write(context, tag, "");
    }

    public static void set(Context context, String tag, int value) {
        set(context, tag, "" + value);
    }

    public static void set(Context context, String tag, boolean value) {
        set(context, tag, "" + value);
    }

    public static void set(Context context, String tag, String value) {
        Log.d(tag, "Writing shared preference: " + tag + "\n" + value);
        PreferencesReaderWriter.write(context, tag, value);
    }

    public static int getInt(Context context, String tag) {
        return Integer.parseInt(get(context, tag));
    }

    public static boolean getBoolean(Context context, String tag) {
        return get(context, tag).equals("" + true);
    }

    public static List<String> getStringList(Context context, String tag, String splitRegex) {
        return Arrays.asList(get(context, tag).split(splitRegex));
    }

    public static String get(Context context, String tag) {
        return PreferencesReaderWriter.read(context, tag);
    }

}
