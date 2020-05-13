package com.example.bihar.view.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import com.example.bihar.view.fragments.ToolBar;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Actividad encargada de mostrar las posibles asignaturas de un alumno o profesor que le dan acceso
 * al foro de la misma
 */
public class ForoVirtual extends AppCompatActivity {

    private ArrayList<String> identificadoresAsignaturas = new ArrayList<>();
    private ArrayList<String> nombresAsignaturas = new ArrayList<>();

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

        super.setContentView(R.layout.lista_asignaturas);

        ToolBar toolbarForoVirtual = (ToolBar) getSupportFragmentManager().findFragmentById(R.id.toolbarForoVirtual);
        toolbarForoVirtual.cambiarTituloToolbar(getResources().getString(R.string.foro_titulo));

        obtenerAsignaturas();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String idiomaNuevo = sharedPreferences.getString("idioma", "es");

        if (!idiomaNuevo.equals(idiomaEstablecido)) {
            idiomaEstablecido = idiomaNuevo;
            if (idiomaEstablecido.equals("es")) {
                Locale locale = new Locale("es");
                cambiarIdiomaOnResume(locale);
            } else if (idiomaEstablecido.equals("eu")) {
                Locale locale = new Locale("eu");
                cambiarIdiomaOnResume(locale);
            }
        }
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
        parametrosJSON.put("idioma", idiomaEstablecido);

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

                                lalista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        Intent i = new Intent(getApplicationContext(), ForoAsignatura.class);
                                        i.putExtra("idAsignatura", identificadoresAsignaturas.get(position));
                                        startActivity(i);
                                    }
                                });
                            // Si salta algun error
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), getString(R.string.error_general), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            } finally {
                                terminarCarga();
                            }
                        }
                    }
                });

        WorkManager.getInstance(this).enqueue(otwr);
    }

    /**
     * Cambia el idioma de la aplicación al reanudarse la actividad. Se destruye la actividad y se
     * vuelve a iniciar
     *
     * @param locale: el idioma almacenado en SharedPreferences
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

    /**
     * Cambia el idioma de la aplicación al crearse la actividad
     *
     * @param locale: el idioma almacenado en SharedPreferences
     */
    public void cambiarIdiomaOnCreate(Locale locale) {
        Locale.setDefault(locale);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);
    }
}