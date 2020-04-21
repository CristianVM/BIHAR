package com.example.bihar.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.bihar.R;

public class AdapterListaAsignaturasMatricula extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private String[] asignaturas;
    private int[] cursos;
    private int[] convocatorias;
    private double[] notasOrdinarias;
    private double[] notasExtraordinarias;

    public AdapterListaAsignaturasMatricula(Context context, String[] asignaturas, int[] cursos,
         int[] convocatorias, double[] notasExtraordinarias,double[] notasOrdinarias){

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
        return asignaturas.length;
    }

    @Override
    public Object getItem(int i) {
        return asignaturas[i];
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

        asignatura.setText(asignaturas[i]);
        curso.setText(cursos[i]+"");
        convocatoria.setText(convocatorias[i]+"");
        notaOrdinaria.setText(notasOrdinarias[i]+"");
        notaExtraordinaria.setText(notasExtraordinarias[i]+"");
        return view;
    }
}
