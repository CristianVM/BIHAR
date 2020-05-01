package com.example.bihar.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.bihar.R;
import com.example.bihar.controller.GestorLibros;
import com.example.bihar.controller.WorkerBihar;
import com.example.bihar.utils.AdapterListaLibros;
import com.example.bihar.utils.AdapterListaUniversidades;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.HashMap;
import java.util.Map;

public class LibroInformacion extends AppCompatActivity {

    private String[] universidades;
    private String[] disponibles;
    private boolean[] estaDisponible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_libro_informacion);

        Map<String, String> map = new HashMap<>();
        map.put("accion", "consultarReservaLibro");
        map.put("titulo", "El gran libro de Android");
        JSONObject jsonWorker = new JSONObject(map);

        Data.Builder data = new Data.Builder();
        data.putString("datos", jsonWorker.toString());

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
                        String resultado = status.getOutputData().getString("result");
                        JSONParser parser = new JSONParser();
                        try {
                            JSONObject jsonResultado = (JSONObject) parser.parse(resultado);
                            JSONArray jsonArrayUniversidades = (JSONArray) jsonResultado.get("universidades");
                            JSONArray jsonArrayReservas = (JSONArray) jsonResultado.get("reservas");

                            universidades = new String[jsonArrayUniversidades.size()];
                            disponibles = new String[jsonArrayUniversidades.size()];
                            estaDisponible = new boolean[jsonArrayUniversidades.size()];

                            for (int i = 0; i < jsonArrayUniversidades.size(); i++) {
                                JSONObject universidad = (JSONObject) jsonArrayUniversidades.get(i);

                                universidades[i] = (String) universidad.get("nombreCentro");
                                String idLibro = (String) universidad.get("idLibro");

                                boolean disponible = true;
                                int j=0;
                                while(j< jsonArrayReservas.size() && disponible){
                                    JSONObject reserva = (JSONObject) jsonArrayReservas.get(j);

                                    if(idLibro.equals((String) reserva.get("idLibro"))){
                                        disponibles[i] = getResources().getText(R.string.libroInformacion_libroNoDisponible).toString()
                                                + reserva.get("fechaFin");
                                        disponible = false;
                                    }
                                    j++;
                                }
                                if(disponible){
                                    disponibles[i] = getResources().getText(R.string.libroInformacion_libroDisponible).toString();
                                }
                                estaDisponible[i] = disponible;
                            }
                            AdapterListaUniversidades adapter = new AdapterListaUniversidades(this,universidades,
                                    disponibles,estaDisponible);
                            ListView listView = (ListView) findViewById(R.id.libroInformacion_listaUniversidades);
                            listView.setAdapter(adapter);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });

        abrirDesplegables();
    }

    private void abrirDesplegables() {
        LinearLayout lyInformacionLibro = (LinearLayout) findViewById(R.id.libroInformacion_lyDesplegableInfoLibro);
        LinearLayout lyListaUniversidades = (LinearLayout) findViewById(R.id.libroInformacion_lyDesplegableUniversidades);

        lyInformacionLibro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout infoLibro = (LinearLayout) findViewById(R.id.libroInformacion_linearLayoutContenidoInfo);
                if (infoLibro.getVisibility() == View.VISIBLE) {
                    infoLibro.setVisibility(View.GONE);
                } else {
                    infoLibro.setVisibility(View.VISIBLE);
                }
            }
        });

        lyListaUniversidades.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RelativeLayout infoLibro = (RelativeLayout) findViewById(R.id.libroInformacion_layoutListaUniversidades);
                if (infoLibro.getVisibility() == View.VISIBLE) {
                    infoLibro.setVisibility(View.GONE);
                } else {
                    infoLibro.setVisibility(View.VISIBLE);
                }
            }
        });

    }
}
