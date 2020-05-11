package com.example.bihar.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bihar.R;
import com.example.bihar.controller.GestorUsuario;
import com.example.bihar.controller.WorkerBihar;
import com.example.bihar.model.Asignatura;
import com.example.bihar.model.Usuario;
import com.example.bihar.view.fragments.ToolBar;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NotasProfesor extends AppCompatActivity {

    private ExpandableListView expandableListView;

    private HashMap<String, AsignaturaImparte> imparte;
    private String[] asignaturasCastellano;
    private String[] asignaturasEuskera;
    private String[] idAsignaturas;

    private String idiomaEstablecido;
    private boolean cargado;


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

        setContentView(R.layout.activity_notas_profesor);

        cargado = false;
        imparte = new HashMap<>();
        expandableListView = findViewById(R.id.notasProfesor_ExpandableListView);

        /*Listener para que solo haya un Grupo expandido.
         * Pregunta: https://stackoverflow.com/questions/7862396/show-only-one-child-of-expandable-list-at-a-time/15856032
         * Autor: https://stackoverflow.com/users/413127/blundell
         */

        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int previousGroup = -1;

            @Override
            public void onGroupExpand(int groupPosition) {
                if (groupPosition != previousGroup)
                    expandableListView.collapseGroup(previousGroup);
                previousGroup = groupPosition;
            }
        });

        ToolBar toolBar = (ToolBar) getSupportFragmentManager().findFragmentById(R.id.frgmt_toolbarNotasProfesor);
        toolBar.cambiarTituloToolbar(getResources().getString(R.string.notasprofesor));
        cargarDatos();
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

        if(cargado){
            expandableListView.setAdapter(new MyExpandableListAdapterNotasProfesor(this,
                    imparte, asignaturasCastellano, asignaturasEuskera, idAsignaturas));

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

    private void cargarDatos() {
        Map<String, String> map = new HashMap<>();
        map.put("accion", "obtenerNotasProfesor");
        map.put("idPersona", GestorUsuario.getGestorUsuario().getUsuario().getIdUsuario());
        JSONObject json = new JSONObject(map);

        Data.Builder data = new Data.Builder();
        data.putString("datos", json.toString());

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
                            JSONObject jsonResultado = (JSONObject) parser.parse(resultado);
                            JSONArray arrayImparte = (JSONArray) jsonResultado.get("profesorImparte");
                            JSONArray arrayAlumnosImpartidos = (JSONArray) jsonResultado.get("alumnosAsignatura");
                            asignaturasCastellano = new String[arrayImparte.size()];
                            asignaturasEuskera = new String[arrayImparte.size()];
                            idAsignaturas = new String[arrayImparte.size()];

                            for(int i=0; i< arrayImparte.size();i++){
                                JSONObject jsonAsignatura = (JSONObject) arrayImparte.get(i);
                                String idAsignatura = (String) jsonAsignatura.get("idAsignatura");

                                asignaturasEuskera[i] = (String) jsonAsignatura.get("nombreAsignaturaEuskera");
                                asignaturasCastellano[i] = (String) jsonAsignatura.get("nombreAsignatura");
                                idAsignaturas[i] = idAsignatura;

                                AsignaturaImparte asignaturaImparte=null;
                                for(int j=0;j<arrayAlumnosImpartidos.size();j++){
                                    JSONObject jsonObjectAlumnos = (JSONObject) arrayAlumnosImpartidos.get(j);
                                    if(jsonObjectAlumnos.containsKey(idAsignatura)){
                                        JSONArray arrayAlumnos = (JSONArray) jsonObjectAlumnos.get(idAsignatura);
                                        asignaturaImparte = new AsignaturaImparte();
                                        for(int k=0; k< arrayAlumnos.size();k++){
                                            JSONObject alumno = (JSONObject) arrayAlumnos.get(k);
                                            asignaturaImparte.addAlumno((String) alumno.get("nombreCompleto"), (String) alumno.get("token"), (String) alumno.get("idPersona"));
                                        }
                                    }
                                }
                                imparte.put(idAsignatura,asignaturaImparte);
                            }

                            expandableListView.setAdapter(new MyExpandableListAdapterNotasProfesor(this,
                                    imparte, asignaturasCastellano, asignaturasEuskera, idAsignaturas));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        } catch (ClassCastException e1) {
                            JSONObject jsonInicial = null;
                            try {
                                jsonInicial = (JSONObject) parser.parse(resultado);
                                JSONArray arrayImparte = (JSONArray) jsonInicial.get("profesorImparte");
                                asignaturasCastellano = new String[arrayImparte.size()];
                                asignaturasEuskera = new String[arrayImparte.size()];
                                idAsignaturas = new String[arrayImparte.size()];

                                JSONObject listadoAsignaturasJSON = (JSONObject) jsonInicial.get("alumnosAsignatura");

                                for (int i = 0; i < arrayImparte.size(); i++) {
                                    JSONObject jsonAsignatura = (JSONObject) arrayImparte.get(i);
                                    String idAsignatura = (String) jsonAsignatura.get("idAsignatura");

                                    JSONArray jsonArrayAlumnos = (JSONArray) listadoAsignaturasJSON.get(idAsignatura);
                                    asignaturasEuskera[i] = (String) jsonAsignatura.get("nombreAsignaturaEuskera");
                                    asignaturasCastellano[i] = (String) jsonAsignatura.get("nombreAsignatura");
                                    idAsignaturas[i] = idAsignatura;
                                    AsignaturaImparte asignaturaImparte = new AsignaturaImparte();
                                    for (int j = 0; j < jsonArrayAlumnos.size(); j++) {
                                        JSONObject alumno = (JSONObject) jsonArrayAlumnos.get(j);
                                        asignaturaImparte.addAlumno((String) alumno.get("nombreCompleto"), (String) alumno.get("token"), (String) alumno.get("idPersona"));
                                    }
                                    imparte.put(idAsignatura, asignaturaImparte);
                                }
                                expandableListView.setAdapter(new MyExpandableListAdapterNotasProfesor(this,
                                        imparte, asignaturasCastellano, asignaturasEuskera, idAsignaturas));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }
                    cargado = true;
                    }
                });

        WorkManager.getInstance(this).enqueue(trabajo);
    }

// ##################################################################################################

}

class MyExpandableListAdapterNotasProfesor extends BaseExpandableListAdapter {

    private Activity activity;

    private HashMap<String, AsignaturaImparte> imparte;
    private String[] cast;
    private String[] eusk;
    private String[] idAsignaturas;

    MyExpandableListAdapterNotasProfesor(Activity pActivity, HashMap<String, AsignaturaImparte> imparteMap, String[] cast, String[] eusk, String[] idAsignaturas) {
        activity = pActivity;
        imparte = imparteMap;
        this.cast = cast;
        this.eusk = eusk;
        this.idAsignaturas = idAsignaturas;
    }

    @Override
    public int getGroupCount() {
        return idAsignaturas.length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return imparte.get(idAsignaturas[groupPosition]).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return idAsignaturas[groupPosition];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return imparte.get(idAsignaturas[groupPosition]).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition * childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.asignaturas_imparte_listgroup, parent, false);

        if (!isExpanded)
            convertView.setPadding(0, 0, 0, 75);
        else
            convertView.setPadding(0, 0, 0, 0);

        TextView asignaturaTxtView = convertView.findViewById(R.id.imparte_txtNombreAsignatura);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String idioma = prefs.getString("idioma","es");
        if(idioma.equals("es")){
            asignaturaTxtView.setText(cast[groupPosition]);
        }else{
            asignaturaTxtView.setText(eusk[groupPosition]);
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.imparte_alumnos_listitem, parent, false);

        if (isLastChild) {
            convertView.setPadding(0, 0, 0, 75);
        } else {
            convertView.setPadding(0, 0, 0, 0);
        }

        Map<String, String> map = (Map<String, String>) getChild(groupPosition, childPosition);
        String nombreUsuario = map.get("nombreAlumno");
        String token = map.get("token");
        String idPersona = map.get("id");

        TextView nombreAlumno = (TextView) convertView.findViewById(R.id.imparte_nombreAlumno);
        nombreAlumno.setText(nombreUsuario);

        Button addNota = (Button) convertView.findViewById(R.id.imparte_ponerNota);
        addNota.setOnClickListener(view -> {
            crearDialog(nombreUsuario, idPersona, token,(String) getGroup(groupPosition),2019,groupPosition);
        });

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }


    private void crearDialog(String nombreUsuario, String idPersona, String token,String idAsignatura, int anio,int group) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_profesor_aniadenota, null);
        builder.setView(view);

        TextView nombreAlumno = (TextView) view.findViewById(R.id.dialog_notaprofesor_alumno);
        nombreAlumno.setText(nombreUsuario);

        AlertDialog dialog = builder.create();

        Button addNota = (Button) view.findViewById(R.id.dialog_notaprofesor_boton);
        addNota.setOnClickListener(vista -> {
            EditText editText = (EditText) view.findViewById(R.id.dialog_notaprofesor_nota);
            if (!editText.getText().toString().equals("")) {
                double nota = Double.parseDouble(editText.getText().toString());
                if (nota > -1 && nota < 11) {
                    Map<String,String> map = new HashMap<>();
                    map.put("idPersona",idPersona);
                    map.put("token",token);
                    map.put("anio",String.valueOf(anio));
                    map.put("idAsignatura",idAsignatura);
                    map.put("nota",String.valueOf(nota));
                    map.put("asignatura",cast[group]);
                    map.put("asignaturaEuskera",eusk[group]);
                    map.put("accion","notaProfesor");
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
                    map.put("idioma",prefs.getString("idioma","es"));
                    Switch sw = (Switch) view.findViewById(R.id.dialog_notaprofesor_switch);
                    if(sw.isChecked()){
                        map.put("ordinaria","si");
                    }else{
                        map.put("ordinaria","no");
                    }
                    JSONObject jsonNota = new JSONObject(map);

                    Data.Builder data = new Data.Builder();
                    data.putString("datos", jsonNota.toString());

                    Constraints restricciones = new Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build();
                    OneTimeWorkRequest trabajo = new OneTimeWorkRequest.Builder(WorkerBihar.class)
                            .setConstraints(restricciones)
                            .setInputData(data.build())
                            .build();


                    WorkManager.getInstance(activity).enqueue(trabajo);
                    dialog.dismiss();
                }else{
                    Toast.makeText(activity,activity.getResources().getString(R.string.dialog_notaprofesor_notaError),Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(activity,activity.getResources().getString(R.string.dialog_notaprofesor_vacio),Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

}


// #######################################################################################################
class AsignaturaImparte {

    private List<String> nombresAlumnos;
    private List<String> tokens;
    private List<String> ids;

    public AsignaturaImparte() {
        nombresAlumnos = new ArrayList<>();
        tokens = new ArrayList<>();
        ids = new ArrayList<>();
    }

    public void addAlumno(String nombre, String token, String id) {
        nombresAlumnos.add(nombre);
        tokens.add(token);
        ids.add(id);
    }

    public int size() {
        return nombresAlumnos.size();
    }

    public Object get(int i) {
        Map<String, String> map = new HashMap<>();
        map.put("nombreAlumno", nombresAlumnos.get(i));
        map.put("token", tokens.get(i));
        map.put("id", ids.get(i));
        return map;
    }
}
