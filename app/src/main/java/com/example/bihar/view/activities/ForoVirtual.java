package com.example.bihar.view.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.bihar.R;
import com.example.bihar.controller.GestorPracticas;
import com.example.bihar.controller.WorkerBihar;
import com.example.bihar.model.Practica;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.Map;

public class ForoVirtual  extends AppCompatActivity {

    private ArrayList<String> identificadoresAsignaturas = new ArrayList<>();
    private ArrayList<String> nombresAsignaturas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.lista_asignaturas);

        obtenerAsignaturas();
    }

    private void comenzarCarga() {
        ProgressBar progressBarForoVirtual = findViewById(R.id.progressBarForoVirtual);
        progressBarForoVirtual.setVisibility(View.VISIBLE);
    }

    private void terminarCarga() {
        ProgressBar progressBarForoVirtual = findViewById(R.id.progressBarForoVirtual);
        progressBarForoVirtual.setVisibility(View.INVISIBLE);
    }

    private void obtenerAsignaturas() {
        comenzarCarga();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String idUsuario = prefs.getString("idUsuario", "");
        boolean esAlumno = prefs.getBoolean("esAlumno", true);

        JSONObject parametrosJSON = new JSONObject();
        parametrosJSON.put("accion", "obtenerAsignaturasForo");
        parametrosJSON.put("idUsuario", idUsuario);
        parametrosJSON.put("esAlumno", esAlumno);

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
                                String rdo = workInfo.getOutputData().getString("result");
                                JSONParser parser = new JSONParser();
                                JSONArray jsonArray = (JSONArray) parser.parse(rdo);
                                for (int i = 0; i < jsonArray.size(); i++) {
                                    JSONObject json = (JSONObject) jsonArray.get(i);
                                    identificadoresAsignaturas.add((String) json.get("idAsignatura"));
                                    nombresAsignaturas.add((String) json.get("nombreAsignatura"));
                                }

                                ArrayAdapter elAdaptador = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, nombresAsignaturas);
                                ListView lalista = findViewById(R.id.listaAsignaturasForo);
                                lalista.setAdapter(elAdaptador);

                                lalista.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        Intent i = new Intent(getApplicationContext(), ForoAsignatura.class);
                                        i.putExtra("idAsignatura", identificadoresAsignaturas.get(position));
                                        getApplicationContext().startActivity(i);
                                    }
                                });
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
}