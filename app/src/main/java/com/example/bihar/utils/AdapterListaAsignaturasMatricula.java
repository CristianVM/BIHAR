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

    /**
     * Constructor del adapter del adapter de las asignaturas de la sección de Matrícula
     *
     * @param context:              el contexto de la aplicación
     * @param asignaturas:          nombres de las asignaturas
     * @param cursos:               cursos
     * @param convocatorias:        convocatorias realizadas
     * @param notasExtraordinarias: notas extraordinarias
     * @param notasOrdinarias:      notas ordinarias
     */
    public AdapterListaAsignaturasMatricula(Context context, ArrayList<String> asignaturas, ArrayList<String> cursos,
                                            ArrayList<String> convocatorias, ArrayList<String> notasExtraordinarias, ArrayList<String> notasOrdinarias) {

        this.context = context;
        this.asignaturas = asignaturas;
        this.cursos = cursos;
        this.convocatorias = convocatorias;
        this.notasExtraordinarias = notasExtraordinarias;
        this.notasOrdinarias = notasOrdinarias;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Devuelve el número de asignaturas de la matrícula
     *
     * @return número de asignaturas
     */
    @Override
    public int getCount() {
        return asignaturas.size();
    }

    /**
     * Devuelve el objeto de la posición i
     *
     * @param i: la posición de la lista
     * @return: el objeto
     */
    @Override
    public Object getItem(int i) {
        return asignaturas.get(i);
    }

    /**
     * Devuelve el identificador
     *
     * @param i: identificador
     * @return: id
     */
    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * Devuelve el listView personalizado habiendole asignado valores
     *
     * @param i:        posición
     * @param view:     la vista
     * @param viewGroup
     * @return
     */
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
