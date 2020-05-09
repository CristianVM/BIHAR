package com.example.bihar.view.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.example.bihar.R;
import com.example.bihar.controller.WorkerBihar;
import com.example.bihar.view.fragments.AjustesPreferencias;

import org.json.simple.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Ajustes extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private String idiomaEstablecido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SE COMPRUEBA EL IDIOMA ELEGIDO
        sharedPreferences = (SharedPreferences) PreferenceManager.getDefaultSharedPreferences(this);
        idiomaEstablecido = sharedPreferences.getString("idioma","es");
        if(idiomaEstablecido.equals("es")){
            Locale locale = new Locale("es");
            cambiarIdiomaOnCreate(locale);
        }else if(idiomaEstablecido.equals("eu")){
            Locale locale = new Locale("eu");
            cambiarIdiomaOnCreate(locale);
        }
        setContentView(R.layout.activity_ajustes);

    }

    /**
     * Se comprueba el idioma que tenía la actividad con el de SharedPreferences: si es distinto se
     * cambia el idioma cerrando y volviendo a iniciar la actividad.
     */
    @Override
    protected void onResume() {
        super.onResume();

        String idiomaNuevo = sharedPreferences.getString("idioma","es");

        if(!idiomaNuevo.equals(idiomaEstablecido)){
            idiomaEstablecido = idiomaNuevo;
            if(idiomaEstablecido.equals("es")){
                Locale locale = new Locale("es");
                cambiarIdiomaOnResume(locale);
            }else if(idiomaEstablecido.equals("eu")){
                Locale locale = new Locale("eu");
                cambiarIdiomaOnResume(locale);
            }
        }
    }
    /**
     * Cambia el idioma de la aplicación al reanudarse la actividad. Se destruye la actividad y se
     * vuelve a iniciar
     * @param locale: el idioma almacenado en SharedPreferences
     */
    public void cambiarIdiomaOnResume(Locale locale){
        Locale.setDefault(locale);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);
        recreate();
    }

    /**
     * Cambia el idioma de la aplicación al crearse la actividad
     * @param locale: el idioma almacenado en SharedPreferences
     */
    public void cambiarIdiomaOnCreate(Locale locale){
        Locale.setDefault(locale);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);
    }

    /**
     * Después de acabar la intención se ejecuta este método. Si es correcto y el código es 80 entonces
     * se considerará que la foto es sacada de la galería y almacena la uri en sharespreferences,
     * y si es 81 será considerado como captura de foto y se guardará en sharespreferences el path.
     * Los dos añaden la foto en la ImageView.
     * @param requestCode el código de la intención
     * @param resultCode el resultado
     * @param data la intención
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            switch (requestCode){
                case 80:
                    // FOTO DE LA GALERIA
                    try{
                        Uri miPath  = data.getData();
                        insertImageBD(true,miPath.toString());
                    }catch(Exception e){
                        Toast.makeText(getApplicationContext(),"ERROR",Toast.LENGTH_LONG).show();
                    }
                    break;
                case 81:
                    //FOTO DE LA CAMARA
                    String path = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("path","");
                    MediaScannerConnection.scanFile(this, new String[]{path}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String s, Uri uri) {
                                    Log.i("Ruta almacenamiento","Path:"+path);
                                }
                            });
                    insertImageBD(false,path);
                    break;
            }
        }
    }

    /**
     * Se inserta la imagen en la BD remota dependiendo de si es desde la galeria o desde la cámara.
     */
    private void insertImageBD(boolean esGaleria,String path){

        // SE MANDA LA IMAGEN A LA BASE DE DATOS GRACIAS AL WORKER
        Map<String,String> map = new HashMap<>();
        map.put("idPersona","835334");
        map.put("path",path);
        map.put("accion","insertarFotoPerfil");
        if(esGaleria){
            map.put("esGaleria","true");
        }else{
            map.put("esGaleria","false");
        }

        JSONObject json = new JSONObject(map);

        Data.Builder data = new Data.Builder();
        data.putString("datos",json.toString());

        Constraints restricciones = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest trabajo = new OneTimeWorkRequest.Builder(WorkerBihar.class)
                .setConstraints(restricciones)
                .setInputData(data.build())
                .build();
        WorkManager.getInstance(this).enqueue(trabajo);

        WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(trabajo.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        String resultado = workInfo.getOutputData().getString("result");

                        if(resultado.equals("Ok")){
                            obtenerImagenUsuario();
                        }else{
                            Toast.makeText(getApplicationContext(),"ERROR",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void obtenerImagenUsuario() {
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        String idUsuario = prefs.getString("idUsuario", "");

        JSONObject parametrosJSON = new JSONObject();
        parametrosJSON.put("accion", "obtenerImagen");
        parametrosJSON.put("idUsuario", idUsuario);

        Data datos = new Data.Builder()
                .putString("datos", parametrosJSON.toJSONString())
                .build();

        Constraints restricciones = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(WorkerBihar.class)
                .setConstraints(restricciones)
                .setInputData(datos)
                .build();

        WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(otwr.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        try {
                            String resultado = workInfo.getOutputData().getString("result");
                            // Si se ha obtenido la imagen correctamente
                            if (resultado.equals("OK")) {
                                Log.i("MY-APP", "IMAGEN USUARIO OBTENIDA");
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putBoolean("imagenCambiada",true);
                                editor.apply();
                            } else {
                                Log.i("MY-APP", "IMAGEN USUARIO NO OBTENIDA");
                                File file = new File(getApplicationContext().getFilesDir(), idUsuario + ".png");
                                file.delete();
                            }
                            // Si salta algun error
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), R.string.error_general, Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                });

        WorkManager.getInstance(this).enqueue(otwr);
    }
}
