package com.example.bihar.view.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Tutorias extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter elAdapter;
    Map<String, Profesor> datos;
    String[] ids;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorias);

        listView = findViewById(R.id.listViewTutorias);

        cargarDatosProfesores();

    }

    /**
     * Llamamos al Worker para recoger los datos de la BD
     */
    public void cargarDatosProfesores(){
        Map<String, String> map = new HashMap<>();
        map.put("accion","obtenerProfesores");
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

                            for(int i = 0; i<jsonArray.size(); i++){
                                JSONObject obj = (JSONObject) jsonArray.get(i);
                                String idPersona = (String) obj.get("idPersona");
                                String nombreCompleto = (String) obj.get("nombreCompleto");
                                int idAsignatura = Integer.parseInt((String) Objects.requireNonNull(obj.get("idAsignatura")));
                                String nombreAsignatura = (String) obj.get("nombreAsignatura");

                                gestor.anadirProfesor(idPersona,nombreCompleto,idAsignatura,nombreAsignatura);
                            }

                            actualizarLista();
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
        String[] ids = GestorProfesores.getGestorProfesores().getIds();
        Log.i("ids", Arrays.toString(ids));

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

                lineaPrincipal.setText(datos.get(ids[position]).getNombreCompleto());
                lineaSecundaria.setText(datos.get(ids[position]).getAsignaturas());

                vista.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(Tutorias.this, ids[position], Toast.LENGTH_SHORT).show();
                    }
                });
                return vista;
            }
        };

        listView.setAdapter(elAdapter);
    }
}
