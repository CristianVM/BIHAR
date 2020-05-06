package com.example.bihar.view.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
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
import com.example.bihar.model.Tutoria;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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


        cargarDatosProfesor();
    }

    private void cargarDatosProfesor(){
        Map<String, String> map = new HashMap<>();
        map.put("accion","obtenerDatosProfesor");
        map.put("idPersona", idPersona);
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
        Map<String, String> map = new HashMap<>();
        map.put("accion","obtenerTutoriasProfesor");
        map.put("idPersona", idPersona);
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

                                profesor.anadirTutoria(idTutoria,fecha,horaInicio,horaFin);
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
        progressBar.setVisibility(View.INVISIBLE);
        mostrarDatos();
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

    private Tutoria[] tutorias;
    private Activity activity;
    private AlertDialog alertDialog;

    MyExpandableListAdapter(Activity pActivity, String idPersona){
        activity = pActivity;
        tutorias = GestorProfesores.getGestorProfesores().getProfesor(idPersona).getTutorias();
    }

    @Override
    public int getGroupCount() {
        return tutorias.length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return tutorias[groupPosition].getHoras().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return tutorias[groupPosition];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return tutorias[groupPosition].getHoras().values().toArray(new String[0])[childPosition];
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
        fecha.setText(tutorias[groupPosition].getFecha());
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

        TextView hora = convertView.findViewById(R.id.txtHora);
        String[] horas = tutorias[groupPosition].getHoras().values().toArray(new String[0]);
        hora.setText(horas[childPosition]);

        ImageView imgReservar = convertView.findViewById(R.id.imgReservar);
        imgReservar.setOnClickListener(v -> {
            Toast.makeText(parent.getContext(), ""+tutorias[groupPosition].getHoras().keySet().toArray(new Integer[0])[childPosition], Toast.LENGTH_SHORT).show();


            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            LayoutInflater inflater = activity.getLayoutInflater();
            View elAspecto = inflater.inflate(R.layout.tutorias_alertdialog,null);
            builder.setView(elAspecto);

            TextView msg = elAspecto.findViewById(R.id.alertMsg);
            Button si  = elAspecto.findViewById(R.id.alertBtnSi);
            Button no = elAspecto.findViewById(R.id.alertBtnNo);
            Space space = elAspecto.findViewById(R.id.alertSpace);
            EditText edt = elAspecto.findViewById(R.id.alertEditText);
            LinearLayout ll1 = elAspecto.findViewById(R.id.alertLL1);
            LinearLayout ll2 = elAspecto.findViewById(R.id.alertLL2);


            si.setOnClickListener(v1 -> {
                space.setVisibility(View.GONE);
                edt.setVisibility(View.VISIBLE);
                msg.setText("Escribe, si lo deseas, el tema que vas a tratar en la tutoria.");
                ll1.setVisibility(View.GONE);
                ll2.setVisibility(View.VISIBLE);
            });


            no.setOnClickListener(v12 -> {
                alertDialog.dismiss();
            });

            alertDialog = builder.create();
            alertDialog.show();

        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}



