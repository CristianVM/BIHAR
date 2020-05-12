package com.example.bihar.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.bihar.R;
import com.example.bihar.controller.GestorMatriculas;
import com.example.bihar.controller.GestorUsuario;
import com.example.bihar.controller.WorkerBihar;
import com.example.bihar.model.AlmacenajeMatricula;
import com.example.bihar.model.MatriculaAnios;
import com.example.bihar.utils.AdapterListaAsignaturasMatricula;
import com.example.bihar.view.fragments.ToolBar;

import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Matricula extends AppCompatActivity{

    private TextView txtAnioSeleccionado;
    private ListView asignaturas;
    private String anioMatriculaMostrado;
    private String idPersona;

    private String idiomaEstablecido;
    private boolean listaAñadida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listaAñadida = false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        idiomaEstablecido = prefs.getString("idioma","es");
        if(idiomaEstablecido.equals("es")){
            Locale locale = new Locale("es");
            cambiarIdiomaOnCreate(locale);
        }else if(idiomaEstablecido.equals("eu")){
            Locale locale = new Locale("eu");
            cambiarIdiomaOnCreate(locale);
        }

        setContentView(R.layout.activity_matricula);

        txtAnioSeleccionado = findViewById(R.id.matricula_seleccionAnio);
        idPersona = GestorUsuario.getGestorUsuario().getUsuario().getIdUsuario();

        // SE CAMBIA EL NOMBRE DE LA TOOLBAR
        ToolBar toolBar = (ToolBar) getSupportFragmentManager().findFragmentById(R.id.frgmt_toolbarMatricula);
        toolBar.cambiarTituloToolbar(getResources().getString(R.string.matricula));


        // SE VAN METIENDO LOS DATOS PARA MANDARLE LA PETICION A LA BASE DE DATOS
        Map<String,String> map = new HashMap<>();
        map.put("idPersona",idPersona);
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
                   if (status != null && status.getState().isFinished()) {
                       rellenarListView();
                   }
               });
    }

    /**
     * Se asigna el listener de tal manera que al pulsar se abra un diálogo para seleccionar la
     * matrícula
     */
    private void asignarListeners(){
        LinearLayout linearLayoutAnio = (LinearLayout) findViewById(R.id.matricula_linearLayoutSeleccionAnio);
        linearLayoutAnio.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getText(R.string.dialog_matricaTxtSelectAnio));
            String[] anios = new String[GestorMatriculas.gestorMatriculas().getMatriculas(idPersona).getAnios().size()];
            anios = GestorMatriculas.gestorMatriculas().getMatriculas(idPersona).getAnios().toArray(anios);

            builder.setItems(anios, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    seleccionAnio(i);
                }
            });

            AlertDialog dialog = builder.create();

            dialog.show();
        });
    }

    /**
     * Al seleccionar la matrícula en el dialog se muestran las asignaturas de esa matrícula y se
     * cambia el año escogido en el TextView
     * @param i: la posición de la matrícula elegida en el dialog
     */
    public void seleccionAnio(int i) {
        String anioSeleccionado = GestorMatriculas.gestorMatriculas().getMatriculas(idPersona).getAnios().get(i);
        AlmacenajeMatricula datos = GestorMatriculas.gestorMatriculas().getMatriculas(idPersona).getMatriculas().get(anioSeleccionado);

        asignaturas.setAdapter(crearAdapter(datos));

        txtAnioSeleccionado.setText(anioSeleccionado+"-"+(Integer.parseInt(anioSeleccionado)+1));
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

        // SI YA SE HABÍA CARGADO LA LISTA ANTERIORMENTE Y LA ACTIVIDAD VUELVE A REANUDARSE
        if(listaAñadida){
            rellenarListView();
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

    /**
     * Se rellenan la ListVIew con los datos que se han recogido de la base de datos que están en el
     * GestorMatriculas
     */
    private void rellenarListView(){
        MatriculaAnios matriculaAnios = GestorMatriculas.gestorMatriculas().getMatriculas(idPersona);
        Map<String, AlmacenajeMatricula> matriculaMap = matriculaAnios.getMatriculas();

        for (Map.Entry<String, AlmacenajeMatricula> datos : matriculaMap.entrySet()) {
            anioMatriculaMostrado = datos.getKey();
            txtAnioSeleccionado.setText(anioMatriculaMostrado + "-" + (Integer.parseInt(anioMatriculaMostrado) + 1));
            asignaturas = (ListView) findViewById(R.id.matricula_lista);
            AlmacenajeMatricula almacenajeMatricula = datos.getValue();

            asignaturas.setAdapter(crearAdapter(almacenajeMatricula));
            // SE ASIGNA EL LISTENER PARA QUE ABRA EL DIALOG
            asignarListeners();

            listaAñadida = true;
            break;
        }
    }

    /**
     * Dependiendo del idioma, crea un adapter u otro
     * @param almacenajeMatricula: datos de la matrícula
     * @return: el adapter
     */
    private AdapterListaAsignaturasMatricula crearAdapter(AlmacenajeMatricula almacenajeMatricula){
        AdapterListaAsignaturasMatricula adapter = null;
        if (idiomaEstablecido.equals("es")) {
            adapter = new AdapterListaAsignaturasMatricula(
                    this, almacenajeMatricula.asignaturaNombres, almacenajeMatricula.asignaturaCursos,
                    almacenajeMatricula.asignaturasConvocatorias, almacenajeMatricula.asignaturasExtraordinarias,
                    almacenajeMatricula.asignaturasOrdinarias);
        } else {
            adapter = new AdapterListaAsignaturasMatricula(
                    this, almacenajeMatricula.asignaturasNombresEuskera, almacenajeMatricula.asignaturaCursos,
                    almacenajeMatricula.asignaturasConvocatorias, almacenajeMatricula.asignaturasExtraordinarias,
                    almacenajeMatricula.asignaturasOrdinarias);
        }
        return adapter;
    }
}
