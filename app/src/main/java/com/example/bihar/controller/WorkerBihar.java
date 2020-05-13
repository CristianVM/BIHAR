package com.example.bihar.controller;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.net.ssl.HttpsURLConnection;

/**
 * Clase encargada de realizar las tareas relacionadas con el servicio web en segundo plano
 */
public class WorkerBihar extends Worker {

    /**
     * Constructor
     *
     * @param context:      el contexto
     * @param workerParams: los parametros que recibe el worker
     */
    public WorkerBihar(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    /**
     * La ejecucion de la tarea en segundo plano
     *
     * @return El resultado generado a partir de los obtenido en el servido web
     */
    @NonNull
    @Override
    public Result doWork() {

        String jsonString = getInputData().getString("datos");
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonDatos = (JSONObject) parser.parse(jsonString);
            // Si la accion es insertar en la BD remota una imagen, realizamos aqui la conversion a
            // base64 a partir de su ruta
            if (((String) jsonDatos.get("accion")).equals("insertarFotoPerfil")) {
                String base64 = obtenerBase64((String) jsonDatos.get("path"), (String) jsonDatos.get("esGaleria"));
                jsonDatos.put("base64", base64);
                jsonString = jsonDatos.toString();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

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
                JSONObject json = (JSONObject) parser.parse(jsonString);
                // Si la accion es obtener una imagen de un usuario de la BD remota,
                // guardamos la imagen en un fichero
                if (json.get("accion").equals("obtenerImagen")) {
                    Bitmap bitmap = BitmapFactory.decodeStream(urlConnection.getInputStream());
                    Log.i("JSON", json.toString());

                    File imagenFichero = new File(getApplicationContext().getFilesDir(), json.get("idUsuario").toString() + ".png");
                    OutputStream os = new FileOutputStream(imagenFichero);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                    os.flush();
                    os.close();

                    Data.Builder data = new Data.Builder();
                    data.putString("result", "OK");
                    return Result.success(data.build());

                // Si la accion es obtener una imagen de una empresa de la BD remota,
                // guardamos la imagen en un fichero
                } else if (json.get("accion").equals("obtenerImagenEmpresa")) {
                    // Transformamos el resultado de la llamada en un Bitmap
                    Bitmap bitmap = BitmapFactory.decodeStream(urlConnection.getInputStream());

                    File imagenFichero = new File(getApplicationContext().getFilesDir(), json.get("idPractica").toString() + ".png");
                    OutputStream os = new FileOutputStream(imagenFichero);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                    os.flush();
                    os.close();

                    Data.Builder data = new Data.Builder();
                    data.putString("result", "OK");
                    return Result.success(data.build());

                // En caso de obtener un JSON con datos pero no imagenes
                } else {
                    BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    String line;
                    String result = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        result += line;
                    }
                    inputStream.close();

                    if (!result.isEmpty()) {
                        Data.Builder data = new Data.Builder();
                        data.putString("result", result);
                        Log.i("BDD", result);

                        JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
                        String accion = (String) jsonObject.get("accion");
                        if (accion.equals("consultarLibros")) {
                            GestorLibros.getGestorLibros().addLibro(result);
                            return Result.success();
                        } else if (accion.equals("verMatricula")) {
                            GestorMatriculas.gestorMatriculas().addMatriculas(result, getApplicationContext(), (String) jsonObject.get("idPersona"));
                            return Result.success();
                        } else if (accion.equals("consultarHorario")) {
                            GestorHorarios.gestorHorarios().anadirHorarios(result, getApplicationContext());
                            return Result.success();
                        } else if (accion.equals("obtenerPracticas")) {
                            GestorPracticas.getGestorPracticas().addPracticas(result);
                            return Result.success();
                        }
                        return Result.success(data.build());
                    } else {
                        Log.i("BDD", "Vacio");
                    }
                }
            } else {
                Log.i("BDD", statusCode + "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Data.Builder data = new Data.Builder();
        data.putString("result", "FAIL");
        return Result.failure(data.build());
    }

    /**
     * Metodo que permite obtener el String en base 64 de una imagen
     *
     * @param path: la ruta de la imagen a convertir
     * @param esGaleria: indica si la foto ha sido recogida de la galeria o tomada desde la camara
     * @return la imagen convertida en un String de base64
     */
    private String obtenerBase64(String path, String esGaleria) {
        Bitmap bitmap = null;
        if (esGaleria.equals("true")) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), Uri.parse(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            bitmap = BitmapFactory.decodeFile(path);
        }

        bitmap = redimensionarImagen(bitmap, 300);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /**
     * Metodo que permite redimensaionar un bitmap.
     *
     * @param bitmap: la imagen a redimensionar
     * @param tamano: el tamano
     * @return: el bitmap redimensionado
     */
    private Bitmap redimensionarImagen(Bitmap bitmap, int tamano) {
        int anchoImg = bitmap.getWidth();
        int alturaImg = bitmap.getHeight();
        float ratioImg = (float) anchoImg / alturaImg;
        float ratioDestino = (float) tamano / tamano;
        int anchoFinal = tamano;
        int alturaFinal = tamano;
        if (ratioDestino > ratioImg) {
            anchoFinal = (int) ((float) tamano * ratioImg);
        } else {
            alturaFinal = (int) ((float) tamano / ratioImg);
        }
        return Bitmap.createScaledBitmap(bitmap, anchoFinal, alturaFinal, true);
    }
}
