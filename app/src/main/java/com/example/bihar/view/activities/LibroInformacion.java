package com.example.bihar.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.Map;

public class LibroInformacion extends AppCompatActivity {

    private String[] universidades;
    private String[] disponibles;
    private boolean[] estaDisponible;

    private String usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_libro_informacion);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        usuario = prefs.getString("idUsuario","");

        Bundle bundle = getIntent().getExtras();

        Map<String, String> map = new HashMap<>();
        if(bundle !=null){
            map.put("accion", "consultarReservaLibro");
            map.put("titulo",bundle.getString("titulo"));

            rellenarDatosLibro(
                    bundle.getString("editorial"),
                    bundle.getString("autor"),
                    bundle.getString("descripcion"),
                    bundle.getString("fecha"),
                    bundle.getString("titulo"));
        }

        // SE RECOGE LOS DATOS PARA ENVIARSELOS AL WORKER
        JSONObject jsonWorker = new JSONObject(map);
        Data.Builder data = new Data.Builder();
        data.putString("datos", jsonWorker.toString());

        // SE CREA LA RESTRICCION DE QUE ES INTERNET NECESARIO
        Constraints restricciones = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        // SE CREA EL WORKER
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
                            // SE OBTIENE EL JSON OBTENIDO
                            JSONObject jsonResultado = (JSONObject) parser.parse(resultado);
                            JSONArray jsonArrayReservas = (JSONArray) jsonResultado.get("reservas");
                            JSONArray jsonArrayUniversidades = (JSONArray) jsonResultado.get("universidades");

                            // SE INICIALIZAN LOS ARRAYS
                            universidades = new String[jsonArrayUniversidades.size()];
                            disponibles = new String[jsonArrayUniversidades.size()];
                            estaDisponible = new boolean[jsonArrayUniversidades.size()];
                            String[] idLibros = new String[jsonArrayUniversidades.size()];
                            String[] latitudes = new String[jsonArrayUniversidades.size()];
                            String[] longitudes = new String[jsonArrayUniversidades.size()];

                            // SE OBTIENEN LOS DATOS DEL JSON
                            for (int i = 0; i < jsonArrayUniversidades.size(); i++) {
                                JSONObject universidad = (JSONObject) jsonArrayUniversidades.get(i);

                                universidades[i] = (String) universidad.get("nombreCentro");
                                String idLibro = (String) universidad.get("idLibro");
                                idLibros[i] = idLibro;
                                latitudes[i] = (String) universidad.get("latitud");
                                longitudes[i] = (String) universidad.get("longitud");
                                boolean disponible = true;
                                int j = 0;
                                while (j < jsonArrayReservas.size() && disponible) {
                                    JSONObject reserva = (JSONObject) jsonArrayReservas.get(j);

                                    // SE COMPRUEBA SI NO ESTÁ DISPONIBLE EL LIBRO
                                    if (idLibro.equals((String) reserva.get("idLibro"))) {
                                        disponibles[i] = getResources().getText(R.string.libroInformacion_libroNoDisponible).toString()
                                                + reserva.get("fechaFin");
                                        disponible = false;
                                    }
                                    j++;
                                }
                                // EL PRODUCTO ESTÁ DISPONIBLE
                                if (disponible) {
                                    disponibles[i] = getResources().getText(R.string.libroInformacion_libroDisponible).toString();
                                }
                                estaDisponible[i] = disponible;
                            }

                            // SE INICIALIZA EL LISTVIEW
                            AdapterListaUniversidades adapter = new AdapterListaUniversidades(this, universidades,
                                    disponibles, estaDisponible,idLibros,usuario,this,latitudes,
                                    longitudes,this);
                            ListView listView = (ListView) findViewById(R.id.libroInformacion_listaUniversidades);
                            listView.setScrollContainer(false);
                            listView.setAdapter(adapter);
                        }catch (ParseException e){
                            e.printStackTrace();
                        }catch (ClassCastException e1) {
                            try {
                                // CASO DE QUE TODOS LOS LIBROS ESTÁN DISPONIBLES, NO HAY NINGUNA RESERVA HECHA
                                JSONObject jsonResultado = (JSONObject) parser.parse(resultado);
                                JSONArray jsonArrayUniversidades = (JSONArray) jsonResultado.get("universidades");

                                // SE INICIALIZAN LOS ARRAYS
                                universidades = new String[jsonArrayUniversidades.size()];
                                disponibles = new String[jsonArrayUniversidades.size()];
                                estaDisponible = new boolean[jsonArrayUniversidades.size()];
                                String[] idLibros = new String[jsonArrayUniversidades.size()];
                                String[] latitudes = new String[jsonArrayUniversidades.size()];
                                String[] longitudes = new String[jsonArrayUniversidades.size()];

                                // SE OBTIENEN LOS DATOS DEL JSON
                                for (int i = 0; i < jsonArrayUniversidades.size(); i++) {
                                    JSONObject universidad = (JSONObject) jsonArrayUniversidades.get(i);

                                    universidades[i] = (String) universidad.get("nombreCentro");
                                    disponibles[i] = getResources().getText(R.string.libroInformacion_libroDisponible).toString();
                                    estaDisponible[i] = true;
                                    idLibros[i] = (String) universidad.get("idLibro");
                                    latitudes[i] = (String) universidad.get("latitud");
                                    longitudes[i] = (String) universidad.get("longitud");

                                    // SE INICIALIZA EL LISTVIEW
                                    AdapterListaUniversidades adapter = new AdapterListaUniversidades(this, universidades,
                                            disponibles, estaDisponible, idLibros, "ulopez", this,latitudes,
                                            longitudes,this);
                                    ListView listView = (ListView) findViewById(R.id.libroInformacion_listaUniversidades);
                                    listView.setScrollContainer(false);
                                    listView.setAdapter(adapter);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

        abrirDesplegables();
    }


    /**
     * Al hacerle click en cualquiera de los dos LineasLayout se abrirá su supuesto menú, ya sea el ménu
     * del libro o el menú de las localizaciones del libro
     */
    private void abrirDesplegables() {
        LinearLayout lyInformacionLibro = (LinearLayout) findViewById(R.id.libroInformacion_lyDesplegableInfoLibro);
        LinearLayout lyListaUniversidades = (LinearLayout) findViewById(R.id.libroInformacion_lyDesplegableUniversidades);

        lyInformacionLibro.setOnClickListener(view -> {
            LinearLayout infoLibro = (LinearLayout) findViewById(R.id.libroInformacion_linearLayoutContenidoInfo);
            if (infoLibro.getVisibility() == View.VISIBLE) {
                infoLibro.setVisibility(View.GONE);
            } else {
                infoLibro.setVisibility(View.VISIBLE);
            }
        });

        lyListaUniversidades.setOnClickListener(view -> {
            RelativeLayout infoLibro = (RelativeLayout) findViewById(R.id.libroInformacion_layoutListaUniversidades);
            if (infoLibro.getVisibility() == View.VISIBLE) {
                infoLibro.setVisibility(View.GONE);
            } else {
                infoLibro.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Se rellena los TextView con la información del libro
     * @param editorial: editorial del libro
     * @param autor: autor del libro
     * @param descripcion: una descripción del libro
     * @param fecha: fecha de publicación
     * @param titulo: titulo del libro
     */
    private void rellenarDatosLibro(String editorial,String autor, String descripcion, String fecha,String titulo){
        TextView txtEditorial = (TextView) findViewById(R.id.libroInformacion_editorial);
        txtEditorial.setText(editorial);
        TextView txtAutor = (TextView) findViewById(R.id.libroInformacion_escritor);
        txtAutor.setText(autor);
        TextView txtDescrip = (TextView) findViewById(R.id.libroInformacion_descripcion);
        txtDescrip.setText(descripcion);
        TextView txtFecha = (TextView) findViewById(R.id.libroInformacion_publicacion);
        txtFecha.setText(fecha);

        TextView txtTitulo = (TextView) findViewById(R.id.libroInformacion_titulo);
        txtTitulo.setText(titulo);
    }
}
