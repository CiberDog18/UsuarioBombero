package com.example.usuario.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;

import com.example.usuario.providers.AuthProvider;
import com.example.usuario.providers.ClientProvider;

import java.util.List;

public class AppBackgroundHelper {

    public static void online(Context context, boolean status) {
        ClientProvider clientProvider = new ClientProvider();
        AuthProvider authProvider = new AuthProvider();

        if (authProvider.getId() != null) {

            if (isApplicationSentToBackground(context)) {
                clientProvider.updateOnline(authProvider.getId(), status);
            }
            else if (status) {
                clientProvider.updateOnline(authProvider.getId(), status);
            }
        }

    }
    public static boolean isApplicationSentToBackground(final Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }
}
