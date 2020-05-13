package com.example.bihar.controller;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.example.bihar.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Clase encargada de gestionar el servicio de mensajeria con Firebase
 */
public class ServicioFirebase extends FirebaseMessagingService {

    /**
     * Al recibir un nuevo token o una actualizacion del anterior se almacena en el SharedPreferences
     *
     * @param refreshedToken: el token
     */
    @Override
    public void onNewToken(String refreshedToken) {
        super.onNewToken(refreshedToken);
        Log.i("TOKEN", refreshedToken);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("token", refreshedToken);
        editor.apply();
    }

    /**
     * Gestion de las notificaciones recibidas desde Firebase.
     *
     * @param remoteMessage: el mensaje recibido
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getNotification() != null) {
            RemoteMessage.Notification notification = remoteMessage.getNotification();

            // SE CREA LA NOTIFICACION DEPENDIENDO DE LA VERSION DEL MOVIL
            NotificationCompat.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder = new NotificationCompat.Builder(getApplicationContext(), "BIHAR");
                builder.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_stat_call_white));
            } else {
                builder = new NotificationCompat.Builder(getApplicationContext());
                builder.setPriority(NotificationCompat.PRIORITY_HIGH);
            }

            // ESPECIFICACIONES DE LA NOTIFICACION
            builder.setContentTitle(notification.getTitle());
            builder.setContentText(notification.getBody());
            builder.setSmallIcon(R.drawable.ic_stat_call_white);
            builder.setAutoCancel(true);
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(notification.getBody()));
            builder.setColor(getResources().getColor(R.color.verde));

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            //SE LANZA LA NOTIFICACION
            notificationManager.notify(6859, builder.build());
        }
    }
}
