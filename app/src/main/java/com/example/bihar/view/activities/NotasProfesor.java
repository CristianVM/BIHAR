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

        idiomaEstablecido = prefs.getString("idioma", "es");
        if (idiomaEstablecido.equals("es")) {
            Locale locale = new Locale("es");
            cambiarIdiomaOnCreate(locale);
        } else if (idiomaEstablecido.equals("eu")) {
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

        // SE CAMBIA EL TITULO DEL TOOLBAR
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

        // SE CAMBIA EL ADAPTER CUANDO SE HA CAMBIADO EL IDIOMA
        if (cargado) {
            expandableListView.setAdapter(new MyExpandableListAdapterNotasProfesor(this,
                    imparte, asignaturasCastellano, asignaturasEuskera, idAsignaturas));
        }

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

    /**
     * Se recogen los las asignaturas que imparte el profesor y los alumnos que están matriculados en
     * cada asignatura, y los almacena en la lista expandible
     */
    private void cargarDatos() {
        Map<String, String> map = new HashMap<>();
        map.put("accion", "obtenerNotasProfesor");
        map.put("idPersona", GestorUsuario.getGestorUsuario().getUsuario().getIdUsuario());
        map.put("anio","2019");
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
                            // CASO DE QUE EL PROFESOR IMPARTE MÁS DE UNA ASIGNATURA
                            JSONObject jsonResultado = (JSONObject) parser.parse(resultado);
                            JSONArray arrayImparte = (JSONArray) jsonResultado.get("profesorImparte");
                            JSONArray arrayAlumnosImpartidos = (JSONArray) jsonResultado.get("alumnosAsignatura");
                            asignaturasCastellano = new String[arrayImparte.size()];
                            asignaturasEuskera = new String[arrayImparte.size()];
                            idAsignaturas = new String[arrayImparte.size()];

                            // RECORRE LAS ASIGNATURAS QUE IMPARTE EL PROFESOR
                            for (int i = 0; i < arrayImparte.size(); i++) {
                                JSONObject jsonAsignatura = (JSONObject) arrayImparte.get(i);

                                // SE RECOGEN LOS DATOS DE LA ASIGNATURA
                                String idAsignatura = (String) jsonAsignatura.get("idAsignatura");
                                asignaturasEuskera[i] = (String) jsonAsignatura.get("nombreAsignaturaEuskera");
                                asignaturasCastellano[i] = (String) jsonAsignatura.get("nombreAsignatura");
                                idAsignaturas[i] = idAsignatura;

                                AsignaturaImparte asignaturaImparte = null;
                                // RECORRE LOS ALUMNOS QUE ESTÁN MATRICULADOS CADA ASIGNATURA
                                for (int j = 0; j < arrayAlumnosImpartidos.size(); j++) {
                                    JSONObject jsonObjectAlumnos = (JSONObject) arrayAlumnosImpartidos.get(j);
                                    if (jsonObjectAlumnos.containsKey(idAsignatura)) {
                                        JSONArray arrayAlumnos = (JSONArray) jsonObjectAlumnos.get(idAsignatura);
                                        asignaturaImparte = new AsignaturaImparte();
                                        for (int k = 0; k < arrayAlumnos.size(); k++) {
                                            JSONObject alumno = (JSONObject) arrayAlumnos.get(k);
                                            // SE RECOGEN LOS DATOS DEL ALUMNO
                                            asignaturaImparte.addAlumno((String) alumno.get("nombreCompleto"), (String) alumno.get("token"), (String) alumno.get("idPersona"));
                                        }
                                    }
                                }
                                // SE ALMACENA EN EL MAP
                                imparte.put(idAsignatura, asignaturaImparte);
                            }

                            // SE CREA EL ADAPTER
                            expandableListView.setAdapter(new MyExpandableListAdapterNotasProfesor(this,
                                    imparte, asignaturasCastellano, asignaturasEuskera, idAsignaturas));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        } catch (ClassCastException e1) {
                            JSONObject jsonInicial = null;
                            try {

                                //CASO DE QUE EL PROFESOR SOLO IMPARTE UNA ASIGNATURA
                                jsonInicial = (JSONObject) parser.parse(resultado);

                                // ASIGNATURA QUE IMPARTE EL PROFESOR
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

                                    // RECOGE LOS ALUMNOS QUE ESTÁN MATRICULADOS
                                    for (int j = 0; j < jsonArrayAlumnos.size(); j++) {
                                        JSONObject alumno = (JSONObject) jsonArrayAlumnos.get(j);
                                        asignaturaImparte.addAlumno((String) alumno.get("nombreCompleto"), (String) alumno.get("token"), (String) alumno.get("idPersona"));
                                    }
                                    //LOS ALMACENA EN EL MAP
                                    imparte.put(idAsignatura, asignaturaImparte);
                                }
                                //SE CREA EL ADAPTER Y SE ESTABLECE EN LA LISTA EXPANDIBLE
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

    /**
     * Cuenta el número de grupos
     * @return: cantidad de grupos
     */
    @Override
    public int getGroupCount() {
        return idAsignaturas.length;
    }

    /**
     * Cuenta el número de hijos que tiene el grupo en cuestión
     * @param groupPosition: el grupo
     * @return: cantidad de hijos que tiene el grupo
     */
    @Override
    public int getChildrenCount(int groupPosition) {
        return imparte.get(idAsignaturas[groupPosition]).size();
    }

    /**
     * Obtiene el grupo
     * @param groupPosition: el número dle grupo
     * @return: el objeto del grupo
     */
    @Override
    public Object getGroup(int groupPosition) {
        return idAsignaturas[groupPosition];
    }

    /**
     * Obtiene el hijo respecto al grupo
     * @param groupPosition: el número del grupo
     * @param childPosition: el número del hijo
     * @return: el objeto del hijo
     */
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return imparte.get(idAsignaturas[groupPosition]).get(childPosition);
    }

    /**
     * Id dlel grupo
     * @param groupPosition: la posición del grupo
     * @return: id
     */
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    /**
     * Id del hijo del grupo
     * @param groupPosition: la posición del grupo
     * @param childPosition: la posición del hijo
     * @return id
     */
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition * childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    /**
     * Carga la visualización del grupo
     * @param groupPosition: posición del grupo
     * @param isExpanded: si está expandido o no
     * @param convertView:
     * @param parent:
     * @return: la vista del grupo
     */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.asignaturas_imparte_listgroup, parent, false);

        if (!isExpanded)
            convertView.setPadding(0, 0, 0, 75);
        else
            convertView.setPadding(0, 0, 0, 0);


        TextView asignaturaTxtView = convertView.findViewById(R.id.imparte_txtNombreAsignatura);

        // DEPENDIENDO DEL IDIOMA, EL NOMBRE DE LA ASIGNATURA SE RECOGERÁ DE UN ARRAY O DE OTRO
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String idioma = prefs.getString("idioma", "es");
        if (idioma.equals("es")) {
            asignaturaTxtView.setText(cast[groupPosition]);
        } else {
            asignaturaTxtView.setText(eusk[groupPosition]);
        }
        return convertView;
    }

    /**
     * Carga la visualización de los hijos
     * @param groupPosition: posición del grupo
     * @param childPosition: posiicón del hijo
     * @param isLastChild: si es el último hijo
     * @param convertView: la view
     * @param parent
     * @return: la view
     */
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.imparte_alumnos_listitem, parent, false);

        if (isLastChild) {
            convertView.setPadding(0, 0, 0, 75);
        } else {
            convertView.setPadding(0, 0, 0, 0);
        }

        // SE RECOGE LOS DATOS DEL ALUMNO EN CUESTIÓN Y LOS ALMACENA EN LOS TEXTVIEW
        Map<String, String> map = (Map<String, String>) getChild(groupPosition, childPosition);
        String nombreUsuario = map.get("nombreAlumno");
        String token = map.get("token");
        String idPersona = map.get("id");

        TextView nombreAlumno = (TextView) convertView.findViewById(R.id.imparte_nombreAlumno);
        nombreAlumno.setText(nombreUsuario);

        // CUANDO SE PULSA EL BOTÓN SE ABRE EL DIÁLOGO PARA AÑADIR UNA NOTA
        Button addNota = (Button) convertView.findViewById(R.id.imparte_ponerNota);
        addNota.setOnClickListener(view -> {
            crearDialog(nombreUsuario, idPersona, token, (String) getGroup(groupPosition), 2019, groupPosition);
        });

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    /**
     * Se crea un diálogo para añadir una nota a un alumno, ya sea para la convocatoria ordinaria o
     * extraordinaria. Al añadir la nota, se le envía una notificación al alumno con la nota.
     * @param nombreUsuario: nombre del alumno
     * @param idPersona: id del alumno
     * @param token: token del alumno
     * @param idAsignatura: id de la asignatura
     * @param anio: anio de la matricula
     * @param group: grupo en que está el alumno
     */
    private void crearDialog(String nombreUsuario, String idPersona, String token, String idAsignatura, int anio, int group) {

        //DIALOGO PERSONALIZADO
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_profesor_aniadenota, null);
        builder.setView(view);

        TextView nombreAlumno = (TextView) view.findViewById(R.id.dialog_notaprofesor_alumno);
        nombreAlumno.setText(nombreUsuario);

        AlertDialog dialog = builder.create();

        //LISTENER AL PULSAR EL BOTÓN DE "AÑADIR NOTA"
        Button addNota = (Button) view.findViewById(R.id.dialog_notaprofesor_boton);
        addNota.setOnClickListener(vista -> {
            EditText editText = (EditText) view.findViewById(R.id.dialog_notaprofesor_nota);
            // SI EL EDIT TEXT NO ESTÁ VACÍO
            if (!editText.getText().toString().equals("")) {
                double nota = Double.parseDouble(editText.getText().toString());

                // SI LA NOTA ES CORRECTA
                if (nota > -1 && nota < 11) {

                    // SE PREPARA PARA AÑADIR LA NOTA EN LA BASE DE DATOS
                    Map<String, String> map = new HashMap<>();
                    map.put("idPersona", idPersona);
                    map.put("token", token);
                    map.put("anio", String.valueOf(anio));
                    map.put("idAsignatura", idAsignatura);
                    map.put("nota", String.valueOf(nota));
                    map.put("asignatura", cast[group]);
                    map.put("asignaturaEuskera", eusk[group]);
                    map.put("accion", "notaProfesor");
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
                    map.put("idioma", prefs.getString("idioma", "es"));
                    Switch sw = (Switch) view.findViewById(R.id.dialog_notaprofesor_switch);
                    if (sw.isChecked()) {
                        map.put("ordinaria", "si");
                    } else {
                        map.put("ordinaria", "no");
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
                } else {
                    Toast.makeText(activity, activity.getResources().getString(R.string.dialog_notaprofesor_notaError), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(activity, activity.getResources().getString(R.string.dialog_notaprofesor_vacio), Toast.LENGTH_SHORT).show();
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

    /**
     * Constructor de la clase que almacena los nombres de los alumnos, tokens e ids de la asignatura
     */
    public AsignaturaImparte() {
        nombresAlumnos = new ArrayList<>();
        tokens = new ArrayList<>();
        ids = new ArrayList<>();
    }

    /**
     * Se añade un alumno que está matriculado en la asignatura en las listas
     * @param nombre: nombre del alumno
     * @param token: token del alumno
     * @param id: id del alumno
     */
    public void addAlumno(String nombre, String token, String id) {
        nombresAlumnos.add(nombre);
        tokens.add(token);
        ids.add(id);
    }

    /**
     * Tamaño de las listas, cantidad de alumnos que hay matriculados
     * @return
     */
    public int size() {
        return nombresAlumnos.size();
    }

    /**
     * Se recoge el nombre, el token y el id del alumno respecto a la posición de la lista.
     * @param i: posición de la lista
     * @return: Un Map con los datos del alumno
     */
    public Object get(int i) {
        Map<String, String> map = new HashMap<>();
        map.put("nombreAlumno", nombresAlumnos.get(i));
        map.put("token", tokens.get(i));
        map.put("id", ids.get(i));
        return map;
    }
}
