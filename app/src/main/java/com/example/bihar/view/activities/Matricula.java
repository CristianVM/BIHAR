package com.example.bihar.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.bihar.R;
import com.example.bihar.controller.GestorMatriculas;
import com.example.bihar.controller.WorkerBihar;
import com.example.bihar.model.AlmacenajeMatricula;
import com.example.bihar.model.MatriculaAnios;
import com.example.bihar.utils.AdapterListaAsignaturasMatricula;
import com.example.bihar.view.dialog.DialogSelectAnioMatricula;

import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Matricula extends AppCompatActivity implements DialogSelectAnioMatricula.ListenerSelectAnioMatricula {

    private TextView txtAnioSeleccionado;
    private ListView asignaturas;
    private String anioMatriculaMostrado;
    private String idPersona;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matricula);

        txtAnioSeleccionado = findViewById(R.id.matricula_seleccionAnio);
        idPersona = "835334";

        Map<String,String> map = new HashMap<>();
        map.put("idPersona","835334");
        map.put("accion","verMatricula");
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

       WorkManager.getInstance(this).getWorkInfoByIdLiveData(trabajo.getId()).observe(
                this, status -> {
                   if (status != null && status.getState().isFinished()) {
                       MatriculaAnios matriculaAnios = GestorMatriculas.gestorMatriculas().getMatriculas(idPersona);
                       Map<String, AlmacenajeMatricula> matriculaMap = matriculaAnios.getMatriculas();

                       for (Map.Entry<String, AlmacenajeMatricula> datos : matriculaMap.entrySet()) {
                           anioMatriculaMostrado = datos.getKey();
                           txtAnioSeleccionado.setText(anioMatriculaMostrado + "-" + (Integer.parseInt(anioMatriculaMostrado) + 1));
                           asignaturas = (ListView) findViewById(R.id.matricula_lista);
                           AlmacenajeMatricula almacenajeMatricula = datos.getValue();

                           AdapterListaAsignaturasMatricula adapter = new AdapterListaAsignaturasMatricula(
                                   this, almacenajeMatricula.asignaturaNombres, almacenajeMatricula.asignaturaCursos,
                                   almacenajeMatricula.asignaturasConvocatorias, almacenajeMatricula.asignaturasExtraordinarias,
                                   almacenajeMatricula.asignaturasOrdinarias);
                           asignaturas.setAdapter(adapter);

                           // SE ASIGNA EL LISTENER PARA QUE ABRA EL DIALOG
                           asignarListeners();
                           break;
                       }
                   }
               });
    }

    /**
     * Se asigna el listener de tal manera que al pulsar se abra un diálogo para seleccionar la
     * matrícula
     */
    private void asignarListeners(){
        LinearLayout linearLayoutAnio = (LinearLayout) findViewById(R.id.matricula_linearLayoutSeleccionAnio);
        linearLayoutAnio.setOnClickListener(view -> {
            DialogSelectAnioMatricula dialog = new DialogSelectAnioMatricula(GestorMatriculas.gestorMatriculas().getMatriculas(idPersona).getAnios());
            dialog.show(getSupportFragmentManager(),"etiquetaMatricula");
        });
    }

    /**
     * Al seleccionar la matrícula en el dialog se muestran las asignaturas de esa matrícula y se
     * cambia el año escogido en el TextView
     * @param i: la posición de la matrícula elegida en el dialog
     */
    @Override
    public void seleccionAnio(int i) {
        String anioSeleccionado = GestorMatriculas.gestorMatriculas().getMatriculas(idPersona).getAnios().get(i);
        AlmacenajeMatricula datos = GestorMatriculas.gestorMatriculas().getMatriculas(idPersona).getMatriculas().get(anioSeleccionado);

        AdapterListaAsignaturasMatricula adapter = new AdapterListaAsignaturasMatricula(
                this, datos.asignaturaNombres, datos.asignaturaCursos,
                datos.asignaturasConvocatorias, datos.asignaturasExtraordinarias,
                datos.asignaturasOrdinarias);
        asignaturas.setAdapter(adapter);
        txtAnioSeleccionado.setText(anioSeleccionado+"-"+(Integer.parseInt(anioSeleccionado)+1));
    }

}
