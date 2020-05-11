package com.example.bihar.controller;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import com.example.bihar.R;

public class GestorNotificaciones {

    private static GestorNotificaciones gestorNotificaciones;
    private static Context context;
    private static NotificationManager notificationManager;
    private GestorNotificaciones(){
    }

    public static GestorNotificaciones getGestorNotificaciones(Context pContext){
        if(gestorNotificaciones==null){
            gestorNotificaciones = new GestorNotificaciones();
        }
        context = pContext;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        return gestorNotificaciones;
    }

    public void createCanalNotificacion(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = context.getResources().getString(R.string.notificacion_nota);
            String descripcion = context.getResources().getString(R.string.notificacion_descripcion);
            int importancia = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("Notas",name,importancia);
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);
            channel.setVibrationPattern(new long[]{0, 1000});
            channel.enableVibration(true);
            channel.setDescription(descripcion);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
