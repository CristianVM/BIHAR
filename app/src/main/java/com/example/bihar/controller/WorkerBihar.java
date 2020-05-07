package com.example.bihar.controller;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.net.ssl.HttpsURLConnection;

public class WorkerBihar extends Worker {

    public WorkerBihar(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        String jsonString = getInputData().getString("datos");

        HttpsURLConnection urlConnection = GeneradorConexionesSeguras.getInstance()
                .crearConexionSegura(getApplicationContext(), "https://134.209.235.115/uluque001/WEB/BIHAR/Principal.php");

        try {
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
            out.print(jsonString);
            out.close();

            int statusCode = urlConnection.getResponseCode();
            if (statusCode == 200) {
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(jsonString);
                if (json.get("accion").equals("obtenerImagen")) {
                    // Transformamos el resultado de la llamada en un Bitmap
                    Bitmap bitmap = BitmapFactory.decodeStream(urlConnection.getInputStream());

                    File imagenFichero = new File(getApplicationContext().getFilesDir(), json.get("idUsuario").toString() + ".png");
                    OutputStream os = new FileOutputStream(imagenFichero);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                    os.flush();
                    os.close();

                    Data.Builder data = new Data.Builder();
                    data.putString("result", "OK");
                    return Result.success(data.build());
                } else {
                    BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    String line;
                    String result="";
                    while ((line = bufferedReader.readLine()) != null) {
                        result += line;
                    }
                    inputStream.close();

                    if(!result.isEmpty()){
                        Data.Builder data = new Data.Builder();
                        data.putString("result",result);
                        Log.i("BDD",result);

                        JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
                        String accion = (String) jsonObject.get("accion");
                        if(accion.equals("consultarLibros")){
                            GestorLibros.getGestorLibros().addLibro(result);
                            return Result.success();
                        }else if(accion.equals("verMatricula")){
                            GestorMatriculas.gestorMatriculas().addMatriculas(result,getApplicationContext(),(String) jsonObject.get("idPersona"));
                            return Result.success();
                        }else if(accion.equals("consultarHorario")){
                            GestorHorarios.gestorHorarios().anadirHorarios(result,getApplicationContext());
                            return Result.success();
                        }else if(accion.equals("obtenerPracticas")) {
                            GestorPracticas.getGestorPracticas().addPracticas(result);
                        }

                        return Result.success(data.build());
                    }else{
                        Log.i("BDD","Vacio");
                    }
                }
            }else{
                Log.i("BDD",statusCode+"");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Data.Builder data = new Data.Builder();
        data.putString("result","FAIL");
        return Result.failure(data.build());
    }
}
