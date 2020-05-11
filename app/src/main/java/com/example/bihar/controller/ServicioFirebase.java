package com.example.bihar.controller;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.example.bihar.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class ServicioFirebase extends FirebaseMessagingService {

    @Override
    public void onNewToken(String refreshedToken) {
        super.onNewToken(refreshedToken);
        Log.i("TOKEN", refreshedToken);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("token", refreshedToken);
        editor.apply();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if(remoteMessage.getNotification() !=null){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            boolean notificacion = prefs.getBoolean("notificacion",true);
            Log.i("NOTI",notificacion+"k");
            if(notificacion){
                RemoteMessage.Notification notification = remoteMessage.getNotification();

                NotificationCompat.Builder builder;
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    builder = new NotificationCompat.Builder(getApplicationContext(),"Notas");
                    builder.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_launcher));
                }else{
                    builder = new NotificationCompat.Builder(getApplicationContext());
                    builder.setPriority(NotificationCompat.PRIORITY_HIGH);
                }

                builder.setContentTitle(notification.getTitle());
                builder.setContentText(notification.getBody());
                builder.setSmallIcon(R.drawable.ic_launcher);
                builder.setAutoCancel(true);
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(notification.getBody()));

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(6859, builder.build());
            }

        }
    }
}
