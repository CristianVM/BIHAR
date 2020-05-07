package com.example.bihar.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.bihar.R;
import com.example.bihar.controller.GestorLibros;
import com.example.bihar.controller.GestorProfesores;
import com.example.bihar.controller.WorkerBihar;
import com.example.bihar.model.Libro;
import com.example.bihar.utils.AdapterListaAsignaturasMatricula;
import com.example.bihar.utils.AdapterListaLibros;
import com.example.bihar.view.dialog.DialogFiltradoLibros;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Biblioteca extends AppCompatActivity implements DialogFiltradoLibros.ListenerFiltradoLibros {

    private ArrayList<Integer> imagenes;
    private ArrayList<String> autores;
    private ArrayList<String> titulares;
    private ArrayList<String> fechas;
    private ArrayList<String> idLibros;
    private ListView listView;
    private AdapterListaLibros adapterListaLibros;

    private TextView tituloToolbar;
    private CircleImageView imagenBusqueda;
    private SearchView searchView;
    private CircleImageView imagenAtras;

    private String temaEconomia;
    private String temaInformatica;
    private String temaMedicina;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biblioteca);

        listView = (ListView) findViewById(R.id.biblioteca_lista);
        tituloToolbar = (TextView) findViewById(R.id.biblioteca_tituloToolbar);
        imagenBusqueda = (CircleImageView) findViewById(R.id.biblioteca_imgBusqueda);
        imagenAtras = (CircleImageView) findViewById(R.id.biblioteca_imgAtras);
        searchView = (SearchView) findViewById(R.id.biblioteca_searchView);

        autores = new ArrayList<>();
        imagenes = new ArrayList<>();
        titulares = new ArrayList<>();
        fechas = new ArrayList<>();
        idLibros = new ArrayList<>();
        temaMedicina = "";
        temaEconomia = "";
        temaInformatica = "";


        imagenAtras.setOnClickListener( view -> {
            finish();
        });

        //Cuando pulsamos la lupa debemos hacer invisible el titulo del ToolBar y la imagen
        searchView.setOnSearchClickListener(v -> {
            tituloToolbar.setVisibility(View.INVISIBLE);
            imagenBusqueda.setVisibility(View.INVISIBLE);
        });

        //Cuando salimos de la búsqueda volvemos a hacer visible el titulo y la imagen
        searchView.setOnCloseListener(() -> {
            tituloToolbar.setVisibility(View.VISIBLE);
            imagenBusqueda.setVisibility(View.VISIBLE);
            return false;
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                limpiarArrayLists();
                List<String> filtradoLibros = GestorLibros.getGestorLibros().buscarLibro(newText);
                actualizarListadoLibros(filtradoLibros);
                if(adapterListaLibros !=null){
                    adapterListaLibros.notifyDataSetChanged();
                }
                return false;
            }
        });

        Map<String, String> map = new HashMap<>();
        map.put("accion", "consultarLibros");
        JSONObject jsonWorker = new JSONObject(map);

        Data.Builder data = new Data.Builder();
        data.putString("datos", jsonWorker.toString());

        cargarLibros(data);
    }

    private void filtrarLibros() {

        Map<String, String> map = new HashMap<>();
        map.put("accion", "consultarLibros");
        map.put("filtroInformatica", temaInformatica);
        map.put("filtroMedicina", temaMedicina);
        map.put("filtroEconomia", temaEconomia);
        JSONObject jsonFiltrado = new JSONObject(map);

        limpiarArrayLists();
        List<String> listaLibrosFiltrado = GestorLibros.getGestorLibros().filtrarLibro(jsonFiltrado.toString());
        temaInformatica="";
        temaEconomia="";
        temaMedicina="";
        actualizarListadoLibros(listaLibrosFiltrado);
        adapterListaLibros.notifyDataSetChanged();
    }

    public void dialogFiltrado(View view){
        DialogFiltradoLibros dialogFiltradoLibros = new DialogFiltradoLibros();
        dialogFiltradoLibros.show(getSupportFragmentManager(),"dialogFiltradoLibros");
    }

    private void cargarLibros(Data.Builder data) {

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
                        //String resultado = status.getOutputData().getString("result");

                        Map<String,Libro> librosMap = GestorLibros.getGestorLibros().getLibros();
                        for(Map.Entry<String,Libro> datos: librosMap.entrySet()){
                            autores.add(datos.getValue().getAutor());
                            titulares.add(datos.getValue().getTitulo());
                            fechas.add(datos.getValue().getFecha());
                            imagenes.add(R.drawable.ic_laptop_black_24dp);
                            idLibros.add(datos.getKey());
                        }

                        adapterListaLibros = new AdapterListaLibros(
                                this, imagenes, autores, titulares, fechas);
                        listView.setAdapter(adapterListaLibros);

                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                Intent intent = new Intent(Biblioteca.this, LibroInformacion.class);
                                Libro libro = GestorLibros.getGestorLibros().getInfoLibro(idLibros.get(i));
                                intent.putExtra("editorial", libro.getEditorial());
                                intent.putExtra("autor", libro.getAutor());
                                intent.putExtra("descripcion", libro.getDescripcion());
                                intent.putExtra("fecha", libro.getFecha());
                                intent.putExtra("titulo", libro.getTitulo());
                                startActivity(intent);
                            }
                        });
                    }
                });
    }

    /**
     * Se comprueba que temas de los libros se han seleccionado en el díalogo y se filtran para mostrarlos
     * en el ListView
     * @param lista: ArrayList de los temas seleccionados en el diálogo
     */
    @Override
    public void temasSeleccionados(ArrayList<String> lista) {
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).equals(getResources().getString(R.string.biblioteca_temaInformatica))) {
                temaInformatica = lista.get(i);
            } else if (lista.get(i).equals(getResources().getString(R.string.biblioteca_temaEconomia))) {
                temaEconomia = lista.get(i);
            } else {
                temaMedicina = lista.get(i);
            }
        }
        filtrarLibros();
    }

    /**
     * Vacía los ArrayList del ListView
     */
    private void limpiarArrayLists(){
        autores.clear();
        titulares.clear();
        fechas.clear();
        imagenes.clear();
    }

    /**
     * Rellena los ArrayList para el ListView con los libros recogidos.
     * @param listaFiltrada: Lista de ids de los libros
     */
    private void actualizarListadoLibros(List<String> listaFiltrada){
        for(String id: listaFiltrada){
            Libro libro = GestorLibros.getGestorLibros().getInfoLibro(id);
            autores.add(libro.getAutor());
            titulares.add(libro.getTitulo());
            fechas.add(libro.getFecha());
            imagenes.add(R.drawable.ic_laptop_black_24dp);
        }
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            tituloToolbar.setVisibility(View.VISIBLE);
            imagenAtras.setVisibility(View.VISIBLE);
            searchView.onActionViewCollapsed();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("visible", tituloToolbar.getVisibility());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int visible = savedInstanceState.getInt("visible");
        tituloToolbar.setVisibility(visible);
        imagenAtras.setVisibility(visible);
    }
}
