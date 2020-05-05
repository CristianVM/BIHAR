package com.example.bihar.view.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bihar.R;
import com.example.bihar.controller.GestorProfesores;
import com.example.bihar.controller.WorkerBihar;
import com.example.bihar.model.Profesor;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class Tutorias extends AppCompatActivity {

    private TextView titulo;
    private CircleImageView imagen;
    private ListView listView;
    private ArrayAdapter elAdapter;
    private ProgressBar progressBar;
    private Switch aSwitch;
    private SearchView searchView;


    private boolean todos;
    private List<String> ids;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorias);

        todos = false;

        titulo = findViewById(R.id.textView);
        imagen = findViewById(R.id.imgAjustes);
        listView = findViewById(R.id.listViewTutorias);
        progressBar = findViewById(R.id.tutoriasProgressBar);
        aSwitch = findViewById(R.id.switchMostrar);
        aSwitch.setChecked(todos);
        searchView = findViewById(R.id.tutoriasSearchView);


        aSwitch.setOnClickListener(v -> {
            todos = aSwitch.isChecked();
            progressBar.setVisibility(View.VISIBLE);
            cargarDatosProfesores();
        });

        //Cuando pulsamos la lupa debemos hacer invisible el titulo del ToolBar y la imagen
        searchView.setOnSearchClickListener(v -> {
            titulo.setVisibility(View.INVISIBLE);
            imagen.setVisibility(View.INVISIBLE);
        });

        //Cuando salimos de la búsqueda volvemos a hacer visible el titulo y la imagen
        searchView.setOnCloseListener(() -> {
            titulo.setVisibility(View.VISIBLE);
            imagen.setVisibility(View.VISIBLE);
            return false;
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ids.clear();
                ids.addAll(GestorProfesores.getGestorProfesores().getIds(newText));
                elAdapter.notifyDataSetChanged();
                return false;
            }
        });


        cargarDatosProfesores();

    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            titulo.setVisibility(View.VISIBLE);
            imagen.setVisibility(View.VISIBLE);
            searchView.onActionViewCollapsed();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("visible", titulo.getVisibility());
        outState.putBoolean("todos",todos);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        int visible = savedInstanceState.getInt("visible");
        titulo.setVisibility(visible);
        imagen.setVisibility(visible);
        todos = savedInstanceState.getBoolean("todos");
        actualizarLista();
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * Llamamos al Worker para recoger los datos de la BD
     */
    public void cargarDatosProfesores(){
        Map<String, String> map = new HashMap<>();
        map.put("accion","obtenerProfesores");

        if(!todos){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String idPersona = prefs.getString("idUsuario",null);
            Log.i("idPersona", idPersona);
            map.put("idPersona", idPersona);
        }

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


        WorkManager.getInstance(this).getWorkInfoByIdLiveData(trabajo.getId()).observe(
                this, status -> {
                    if (status != null && status.getState().isFinished()) {
                        String resultado = status.getOutputData().getString("result");

                        JSONParser parser = new JSONParser();
                        try {
                            JSONArray jsonArray = (JSONArray) parser.parse(resultado);
                            GestorProfesores gestor = GestorProfesores.getGestorProfesores();
                            gestor.limpiar();
                            for(int i = 0; i<jsonArray.size(); i++){
                                JSONObject obj = (JSONObject) jsonArray.get(i);
                                String idPersona = (String) obj.get("idPersona");
                                String nombreCompleto = (String) obj.get("nombreCompleto");
                                int idAsignatura = Integer.parseInt((String) Objects.requireNonNull(obj.get("idAsignatura")));
                                String nombreAsignatura = (String) obj.get("nombreAsignatura");

                                gestor.anadirProfesor(idPersona,nombreCompleto,idAsignatura,nombreAsignatura);
                            }

                            actualizarLista();
                            progressBar.setVisibility(View.INVISIBLE);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        WorkManager.getInstance(this).enqueue(trabajo);
    }

    /**
     * Creamos el adapter con los datos guardados del gestor y lo cargamos en la listView
     */
    private void actualizarLista(){

        Map<String, Profesor> datos = GestorProfesores.getGestorProfesores().getListaProfesores();
        ids = GestorProfesores.getGestorProfesores().getIds("");

        elAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, android.R.id.text1, ids){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View vista = super.getView(position, convertView, parent);
                TextView lineaPrincipal = (TextView) vista.findViewById(android.R.id.text1);
                TextView lineaSecundaria = (TextView) vista.findViewById(android.R.id.text2);

                lineaPrincipal.setTypeface(null, Typeface.BOLD);
                lineaPrincipal.setTextSize(20);

                lineaSecundaria.setTextSize(13);

                lineaPrincipal.setText(datos.get(ids.get(position)).getNombreCompleto());
                lineaSecundaria.setText(datos.get(ids.get(position)).getAsignaturas());

                vista.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(Tutorias.this, ids.get(position), Toast.LENGTH_SHORT).show();
                    }
                });
                return vista;
            }
        };

        listView.setAdapter(elAdapter);
    }
}
