package com.example.bihar.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.bihar.R;

import java.util.ArrayList;

public class AdapterListaHorarios extends BaseAdapter {

    private ArrayList<String> nombresAsignaturas;
    private ArrayList<String> hInicios;
    private ArrayList<String> hFinales;
    private ArrayList<String> diasSemanas;
    private ArrayList<String> semanas;
    private Context context;
    private LayoutInflater layoutInflater;


    /**
     * Constructor del adapter del adapter de los horarios
     * @param nombresAsignaturas: nombres de las aignaturas
     * @param hInicios: horas iniciales
     * @param hFinales: horas finales
     * @param diasSemanas: dias de la semana (Lunes, martes, miercoles...)
     * @param semanas: semanas
     * @param context: el contexto
     */
    public AdapterListaHorarios(ArrayList<String> nombresAsignaturas, ArrayList<String> hInicios, ArrayList<String> hFinales,
                                ArrayList<String> diasSemanas, ArrayList<String> semanas, Context context) {
        this.nombresAsignaturas = nombresAsignaturas;
        this.hInicios = hInicios;
        this.hFinales = hFinales;
        this.diasSemanas = diasSemanas;
        this.semanas = semanas;
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Devuelve el número de asignatura que hay
     * @return número de asignatura
     */
    @Override
    public int getCount() {
        return nombresAsignaturas.size();
    }

    /**
     * Devuelve el objeto de la posición i
     * @param i: la posición de la lista
     * @return: el objeto
     */
    @Override
    public Object getItem(int i) {
        return nombresAsignaturas.get(i);
    }

    /**
     * Devuelve el identificador
     * @param i: identificador
     * @return: id
     */
    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * Devuelve el listView personalizado habiendole asignado valores
     * @param i: posición
     * @param view: la vista
     * @param viewGroup
     * @return
     */
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = layoutInflater.inflate(R.layout.lista_horarios,null);

        TextView txtAsignatura = (TextView) view.findViewById(R.id.lista_horario_asignatura);
        TextView txtHFin = (TextView) view.findViewById(R.id.lista_horario_hFin);
        TextView txtHIncio = (TextView) view.findViewById(R.id.lista_horario_hInicio);
        //TextView txtDiaSemana = (TextView) view.findViewById(R.id.lista_horario_dia);

        txtAsignatura.setText(nombresAsignaturas.get(i));
        txtHFin.setText(hFinales.get(i));
        txtHIncio.setText(hInicios.get(i));
        //txtDiaSemana.setText(diasSemanas.get(i) + " " + semanas.get(i));

        return view;
    }
}
