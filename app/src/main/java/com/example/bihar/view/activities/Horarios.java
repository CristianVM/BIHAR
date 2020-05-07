package com.example.bihar.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.content.SharedPreferences;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.bihar.R;
import com.example.bihar.controller.GestorHorarios;
import com.example.bihar.controller.WorkerBihar;
import com.example.bihar.utils.AdapterListaHorarios;

import org.json.simple.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Horarios extends AppCompatActivity {

    private ArrayList<String> nombreAsignaturas;
    private ArrayList<String> horaFinales;
    private ArrayList<String> horaIniciales;
    private ArrayList<String> diaSemanas;
    private ArrayList<String> semanas;

    private AdapterListaHorarios adapterListaHorarios;
    private Calendar fechaEscogida;

    private ListView listView;
    private TextView diaFecha;
    private String usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horarios);

        nombreAsignaturas = new ArrayList<>();
        horaFinales = new ArrayList<>();
        horaIniciales = new ArrayList<>();
        diaSemanas = new ArrayList<>();
        semanas = new ArrayList<>();

        listView = (ListView) findViewById(R.id.horario_lista);
        diaFecha = (TextView) findViewById(R.id.horario_diaSemana);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        usuario = prefs.getString("idUsuario","");


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        fechaEscogida = Calendar.getInstance();
        Date date = fechaEscogida.getTime();
        diaFecha.setText(dateFormat.format(date));

        Map<String, String> map = new HashMap<>();
        map.put("accion", "consultarHorario");
        map.put("idPersona",usuario);
        map.put("anio",anioActualMatricula());
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
                    if (status.getState().isFinished()) {
                        rellenarListas(0);
                        adapterListaHorarios = new AdapterListaHorarios(
                                nombreAsignaturas,horaIniciales,horaFinales,diaSemanas,semanas,this);
                        listView.setAdapter(adapterListaHorarios);
                        pasarDias();
                    }
                });
    }



    private String anioActualMatricula(){
        int anio = fechaEscogida.get(Calendar.YEAR);
        int mes = fechaEscogida.get(Calendar.MONTH);

        if(mes <= 7){
            anio--;
        }
        return String.valueOf(anio);
    }

    private void pasarDias(){
        ImageView uniIzq = (ImageView) findViewById(R.id.horarios_uniflechaIzq);
        ImageView biIzq = (ImageView) findViewById(R.id.horarios_biflechaIzq);
        ImageView uniDer = (ImageView) findViewById(R.id.horarios_uniflechaDer);
        ImageView biDer = (ImageView) findViewById(R.id.horarios_biflechaDer);

        uniIzq.setOnClickListener( view -> {
            limpiarListas();
            if(fechaEscogida.get(Calendar.DAY_OF_WEEK)!=Calendar.MONDAY){
                rellenarListas(-1);
            }else{
                rellenarListas(-3);
            }
            adapterListaHorarios.notifyDataSetChanged();
        });

        biIzq.setOnClickListener( view -> {
            limpiarListas();
            rellenarListas(-7);
            adapterListaHorarios.notifyDataSetChanged();
        });

        uniDer.setOnClickListener(view -> {
            limpiarListas();
            if(fechaEscogida.get(Calendar.DAY_OF_WEEK)!=Calendar.FRIDAY){
                rellenarListas(1);
            }else{
                rellenarListas(3);
            }
            adapterListaHorarios.notifyDataSetChanged();
        });

        biDer.setOnClickListener( view -> {
            limpiarListas();
            rellenarListas(7);
            adapterListaHorarios.notifyDataSetChanged();
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GestorHorarios.gestorHorarios().limpiarHorarios();
    }

    private void limpiarListas(){
        nombreAsignaturas.clear();
        horaFinales.clear();
        horaIniciales.clear();
        diaSemanas.clear();
        semanas.clear();
    }

    private void rellenarListas(int diasNuevos){
        fechaEscogida.add(Calendar.DATE,diasNuevos);
        List<JSONObject> listaJSONs = GestorHorarios.gestorHorarios().obtHorariosDelDia(fechaEscogida);
        if(listaJSONs.size()>0){
            for(int i=listaJSONs.size()-1;i >=0 ;i--){
                JSONObject jsonObject = (JSONObject) listaJSONs.get(i);
                nombreAsignaturas.add((String) jsonObject.get("nombresAsignaturas"));
                horaIniciales.add((String) jsonObject.get("hInicios"));
                horaFinales.add((String) jsonObject.get("hFinales"));
                diaSemanas.add((String) jsonObject.get("diaSemanas"));
                semanas.add((String) jsonObject.get("semanas"));
            }
            diaFecha.setText(diaSemanas.get(0) + " " + semanas.get(0));

        }

    }
}
