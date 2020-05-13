package com.example.bihar.view.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Space;

import com.example.bihar.R;
import com.example.bihar.controller.GestorProfesores;
import com.example.bihar.controller.WorkerBihar;
import com.example.bihar.model.Profesor;
import com.example.bihar.model.FechaTutoria;
import com.example.bihar.model.Tutoria;
import com.example.bihar.view.fragments.ToolBar;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DatosTutoria extends AppCompatActivity {

    private ProgressBar progressBar;
    private ImageView imgProfesor;
    private TextView nombreProfesor,
                     departamentoProfesor,
                     despachoProfesor,
                     nombreCentroProfesor;
    private ExpandableListView expandableListView;
    private MyExpandableListAdapter adapter;

    private String idPersona;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datostutoria);

        Bundle bundle = getIntent().getExtras();
        if(bundle == null){
            finish();
            return;
        }
        idPersona = bundle.getString("idPersona");

        progressBar = findViewById(R.id.tutoriasProgressBar);
        imgProfesor = findViewById(R.id.imgProfesor);
        nombreProfesor = findViewById(R.id.nombreProfesor);
        departamentoProfesor = findViewById(R.id.departamentoProfesor);
        despachoProfesor = findViewById(R.id.despachoProfesor);
        nombreCentroProfesor = findViewById(R.id.nombreCentroProfesor);
        expandableListView = findViewById(R.id.tutoriasExpandableListView);

        ToolBar toolbarDatosTutoria = (ToolBar) getSupportFragmentManager().findFragmentById(R.id.toolbarDatosTutoria);
        toolbarDatosTutoria.cambiarTituloToolbar(getResources().getString(R.string.tutorias));

        cargarDatosProfesor();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        File file = new File(getApplicationContext().getFilesDir(), idPersona + ".png");
        if(file.exists())
            file.delete();
    }

    private void cargarDatosProfesor(){
        Map<String, String> map = new HashMap<>();
        map.put("accion","obtenerDatosProfesor");
        map.put("idPersona", idPersona);
        SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        map.put("idioma",preferences.getString("idioma","es"));
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
                        Profesor profesor = GestorProfesores.getGestorProfesores().getProfesor(idPersona);
                        JSONParser parser = new JSONParser();
                        try {
                            JSONObject obj = (JSONObject) parser.parse(resultado);

                            String departamento = (String) obj.get("departamento");
                            String despacho = (String) obj.get("despacho");
                            String nombreCentro = (String) obj.get("nombreCentro");

                            profesor.setDepartamento(departamento==null?"":departamento);
                            profesor.setDespacho(despacho==null?"":despacho);
                            profesor.setNombreCentro(nombreCentro==null?"":nombreCentro);

                            cargarTutorias();

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        WorkManager.getInstance(this).enqueue(trabajo);
    }

    private void cargarTutorias(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String idUsuario = prefs.getString("idUsuario", "");
        Map<String, String> map = new HashMap<>();
        map.put("accion","obtenerTutoriasProfesor");
        map.put("idPersona", idPersona);
        map.put("idUsuario", idUsuario);
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
                        if(resultado == null || resultado.isEmpty()) {
                            finish();
                            return;
                        }

                        Profesor profesor = GestorProfesores.getGestorProfesores().getProfesor(idPersona);
                        profesor.limpiarTutorias();
                        JSONParser parser = new JSONParser();
                        try {
                            JSONArray array = (JSONArray) parser.parse(resultado);
                            for(int i = 0; i<array.size();i++){
                                JSONObject obj = (JSONObject) array.get(i);
                                int idTutoria = Integer.parseInt((String) obj.get("idTutoria"));
                                String fecha = (String) obj.get("fecha");
                                String horaInicio = (String) obj.get("horaInicio");
                                String horaFin = (String) obj.get("horaFin");
                                int estado = Integer.parseInt(String.valueOf(obj.get("estado")));
                                profesor.anadirTutoria(idTutoria,fecha,horaInicio,horaFin,estado);
                            }

                            cargarFotoProfesor();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
        WorkManager.getInstance(this).enqueue(trabajo);
    }

    private void cargarFotoProfesor(){

        JSONObject parametrosJSON = new JSONObject();
        parametrosJSON.put("accion", "obtenerImagen");
        parametrosJSON.put("idUsuario", idPersona);

        Data datos = new Data.Builder()
                .putString("datos", parametrosJSON.toJSONString())
                .build();

        Constraints restricciones = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest trabajo = new OneTimeWorkRequest.Builder(WorkerBihar.class)
                .setConstraints(restricciones)
                .setInputData(datos)
                .build();

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(trabajo.getId()).observe(
                this, status -> {
                    if (status != null && status.getState().isFinished()) {
                        String resultado = status.getOutputData().getString("result");
                        if (status.getState() == WorkInfo.State.FAILED || resultado == null || resultado.isEmpty()) {
                            imgProfesor.setImageResource(R.drawable.defecto);
                            mostrarDatos();
                        }
                        File file = new File(getApplicationContext().getFilesDir(), idPersona + ".png");
                        GestorProfesores.getGestorProfesores().getProfesor(idPersona).setFoto(Uri.fromFile(file));
                        progressBar.setVisibility(View.INVISIBLE);
                        imgProfesor.setImageURI( GestorProfesores.getGestorProfesores().getProfesor(idPersona).getFoto());
                        mostrarDatos();
                    }
                }
        );

        WorkManager.getInstance(this).enqueue(trabajo);
    }

    private void mostrarDatos(){
        Profesor p = GestorProfesores.getGestorProfesores().getProfesor(idPersona);

        nombreProfesor.setText(p.getNombreCompleto());
        departamentoProfesor.setText(p.getDepartamento());
        despachoProfesor.setText(p.getDespacho());
        nombreCentroProfesor.setText(p.getNombreCentro());


        adapter = new MyExpandableListAdapter(this, idPersona);
        expandableListView.setAdapter(adapter);
    }

}


class MyExpandableListAdapter extends BaseExpandableListAdapter{

    private FechaTutoria[] fechaTutorias;
    private Activity activity;
    private AlertDialog alertDialog;

    MyExpandableListAdapter(Activity pActivity, String idPersona){
        activity = pActivity;
        fechaTutorias = GestorProfesores.getGestorProfesores().getProfesor(idPersona).getFechaTutorias();
    }

    @Override
    public int getGroupCount() {
        return fechaTutorias.length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return fechaTutorias[groupPosition].getHoras().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return fechaTutorias[groupPosition];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return fechaTutorias[groupPosition].getHoras().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition*childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if(convertView==null)
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.tutorias_listgroup,parent ,false);

        if(!isExpanded)
            convertView.setPadding(0,0,0,75);
        else
            convertView.setPadding(0,0,0,0);

        TextView fecha = convertView.findViewById(R.id.txtFecha);
        fecha.setText(fechaTutorias[groupPosition].getFecha());
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView=LayoutInflater.from(parent.getContext()).inflate(R.layout.tutorias_listitem,parent,false);

        if(isLastChild){
            convertView.setPadding(0,0,0,75);
        }else{
            convertView.setPadding(0,0,0,0);
        }

        Tutoria t = (Tutoria) getChild(groupPosition,childPosition);
        TextView hora = convertView.findViewById(R.id.txtHora);
        hora.setText(t.getHora());

        ImageView imgReservar = convertView.findViewById(R.id.imgReservar);
        if(t.getEstado() == 0 || t.getEstado() == 1){
            if(t.getEstado() == 0)
                imgReservar.setImageResource(R.drawable.ic_pending);
            else
                imgReservar.setImageResource(R.drawable.ic_check);

            imgReservar.setOnClickListener(v -> {
                final int idTutoria = t.getIdTutoria();
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                LayoutInflater inflater = activity.getLayoutInflater();
                View elAspecto = inflater.inflate(R.layout.tutorias_alertdialog,null);
                builder.setView(elAspecto);

                TextView msg = elAspecto.findViewById(R.id.alertMsg);
                Button si  = elAspecto.findViewById(R.id.alertBtnSi);
                Button no = elAspecto.findViewById(R.id.alertBtnNo);

                msg.setText(activity.getString(R.string.preguntaCancelar));

                si.setOnClickListener(v14 -> {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
                    String idPersona = prefs.getString("idUsuario",null);
                    cancelarReserva(idPersona, idTutoria, groupPosition, childPosition);
                });
                no.setOnClickListener(v15 -> {
                    alertDialog.dismiss();
                });

                alertDialog = builder.create();
                alertDialog.show();
            });
        }else if(t.getEstado() == -1){
            imgReservar.setImageResource(R.drawable.ic_add);
            imgReservar.setOnClickListener(v -> {

                final int idTutoria = t.getIdTutoria();
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                LayoutInflater inflater = activity.getLayoutInflater();
                View elAspecto = inflater.inflate(R.layout.tutorias_alertdialog,null);
                builder.setView(elAspecto);

                TextView msg = elAspecto.findViewById(R.id.alertMsg);
                Button si  = elAspecto.findViewById(R.id.alertBtnSi);
                Button no = elAspecto.findViewById(R.id.alertBtnNo);
                Button hecho = elAspecto.findViewById(R.id.alertBtnHecho);
                Space space = elAspecto.findViewById(R.id.alertSpace);
                EditText edt = elAspecto.findViewById(R.id.alertEditText);
                LinearLayout ll1 = elAspecto.findViewById(R.id.alertLL1);
                LinearLayout ll2 = elAspecto.findViewById(R.id.alertLL2);



                si.setOnClickListener(v1 -> {
                    space.setVisibility(View.GONE);
                    edt.setVisibility(View.VISIBLE);
                    msg.setText(activity.getString(R.string.msgConfirmar));
                    ll1.setVisibility(View.GONE);
                    ll2.setVisibility(View.VISIBLE);
                });


                no.setOnClickListener(v12 -> {
                    alertDialog.dismiss();
                });

                hecho.setOnClickListener(v13 -> {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
                    String idPersona = prefs.getString("idUsuario",null);
                    reservarTutoria(idPersona,idTutoria,edt.getText().toString(), groupPosition, childPosition);
                });

                alertDialog = builder.create();
                alertDialog.show();

            });
        }else if(t.getEstado() == 2){
            imgReservar.setImageResource(R.drawable.ic_close_red);
            imgReservar.setOnClickListener(null);
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    private void reservarTutoria(String idPersona, int idTutoria, String msg, int groupPosition, int childPosition){
        Map<String, String> map = new HashMap<>();
        map.put("accion","reservarTutoria");
        map.put("idPersona", idPersona);
        map.put("idTutoria",String.valueOf(idTutoria));
        map.put("msg",msg);

        SharedPreferences sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(activity);
        String nombre = sharedPreferences.getString("nombreUsuario","");

        String msgN = activity.getString(R.string.notificacionSolicitud,nombre);
        map.put("msgN", msgN);
        map.put("titulo",activity.getString(R.string.tutorias));
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

        WorkManager.getInstance(activity).getWorkInfoByIdLiveData(trabajo.getId()).observe(
                (LifecycleOwner) activity, status -> {
                    if (status != null && status.getState().isFinished()) {
                        String resultado = status.getOutputData().getString("result");
                        if (status.getState() == WorkInfo.State.FAILED) {
                            activity.finish();
                            return;
                        }

                        alertDialog.dismiss();
                        Toast.makeText(activity, activity.getString(R.string.tutoriaReservada),Toast.LENGTH_SHORT).show();
                        Tutoria t = (Tutoria) getChild(groupPosition,childPosition);
                        t.setEstado(0);
                        notifyDataSetChanged();
                    }
                }
        );

        WorkManager.getInstance(activity).enqueue(trabajo);
    }

    private void cancelarReserva(String idPersona, int idTutoria, int groupPosition, int childPosition){
        Map<String, String> map = new HashMap<>();
        map.put("accion","borrarReservaTutoria");
        map.put("idPersona", idPersona);
        map.put("idTutoria",String.valueOf(idTutoria));

        SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(activity);

        map.put("msg",activity.getString(R.string.notificacionCancelar,preferences.getString("nombreUsuario","")));
        map.put("titulo",activity.getString(R.string.tutorias));
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

        WorkManager.getInstance(activity).getWorkInfoByIdLiveData(trabajo.getId()).observe(
                (LifecycleOwner) activity, status -> {
                    if (status != null && status.getState().isFinished()) {
                        if(status.getState() == WorkInfo.State.SUCCEEDED){
                            alertDialog.dismiss();
                            Toast.makeText(activity, activity.getString(R.string.reservaCancelada),Toast.LENGTH_SHORT).show();
                            Tutoria t = (Tutoria) getChild(groupPosition,childPosition);
                            t.setEstado(-1);
                            notifyDataSetChanged();
                        }else{
                            Toast.makeText(activity, activity.getString(R.string.error_general),Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        WorkManager.getInstance(activity).enqueue(trabajo);
    }
}



