package com.example.bihar.view.activities;

import androidx.appcompat.app.AppCompatActivity;

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
import com.example.bihar.model.Asignatura;
import com.example.bihar.model.Usuario;

import java.util.HashMap;
import java.util.List;

public class Asignaturas extends AppCompatActivity {

    private ExpandableListView expandableListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asignaturas);
        expandableListView = findViewById(R.id.asignaturasExpandableListView);

        /*
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
        GestorUsuario.getGestorUsuario().setUsuario(new Usuario("837448","iknafa@hotmail.com"));
        GestorUsuario.getGestorUsuario().getUsuario().anadirAsignatura(new Asignatura("Algebra",5.0,1,"Básica de rama",2018,1));
        GestorUsuario.getGestorUsuario().getUsuario().anadirAsignatura(new Asignatura("Programación Básica",5.7,1,"Básica de rama",2016,1));
        GestorUsuario.getGestorUsuario().getUsuario().anadirAsignatura(new Asignatura("Programación Modular y Orientado a Objetos",7.6,1,"Obligatoria",2017,2));
        GestorUsuario.getGestorUsuario().getUsuario().anadirAsignatura(new Asignatura("Desarrollo Avanzado de Software",10.0,1,"Optativa",2019,4));

        expandableListView.setAdapter(new MyExpandableListAdapterAsignaturas(this));

    }
}


class MyExpandableListAdapterAsignaturas extends BaseExpandableListAdapter {
    private HashMap<Integer, List<Asignatura>> asignaturas;
    private Integer[] cursos;
    private Activity activity;

    MyExpandableListAdapterAsignaturas(Activity pActivity){
        activity = pActivity;
        asignaturas = GestorUsuario.getGestorUsuario().getUsuario().getAsignaturas_por_curso();
        cursos = asignaturas.keySet().toArray(new Integer[0]);
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
        anyo.setText(String.valueOf(a.getAnyo()));
        tipo.setText(a.getTipo());
        nota.setText(String.valueOf(a.getCalificacionOrd()));
        conv.setText(String.valueOf(a.getConvocatoria()));


        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
