package com.example.usuario.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.usuario.R;
import com.example.usuario.channel.NotificationHelper;
import com.example.usuario.models.Message;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Map;

public class MyFirebaseMessagingClient extends FirebaseMessagingService {

    private static final int NOTIFICATION_CODE = 100;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String body = data.get("body");
        //String idNotification = data.get("idNotification");


        if (title != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                 if (title.contains("EMERGENCIA CANCELADO")) {
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(2);
                    showNotificationApiOreo(title, body);
                }
                 else if (title.equals("MENSAJE")){
                     getImageReceiver(data);
                 }
                else {
                    showNotificationApiOreo(title, body);
                }
            }
            else {

                if (title.contains("EMERGENCIA CANCELADO")) {
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(2);
                    showNotification(title, body);
                }
                else {
                    showNotification(title, body);
                }
            }
        }
    }


    private void getImageReceiver(final Map<String, String> data){
        final String imageReceiver = data.get("imageReceiver");
        Log.d("NOTIFICACION", "imageReceiver: " + imageReceiver);
        if (imageReceiver == null) {
            showNotificationMessage(data, null);
            return;
        }
        if (imageReceiver.equals("")) {
            showNotificationMessage(data, null);
            return;
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Picasso.with(getApplicationContext()).load(imageReceiver).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        showNotificationMessage(data, bitmap);

                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        showNotificationMessage(data, null);
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
            }
        });

    }



    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void showNotificationMessage(Map<String, String> data, Bitmap bitmapReceiver) {
       // String idNotification = data.get("idNotification");
        String usernameSender = data.get("usernameSender");
        String usernameReceiver = data.get("usernameReceiver");
        String messagesJSON = data.get("messagesJSON");

        Gson gson = new Gson();
        Message[] messages = gson.fromJson(messagesJSON, Message[].class);

        NotificationHelper helper = new NotificationHelper(getBaseContext());

        NotificationCompat.Builder builder = helper.getNotificationMessage(messages, usernameReceiver, usernameSender, bitmapReceiver);
     //   int id = Integer.parseInt(idNotification);
      //  Log.d("NOTIFICACION", "ID: " + id);
        Log.d("NOTIFICACION", "usernameSender: " + usernameSender);
        Log.d("NOTIFICACION", "usernameReceiver: " + usernameReceiver);
        helper.getManager().notify(2, builder.build());



    }


    private void showNotification(String title, String body) {
        PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(), PendingIntent.FLAG_ONE_SHOT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder = notificationHelper.getNotificationOldAPI(title, body, intent, sound);
        notificationHelper.getManager().notify(1, builder.build());
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationApiOreo(String title, String body) {
        PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(), PendingIntent.FLAG_ONE_SHOT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        Notification.Builder builder = notificationHelper.getNotification(title, body, intent, sound);
        notificationHelper.getManager().notify(1, builder.build());
    }


}
