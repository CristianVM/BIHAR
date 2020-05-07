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

    @Override
    public int getCount() {
        return nombresAsignaturas.size();
    }

    @Override
    public Object getItem(int i) {
        return nombresAsignaturas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

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
