package com.example.bihar.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.example.bihar.R;
import com.example.bihar.controller.WorkerBihar;
import com.example.bihar.utils.AdapterListaAsignaturasMatricula;

import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Matricula extends AppCompatActivity {

    private String[] asignaturaNombres;
    private int[] asignaturaCursos;
    private int[] asignaturasConvocatorias;
    private double[] asignaturasOrdinarias;
    private double[] asignaturasExtraordinarias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matricula);

        TextView txtSeleccionAnio = findViewById(R.id.matricula_seleccionAnio);

        //https://stackoverflow.com/questions/18003021/how-to-add-border-around-tablelayout
        asignaturaNombres = new String[2];
        asignaturaNombres[0] = "Gestion de proyectos";
        asignaturaCursos = new int[2];
        asignaturaCursos[0] = 1;
        asignaturasConvocatorias = new int[2];
        asignaturasConvocatorias[0] = 1;
        asignaturasOrdinarias = new double[2];
        asignaturasOrdinarias[0] = 5;
        asignaturasExtraordinarias = new double[2];
        asignaturasExtraordinarias[0] = 9;

        ListView asignaturas = (ListView) findViewById(R.id.matricula_lista);
        AdapterListaAsignaturasMatricula adapter = new AdapterListaAsignaturasMatricula(
                this, asignaturaNombres, asignaturaCursos, asignaturasConvocatorias, asignaturasOrdinarias, asignaturasExtraordinarias);
        asignaturas.setAdapter(adapter);

        Map<String,String> map = new HashMap<>();
        map.put("idPersona","837448");
        map.put("pass","12345");
        map.put("accion","comprobarUsuario");
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

        /*WorkManager.getInstance(this).getWorkInfoByIdLiveData(trabajo.getId()).observe(
                this, status -> {
                    if(status != null && status.getState().isFinished()) {

                    }});*/
        WorkManager.getInstance(this).enqueue(trabajo);
    }
}
