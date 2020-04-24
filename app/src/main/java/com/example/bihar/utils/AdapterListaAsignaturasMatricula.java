package com.example.bihar.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.bihar.R;

import java.util.ArrayList;

public class AdapterListaAsignaturasMatricula extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<String> asignaturas;
    private ArrayList<String> cursos;
    private ArrayList<String> convocatorias;
    private ArrayList<String> notasOrdinarias;
    private ArrayList<String> notasExtraordinarias;

    public AdapterListaAsignaturasMatricula(Context context, ArrayList<String> asignaturas, ArrayList<String> cursos,
          ArrayList<String> convocatorias, ArrayList<String> notasExtraordinarias,ArrayList<String> notasOrdinarias){

        this.context = context;
        this.asignaturas = asignaturas;
        this.cursos = cursos;
        this.convocatorias = convocatorias;
        this.notasExtraordinarias = notasExtraordinarias;
        this.notasOrdinarias = notasOrdinarias;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return asignaturas.size();
    }

    @Override
    public Object getItem(int i) {
        return asignaturas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.lista_matriculas, null);

        TextView asignatura = (TextView) view.findViewById(R.id.lista_matriculas_asignatura);
        TextView curso = (TextView) view.findViewById(R.id.lista_matricula_curso);
        TextView convocatoria = (TextView) view.findViewById(R.id.lista_matricula_convocatoria);
        TextView notaOrdinaria = (TextView) view.findViewById(R.id.lista_matricula_notaOrdinaria);
        TextView notaExtraordinaria = (TextView) view.findViewById(R.id.lista_matricula_notaExtraordinaria);

        asignatura.setText(asignaturas.get(i));
        curso.setText(cursos.get(i));
        convocatoria.setText(convocatorias.get(i));
        notaOrdinaria.setText(notasOrdinarias.get(i));
        notaExtraordinaria.setText(notasExtraordinarias.get(i));
        return view;
    }
}
