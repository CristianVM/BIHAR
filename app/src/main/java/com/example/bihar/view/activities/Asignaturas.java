package com.example.bihar.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.example.bihar.R;
import com.example.bihar.controller.GestorUsuario;
import com.example.bihar.controller.WorkerBihar;
import com.example.bihar.model.Asignatura;
import com.example.bihar.model.Usuario;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Asignaturas extends AppCompatActivity {

    private ExpandableListView expandableListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asignaturas);

        //TODO: QUITAR
        GestorUsuario.getGestorUsuario().setUsuario(new Usuario("837448","i@i.com"));

        expandableListView = findViewById(R.id.asignaturasExpandableListView);

        /*
           Listener para que solo haya un Grupo expandido.
         * Pregunta: https://stackoverflow.com/questions/7862396/show-only-one-child-of-expandable-list-at-a-time/15856032
         * Autor: https://stackoverflow.com/users/413127/blundell
         */
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int previousGroup = -1;

            @Override
            public void onGroupExpand(int groupPosition) {
                if(groupPosition != previousGroup)
                    expandableListView.collapseGroup(previousGroup);
                previousGroup = groupPosition;
            }
        });

        cargarDatos();
    }

    private void cargarDatos(){
        Map<String, String> map = new HashMap<>();
        map.put("accion","obtenerAsignaturasPorAnyo");
        map.put("idPersona", GestorUsuario.getGestorUsuario().getUsuario().getIdUsuario());
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

                        if(status.getState() == WorkInfo.State.FAILED){
                            finish();
                            return;
                        }
                        String resultado = status.getOutputData().getString("result");
                        Usuario u = GestorUsuario.getGestorUsuario().getUsuario();
                        u.limpiarAsignaturas();

                        JSONParser parser = new JSONParser();
                        try {
                            JSONArray array = (JSONArray) parser.parse(resultado);
                            for(int i = 0; i<array.size(); i++){
                                JSONObject obj = (JSONObject) array.get(i);
                                int anyo = Integer.parseInt((String)obj.get("anio"));
                                int conv = Integer.parseInt((String)obj.get("convocatoria"));
                                double nota = Double.parseDouble((String)obj.get("notaFinal"));
                                String nombreAsignatura = (String) obj.get("nombreAsignatura");
                                int curso = Integer.parseInt((String)obj.get("curso"));
                                String tipo = (String) obj.get("tipo");

                                Asignatura a = new Asignatura(nombreAsignatura,nota,conv,tipo,anyo,curso);
                                u.anadirAsignatura(a);
                            }

                            expandableListView.setAdapter(new MyExpandableListAdapterAsignaturas(this));
                        } catch (ParseException e) {
                            finish();
                            e.printStackTrace();
                        }


                    }
                }
        );

        WorkManager.getInstance(this).enqueue(trabajo);

    }
}


class MyExpandableListAdapterAsignaturas extends BaseExpandableListAdapter {
    private HashMap<Integer, List<Asignatura>> asignaturas;
    private Integer[] cursos;
    private Activity activity;

    MyExpandableListAdapterAsignaturas(Activity pActivity){
        activity = pActivity;
        asignaturas = GestorUsuario.getGestorUsuario().getUsuario().getAsignaturas_por_curso();
        List<Integer> lista = new ArrayList<>(asignaturas.keySet());
        Collections.sort(lista);
        cursos = lista.toArray(new Integer[0]);
    }

    @Override
    public int getGroupCount() {
        return cursos.length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return asignaturas.get(cursos[groupPosition]).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return cursos[groupPosition];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return asignaturas.get(cursos[groupPosition]).get(childPosition);
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

        String[] strCurso = new String[]{activity.getString(R.string.primero),activity.getString(R.string.segundo),activity.getString(R.string.tercero),activity.getString(R.string.cuarto),activity.getString(R.string.quinto)};
        TextView fecha = convertView.findViewById(R.id.txtFecha);
        fecha.setText(activity.getString(R.string.asignaturas_curso,strCurso[(Integer) getGroup(groupPosition) - 1]));

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if(convertView==null)
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.asignaturas_listitem,parent ,false);

        if(isLastChild){
            convertView.setPadding(0,0,0,75);
        }else{
            convertView.setPadding(0,0,0,0);
        }

        Asignatura a = (Asignatura) getChild(groupPosition,childPosition);

        TextView nombreAsignatura = convertView.findViewById(R.id.nombreAsignatura);
        TextView curso = convertView.findViewById(R.id.txtCurso);
        TextView anyo = convertView.findViewById(R.id.txtAnyo);
        TextView tipo = convertView.findViewById(R.id.txtTipo);
        TextView nota = convertView.findViewById(R.id.txtNota);
        TextView conv = convertView.findViewById(R.id.txtConv);

        nombreAsignatura.setText(a.getNombreAsignatura());
        curso.setText(String.valueOf(a.getCurso()));
        String strAnyo = String.valueOf(a.getAnyo());
        strAnyo += "/" + (Integer.parseInt(strAnyo.substring(2,4))+1);
        anyo.setText(strAnyo);
        tipo.setText(a.getTipo());

        double notaFinal = a.getCalificacionOrd();
        if(notaFinal == -1){
            nota.setText("");
        }else{
            nota.setText(String.valueOf(notaFinal));
        }

        conv.setText(String.valueOf(a.getConvocatoria()));


        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
