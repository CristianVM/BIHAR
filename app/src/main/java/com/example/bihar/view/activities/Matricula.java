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
import com.example.bihar.controller.WorkerBihar;
import com.example.bihar.utils.AdapterListaAsignaturasMatricula;
import com.example.bihar.view.dialog.DialogSelectAnioMatricula;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Matricula extends AppCompatActivity implements DialogSelectAnioMatricula.ListenerSelectAnioMatricula {

    private TextView txtAnioSeleccionado;
    private ListView asignaturas;

    private HashMap<String,AlmacenajeDatos> matriculas;
    private ArrayList<String> anios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matricula);

        matriculas = new HashMap<>();
        txtAnioSeleccionado = findViewById(R.id.matricula_seleccionAnio);

        Map<String,String> map = new HashMap<>();
        map.put("idPersona","837448");
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
                    if(status != null && status.getState().isFinished()) {
                        String resultado = status.getOutputData().getString("result");
                        JSONParser parser = new JSONParser();
                        try{
                            JSONArray jsonArray = (JSONArray) parser.parse(resultado);
                            AlmacenajeDatos almacenajeArrays =null;
                            String anioEnJson="0";
                            anios = new ArrayList<>();

                            for(int i=0; i< jsonArray.size();i++){
                                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                // SI ES UN AÑO QUE NO HA APARECIDO SE AÑADE AL MAP LA MATRICULA ANTERIOR
                                // Y SE CREA UN NUEVO ALMACEN DE MATRICULAS
                                if(!anioEnJson.equals(jsonObject.get("anio"))){
                                    if(i>0){
                                        matriculas.put(anioEnJson,almacenajeArrays);
                                    }
                                    almacenajeArrays = new AlmacenajeDatos();
                                    anioEnJson = (String) jsonObject.get("anio");
                                    anios.add(anioEnJson);
                                }
                                //SE RECOGE LOS DATOS DEL JSON
                                almacenajeArrays.setDato(jsonObject);
                            }
                            txtAnioSeleccionado.setText(anioEnJson+"-"+(Integer.parseInt(anioEnJson)+1));
                            matriculas.put(anioEnJson,almacenajeArrays);

                            // RELLENA EL LISTVIEW
                            asignaturas = (ListView) findViewById(R.id.matricula_lista);
                            AdapterListaAsignaturasMatricula adapter = new AdapterListaAsignaturasMatricula(
                                    this, almacenajeArrays.asignaturaNombres, almacenajeArrays.asignaturaCursos,
                                    almacenajeArrays.asignaturasConvocatorias, almacenajeArrays.asignaturasOrdinarias,
                                    almacenajeArrays.asignaturasExtraordinarias);
                            asignaturas.setAdapter(adapter);

                            // SE ASIGNA EL LISTENER PARA QUE ABRA EL DIALOG
                            asignarListeners();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }});

    }

    /**
     * Se asigna el listener de tal manera que al pulsar se abra un diálogo para seleccionar la
     * matrícula
     */
    private void asignarListeners(){
        LinearLayout linearLayoutAnio = (LinearLayout) findViewById(R.id.matricula_linearLayoutSeleccionAnio);
        linearLayoutAnio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogSelectAnioMatricula dialog = new DialogSelectAnioMatricula(anios);
                dialog.show(getSupportFragmentManager(),"etiquetaMatricula");
            }
        });
    }

    /**
     * Al seleccionar la matrícula en el dialog se muestran las asignaturas de esa matrícula y se
     * cambia el año escogido en el TextView
     * @param i: la posición de la matrícula elegida en el dialog
     */
    @Override
    public void seleccionAnio(int i) {
        String anioSeleccionado = anios.get(i);
        AlmacenajeDatos almacenajeDatos = matriculas.get(anioSeleccionado);
        AdapterListaAsignaturasMatricula adapter = new AdapterListaAsignaturasMatricula(
                this, almacenajeDatos.asignaturaNombres, almacenajeDatos.asignaturaCursos,
                almacenajeDatos.asignaturasConvocatorias, almacenajeDatos.asignaturasOrdinarias,
                almacenajeDatos.asignaturasExtraordinarias);
        asignaturas.setAdapter(adapter);
        txtAnioSeleccionado.setText(anioSeleccionado+"-"+(Integer.parseInt(anioSeleccionado)+1));
    }


    class AlmacenajeDatos{

        ArrayList<String> asignaturaNombres;
        ArrayList<String>  asignaturaCursos;
        ArrayList<String>  asignaturasConvocatorias;
        ArrayList<String>  asignaturasOrdinarias;
        ArrayList<String>  asignaturasExtraordinarias;

        /**
         * Constructor de AlmacenajeDatos
         */
        public AlmacenajeDatos(){
            asignaturaNombres = new ArrayList<>();
            asignaturaCursos = new ArrayList<>();
            asignaturasConvocatorias = new ArrayList<>();
            asignaturasExtraordinarias = new ArrayList<>();
            asignaturasOrdinarias = new ArrayList<>();
        }

        /**
         * Se almacenan en las listas el nombre, el curso, la convocatoria y las notas de la asignatura
         * @param jsonDato: el json con los datos de la asignatura
         */
        public void setDato(JSONObject jsonDato){
            asignaturaNombres.add((String)jsonDato.get("nombreAsignatura"));
            asignaturaCursos.add((String) jsonDato.get("curso"));
            asignaturasConvocatorias.add((String) jsonDato.get("convocatoria"));

            String notaOrd = (String) jsonDato.get("notaOrdinaria");
            String notaExtrad = (String) jsonDato.get("notaExtraordinaria");
            setNotaTabla(notaOrd,notaExtrad);
        }

        /**
         * Con los datos obtenidos del json se añade la nota obtenida o un no presentado o aún no ha
         * realizado el examen
         * @param notaOrd: nota obtenida en la convocatoria ordinaria
         * @param notaExtrad: nota obtenida en la convocatoria extraordinaria
         */
        private void setNotaTabla(String notaOrd,String notaExtrad){
            if(notaOrd.equals("-1")){
                asignaturasOrdinarias.add("-");
            }else if(notaOrd.equals("-2")){
                asignaturasOrdinarias.add(getResources().getString(R.string.matricula_notaNP));
            }else{
                asignaturasOrdinarias.add(notaOrd);
            }

            if(notaExtrad.equals("-1")){
                asignaturasExtraordinarias.add("-");
            }else if(notaExtrad.equals("-2")){
                asignaturasExtraordinarias.add(getResources().getString(R.string.matricula_notaNP));
            }else{
                asignaturasExtraordinarias.add(notaExtrad);
            }
        }
    }
}
