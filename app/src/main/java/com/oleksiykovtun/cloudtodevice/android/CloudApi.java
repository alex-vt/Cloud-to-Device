package com.oleksiykovtun.cloudtodevice.android;

import android.content.Context;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

/**
 * CloudApi
 */
public class CloudApi {

    public static void authenticate(Context context) {
        if (Preferences.get(context, Preferences.TOKEN).isEmpty()) {
            get(context).getSession().startOAuth2Authentication(context);
        }
    }

    public static boolean isAuthenticationSuccessful(Context context) {
        return get(context).getSession().authenticationSuccessful();
    }

    public static DropboxAPI<AndroidAuthSession> get(Context context) {
        AppKeyPair appKeys = new AppKeyPair(Preferences.get(context, Preferences.APP_KEY),
                Preferences.get(context, Preferences.APP_SECRET));
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        session.setOAuth2AccessToken(Preferences.get(context, Preferences.TOKEN));
        return new DropboxAPI<>(session);
    }

    public static void finalizeAuthentication(Context context) {
        try {
            AndroidAuthSession session = get(context).getSession();
            session.finishAuthentication();
            Preferences.set(context, Preferences.TOKEN, session.getOAuth2AccessToken());
        } catch (IllegalStateException e) {
            // todo string to xml
            Preferences.processException(context, "Authentication failed: ", e);
        }
    }

}
