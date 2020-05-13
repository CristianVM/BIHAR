package com.example.bihar.model;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.bihar.R;

import org.json.simple.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HorarioAsignatura {

    private List<String> diaSemanas;
    private List<String> semanas;
    private List<String> hInicios;
    private List<String> hFinales;
    private List<String> nombresAsignaturas;
    private List<String> nombreAsignaturaEuskera;
    private Context context;

    /**
     * Constructor de los horarios de las asignaturas
     * @param context: el contexto
     */
    public HorarioAsignatura(Context context) {
        diaSemanas = new ArrayList<>();
        semanas = new ArrayList<>();
        hFinales = new ArrayList<>();
        hInicios = new ArrayList<>();
        nombresAsignaturas = new ArrayList<>();
        nombreAsignaturaEuskera = new ArrayList<>();
        this.context = context;
    }

    /**
     * Añade un horario de la asignatura
     * @param diaSemana: día de la semana
     * @param semana: semana de la hora
     * @param hInicio: hora inicio de la clase
     * @param hFinal: hora final de la clase
     * @param asg: id asignatura
     */
    public void addHorarioAsignatura(String diaSemana, String semana, String hInicio, String hFinal, String asg,String asgEusk) {
        procesarDiaSemana(diaSemana);
        procesarDiaClase(diaSemana, semana);
        hFinales.add(hFinal);
        hInicios.add(hInicio);
        nombresAsignaturas.add(asg);
        nombreAsignaturaEuskera.add(asgEusk);
    }

    /**
     * Se obtiene el horario de una posición concreta de la lista
     * @param pos: posición
     * @return: Un JSON con el horario
     */
    public JSONObject getHorario(int pos) {
        Map<String, String> map = new HashMap<>();
        map.put("diaSemanas", diaSemanas.get(pos));
        map.put("semanas", semanas.get(pos));
        map.put("hInicios", hInicios.get(pos));
        map.put("hFinales", hFinales.get(pos));
        map.put("nombresAsignaturas", nombresAsignaturas.get(pos));
        map.put("nombresAsignaturasEuskera",nombreAsignaturaEuskera.get(pos));
        return new JSONObject(map);
    }

    /**
     * Procesa el día de la semana dependiendo del número
     * @param diaSemana: día de la semana
     */
    private void procesarDiaSemana(String diaSemana) {
        if (diaSemana.equals("0")) {
            diaSemanas.add(context.getResources().getString(R.string.horario_diaLunes));
        } else if (diaSemana.equals("1")) {
            diaSemanas.add(context.getResources().getString(R.string.horario_diaMartes));
        } else if (diaSemana.equals("2")) {
            diaSemanas.add(context.getResources().getString(R.string.horario_diaMiercoles));
        } else if (diaSemana.equals("3")) {
            diaSemanas.add(context.getResources().getString(R.string.horario_diaJueves));
        } else if (diaSemana.equals("4")) {
            diaSemanas.add(context.getResources().getString(R.string.horario_diaViernes));
        }
    }

    /**
     * **Extraído de Stack Overflow
     * Pregunta: https://stackoverflow.com/questions/5301226/convert-string-to-calendar-object-in-java
     * Autor: https://stackoverflow.com/users/260990/jigar-joshi
     *
     * @param diaSemana: dia de la semana. 0 es Lunes, 1 Martes...
     * @param semana: la fecha de la semana
     */
    private void procesarDiaClase(String diaSemana, String semana) {

        // FALTA CAMBIAR EL AUTOR
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(dateFormat.parse(semana));
            calendar.add(Calendar.DATE, Integer.parseInt(diaSemana));

            Date date = calendar.getTime();
            semanas.add(dateFormat.format(date));

        } catch (ParseException e) {
            semanas.add("-");
            e.printStackTrace();
        }
    }

    /**
     * Devuelve el JSON con con el horario de la asignatura a tal fecha
     * @param fecha: fecha elegida
     * @param format: formato de la fecha
     * @return: JSON con el horario
     */
    public JSONObject horarioAsignatura(Calendar fecha,SimpleDateFormat format) {
        for (int i = 0; i < semanas.size(); i++) {
            String semana = semanas.get(i);
            Calendar calendar = Calendar.getInstance();
            try {
                calendar.setTime(format.parse(semana));
                Date date = calendar.getTime();
                String fechaLista = format.format(date);

                Date date2 = fecha.getTime();

                if(fechaLista.equals(format.format(date2))){
                    return getHorario(i);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return new JSONObject();
    }
}
