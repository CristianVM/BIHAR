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
import android.widget.ListView;
import android.widget.RadioButton;

import com.example.bihar.R;
import com.example.bihar.controller.GestorLibros;
import com.example.bihar.controller.WorkerBihar;
import com.example.bihar.utils.AdapterListaAsignaturasMatricula;
import com.example.bihar.utils.AdapterListaLibros;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Biblioteca extends AppCompatActivity {

    private ArrayList<Integer> imagenes;
    private ArrayList<String> autores;
    private ArrayList<String> titulares;
    private ArrayList<String> fechas;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biblioteca);

        autores = new ArrayList<>();
        imagenes = new ArrayList<>();
        titulares = new ArrayList<>();
        fechas = new ArrayList<>();
        listView = (ListView) findViewById(R.id.biblioteca_lista);

        Map<String,String> map = new HashMap<>();
        map.put("accion","consultarLibros");
        map.put("filtrado","NO");
        JSONObject jsonWorker = new JSONObject(map);

        Data.Builder data = new Data.Builder();
        data.putString("datos",jsonWorker.toString());

        //worker(data);
    }

    public void filtrarLibros(View vista){

        String tema = temaFiltrado();

        Map<String,String> map = new HashMap<>();
        map.put("accion","consultarLibros");
        map.put("filtrado",tema);

        JSONObject jsonWorker = new JSONObject(map);

        Data.Builder data = new Data.Builder();
        data.putString("datos",jsonWorker.toString());

        worker(data);
    }

    private String temaFiltrado(){

        RadioButton rInformatica = (RadioButton) findViewById(R.id.biblioteca_radio_informatica);
        RadioButton rEconomia = (RadioButton) findViewById(R.id.biblioteca_radio_economia);
        RadioButton rMedicina = (RadioButton) findViewById(R.id.biblioteca_radio_medicina);

        if(rInformatica.isChecked()){
            return getResources().getText(R.string.biblioteca_temaInformatica).toString();
        }else if(rEconomia.isChecked()){
            return getResources().getText(R.string.biblioteca_temaEconomia).toString();
        }else if(rMedicina.isChecked()){
            return getResources().getText(R.string.biblioteca_temaMedicina).toString();
        }

        return "";
    }

    private void worker(Data.Builder data){

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
                        GestorLibros.getGestorLibros().addLibro(resultado);
                        if(!resultado.equals("Fail")){
                            JSONParser parser = new JSONParser();
                            try{
                                Log.i("Bibliotecaa",resultado+"kkk");
                                JSONArray jsonArray = (JSONArray) parser.parse(resultado);

                                for(int i=0; i<jsonArray.size();i++){
                                    JSONObject json = (JSONObject) jsonArray.get(i);
                                    Log.i("HOLA",(String) json.get("fecha")+"kkk");
                                    autores.add((String) json.get("autor"));
                                    titulares.add((String) json.get("titulo"));
                                    fechas.add((String) json.get("fecha"));
                                    imagenes.add(R.drawable.ic_laptop_black_24dp);
                                }

                                AdapterListaLibros adapterListaLibros = new AdapterListaLibros(
                                        this,imagenes,autores,titulares,fechas);
                                listView.setAdapter(adapterListaLibros);

                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }else{
                            autores.clear();
                            titulares.clear();
                            fechas.clear();
                            imagenes.clear();
                            AdapterListaLibros adapterListaLibros = new AdapterListaLibros(
                                    this,imagenes,autores,titulares,fechas);
                            listView.setAdapter(adapterListaLibros);

                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    Intent intent = new Intent(Biblioteca.this,LibroInformacion.class);

                                }
                            });
                        }
                    }});
    }
}
