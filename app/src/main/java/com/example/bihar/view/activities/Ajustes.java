package com.example.bihar.view.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.bihar.R;
import com.example.bihar.controller.GestorUsuario;
import com.example.bihar.controller.WorkerBihar;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.simple.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

                    break;
                case 81:
                    //FOTO DE LA CAMARA
                    /*String path = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("path","");
                    MediaScannerConnection.scanFile(this, new String[]{path}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String s, Uri uri) {
                                    Log.i("Ruta almacenamiento","Path:"+path);
                                }
                            });
                    insertImageBD(false,path);*/
                    break;
            }
        }
    }


}
