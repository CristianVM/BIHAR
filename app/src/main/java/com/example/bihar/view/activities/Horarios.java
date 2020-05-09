package com.example.bihar.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.bihar.R;
import com.example.bihar.controller.GestorHorarios;
import com.example.bihar.controller.WorkerBihar;
import com.example.bihar.utils.AdapterListaHorarios;
import com.example.bihar.view.fragments.ToolBar;

import org.json.simple.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

    private String idiomaEstablecido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        idiomaEstablecido = prefs.getString("idioma","es");
        if(idiomaEstablecido.equals("es")){
            Locale locale = new Locale("es");
            cambiarIdiomaOnCreate(locale);
        }else if(idiomaEstablecido.equals("eu")){
            Locale locale = new Locale("eu");
            cambiarIdiomaOnCreate(locale);
        }

        setContentView(R.layout.activity_horarios);

        nombreAsignaturas = new ArrayList<>();
        horaFinales = new ArrayList<>();
        horaIniciales = new ArrayList<>();
        diaSemanas = new ArrayList<>();
        semanas = new ArrayList<>();

        listView = (ListView) findViewById(R.id.horario_lista);
        diaFecha = (TextView) findViewById(R.id.horario_diaSemana);
        usuario = prefs.getString("idUsuario","");


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        fechaEscogida = Calendar.getInstance();

        if(fechaEscogida.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
            rellenarListas(2);
        }else if(fechaEscogida.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
            rellenarListas(1);
        }
        Date date = fechaEscogida.getTime();
        diaFecha.setText(dateFormat.format(date));

        ToolBar toolBar = (ToolBar) getSupportFragmentManager().findFragmentById(R.id.frgmt_toolbarhorario);
        toolBar.cambiarTituloToolbar(getResources().getString(R.string.horario));

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
        anadirDiaSemanaTextView();
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
        }
    }

    private void anadirDiaSemanaTextView(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date hoy = fechaEscogida.getTime();
        String fecha = dateFormat.format(hoy);
        if(fechaEscogida.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY){
            diaFecha.setText(R.string.horario_diaLunes);
        }else if(fechaEscogida.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY){
            diaFecha.setText(R.string.horario_diaMartes);
        }else if(fechaEscogida.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY){
            diaFecha.setText(R.string.horario_diaMiercoles);
        }else if(fechaEscogida.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY){
            diaFecha.setText(R.string.horario_diaJueves);
        }else if(fechaEscogida.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            diaFecha.setText(R.string.horario_diaViernes);
        }
        diaFecha.setText(diaFecha.getText() +" " + fecha);
    }


    /**
     * Se comprueba el idioma que tenía la actividad con el de SharedPreferences: si es distinto se
     * cambia el idioma cerrando y volviendo a iniciar la actividad.
     */
    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

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
}
