package com.example.bihar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;

import com.example.bihar.controller.GeneradorConexionesSeguras;
import com.example.bihar.controller.WorkerBihar;

import org.json.simple.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ProtocolException;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

/*
        JSONObject parametrosJSON = new JSONObject();
        parametrosJSON.put("accion", "actualizarImagen");
        parametrosJSON.put("idUsuario", "ulopez");

        Bitmap src = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.blanco);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        src.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] imagen = stream.toByteArray();

        // Obtenemos la imagen en Bitmap
        ByteArrayInputStream imageStream = new ByteArrayInputStream(imagen);
        Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
        stream = new ByteArrayOutputStream();

        // Hay que convertir el bitmap en un string en Base64
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] fotoTransformada = stream.toByteArray();
        String foto64 = Base64.encodeToString(fotoTransformada, Base64.DEFAULT);

        // Insertamos el String en base 64 al JSON
        parametrosJSON.put("foto", foto64);

        HttpsURLConnection urlConnection = GeneradorConexionesSeguras.getInstance()
                .crearConexionSegura(getApplicationContext(), "https://134.209.235.115/uluque001/WEB/BIHAR/Principal.php");

        try {
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
            out.print(parametrosJSON.toJSONString());
            out.close();

            int statusCode = urlConnection.getResponseCode();
            if (statusCode == 200) {

            }
        } catch (Exception e) {

        }*/
    }
}
