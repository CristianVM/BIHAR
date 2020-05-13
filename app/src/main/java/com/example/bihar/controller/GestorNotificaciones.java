package com.example.bihar.controller;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.example.bihar.R;

public class GestorNotificaciones {

    private static GestorNotificaciones gestorNotificaciones;
    private static Context context;
    private static NotificationManager notificationManager;

    /**
     * Constructor
     */
    private GestorNotificaciones() {
    }

    /**
     * Devuelve el gestor de notificaciones. Cambia el contexto por el contexto actual
     *
     * @param pContext: el contexto
     * @return: el gestor de notificaciones
     */
    public static GestorNotificaciones getGestorNotificaciones(Context pContext) {
        if (gestorNotificaciones == null) {
            gestorNotificaciones = new GestorNotificaciones();
        }
        context = pContext;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        return gestorNotificaciones;
    }

    /**
     * Crea el canal de las notificaciones para las versiones Oreo en adelante
     */
    public void createCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getResources().getString(R.string.notificacion_nota);
            String descripcion = context.getResources().getString(R.string.notificacion_descripcion);
            int importancia = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("BIHAR", name, importancia);
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);
            channel.setVibrationPattern(new long[]{0, 1000});
            channel.enableVibration(true);
            channel.setDescription(descripcion);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Se envia una notificación al reservar un libro. Indica que libro ha sido reservado y hasta que
     * fecha
     *
     * @param tituloLibro: titulo del libro
     * @param fecha:       fecha hasta que dura la reserva
     */
    public void notificacionReservaLibro(String tituloLibro, String fecha) {
        NotificationCompat.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(context, "BIHAR");
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_stat_call_white));
        } else {
            builder = new NotificationCompat.Builder(context);
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        //ESPECIFICACIONES DE LA NOTIFICACION
        builder.setContentTitle(context.getResources().getText(R.string.libroReservado));

        String descripcion;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getString("idioma", "es").equals("es")) {
            descripcion = tituloLibro + " " + context.getResources().getText(R.string.libroReservadoQueda) + " " + fecha;
        } else {
            descripcion = tituloLibro + " " + fecha + " " + context.getResources().getText(R.string.libroReservadoQueda);
        }

        builder.setContentText(descripcion);
        builder.setSmallIcon(R.drawable.ic_stat_call_white);
        builder.setAutoCancel(true);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(descripcion));
        builder.setColor(context.getResources().getColor(R.color.verde));

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //SE LANZA LA NOTIFICACION
        notificationManager.notify(8888, builder.build());
    }
}
