package com.example.bihar.view.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.bihar.R;
import com.example.bihar.controller.GestorPracticas;
import com.example.bihar.controller.WorkerBihar;
import com.example.bihar.model.Practica;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class Practicas extends AppCompatActivity {

    private ArrayList<String> IDs = new ArrayList<>();
    private ArrayList<String> lugares = new ArrayList<>();
    private ArrayList<String> empresas = new ArrayList<>();
    private ArrayList<String> descripciones = new ArrayList<>();
    private AdapterPracticas adapterPracticas;

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

        super.setContentView(R.layout.lista_practicas);

        cargarPracticas();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String idiomaNuevo = sharedPreferences.getString("idioma","es");

        if(!idiomaNuevo.equals(idiomaEstablecido)){
            idiomaEstablecido = idiomaNuevo;
            if(idiomaEstablecido.equals("es")) {
                Locale locale = new Locale("es");
                cambiarIdiomaOnResume(locale);
            } else if(idiomaEstablecido.equals("eu")) {
                Locale locale = new Locale("eu");
                cambiarIdiomaOnResume(locale);
            }
        }
    }

    private void cargarPracticas() {

        comenzarCarga();

        JSONObject parametrosJSON = new JSONObject();
        parametrosJSON.put("accion", "obtenerPracticas");
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
                                Map<String, Practica> mapPracticas = GestorPracticas.getGestorPracticas().getPracticas();
                                for (Map.Entry<String, Practica> datos : mapPracticas.entrySet()) {
                                    IDs.add(datos.getKey());
                                    if (idiomaEstablecido.equals("es")) {
                                        lugares.add(datos.getValue().getProvincia_es() + "\n" + datos.getValue().getLocalidad_es());
                                    } else {
                                        lugares.add(datos.getValue().getProvincia_eu() + "\n" + datos.getValue().getLocalidad_eu());
                                    }
                                    empresas.add(datos.getValue().getNombreEmpresa());
                                    descripciones.add(datos.getValue().getTitulo());
                                }

                                RecyclerView listaPracticas = findViewById(R.id.listaPracticas);

                                adapterPracticas = new AdapterPracticas(IDs, lugares, empresas, descripciones, Practicas.this);
                                listaPracticas.setAdapter(adapterPracticas);

                                GridLayoutManager elLayoutRejillaIgual = new GridLayoutManager(getApplicationContext(), 1, GridLayoutManager.VERTICAL, false);
                                listaPracticas.setLayoutManager(elLayoutRejillaIgual);

                                listaPracticas.addItemDecoration(new GridSpacingItemDecoration(1, 20, true));

                                // Si salta algun error
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), R.string.error_general, Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            } finally {
                                terminarCarga();
                            }
                        }
                    }
                });

        WorkManager.getInstance(this).enqueue(otwr);
    }

    private void comenzarCarga() {
        ProgressBar progressBarPracticas = findViewById(R.id.progressBarPracticas);
        progressBarPracticas.setVisibility(View.VISIBLE);

        ChipGroup chipGroup = findViewById(R.id.chipGroup);
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            chip.setClickable(false);
        }
    }

    private void terminarCarga() {
        ProgressBar progressBarPracticas = findViewById(R.id.progressBarPracticas);
        progressBarPracticas.setVisibility(View.INVISIBLE);

        ChipGroup chipGroup = findViewById(R.id.chipGroup);
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            chip.setClickable(true);
        }
    }

    // https://stackoverflow.com/questions/58224630/how-to-get-selected-chips-from-chipgroup
    public void filtrarPracticas(View v) {
        try {
            ArrayList<String> provinciasSeleccionadas = new ArrayList<>();
            ChipGroup chipGroup = findViewById(R.id.chipGroup);
            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                Chip chip = (Chip) chipGroup.getChildAt(i);
                if (chip.isChecked()) {
                    provinciasSeleccionadas.add((String) chip.getText());
                }
            }

            vaciarDatos();

            Map<String, Practica> mapPracticas = GestorPracticas.getGestorPracticas().getPracticas();

            for (Map.Entry<String, Practica> datos : mapPracticas.entrySet()) {
                if (provinciasSeleccionadas.contains(datos.getValue().getProvincia_es()) || provinciasSeleccionadas.contains(datos.getValue().getProvincia_eu())) {
                    IDs.add(datos.getKey());
                    if (idiomaEstablecido.equals("es")) {
                        lugares.add(datos.getValue().getProvincia_es() + "\n" + datos.getValue().getLocalidad_es());
                    } else {
                        lugares.add(datos.getValue().getProvincia_eu() + "\n" + datos.getValue().getLocalidad_eu());
                    }
                    empresas.add(datos.getValue().getNombreEmpresa());
                    descripciones.add(datos.getValue().getTitulo());
                }
            }

            adapterPracticas.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void vaciarDatos() {
        IDs.clear();
        lugares.clear();
        empresas.clear();
        descripciones.clear();
    }

    /** Cambia el idioma de la aplicación al reanudarse la actividad. Se destruye la actividad y se
     *  vuelve a iniciar
     *  @param locale: el idioma almacenado en SharedPreferences
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

    /** Cambia el idioma de la aplicación al crearse la actividad
     *  @param locale: el idioma almacenado en SharedPreferences
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

class ViewHolderPracticas extends RecyclerView.ViewHolder {

    public CardView cardViewPractica;
    public TextView IDPractica;
    public TextView lugarPractica;
    public TextView empresaPractica;
    public TextView descripcionPractica;

    public ViewHolderPracticas(View itemView) {
        super(itemView);
        cardViewPractica = itemView.findViewById(R.id.cardViewPractica);
        IDPractica = itemView.findViewById(R.id.IDPractica);
        lugarPractica = itemView.findViewById(R.id.lugarPractica);
        empresaPractica = itemView.findViewById(R.id.empresaPractica);
        descripcionPractica = itemView.findViewById(R.id.descripcionPractica);
    }
}

class AdapterPracticas extends RecyclerView.Adapter<ViewHolderPracticas> {

    private ArrayList<String> losIDs;
    private ArrayList<String> losLugares;
    private ArrayList<String> lasEmpresas;
    private ArrayList<String> lasDescripciones;
    private Context elContexto;

    public AdapterPracticas(ArrayList<String> IDs, ArrayList<String> lugares, ArrayList<String> empresas, ArrayList<String> descripciones, Context contexto) {
        losIDs = IDs;
        losLugares = lugares;
        lasEmpresas = empresas;
        lasDescripciones = descripciones;
        elContexto = contexto;
    }

    public ViewHolderPracticas onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View elLayoutFila = LayoutInflater.from(parent.getContext()).inflate(R.layout.lista_practicas_elemento, null);
        ViewHolderPracticas viewHolderPracticas = new ViewHolderPracticas(elLayoutFila);
        return viewHolderPracticas;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderPracticas holder, int position) {
        holder.IDPractica.setText(losIDs.get(position));
        holder.lugarPractica.setText(losLugares.get(position));
        holder.empresaPractica.setText(lasEmpresas.get(position));
        holder.descripcionPractica.setText(lasDescripciones.get(position));
        holder.cardViewPractica.setOnClickListener(v -> {
            Intent intent = new Intent(elContexto, PracticaInformacion.class);
            intent.putExtra("IDPractica", holder.IDPractica.getText());
            elContexto.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return losIDs.size();
    }
}
