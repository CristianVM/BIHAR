package com.example.bihar.view.activities;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.bihar.R;
import com.example.bihar.controller.WorkerBihar;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.Locale;

public class Creditos extends AppCompatActivity {

    private static final int TIEMPO_ANIMACION_MS = 1250;
    private String idiomaEstablecido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        idiomaEstablecido = prefs.getString("idioma", "es");
        if (idiomaEstablecido.equals("es")) {
            Locale locale = new Locale("es");
            cambiarIdiomaOnCreate(locale);
        } else if (idiomaEstablecido.equals("eu")) {
            Locale locale = new Locale("eu");
            cambiarIdiomaOnCreate(locale);
        }

        super.setContentView(R.layout.activity_creditos);

        obtenerDesgloseCreditosSuperados();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String idiomaNuevo = sharedPreferences.getString("idioma","es");

        if(!idiomaNuevo.equals(idiomaEstablecido)){
            idiomaEstablecido = idiomaNuevo;
            if(idiomaEstablecido.equals("es")) {
                Locale locale = new Locale("es");
                cambiarIdiomaOnResume(locale);
            } else if(idiomaEstablecido.equals("eu")) {
                Locale locale = new Locale("eu");
                cambiarIdiomaOnResume(locale);
            }
        }
    }

    private void obtenerDesgloseCreditosSuperados() {
        comenzarCarga();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String idUsuario = prefs.getString("idUsuario", "");

        JSONObject parametrosJSON = new JSONObject();
        parametrosJSON.put("accion", "obtenerDesgloseCreditosSuperados");
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
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (workInfo != null && workInfo.getState().isFinished()) {
                            try {
                                String resultado = workInfo.getOutputData().getString("result");
                                JSONParser parser = new JSONParser();
                                JSONObject json = (JSONObject) parser.parse(resultado);
                                float creditosObligatorios = Float.parseFloat((String) json.get("creditosObligatorios"));
                                float creditosBasicos = Float.parseFloat((String) json.get("creditosBasicos"));
                                float creditosOptativos = Float.parseFloat((String) json.get("creditosOptativos"));
                                float creditosTFG = Float.parseFloat((String) json.get("creditosTFG"));

                                mostrarDesglose(creditosObligatorios, creditosBasicos, creditosOptativos, creditosTFG);

                                // Si salta algun error
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), R.string.error_general, Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            } finally {
                                terminarCarga();
                            }
                        }
                    }
                });

        WorkManager.getInstance(this).enqueue(otwr);
    }

    private void mostrarDesglose(float creditosObligatorios, float creditosBasicos, float creditosOptativos, float creditosTFG) {
        ScrollView scrollViewCreditos = findViewById(R.id.scrollViewCreditos);
        scrollViewCreditos.setVisibility(View.VISIBLE);

        ProgressBar progressBarCreditosObligatorios = findViewById(R.id.progressBarCreditosObligatorios);
        progressBarCreditosObligatorios.setMax(120);
        TextView textCreditosObligatorios = findViewById(R.id.textCreditosObligatorios);
        ProgressBarAnimation anim = new ProgressBarAnimation(textCreditosObligatorios, progressBarCreditosObligatorios, 0, creditosObligatorios);
        anim.setDuration(TIEMPO_ANIMACION_MS);
        progressBarCreditosObligatorios.startAnimation(anim);

        ProgressBar progressBarCreditosBasicos = findViewById(R.id.progressBarCreditosBasicos);
        progressBarCreditosBasicos.setMax(60);
        TextView textCreditosBasicos = findViewById(R.id.textCreditosBasicos);
        anim = new ProgressBarAnimation(textCreditosBasicos, progressBarCreditosBasicos, 0, creditosBasicos);
        anim.setDuration(TIEMPO_ANIMACION_MS);
        progressBarCreditosBasicos.startAnimation(anim);

        ProgressBar progressBarCreditosOptativos = findViewById(R.id.progressBarCreditosOptativos);
        progressBarCreditosOptativos.setMax(48);
        TextView textCreditosOptativos = findViewById(R.id.textCreditosOptativos);
        anim = new ProgressBarAnimation(textCreditosOptativos, progressBarCreditosOptativos, 0, creditosOptativos);
        anim.setDuration(TIEMPO_ANIMACION_MS);
        progressBarCreditosOptativos.startAnimation(anim);

        ProgressBar progressBarCreditosTFG = findViewById(R.id.progressBarCreditosTFG);
        progressBarCreditosTFG.setMax(12);
        TextView textCreditosTFG = findViewById(R.id.textCreditosTFG);
        anim = new ProgressBarAnimation(textCreditosTFG, progressBarCreditosTFG, 0, creditosTFG);
        anim.setDuration(TIEMPO_ANIMACION_MS);
        progressBarCreditosTFG.startAnimation(anim);
    }

    private void comenzarCarga() {
        ProgressBar progressBarCreditos = findViewById(R.id.progressBarCreditos);
        progressBarCreditos.setVisibility(View.VISIBLE);
    }

    private void terminarCarga() {
        ProgressBar progressBarCreditos = findViewById(R.id.progressBarCreditos);
        progressBarCreditos.setVisibility(View.INVISIBLE);
    }

    /** Cambia el idioma de la aplicación al reanudarse la actividad. Se destruye la actividad y se
     *  vuelve a iniciar
     *  @param locale: el idioma almacenado en SharedPreferences
     */
    public void cambiarIdiomaOnResume(Locale locale) {
        Locale.setDefault(locale);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);
        recreate();
    }

    /** Cambia el idioma de la aplicación al crearse la actividad
     *  @param locale: el idioma almacenado en SharedPreferences
     */
    public void cambiarIdiomaOnCreate(Locale locale){
        Locale.setDefault(locale);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);
    }
}
