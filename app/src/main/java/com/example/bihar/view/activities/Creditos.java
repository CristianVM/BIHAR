package com.example.bihar.view.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
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

import java.io.File;

public class Creditos extends AppCompatActivity {

    private static final int TIEMPO_ANIMACION_MS = 1250;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_creditos);

        obtenerDesgloseCreditosSuperados();
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
}